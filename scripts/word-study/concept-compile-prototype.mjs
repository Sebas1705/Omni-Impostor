#!/usr/bin/env node
/**
 * Prototype of the concept map compiler (docs/studies/word-collection/concept-map-spec.md, section 6).
 * It reads concepts.jsonl + links.jsonl from a directory and prints the flat `{category}_{lang}.json` content the
 * app consumes today, so the rules of the spec (language filter, help level, giveaway, hubs, proposals) can be tried.
 *
 *   node scripts/word-study/concept-compile-prototype.mjs <dir> <lang> [low|normal|high] [--include-proposed]
 *   node scripts/word-study/concept-compile-prototype.mjs docs/studies/word-collection/concept-map-example es high
 *
 * Prototype only: no validation beyond what the compile needs, and it is not wired into the build.
 */
import { readFileSync } from 'node:fs';
import { join } from 'node:path';

const [dir, lang = 'es', help = 'normal'] = process.argv.slice(2).filter((a) => !a.startsWith('--'));
const includeProposed = process.argv.includes('--include-proposed');

const MAX_REVEAL = { low: 1, normal: 2, high: 3 };
const REVEAL = { association: 1, context: 1, contrast: 2, broader: 2, part: 2, synonym: 3 };
const MAX_CLUES = 6; // cap per entry
const MIN_CLUES = 3; // never drop below this
const FLOOR = 0.2; // clues scoring under this are dropped while more than MIN_CLUES remain
const HUB_K = 8;

const readJsonl = (name) =>
  readFileSync(join(dir, name), 'utf8').split(/\r?\n/).filter((l) => l.trim()).map((l) => JSON.parse(l));
const norm = (s) => s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim().replace(/\s+/g, ' ');
const approved = (x) => includeProposed || (x.status ?? 'approved') === 'approved';

const concepts = new Map(readJsonl('concepts.jsonl').map((c) => [c.id, c]));
const links = readJsonl('links.jsonl').filter(approved);

// in-degree over the whole map (language neutral): how many concepts use this one as a clue
const inDegree = new Map();
for (const l of links) inDegree.set(l.to, (inDegree.get(l.to) ?? 0) + 1);
const hubFactor = (id) => Math.min(1, Math.sqrt(HUB_K / Math.max(inDegree.get(id) ?? 1, 1)));

const out = {};
const report = [];
for (const c of concepts.values()) {
  const label = c.labels[lang];
  if (!label) continue; // no label in this language: the concept is not a word here
  const own = new Set([label.text, ...(label.aliases ?? [])].map(norm));
  const candidates = [];
  for (const l of links.filter((x) => x.from === c.id)) {
    if (l.langs && !l.langs.includes(lang)) continue; // link only valid in some languages
    const target = concepts.get(l.to)?.labels[lang];
    if (!target) { report.push(`${c.id} -> ${l.to}: target has no ${lang} label, not usable`); continue; }
    candidates.push({ text: target.text, type: l.type ?? 'association', w: l.w ?? 3, factor: hubFactor(l.to) });
  }
  for (const h of c.hints?.[lang] ?? []) {
    if (!approved(h)) continue;
    candidates.push({ text: h.t, type: h.type ?? 'association', w: h.w ?? 3, factor: 1 });
  }
  const seen = new Set();
  const usable = candidates
    .filter((x) => REVEAL[x.type] <= MAX_REVEAL[help])
    .filter((x) => !own.has(norm(x.text)))
    .filter((x) => !seen.has(norm(x.text)) && seen.add(norm(x.text)))
    .map((x) => ({ ...x, score: (x.w / 5) * x.factor }))
    .sort((a, b) => b.score - a.score || a.text.localeCompare(b.text));
  let clues = usable.slice(0, MAX_CLUES);
  while (clues.length > MIN_CLUES && clues[clues.length - 1].score < FLOOR) clues.pop();
  if (clues.length < MIN_CLUES) { report.push(`${c.id}: only ${clues.length} usable clue(s) in ${lang}/${help}, not emitted`); continue; }
  for (const category of c.categories) {
    (out[`${category}_${lang}`] ??= []).push({ word: label.text, clues: clues.map((x) => x.text), category, conceptId: c.id });
  }
}
console.log(JSON.stringify(out, null, 1));
if (report.length) console.error('\nnotes:\n - ' + report.join('\n - '));

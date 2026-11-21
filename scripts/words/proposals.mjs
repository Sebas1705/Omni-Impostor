#!/usr/bin/env node
/**
 * Proposal pipeline of the concept map (docs/studies/word-collection/concept-map-spec.md, section 8).
 * Proposals are written by an AI (or by hand) into data/files/concepts/proposals/*.jsonl. They are NOT part of the
 * concept map until the owner reviews them and runs `apply`; so `concepts.jsonl` and `links.jsonl` only ever hold
 * approved data. Reviewing a batch = reading `review`, editing the file (delete a line to reject it, change a value to
 * correct it), then `apply`.
 *
 *   node scripts/words/proposals.mjs export labels <outDir> [size]   # batches of concepts that still need a Spanish label
 *   node scripts/words/proposals.mjs export types <outDir> [size]    # batches of concepts whose clue types are unreviewed
 *   node scripts/words/proposals.mjs check <file...>                 # validate proposal files (exit 1 on any problem)
 *   node scripts/words/proposals.mjs review <file>                   # readable table of a batch
 *   node scripts/words/proposals.mjs apply <file...>                 # merge into the concept map, delete the file
 *
 * Record shapes (one JSON per line):
 *   labels-es-NNN.jsonl  {"id","es":{"text","hints":[...]}}      Spanish label and translated clues of an English-only concept
 *   types-NNN.jsonl      {"id","lang","clues":{"<clue text>":"synonym|broader|part|context|contrast"}}
 *                        only the clues that are NOT plain associations; everything else stays `association`
 */
import { existsSync, mkdirSync, readFileSync, unlinkSync, writeFileSync } from 'node:fs';
import { basename, join } from 'node:path';
import { LANGS, REVEAL, compileConcept, conceptsDir, indexMap, inDegrees, loadMap, norm, readJsonl, tokens } from './lib.mjs';

const [cmd, ...rest] = process.argv.slice(2);
const TYPES = new Set(Object.keys(REVEAL));
const TYPED = new Set(['synonym', 'broader', 'part', 'context', 'contrast']);
const jsonl = (rows) => rows.map((r) => JSON.stringify(r)).join('\n') + '\n';

function clueCandidates(map, indexed, inDeg, concept, lang) {
  const r = compileConcept(concept, lang, 'high', indexed, inDeg);
  return r ? r.clues : [];
}

function exportBatches(kind, outDir, size) {
  const map = loadMap();
  const indexed = indexMap(map);
  const inDeg = inDegrees(map.links);
  mkdirSync(outDir, { recursive: true });
  let items;
  if (kind === 'labels') {
    items = map.concepts
      .filter((c) => !c.labels.es && c.labels.en)
      .map((c) => ({
        id: c.id,
        en: c.labels.en.text,
        categories: c.categories,
        clues: clueCandidates(map, indexed, inDeg, c, 'en'),
      }));
  } else if (kind === 'types') {
    items = map.concepts.flatMap((c) =>
      LANGS.filter((l) => c.labels[l]).map((lang) => ({
        id: c.id,
        lang,
        word: c.labels[lang].text,
        categories: c.categories,
        clues: clueCandidates(map, indexed, inDeg, c, lang),
      })).filter((x) => x.clues.length)
    );
  } else throw new Error(`unknown kind ${kind}`);
  const batches = [];
  for (let i = 0; i < items.length; i += size) batches.push(items.slice(i, i + size));
  batches.forEach((b, i) => writeFileSync(join(outDir, `${kind}-${String(i + 1).padStart(3, '0')}.in.jsonl`), jsonl(b)));
  console.log(`${items.length} items in ${batches.length} batches written to ${outDir}`);
}

function readProposals(file) {
  return readJsonl(file).map((r) => ({ ...r, _file: file }));
}

function kindOf(file) {
  const b = basename(file);
  if (b.startsWith('labels-es-')) return 'labels';
  if (b.startsWith('types-')) return 'types';
  throw new Error(`unknown proposal file name: ${b}`);
}

function check(files) {
  const map = loadMap();
  const indexed = indexMap(map);
  const inDeg = inDegrees(map.links);
  const problems = [];
  const esLabels = new Map();
  for (const o of map.concepts) if (o.labels.es) {
    const k = norm(o.labels.es.text);
    if (!esLabels.has(k)) esLabels.set(k, []);
    esLabels.get(k).push(o);
  }
  const proposedLabels = new Map();
  for (const file of files) {
    const kind = kindOf(file);
    const seen = new Set();
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      const where = `${basename(file)} ${r.id}`;
      if (!c) { problems.push(`${where}: unknown concept`); continue; }
      if (kind === 'labels') {
        if (c.labels.es) problems.push(`${where}: already has a Spanish label`);
        const es = r.es;
        if (!es?.text || !es.text.trim()) { problems.push(`${where}: missing text`); continue; }
        if (tokens(es.text).length > 3 || /[()「」|;]/.test(es.text)) problems.push(`${where}: label "${es.text}" is not 1-3 clean words`);
        const hints = es.hints ?? [];
        if (hints.length < 3) problems.push(`${where}: needs at least 3 hints`);
        const ns = hints.map(norm);
        if (new Set(ns).size !== ns.length) problems.push(`${where}: repeated hint`);
        if (ns.includes(norm(es.text))) problems.push(`${where}: a hint equals the label`);
        const twins = (esLabels.get(norm(es.text)) ?? []).filter((o) => o.id !== r.id);
        for (const twin of twins) {
          const mergeable = !twin.labels.en && twin.categories.some((x) => c.categories.includes(x));
          if (!mergeable) {
            problems.push(`${where}: "${es.text}" is already the Spanish label of ${twin.id}${twin.labels.en ? ` (${twin.labels.en.text})` : ''}: same concept (merge by hand) or pick another label`);
          }
        }
        const clash = proposedLabels.get(norm(es.text));
        if (clash && clash !== r.id) problems.push(`${where}: "${es.text}" is also proposed for ${clash}`);
        proposedLabels.set(norm(es.text), r.id);
        for (const h of hints) if (typeof h !== 'string' || !h.trim() || tokens(h).length > 3 || /[()「」|;]/.test(h)) problems.push(`${where}: bad hint "${h}"`);
      } else {
        const key = `${r.id}|${r.lang}`;
        if (seen.has(key)) problems.push(`${where}: duplicate record for ${r.lang}`);
        seen.add(key);
        if (!c.labels[r.lang]) { problems.push(`${where}: no ${r.lang} label`); continue; }
        const known = new Set(clueCandidates(map, indexed, inDeg, c, r.lang).map(norm));
        for (const [clue, type] of Object.entries(r.clues ?? {})) {
          if (!TYPED.has(type)) problems.push(`${where}: type "${type}" for "${clue}"`);
          if (!known.has(norm(clue))) problems.push(`${where}: "${clue}" is not a clue of this concept in ${r.lang}`);
        }
      }
    }
  }
  if (problems.length) { console.error(problems.slice(0, 40).join('\n') + (problems.length > 40 ? `\n... and ${problems.length - 40} more` : '')); return false; }
  console.log(`${files.length} file(s) valid`);
  return true;
}

function review(file) {
  const map = loadMap();
  const kind = kindOf(file);
  const out = [`# ${basename(file)}`, ''];
  if (kind === 'labels') {
    out.push('| id | English | Spanish proposal | Spanish hints |', '|---|---|---|---|');
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      out.push(`| ${r.id} | ${c?.labels.en?.text} | ${r.es.text} | ${r.es.hints.join(', ')} |`);
    }
  } else {
    out.push('| id | lang | word | typed clues |', '|---|---|---|---|');
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      out.push(`| ${r.id} | ${r.lang} | ${c?.labels[r.lang]?.text} | ${Object.entries(r.clues).map(([k, v]) => `${k} (${v})`).join(', ')} |`);
    }
  }
  console.log(out.join('\n'));
}

/** Folds the English-only concept `from` into the Spanish concept `into`: they are the same concept. */
function mergeInto(map, into, from) {
  into.labels = { ...into.labels, en: from.labels.en };
  into.categories = [...new Set([...into.categories, ...from.categories])].sort();
  if (from.hints?.en) into.hints = { ...(into.hints ?? {}), en: from.hints.en };
  const rewritten = new Map();
  for (const l of map.links) {
    const a = l.from === from.id ? into.id : l.from;
    const b = l.to === from.id ? into.id : l.to;
    if (a === b) { l.drop = true; continue; }
    const key = a + '>' + b;
    if (rewritten.has(key)) {
      const kept = rewritten.get(key);
      kept.langs = kept.langs && l.langs ? [...new Set([...kept.langs, ...l.langs])].sort() : undefined;
      if (!kept.langs) delete kept.langs;
      l.drop = true;
    } else { l.from = a; l.to = b; rewritten.set(key, l); }
  }
  for (let i = map.links.length - 1; i >= 0; i--) if (map.links[i].drop) map.links.splice(i, 1);
  from.merged = true;
}

function apply(files) {
  if (!check(files)) process.exit(1);
  const map = loadMap();
  const links = map.links;
  map.linksRef = links;
  let labels = 0, typedLinks = 0, typedHints = 0, merges = 0;
  for (const file of files) {
    const kind = kindOf(file);
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      if (kind === 'labels') {
        const twin = map.concepts.find((o) => o.id !== c.id && o.labels.es && !o.labels.en && norm(o.labels.es.text) === norm(r.es.text) &&
          o.categories.some((x) => c.categories.includes(x)));
        if (twin) { mergeInto(map, twin, c); merges++; continue; }
        c.labels = { es: { text: r.es.text }, ...c.labels };
        c.hints = { ...(c.hints ?? {}), es: r.es.hints.map((t) => ({ t })) };
        c.todo = (c.todo ?? []).filter((l) => l !== 'es');
        if (!c.todo.length) delete c.todo;
        labels++;
      } else {
        for (const [clue, type] of Object.entries(r.clues)) {
          const n = norm(clue);
          const hint = (c.hints?.[r.lang] ?? []).find((h) => norm(h.t) === n);
          if (hint) { hint.type = type; typedHints++; continue; }
          const link = links.find((l) => l.from === c.id && (!l.langs || l.langs.includes(r.lang)) &&
            map.byId.get(l.to)?.labels[r.lang] && norm(map.byId.get(l.to).labels[r.lang].text) === n);
          if (link) { link.type = type; typedLinks++; }
        }
      }
    }
  }
  const sortedConcepts = map.concepts.filter((c) => !c.merged).sort((a, b) => a.id.localeCompare(b.id));
  writeFileSync(join(conceptsDir, 'concepts.jsonl'), jsonl(sortedConcepts));
  writeFileSync(join(conceptsDir, 'links.jsonl'), jsonl(links));
  files.forEach((f) => unlinkSync(f));
  console.log(`applied: ${labels} Spanish labels, ${merges} merged into an existing Spanish concept, ${typedLinks} typed links, ${typedHints} typed hints. Now run: node scripts/words/compile.mjs`);
}

switch (cmd) {
  case 'export': exportBatches(rest[0], rest[1], Number(rest[2] ?? 250)); break;
  case 'check': process.exit(check(rest) ? 0 : 1); break;
  case 'review': review(rest[0]); break;
  case 'apply': apply(rest); break;
  default: console.error('usage: proposals.mjs export|check|review|apply ...'); process.exit(2);
}

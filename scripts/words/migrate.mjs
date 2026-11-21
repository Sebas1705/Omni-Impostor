#!/usr/bin/env node
/**
 * One-off migration (phase 1): flat word lists -> concept map sources.
 *
 *   node scripts/words/migrate.mjs            # writes data/files/concepts/{concepts,links}.jsonl
 *   node scripts/words/migrate.mjs --dry      # only prints the summary
 *
 * Reads data/files/src/main/assets/*_{en,es}.json (the lists as they are today). Entries of the same word that share a
 * clue are one sense (merged); the rest are homonyms. A clue equal to the label of a single concept (or of the best
 * matching sense) becomes a link (`association`, w 3, restricted to its language), anything else a hint. English and
 * Spanish senses with the same normalised label and a shared category are paired into one concept. Every migrated
 * record is `approved`: it is data that already ships. English-only concepts get `todo: ["es"]`.
 * Design: docs/studies/word-collection/concept-map-spec.md.
 */
import { readdirSync, readFileSync, writeFileSync, mkdirSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { assetsDir, conceptsDir, norm, slug } from './lib.mjs';

const dry = process.argv.includes('--dry');
if (!dry && existsSync(join(conceptsDir, 'concepts.jsonl')) && !process.argv.includes('--force')) {
  console.error('data/files/concepts already exists. The assets are generated from it now, so migrating them again would lose data. Edit the concepts and run compile.mjs; use --force only on pristine hand-written lists.');
  process.exit(1);
}

// A clue with a parenthetical (`Hierbas (Anís)`) is two clues: the base text and the aside.
const splitParentheses = (clue) => {
  const m = clue.match(/^(.*?)\s*\(([^)]*)\)\s*(.*)$/);
  return m ? [`${m[1]} ${m[3]}`.trim(), m[2].trim()].filter(Boolean) : [clue];
};

// ---------------------------------------------------------------- load
const rows = [];
for (const file of readdirSync(assetsDir).sort()) {
  const m = file.match(/^(.+)_(en|es)\.json$/);
  if (!m) continue;
  const json = JSON.parse(readFileSync(join(assetsDir, file), 'utf8').replace(/^﻿/, ''));
  for (const w of json.words ?? []) rows.push({ category: m[1], lang: m[2], word: w.word, clues: (w.clues ?? []).flatMap(splitParentheses) });
}

// ---------------------------------------------------------------- senses per language
const senses = []; // { lang, key, word, categories:Set, clues: Map(norm -> text), rows }
for (const lang of ['es', 'en']) {
  const byKey = new Map();
  for (const r of rows.filter((x) => x.lang === lang)) {
    const k = norm(r.word);
    if (!byKey.has(k)) byKey.set(k, []);
    byKey.get(k).push(r);
  }
  for (const [key, entries] of [...byKey].sort(([a], [b]) => a.localeCompare(b))) {
    const parent = entries.map((_, i) => i);
    const find = (i) => (parent[i] === i ? i : (parent[i] = find(parent[i])));
    const seen = new Map();
    entries.forEach((e, i) => e.clues.forEach((c) => {
      const n = norm(c);
      if (seen.has(n)) parent[find(i)] = find(seen.get(n));
      else seen.set(n, i);
    }));
    const groups = new Map();
    entries.forEach((e, i) => { const g = find(i); if (!groups.has(g)) groups.set(g, []); groups.get(g).push(e); });
    for (const group of groups.values()) {
      const clues = new Map();
      group.forEach((e) => e.clues.forEach((c) => { const n = norm(c); if (n !== key && !clues.has(n)) clues.set(n, c); }));
      // keep the spelling with the most diacritics (accented form is the correct one)
      const word = group.map((e) => e.word).sort((a, b) => (b.normalize('NFD').length - a.normalize('NFD').length))[0];
      senses.push({ lang, key, word, categories: new Set(group.map((e) => e.category)), clues, homonym: groups.size > 1 });
    }
  }
}

// ---------------------------------------------------------------- pair EN <-> ES
const esByKey = new Map();
senses.filter((s) => s.lang === 'es').forEach((s) => { if (!esByKey.has(s.key)) esByKey.set(s.key, []); esByKey.get(s.key).push(s); });
const taken = new Set();
const claims = new Map(); // es sense -> English senses that point at it
const unpaired = [];
for (const en of senses.filter((s) => s.lang === 'en')) {
  const cands = (esByKey.get(en.key) ?? []).filter((c) => [...c.categories].some((x) => en.categories.has(x)));
  if (cands.length === 1) { if (!claims.has(cands[0])) claims.set(cands[0], []); claims.get(cands[0]).push(en); }
  else unpaired.push(en);
}
const concepts = []; // { es?, en?, categories:Set }
for (const [es, ens] of claims) {
  taken.add(es);
  // several English senses of one word that all match the same Spanish sense are one concept (e.g. a brand listed in two categories)
  const en = ens.length === 1 ? ens[0] : {
    ...ens[0],
    categories: new Set(ens.flatMap((e) => [...e.categories])),
    clues: new Map(ens.flatMap((e) => [...e.clues])),
    homonym: false,
  };
  concepts.push({ es, en });
}
for (const en of unpaired) concepts.push({ en });
for (const es of senses.filter((s) => s.lang === 'es' && !taken.has(s))) concepts.push({ es });

// ---------------------------------------------------------------- ids
const primaryLabel = (c) => (c.es ?? c.en).word;
const tag = (c) => slug([...(c.es ?? c.en).categories].sort()[0].replace(/_/g, '-'));
for (const c of concepts) c.categories = new Set([...(c.es?.categories ?? []), ...(c.en?.categories ?? [])]);
const bySlug = new Map();
for (const c of concepts) {
  c.slug = slug(primaryLabel(c)) || `x-${slug(norm(primaryLabel(c))) || 'item'}`;
  if (!bySlug.has(c.slug)) bySlug.set(c.slug, []);
  bySlug.get(c.slug).push(c);
}
const ids = new Set();
for (const [s, group] of [...bySlug].sort(([a], [b]) => a.localeCompare(b))) {
  group.sort((a, b) => [...a.categories].sort().join().localeCompare([...b.categories].sort().join()));
  group.forEach((c, i) => {
    let id = group.length === 1 ? s : `${s}~${tag(c)}`;
    let n = 2;
    while (ids.has(id)) id = `${s}~${tag(c)}-${n++}`;
    ids.add(id);
    c.id = id;
  });
}

// ---------------------------------------------------------------- links and hints
const conceptsByLangKey = { es: new Map(), en: new Map() };
for (const c of concepts) for (const lang of ['es', 'en']) {
  if (!c[lang]) continue;
  const m = conceptsByLangKey[lang];
  if (!m.has(c[lang].key)) m.set(c[lang].key, []);
  m.get(c[lang].key).push(c);
}
const overlap = (a, b) => [...a.categories].filter((x) => b.categories.has(x)).length;
const linkMap = new Map(); // "from>to" -> {from,to,langs:Set}
let ambiguousResolved = 0, ambiguousLeft = 0, linkCount = 0, hintCount = 0;
for (const c of concepts) {
  c.hints = {};
  for (const lang of ['es', 'en']) {
    const s = c[lang];
    if (!s) continue;
    for (const [n, text] of s.clues) {
      let targets = conceptsByLangKey[lang].get(n) ?? [];
      targets = targets.filter((t) => t !== c);
      if (targets.length > 1) {
        const scored = targets.map((t) => ({ t, o: overlap(c, t) })).sort((a, b) => b.o - a.o);
        if (scored[0].o > 0 && scored[0].o > scored[1].o) { targets = [scored[0].t]; ambiguousResolved++; }
        else { targets = []; ambiguousLeft++; }
      }
      if (targets.length === 1) {
        const key = `${c.id}>${targets[0].id}`;
        if (!linkMap.has(key)) linkMap.set(key, { from: c.id, to: targets[0].id, langs: new Set() });
        linkMap.get(key).langs.add(lang);
      } else {
        (c.hints[lang] ??= []).push({ t: text });
        hintCount++;
      }
    }
  }
}
const links = [...linkMap.values()]
  .map((l) => ({ from: l.from, to: l.to, type: 'association', w: 3, langs: [...l.langs].sort() }))
  .sort((a, b) => a.from.localeCompare(b.from) || a.to.localeCompare(b.to));
linkCount = links.length;

// ---------------------------------------------------------------- records
const records = concepts
  .map((c) => {
    const labels = {};
    if (c.es) labels.es = { text: c.es.word };
    if (c.en) labels.en = { text: c.en.word };
    const r = { id: c.id, labels, categories: [...c.categories].sort(), hints: c.hints };
    if (!c.es) r.todo = ['es'];
    return r;
  })
  .sort((a, b) => a.id.localeCompare(b.id));

// ---------------------------------------------------------------- lossless check
const byId = new Map(records.map((r) => [r.id, r]));
const outgoing = new Map();
links.forEach((l) => { if (!outgoing.has(l.from)) outgoing.set(l.from, []); outgoing.get(l.from).push(l); });
let checked = 0, lost = 0;
for (const r of rows) {
  const c = concepts.find((x) => x[r.lang]?.key === norm(r.word) && x[r.lang].categories.has(r.category));
  for (const clue of r.clues) {
    const n = norm(clue);
    if (n === norm(r.word)) continue;
    checked++;
    const asHint = (c.hints[r.lang] ?? []).some((h) => norm(h.t) === n);
    const asLink = (outgoing.get(c.id) ?? []).some((l) => l.langs.includes(r.lang) && byId.get(l.to).labels[r.lang] && norm(byId.get(l.to).labels[r.lang].text) === n);
    if (!asHint && !asLink) { lost++; if (process.env.DEBUG) console.log("LOST", r.lang, r.category, r.word, "->", clue, c.id); }
  }
}

const summary = [
  `concepts: ${records.length} (paired ${concepts.filter((c) => c.es && c.en).length}, es only ${concepts.filter((c) => c.es && !c.en).length}, en only ${concepts.filter((c) => !c.es).length})`,
  `links: ${linkCount}, hints: ${hintCount}, homonym clues resolved by category: ${ambiguousResolved}, kept as hints: ${ambiguousLeft}`,
  `lossless check: ${checked} clues, ${lost} lost`,
];
console.log(summary.join('\n'));
if (lost > 0) { console.error('migration would lose clues, nothing written'); process.exit(1); }
if (!dry) {
  mkdirSync(conceptsDir, { recursive: true });
  writeFileSync(join(conceptsDir, 'concepts.jsonl'), records.map((r) => JSON.stringify(r)).join('\n') + '\n');
  writeFileSync(join(conceptsDir, 'links.jsonl'), links.map((l) => JSON.stringify(l)).join('\n') + '\n');
  console.log(`written to ${conceptsDir}`);
}

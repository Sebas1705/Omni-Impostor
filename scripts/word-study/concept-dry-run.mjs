#!/usr/bin/env node
/**
 * Dry run of the migration from the flat word lists to the concept map described in
 * docs/studies/word-collection/concept-map-spec.md. It changes no source file; it only measures what the
 * migration would produce and checks that no clue would be lost.
 *
 *   node scripts/word-study/concept-dry-run.mjs
 *
 * Output: docs/studies/word-collection/data/concept-dry-run.md
 *
 * Simplifications (so every figure is approximate): matching ignores case, accents and leading articles; plural and
 * lemma variants are not merged; "same sense" means two entries of the same word share at least one clue.
 */
import { readdirSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const assets = join(root, 'data/files/src/main/assets');
const out = join(root, 'docs/studies/word-collection/data');
mkdirSync(out, { recursive: true });

const norm = (s) =>
  s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim().replace(/\s+/g, ' ').replace(/^(the|el|la|los|las|un|una)\s/, '');

// ---------------------------------------------------------------- load
const rows = [];
for (const file of readdirSync(assets).sort()) {
  const m = file.match(/^(.+)_(en|es)\.json$/);
  if (!m) continue;
  const json = JSON.parse(readFileSync(join(assets, file), 'utf8').replace(/^﻿/, ''));
  for (const w of json.words ?? []) rows.push({ category: m[1], lang: m[2], word: w.word, clues: w.clues ?? [] });
}

// ---------------------------------------------------------------- 1. senses (concepts per language)
const langs = ['en', 'es'];
const concepts = []; // { lang, key, word, categories:Set, clues:Map(norm -> text), entries }
for (const lang of langs) {
  const byWord = new Map();
  for (const r of rows.filter((x) => x.lang === lang)) {
    const k = norm(r.word);
    if (!byWord.has(k)) byWord.set(k, []);
    byWord.get(k).push(r);
  }
  for (const [key, entries] of byWord) {
    // union-find over entries that share a clue
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
      concepts.push({ lang, key, word: group[0].word, categories: new Set(group.map((e) => e.category)), clues, entries: group, homonym: groups.size > 1 });
    }
  }
}
const per = (lang) => concepts.filter((c) => c.lang === lang);
const entriesTotal = rows.length;

// ---------------------------------------------------------------- 2. translations (EN <-> ES)
const esByKey = new Map();
per('es').forEach((c) => { if (!esByKey.has(c.key)) esByKey.set(c.key, []); esByKey.get(c.key).push(c); });
let paired = 0, pairedNoCat = 0, ambiguousPair = 0;
for (const en of per('en')) {
  const cands = esByKey.get(en.key) ?? [];
  if (!cands.length) continue;
  const withCat = cands.filter((c) => [...c.categories].some((x) => en.categories.has(x)));
  if (withCat.length === 1) paired++;
  else if (withCat.length > 1) ambiguousPair++;
  else pairedNoCat++;
}
const enOnly = per('en').length - paired - pairedNoCat - ambiguousPair;

// ---------------------------------------------------------------- 3. links vs hints (per language)
const stats = {};
const inDegree = {};
const lossless = { checked: 0, lost: 0 };
for (const lang of langs) {
  const byKey = new Map();
  per(lang).forEach((c) => { if (!byKey.has(c.key)) byKey.set(c.key, []); byKey.get(c.key).push(c); });
  let links = 0, ambiguous = 0, hints = 0, mutual = 0;
  const indeg = new Map();
  const linkSet = new Set();
  for (const c of per(lang)) {
    for (const [n] of c.clues) {
      const targets = byKey.get(n);
      if (!targets) { hints++; continue; }
      if (targets.length > 1) { ambiguous++; continue; }
      links++;
      indeg.set(n, (indeg.get(n) ?? 0) + 1);
      linkSet.add(`${c.key}>${n}`);
    }
  }
  for (const l of linkSet) { const [a, b] = l.split('>'); if (linkSet.has(`${b}>${a}`)) mutual++; }
  // every original clue must be a link target label or a hint of its concept (lossless check)
  for (const c of per(lang)) for (const e of c.entries) for (const clue of e.clues) {
    lossless.checked++;
    const n = norm(clue);
    if (n !== c.key && !c.clues.has(n)) lossless.lost++;
  }
  stats[lang] = { links, ambiguous, hints, mutual: mutual / 2, indeg };
  inDegree[lang] = indeg;
}

// ---------------------------------------------------------------- 4. hub factor and compile simulation
const hubFactor = (n) => Math.min(1, Math.sqrt(8 / Math.max(n, 1)));
const K = 6;
const compile = {};
for (const lang of langs) {
  let entries = 0, underfilled = 0, giveaway = 0, clueSlots = 0, hubSlotsBefore = 0, hubSlotsAfter = 0, slotsAfter = 0;
  for (const c of per(lang)) {
    const scored = [...c.clues].map(([n, text]) => {
      const deg = inDegree[lang].get(n) ?? 0;
      return { n, text, s: hubFactor(deg), hub: deg >= 10 };
    });
    scored.sort((a, b) => b.s - a.s || a.text.localeCompare(b.text));
    const picked = scored.slice(0, K);
    const cats = c.categories.size;
    entries += cats;
    if (picked.length < 3) underfilled++;
    clueSlots += scored.length;
    hubSlotsBefore += scored.filter((x) => x.hub).length;
    hubSlotsAfter += picked.filter((x) => x.hub).length;
    slotsAfter += picked.length;
  }
  compile[lang] = { entries, underfilled, clueSlots, hubSlotsBefore, hubSlotsAfter, slotsAfter };
}

// ---------------------------------------------------------------- report
const pct = (a, b) => (b ? ((100 * a) / b).toFixed(1) + ' %' : 'n/a');
const hubs = (lang) => [...inDegree[lang].values()].filter((n) => n >= 10).length;
const md = [];
md.push('# Concept map dry run');
md.push('');
md.push('Generated by `scripts/word-study/concept-dry-run.mjs` from `data/files/src/main/assets/*_{en,es}.json`. Nothing was changed; these are the sizes the migration described in `concept-map-spec.md` would produce. Matching ignores case, accents and leading articles; plurals are not merged.');
md.push('');
md.push('## Concepts (one per sense, per language)');
md.push('');
md.push('| | EN | ES |');
md.push('|---|---:|---:|');
md.push(`| Entries today | ${rows.filter((r) => r.lang === 'en').length} | ${rows.filter((r) => r.lang === 'es').length} |`);
md.push(`| Concepts after merging same-sense duplicates | ${per('en').length} | ${per('es').length} |`);
md.push(`| of which homonyms (same word, different sense) | ${per('en').filter((c) => c.homonym).length} | ${per('es').filter((c) => c.homonym).length} |`);
md.push(`| Concepts with 2+ categories | ${per('en').filter((c) => c.categories.size > 1).length} | ${per('es').filter((c) => c.categories.size > 1).length} |`);
md.push(`| Flat entries the compiler would emit (one per concept and category) | ${compile.en.entries} | ${compile.es.entries} |`);
md.push('');
md.push('## One map for both languages (EN to ES pairing)');
md.push('');
md.push(`- EN concepts with an identical ES label and a shared category (auto-pair candidates): **${paired}**`);
md.push(`- identical label but no shared category (review): **${pairedNoCat}**; several ES candidates (review): **${ambiguousPair}**`);
md.push(`- EN concepts with no identical ES label (need a translation proposal): **${enOnly}**`);
md.push(`- ES concepts: ${per('es').length}, so a merged map would have about ${per('es').length + enOnly} concepts before review (Spanish stays mandatory, so every EN-only concept gets a proposed ES label).`);
md.push('');
md.push('## Links and hints (a clue that is a concept becomes a link, otherwise a hint)');
md.push('');
md.push('| | EN | ES |');
md.push('|---|---:|---:|');
md.push(`| Links to a single concept | ${stats.en.links} | ${stats.es.links} |`);
md.push(`| Clues that point at a homonym (several concepts share the label, needs disambiguation) | ${stats.en.ambiguous} | ${stats.es.ambiguous} |`);
md.push(`| Text-only hints | ${stats.en.hints} | ${stats.es.hints} |`);
md.push(`| Links that already exist in both directions (pairs) | ${stats.en.mutual} | ${stats.es.mutual} |`);
md.push(`| Hub concepts (10+ incoming links) | ${hubs('en')} | ${hubs('es')} |`);
md.push('');
md.push('## Lossless check');
md.push('');
md.push(`Every original clue must end up as a link or a hint of its concept: ${lossless.checked} clues checked, **${lossless.lost} lost**.`);
md.push('');
md.push('## Compile simulation (top 6 clues by hub factor, no other scoring)');
md.push('');
md.push('| | EN | ES |');
md.push('|---|---:|---:|');
md.push(`| Concepts with fewer than 3 clues | ${compile.en.underfilled} | ${compile.es.underfilled} |`);
md.push(`| Hub clues among all clue slots before capping | ${pct(compile.en.hubSlotsBefore, compile.en.clueSlots)} | ${pct(compile.es.hubSlotsBefore, compile.es.clueSlots)} |`);
md.push(`| Hub clues among the clues the impostor can get after capping | ${pct(compile.en.hubSlotsAfter, compile.en.slotsAfter)} | ${pct(compile.es.hubSlotsAfter, compile.es.slotsAfter)} |`);
md.push('');
writeFileSync(join(out, 'concept-dry-run.md'), md.join('\n') + '\n');
console.log(md.join('\n'));

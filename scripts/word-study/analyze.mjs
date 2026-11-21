#!/usr/bin/env node
/**
 * Regenerates the data behind docs/studies/word-collection from the word assets.
 *
 *   node scripts/word-study/analyze.mjs
 *
 * Reads data/files/src/main/assets/*_{en,es}.json and writes CSV files into
 * docs/studies/word-collection/data (word-collection.csv is git-ignored, the others are committed).
 * Matching ignores case, accents and leading articles; plurals are not merged, so every
 * "clue is also a word" figure is a lower bound.
 */
import { readdirSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const assets = join(root, 'data/files/src/main/assets');
const out = join(root, 'docs/studies/word-collection/data');
mkdirSync(out, { recursive: true });

// ---------------------------------------------------------------- load
const rows = [];
for (const file of readdirSync(assets).filter((f) => f.endsWith('.json')).sort()) {
  const m = file.match(/^(.+)_(en|es)\.json$/);
  if (!m) continue;
  const json = JSON.parse(readFileSync(join(assets, file), 'utf8').replace(/^﻿/, ''));
  for (const w of json.words ?? []) rows.push({ category: m[1], lang: m[2], word: w.word, clues: w.clues ?? [] });
}
const cats = [...new Set(rows.map((r) => r.category))].sort();
const langs = ['en', 'es'];

const norm = (s) =>
  s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim().replace(/\s+/g, ' ').replace(/^(the|el|la|los|las|un|una)\s/, '');
const tokens = (s) => s.trim().split(/\s+/).filter(Boolean);
const q = (s) => `"${String(s).replace(/"/g, '""')}"`;
const writeCsv = (name, lines) => writeFileSync(join(out, name), '﻿' + lines.join('\n') + '\n');

// ---------------------------------------------------------------- per-language indexes
const index = {};
for (const lang of langs) {
  const heads = new Map();
  for (const r of rows.filter((x) => x.lang === lang)) {
    const k = norm(r.word);
    if (!heads.has(k)) heads.set(k, { word: r.word, cats: new Set(), clues: new Set(), entries: [] });
    const h = heads.get(k);
    h.cats.add(r.category);
    h.entries.push(r);
    r.clues.forEach((c) => h.clues.add(norm(c)));
  }
  const incoming = new Map();
  for (const [k, h] of heads) {
    for (const c of h.clues) {
      if (c === k || !heads.has(c)) continue;
      if (!incoming.has(c)) incoming.set(c, new Set());
      incoming.get(c).add(k);
    }
  }
  index[lang] = { heads, incoming };
}

// ---------------------------------------------------------------- word-collection.csv (git-ignored)
const all = ['category,language,word,clue_count,clues'];
for (const r of [...rows].sort((a, b) => a.category.localeCompare(b.category) || a.lang.localeCompare(b.lang) || a.word.localeCompare(b.word))) {
  all.push([r.category, r.lang, q(r.word), r.clues.length, q(r.clues.join(' | '))].join(','));
}
writeCsv('word-collection.csv', all);

// ---------------------------------------------------------------- issues.csv
const issues = ['issue,category,language,word,clues'];
const push = (issue, r) => issues.push([issue, r.category, r.lang, q(r.word), q(r.clues.join(' | '))].join(','));
for (const r of rows) {
  if (r.clues.some((c) => norm(c) === norm(r.word))) push('clue_equals_word', r);
  if (new Set(r.clues.map(norm)).size !== r.clues.length) push('repeated_clue', r);
  if (r.clues.length < 3) push('fewer_than_3_clues', r);
  if (tokens(r.word).length > 3) push('more_than_3_words', r);
  if (/[()]/.test(r.word)) push('parentheses_in_word', r);
}
for (const lang of langs) {
  for (const cat of cats) {
    const seen = new Set();
    for (const r of rows.filter((x) => x.lang === lang && x.category === cat)) {
      const k = norm(r.word);
      if (seen.has(k)) push('duplicate_in_list', r);
      seen.add(k);
    }
  }
}
writeCsv('issues.csv', issues);

// ---------------------------------------------------------------- duplicate-words.csv
const dup = ['language,word,categories,clue_relation,clue_lists'];
for (const lang of langs) {
  for (const h of index[lang].heads.values()) {
    const cs = h.entries.map((e) => e.category);
    if (new Set(cs).size < 2) continue;
    const sets = h.entries.map((e) => new Set(e.clues.map(norm)));
    const inter = [...sets[0]].filter((x) => sets.every((s) => s.has(x))).length;
    const union = new Set(sets.flatMap((s) => [...s])).size;
    const rel = inter === union ? 'identical' : inter === 0 ? 'disjoint (likely different sense)' : 'partial overlap (likely same sense)';
    dup.push([lang, q(h.word), q(cs.join(' + ')), rel, q(h.entries.map((e) => `${e.category}: ${e.clues.join(', ')}`).join(' || '))].join(','));
  }
}
writeCsv('duplicate-words.csv', dup);

// ---------------------------------------------------------------- hub clues
const hubs = ['language,clue,entries_using_it,is_word_in_collection'];
const hubsNotWords = ['language,clue,entries_using_it'];
for (const lang of langs) {
  const df = new Map();
  for (const r of rows.filter((x) => x.lang === lang)) for (const c of new Set(r.clues.map(norm))) df.set(c, (df.get(c) ?? 0) + 1);
  for (const [c, n] of [...df].filter((e) => e[1] >= 10).sort((a, b) => b[1] - a[1])) {
    const isWord = index[lang].heads.has(c);
    hubs.push([lang, q(c), n, isWord ? 'yes' : 'no'].join(','));
    if (!isWord) hubsNotWords.push([lang, q(c), n].join(','));
  }
}
writeCsv('hub-clues.csv', hubs);
writeCsv('hub-clues-not-words.csv', hubsNotWords);

// ---------------------------------------------------------------- category candidates (graph evidence)
const cand = ['language,word,current_categories,candidate_category,supporting_neighbours,neighbours_checked'];
const candList = [];
for (const lang of langs) {
  const { heads, incoming } = index[lang];
  for (const [k, h] of heads) {
    const nb = new Set([...[...h.clues].filter((c) => heads.has(c)), ...(incoming.get(k) ?? [])]);
    nb.delete(k);
    if (nb.size < 3) continue;
    const count = {};
    for (const n of nb) for (const c of heads.get(n).cats) count[c] = (count[c] ?? 0) + 1;
    for (const [c, n] of Object.entries(count)) {
      if (!h.cats.has(c) && n >= 3 && n / nb.size >= 0.4) candList.push({ lang, word: h.word, from: [...h.cats], to: c, n, of: nb.size });
    }
  }
}
for (const c of candList.sort((a, b) => b.n - a.n)) cand.push([c.lang, q(c.word), q(c.from.join(' + ')), c.to, c.n, c.of].join(','));
writeCsv('category-candidates.csv', cand);

// ---------------------------------------------------------------- stats
const stats = { entries: rows.length, categories: cats.length, perLanguage: {} };
for (const lang of langs) {
  const R = rows.filter((r) => r.lang === lang);
  const { heads, incoming } = index[lang];
  let slots = 0, clueIsWord = 0, reciprocal = 0;
  for (const [k, h] of heads) {
    for (const c of h.clues) {
      if (c === k) continue;
      slots++;
      if (heads.has(c)) {
        clueIsWord++;
        if (heads.get(c).clues.has(k)) reciprocal++;
      }
    }
  }
  const tok = {};
  R.forEach((r) => { const n = tokens(r.word).length; tok[n] = (tok[n] ?? 0) + 1; });
  stats.perLanguage[lang] = {
    entries: R.length,
    distinctWords: heads.size,
    wordsInSeveralCategories: [...heads.values()].filter((h) => h.cats.size > 1).length,
    clueSlots: slots,
    cluesThatAreWords: clueIsWord,
    reciprocalLinks: reciprocal,
    wordsWithIncomingLinks: incoming.size,
    wordTokens: tok,
  };
}
// ---------------------------------------------------------------- collection-summary.md
const perCat = cats.map((c) => ({ c, en: rows.filter((r) => r.category === c && r.lang === 'en').length, es: rows.filter((r) => r.category === c && r.lang === 'es').length }));
const md = ['# Word collection summary', '', 'Generated by `scripts/word-study/analyze.mjs`. Do not edit by hand.', '',
  `- Categories: ${cats.length}, lists: ${cats.length * 2}`, `- Entries: ${rows.length} (EN ${stats.perLanguage.en.entries}, ES ${stats.perLanguage.es.entries})`,
  `- Distinct words: EN ${stats.perLanguage.en.distinctWords}, ES ${stats.perLanguage.es.distinctWords}`, '', '| Category | EN | ES |', '|---|---:|---:|',
  ...perCat.map((p) => `| ${p.c} | ${p.en} | ${p.es} |`), `| **Total** | **${stats.perLanguage.en.entries}** | **${stats.perLanguage.es.entries}** |`, ''];
writeFileSync(join(root, 'docs/studies/word-collection/collection-summary.md'), md.join('\n'));

console.log(JSON.stringify(stats, null, 2));
console.log(`issues: ${issues.length - 1}, duplicates: ${dup.length - 1}, hub clues: ${hubs.length - 1}, category candidates: ${cand.length - 1}`);

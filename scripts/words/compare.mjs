#!/usr/bin/env node
/**
 * Compares two directories of flat word lists (e.g. the assets before and after a migration or a compile).
 *
 *   node scripts/words/compare.mjs <oldDir> [newDir]     (newDir defaults to data/files/src/main/assets)
 *
 * Reports entries added/removed per language and, for entries present in both, how many clue lists changed and how
 * many clues were dropped or added. Matching ignores case, accents and leading articles.
 */
import { readdirSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { assetsDir, norm } from './lib.mjs';

const [oldDir, newDir = assetsDir] = process.argv.slice(2);
if (!oldDir) { console.error('usage: compare.mjs <oldDir> [newDir]'); process.exit(2); }

function load(dir) {
  const map = new Map();
  for (const f of readdirSync(dir).filter((x) => /_(en|es)\.json$/.test(x))) {
    const [, category, lang] = f.match(/^(.+)_(en|es)\.json$/);
    for (const w of JSON.parse(readFileSync(join(dir, f), 'utf8').replace(/^﻿/, '')).words) {
      map.set(`${lang}|${category}|${norm(w.word)}`, { word: w.word, clues: w.clues.map(norm), raw: w.clues });
    }
  }
  return map;
}
const a = load(oldDir), b = load(newDir);
const stat = { en: { added: 0, removed: 0, same: 0, changed: 0, dropped: 0, gained: 0 }, es: { added: 0, removed: 0, same: 0, changed: 0, dropped: 0, gained: 0 } };
const droppedExamples = [];
for (const [k, o] of a) {
  const lang = k.slice(0, 2);
  const n = b.get(k);
  if (!n) { stat[lang].removed++; continue; }
  const dropped = o.clues.filter((c) => !n.clues.includes(c));
  const gained = n.clues.filter((c) => !o.clues.includes(c));
  if (!dropped.length && !gained.length) stat[lang].same++;
  else {
    stat[lang].changed++;
    stat[lang].dropped += dropped.length;
    stat[lang].gained += gained.length;
    if (dropped.length && droppedExamples.length < 12) droppedExamples.push(`${k}: -[${dropped.join(', ')}]`);
  }
}
for (const k of b.keys()) if (!a.has(k)) stat[k.slice(0, 2)].added++;
console.table(stat);
console.log('entries before/after:', a.size, b.size);
if (droppedExamples.length) console.log('examples of dropped clues:\n - ' + droppedExamples.join('\n - '));

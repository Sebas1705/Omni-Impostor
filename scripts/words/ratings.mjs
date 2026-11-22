#!/usr/bin/env node
/**
 * Calibrates clue weights with the ratings players give after a match (Firestore `clue_ratings`).
 *
 *   node scripts/words/ratings.mjs fetch <outFile>       # download the collection (needs a Google access token, see below)
 *   node scripts/words/ratings.mjs import <exportFile>   # map ratings onto concepts, write data/files/concepts/ratings.jsonl
 *   node scripts/words/ratings.mjs report                # worst and best rated clues of the current ratings.jsonl
 *
 * `compile.mjs` multiplies the score of a rated clue by `ratingFactor` (lower bound of the Wilson interval of the share of
 * positive votes, see lib.mjs); clues with fewer than 5 votes are untouched. Ratings start positive in the app (a player
 * only taps a clue to dislike it), so a low factor is a strong signal and a high one a weak one.
 *
 * fetch: reads the collection through the Firestore REST API with the access token in FIRESTORE_TOKEN (for example
 * `$env:FIRESTORE_TOKEN = gcloud auth print-access-token`) and the project id in FIREBASE_PROJECT. The collection holds
 * anonymous counters only (word, category, clue, positive, negative), no user data. The security rules deny client reads,
 * so this needs the owner's own credentials: run it yourself, nothing in CI does.
 *
 * Export file: JSON array or JSON Lines of {word, category, clue, positive, negative}.
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { conceptsDir, labelOf, loadMap, norm, ratingFactor, RATING, readJsonl } from './lib.mjs';

const ratingsFile = join(conceptsDir, 'ratings.jsonl');

/** Maps raw rating documents to {id, lang, clue, pos, neg} lines; returns also what could not be mapped. */
export function mapRatings(docs, map) {
  const byLabel = new Map(); // `${lang}|${norm(label)}` -> concepts
  for (const c of map.concepts) for (const [lang, l] of Object.entries(c.labels)) {
    const k = `${lang}|${norm(l.text)}`;
    if (!byLabel.has(k)) byLabel.set(k, []);
    byLabel.get(k).push(c);
  }
  const clueTexts = (c, lang) => {
    const own = (c.hints?.[lang] ?? []).map((h) => norm(h.t));
    const linked = map.links
      .filter((l) => l.from === c.id && (!l.langs || l.langs.includes(lang)))
      .map((l) => labelOf(map.byId.get(l.to), lang)?.text)
      .filter(Boolean)
      .map(norm);
    return new Set([...own, ...linked]);
  };
  const acc = new Map();
  const unmatched = [];
  for (const d of docs) {
    const category = String(d.category ?? '').toLowerCase();
    const word = norm(String(d.word ?? ''));
    const clue = norm(String(d.clue ?? ''));
    let hit = false;
    for (const lang of ['es', 'en']) {
      for (const c of byLabel.get(`${lang}|${word}`) ?? []) {
        if (!c.categories.includes(category) || !clueTexts(c, lang).has(clue)) continue;
        const key = `${c.id}|${lang}|${clue}`;
        const cur = acc.get(key) ?? { id: c.id, lang, clue: d.clue, pos: 0, neg: 0 };
        cur.pos += Number(d.positive ?? 0);
        cur.neg += Number(d.negative ?? 0);
        acc.set(key, cur);
        hit = true;
      }
    }
    if (!hit) unmatched.push(d);
  }
  const rows = [...acc.values()].sort((a, b) => a.id.localeCompare(b.id) || a.lang.localeCompare(b.lang) || a.clue.localeCompare(b.clue));
  return { rows, unmatched };
}

function readExport(file) {
  const text = readFileSync(file, 'utf8').replace(/^﻿/, '').trim();
  return text.startsWith('[') ? JSON.parse(text) : text.split(/\r?\n/).filter(Boolean).map((l) => JSON.parse(l));
}

async function fetchAll(outFile) {
  const project = process.env.FIREBASE_PROJECT;
  const token = process.env.FIRESTORE_TOKEN;
  if (!project || !token) throw new Error('set FIREBASE_PROJECT and FIRESTORE_TOKEN (see the header of this file)');
  const docs = [];
  let pageToken = '';
  do {
    const url = `https://firestore.googleapis.com/v1/projects/${project}/databases/(default)/documents/clue_ratings?pageSize=300${pageToken ? `&pageToken=${pageToken}` : ''}`;
    const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
    if (!res.ok) throw new Error(`Firestore answered ${res.status}`);
    const body = await res.json();
    for (const d of body.documents ?? []) {
      const f = d.fields ?? {};
      docs.push({
        word: f.word?.stringValue,
        category: f.category?.stringValue,
        clue: f.clue?.stringValue,
        positive: Number(f.positive?.integerValue ?? 0),
        negative: Number(f.negative?.integerValue ?? 0),
      });
    }
    pageToken = body.nextPageToken ?? '';
  } while (pageToken);
  writeFileSync(outFile, docs.map((d) => JSON.stringify(d)).join('\n') + '\n');
  console.log(`${docs.length} rating documents written to ${outFile}`);
}

const isMain = process.argv[1] && import.meta.url.endsWith(process.argv[1].replace(/\\/g, '/').split('/').pop());
if (isMain) {
  const [cmd, arg] = process.argv.slice(2);
  if (cmd === 'fetch') await fetchAll(arg);
  else if (cmd === 'import') {
    const { rows, unmatched } = mapRatings(readExport(arg), loadMap());
    writeFileSync(ratingsFile, rows.map((r) => JSON.stringify(r)).join('\n') + (rows.length ? '\n' : ''));
    console.log(`${rows.length} rated clues written (${rows.filter((r) => r.pos + r.neg >= RATING.minVotes).length} with enough votes), ${unmatched.length} documents did not match a clue (renamed or removed words)`);
  } else if (cmd === 'report') {
    const rows = readJsonl(ratingsFile).filter((r) => r.pos + r.neg >= RATING.minVotes)
      .map((r) => ({ ...r, factor: ratingFactor(r.pos, r.neg) })).sort((a, b) => a.factor - b.factor);
    for (const r of [...rows.slice(0, 15), ...rows.slice(-5)]) console.log(`${r.factor.toFixed(2)}  ${r.id} (${r.lang}) "${r.clue}"  +${r.pos} -${r.neg}`);
  } else { console.error('usage: ratings.mjs fetch <file> | import <file> | report'); process.exit(2); }
}

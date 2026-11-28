/**
 * Shared helpers of the word tooling (docs/studies/word-collection/concept-map-spec.md).
 * Source of truth: data/files/concepts/{concepts,links}.jsonl. The app bundles these files and compiles them on the device (ConceptCompiler.kt, a port of compileAll below).
 */
import { readFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

export const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
export const conceptsDir = join(root, 'data/files/concepts');

export const LANGS = ['es', 'en'];

/** Code unit order: the same on every machine (localeCompare depends on the OS locale and the ICU data). */
export const cmp = (a, b) => (a < b ? -1 : a > b ? 1 : 0);
export const REVEAL = { association: 1, context: 1, contrast: 2, broader: 2, part: 2, synonym: 3 };
export const MAX_REVEAL = { low: 1, normal: 2, high: 3 };
export const LIMITS = { maxClues: 8, minClues: 3, floor: 0.2, hubK: 8 };

export const norm = (s) =>
  s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim().replace(/\s+/g, ' ').replace(/^(the|el|la|los|las|un|una)\s/, '');
export const tokens = (s) => s.trim().split(/\s+/).filter(Boolean);

export function slug(s) {
  return s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '');
}

export function readJsonl(file) {
  if (!existsSync(file)) return [];
  return readFileSync(file, 'utf8').replace(/^﻿/, '').split(/\r?\n/).filter((l) => l.trim()).map((l) => JSON.parse(l));
}

/**
 * Player ratings of clues (data/files/concepts/ratings.jsonl, written by ratings.mjs): one line per
 * {id, lang, clue, pos, neg}. Fewer than RATING.minVotes votes say nothing, so they leave the clue alone.
 */
export const RATING = { minVotes: 5, z: 1.645, floor: 0.25 };

/** Lower bound of the Wilson interval of the share of positive votes. */
export function wilsonLowerBound(pos, total, z = RATING.z) {
  if (total <= 0) return 0;
  const p = pos / total;
  const z2 = z * z;
  return (p + z2 / (2 * total) - z * Math.sqrt((p * (1 - p) + z2 / (4 * total)) / total)) / (1 + z2 / total);
}

/** 1 until there are enough votes; then 0.25 + lower bound, capped at 1 (so a clue players dislike sinks to 0.25). */
export function ratingFactor(pos, neg) {
  const total = pos + neg;
  if (total < RATING.minVotes) return 1;
  return Math.min(1, RATING.floor + wilsonLowerBound(pos, total));
}

export function loadMap(dir = conceptsDir) {
  const concepts = readJsonl(join(dir, 'concepts.jsonl'));
  const links = readJsonl(join(dir, 'links.jsonl'));
  const ratings = new Map(
    readJsonl(join(dir, 'ratings.jsonl')).map((r) => [`${r.id}|${r.lang}|${norm(r.clue)}`, ratingFactor(r.pos ?? 0, r.neg ?? 0)])
  );
  return { concepts, links, ratings, byId: new Map(concepts.map((c) => [c.id, c])) };
}

const approved = (x) => (x.status ?? 'approved') === 'approved';

/**
 * The label of a concept in a language. A concept marked `translatable: false` is a proper name (Spiderman, The Beatles):
 * a language without its own label uses the text of another one, unless it carries an explicit label of its own
 * (Los Beatles).
 */
export function labelOf(concept, lang) {
  if (!concept) return undefined;
  const own = concept.labels[lang];
  if (own) return own;
  if (concept.translatable === false) return concept.labels.es ?? concept.labels.en ?? Object.values(concept.labels)[0];
  return undefined;
}

/** In-degree of every concept over approved links (language neutral). */
export function inDegrees(links) {
  const m = new Map();
  for (const l of links.filter(approved)) m.set(l.to, (m.get(l.to) ?? 0) + 1);
  return m;
}

/**
 * Compiles one concept for one language: returns { clues, notes } or null when the concept has no label there.
 * clues = ranked clue texts (spec section 6); fewer than minClues means the concept cannot be emitted.
 */
const rated = (map, id, lang, text) => map.ratings?.get(`${id}|${lang}|${norm(text)}`) ?? 1;

export function compileConcept(concept, lang, level, map, inDeg) {
  const label = labelOf(concept, lang);
  if (!label) return null;
  const own = new Set([label.text, ...(label.aliases ?? [])].map(norm));
  const candidates = [];
  for (const l of map.outgoing.get(concept.id) ?? []) {
    if (l.langs && !l.langs.includes(lang)) continue;
    const target = labelOf(map.byId.get(l.to), lang);
    if (!target) continue;
    const factor = Math.min(1, Math.sqrt(LIMITS.hubK / Math.max(inDeg.get(l.to) ?? 1, 1)));
    candidates.push({ text: target.text, type: l.type ?? 'association', w: l.w ?? 3, factor: factor * rated(map, concept.id, lang, target.text) });
  }
  for (const h of concept.hints?.[lang] ?? []) {
    if (approved(h)) candidates.push({ text: h.t, type: h.type ?? 'association', w: h.w ?? 3, factor: rated(map, concept.id, lang, h.t) });
  }
  const seen = new Set();
  const usable = candidates
    .filter((x) => REVEAL[x.type] <= MAX_REVEAL[level])
    .filter((x) => !own.has(norm(x.text)))
    .filter((x) => !seen.has(norm(x.text)) && seen.add(norm(x.text)))
    .map((x) => ({ ...x, score: (x.w / 5) * x.factor }))
    .sort((a, b) => b.score - a.score || cmp(a.text, b.text));
  const clues = usable.slice(0, LIMITS.maxClues);
  while (clues.length > 4 && clues[clues.length - 1].score < LIMITS.floor) clues.pop();
  return {
    label: label.text,
    clues: clues.map((c) => c.text),
    reveal: clues.map((c) => REVEAL[c.type]),
    available: usable.length,
    atNormal: usable.filter((c) => REVEAL[c.type] <= MAX_REVEAL.normal).length,
  };
}

export function indexMap(map) {
  const outgoing = new Map();
  for (const l of map.links.filter(approved)) {
    if (!outgoing.has(l.from)) outgoing.set(l.from, []);
    outgoing.get(l.from).push(l);
  }
  return { ...map, outgoing };
}

/** Flat files content: { "category_lang": [ {word, clues, category, conceptId} ] }, entries sorted by word. */
export function compileAll(map, level = 'high') {
  const indexed = indexMap(map);
  const inDeg = inDegrees(map.links);
  const files = {};
  const underfilled = [];
  const weakAtNormal = [];
  for (const concept of map.concepts.filter(approved)) {
    if (concept.clueOnly) continue; // generic topic word: kept as a clue of other concepts, never a secret word
    for (const lang of LANGS) {
      const r = compileConcept(concept, lang, level, indexed, inDeg);
      if (!r) continue;
      if (r.clues.length < LIMITS.minClues) { underfilled.push(`${concept.id} (${lang}): ${r.clues.length}`); continue; }
      if (r.atNormal < LIMITS.minClues) weakAtNormal.push(`${concept.id} (${lang}): ${r.atNormal}`);
      // `reveal` (one digit per clue: 1 weak, 2 medium, 3 strong giveaway) is only written when some clue is not level 1
      const reveal = r.reveal.every((x) => x === 1) ? undefined : r.reveal.join('');
      for (const category of concept.categories) {
        (files[`${category}_${lang}`] ??= []).push({ word: r.label, clues: r.clues, reveal, category, conceptId: concept.id });
      }
    }
  }
  for (const list of Object.values(files)) list.sort((a, b) => cmp(a.word, b.word) || cmp(a.conceptId, b.conceptId));
  return { files, underfilled, weakAtNormal };
}

/** Same layout as the hand-written files: CRLF, 2/4 space indent, one entry per line, no BOM. */
export function serialize(entries) {
  const lines = entries.map((e) => `    ${JSON.stringify({ word: e.word, clues: e.clues, ...(e.reveal ? { reveal: e.reveal } : {}), category: e.category, conceptId: e.conceptId })}`);
  return `{\r\n  "words": [\r\n${lines.join(',\r\n')}\r\n  ]\r\n}`;
}

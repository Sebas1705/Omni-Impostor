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
import { LANGS, REVEAL, cmp, compileConcept, conceptsDir, indexMap, inDegrees, labelOf, loadMap, norm, readJsonl, tokens } from './lib.mjs';

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
      LANGS.filter((l) => labelOf(c, l)).map((lang) => ({
        id: c.id,
        lang,
        word: labelOf(c, lang).text,
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
        // a proper name (`"translatable": false`) may leave out the text: it is the English one
        const es = { text: r.es?.text ?? (r.translatable === false ? c.labels.en?.text : undefined), hints: r.es?.hints };
        if (!es?.text || !es.text.trim()) { problems.push(`${where}: missing text`); continue; }
        if (tokens(es.text).length > 4 || /[()「」|;]/.test(es.text)) problems.push(`${where}: label "${es.text}" is not 1-4 clean words`);
        const hints = es.hints ?? [];
        if (hints.length < 3) problems.push(`${where}: needs at least 3 hints`);
        const ns = hints.map(norm);
        if (new Set(ns).size !== ns.length) problems.push(`${where}: repeated hint`);
        if (ns.includes(norm(es.text))) problems.push(`${where}: a hint equals the label`);
        const twins = (esLabels.get(norm(es.text)) ?? []).filter((o) => o.id !== r.id);
        if (twins.length) {
          if (!uniqueTwin(map, c, es.text)) {
            problems.push(`${where}: "${es.text}" is already the Spanish label of ${twins.map((t) => t.id + (t.labels.en ? ` (${t.labels.en.text})` : '')).join(', ')} and there is no single unpaired concept of the same category to merge into: rename this label`);
          }
        } else {
          const k = norm(es.text);
          if (!proposedLabels.has(k)) proposedLabels.set(k, []);
          proposedLabels.get(k).push({ where, c });
        }
        for (const h of hints) if (typeof h !== 'string' || !h.trim() || tokens(h).length > 3 || /[()「」|;]/.test(h)) problems.push(`${where}: bad hint "${h}"`);
      } else {
        const key = `${r.id}|${r.lang}`;
        if (seen.has(key)) problems.push(`${where}: duplicate record for ${r.lang}`);
        seen.add(key);
        if (!labelOf(c, r.lang)) { problems.push(`${where}: no ${r.lang} label`); continue; }
        const known = new Set(clueCandidates(map, indexed, inDeg, c, r.lang).map(norm));
        for (const [clue, type] of Object.entries(r.clues ?? {})) {
          if (!TYPED.has(type)) problems.push(`${where}: type "${type}" for "${clue}"`);
          if (!known.has(norm(clue))) problems.push(`${where}: "${clue}" is not a clue of this concept in ${r.lang}`);
        }
      }
    }
  }
  // several English words proposed with the same Spanish label are one concept (aliases of each other): they must share a category
  for (const group of proposedLabels.values()) {
    for (const m of group.slice(1)) {
      if (!m.c.categories.some((x) => group[0].c.categories.includes(x))) {
        problems.push(`${m.where}: same Spanish label as ${group[0].c.id} but no shared category (homonym?): rename one of them`);
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
      out.push(`| ${r.id} | ${c?.labels.en?.text} | ${r.es.text ?? '(same name)'} | ${r.es.hints.join(', ')} |`);
    }
  } else {
    out.push('| id | lang | word | typed clues |', '|---|---|---|---|');
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      out.push(`| ${r.id} | ${r.lang} | ${labelOf(c, r.lang)?.text} | ${Object.entries(r.clues).map(([k, v]) => `${k} (${v})`).join(', ')} |`);
    }
  }
  console.log(out.join('\n'));
}

/** The one unpaired concept with the same Spanish label and a shared category that an English-only concept can merge into. */
function uniqueTwin(map, c, esText) {
  const found = map.concepts.filter((o) => o.id !== c.id && !o.merged && o.labels.es && !o.labels.en &&
    norm(o.labels.es.text) === norm(esText) && o.categories.some((x) => c.categories.includes(x)));
  return found.length === 1 ? found[0] : null;
}

const unionHints = (a = [], b = []) => {
  const seen = new Set(a.map((h) => norm(h.t)));
  return [...a, ...b.filter((h) => !seen.has(norm(h.t)))];
};

/**
 * Folds the English-only concept `from` into `into`: they are the same concept. When `into` already has an English label the
 * second English word becomes an alias of it (never shown, only used to reject clues that give the word away) and its
 * clues are merged.
 */
function mergeInto(map, into, from) {
  if (into.labels.en) {
    const en = into.labels.en;
    en.aliases = [...new Set([...(en.aliases ?? []), from.labels.en.text])];
  } else {
    into.labels = { ...into.labels, en: from.labels.en };
  }
  into.categories = [...new Set([...into.categories, ...from.categories])].sort();
  if (from.hints?.en) into.hints = { ...(into.hints ?? {}), en: unionHints(into.hints?.en, from.hints.en) };
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

/**
 * Two concepts with the same label in a language are homonyms and their ids must carry a sense tag (`espacio~universo`).
 * Concepts that applying labels made clash get `~<first category>`; links follow the new id. Returns how many were renamed.
 */
function ensureSenseTags(map) {
  const live = map.concepts.filter((c) => !c.merged);
  const groups = new Map();
  for (const c of live) for (const lang of LANGS) {
    const l = labelOf(c, lang);
    if (!l) continue;
    const k = lang + '|' + norm(l.text);
    if (!groups.has(k)) groups.set(k, new Set());
    groups.get(k).add(c);
  }
  const taken = new Set(live.map((c) => c.id));
  const renames = new Map();
  for (const group of groups.values()) {
    if (group.size < 2) continue;
    for (const c of group) {
      if (c.id.includes('~') || renames.has(c.id)) continue;
      const tag = [...c.categories].sort()[0].replace(/_/g, '-');
      let id = `${c.id}~${tag}`, n = 2;
      while (taken.has(id)) id = `${c.id}~${tag}-${n++}`;
      taken.add(id);
      renames.set(c.id, id);
    }
  }
  for (const c of live) if (renames.has(c.id)) c.id = renames.get(c.id);
  for (const l of map.links) { l.from = renames.get(l.from) ?? l.from; l.to = renames.get(l.to) ?? l.to; }
  return renames.size;
}

function apply(files) {
  if (!check(files)) process.exit(1);
  const map = loadMap();
  const links = map.links;
  map.linksRef = links;
  let labels = 0, typedLinks = 0, typedHints = 0, merges = 0;
  const labelTargets = new Map(); // Spanish label (normalised) -> concept that received it in this run
  for (const file of files) {
    const kind = kindOf(file);
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      if (kind === 'labels') {
        const text = r.es?.text ?? c.labels.en.text;
        const isName = r.translatable === false || norm(text) === norm(c.labels.en.text);
        const twin = uniqueTwin(map, c, text);
        const target = twin ?? labelTargets.get(norm(text));
        if (target) {
          labelTargets.set(norm(text), target);
          mergeInto(map, target, c);
          if (!twin) target.hints.es = unionHints(target.hints.es, r.es.hints.map((t) => ({ t }))).slice(0, 8);
          if (isName && target.translatable === undefined) target.translatable = false;
          merges++;
          continue;
        }
        if (isName) c.translatable = false;
        else c.labels = { es: { text }, ...c.labels };
        c.hints = { ...(c.hints ?? {}), es: r.es.hints.map((t) => ({ t })) };
        c.todo = (c.todo ?? []).filter((l) => l !== 'es');
        if (!c.todo.length) delete c.todo;
        labelTargets.set(norm(text), c);
        labels++;
      } else {
        for (const [clue, type] of Object.entries(r.clues)) {
          const n = norm(clue);
          const hint = (c.hints?.[r.lang] ?? []).find((h) => norm(h.t) === n);
          if (hint) { hint.type = type; typedHints++; continue; }
          const link = links.find((l) => l.from === c.id && (!l.langs || l.langs.includes(r.lang)) &&
            labelOf(map.byId.get(l.to), r.lang) && norm(labelOf(map.byId.get(l.to), r.lang).text) === n);
          if (link) { link.type = type; typedLinks++; }
        }
      }
    }
  }
  const retagged = ensureSenseTags(map);
  const sortedConcepts = map.concepts.filter((c) => !c.merged).sort((a, b) => cmp(a.id, b.id));
  writeFileSync(join(conceptsDir, 'concepts.jsonl'), jsonl(sortedConcepts));
  writeFileSync(join(conceptsDir, 'links.jsonl'), jsonl(links));
  files.forEach((f) => unlinkSync(f));
  console.log(`applied: ${labels} Spanish labels, ${merges} merged into an existing Spanish concept, ${retagged} ids given a sense tag, ${typedLinks} typed links, ${typedHints} typed hints. Now run: node scripts/words/compile.mjs`);
}

/** Removes type proposals whose concept or clue no longer exists (after labels were applied and concepts merged). */
function prune(files) {
  const map = loadMap();
  const indexed = indexMap(map);
  const inDeg = inDegrees(map.links);
  let kept = 0, dropped = 0;
  for (const file of files) {
    if (kindOf(file) !== 'types') continue;
    const rows = [];
    for (const r of readProposals(file)) {
      const c = map.byId.get(r.id);
      const known = c && labelOf(c, r.lang) ? new Set(clueCandidates(map, indexed, inDeg, c, r.lang).map(norm)) : new Set();
      const clues = Object.fromEntries(Object.entries(r.clues).filter(([k]) => known.has(norm(k))));
      dropped += Object.keys(r.clues).length - Object.keys(clues).length;
      kept += Object.keys(clues).length;
      if (Object.keys(clues).length) rows.push({ id: r.id, lang: r.lang, clues });
    }
    writeFileSync(file, jsonl(rows));
  }
  console.log(`type proposals pruned: ${kept} typed clues kept, ${dropped} dropped`);
}

/** Merge by hand: `merge <from> <into>` folds an English-only concept into another one (alias, clues, categories, links). */
function mergeCommand(fromId, intoId) {
  const map = loadMap();
  const from = map.byId.get(fromId), into = map.byId.get(intoId);
  if (!from || !into) throw new Error('unknown concept');
  if (!from.labels.en || from.labels.es) throw new Error('only an English-only concept can be merged');
  mergeInto(map, into, from);
  writeFileSync(join(conceptsDir, 'concepts.jsonl'), jsonl(map.concepts.filter((c) => !c.merged).sort((a, b) => a.id.localeCompare(b.id))));
  writeFileSync(join(conceptsDir, 'links.jsonl'), jsonl(map.links));
  console.log(`${fromId} merged into ${intoId}`);
}

switch (cmd) {
  case 'merge': mergeCommand(rest[0], rest[1]); break;
  case 'prune': prune(rest); break;
  case 'export': exportBatches(rest[0], rest[1], Number(rest[2] ?? 250)); break;
  case 'check': process.exit(check(rest) ? 0 : 1); break;
  case 'review': review(rest[0]); break;
  case 'apply': apply(rest); break;
  default: console.error('usage: proposals.mjs export|check|review|apply ...'); process.exit(2);
}

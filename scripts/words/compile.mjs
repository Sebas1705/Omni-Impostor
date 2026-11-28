#!/usr/bin/env node
/**
 * Reports on the concept map (data/files/concepts) as the app will compile it, and keeps the shared test fixture honest.
 *
 *   node scripts/words/compile.mjs            # report what would not play well and refresh docs/word-map/index.html
 *   node scripts/words/compile.mjs --golden   # rewrite the expected lists of the fixture (after changing lib.mjs rules)
 *   node scripts/words/compile.mjs --check    # exit 1 when the fixture's expected lists are out of date
 *
 * The app does NOT read compiled lists: it bundles concepts.jsonl / links.jsonl and compiles them on the device with
 * ConceptCompiler.kt, a port of compileAll in lib.mjs. The fixture in scripts/words/fixtures/graph (with its expected
 * lists) is what both implementations are tested against. Rules: docs/studies/word-collection/concept-map-spec.md, section 6.
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { LANGS, cmp, compileAll, loadMap, root, serialize } from './lib.mjs';

export const fixtureDir = join(root, 'scripts/words/fixtures/graph');

/** The words of one language as the Kotlin compiler returns them: one list, sorted by word then concept id. */
export function flatLists(files, lang) {
  return Object.entries(files)
    .filter(([name]) => name.endsWith(`_${lang}`))
    .flatMap(([, entries]) => entries)
    .sort((a, b) => cmp(a.word, b.word) || cmp(a.conceptId, b.conceptId));
}

export function expectedFixture() {
  const { files } = compileAll(loadMap(fixtureDir));
  return Object.fromEntries(LANGS.map((lang) => [lang, serialize(flatLists(files, lang))]));
}

if (process.argv[1] && import.meta.url.endsWith(process.argv[1].replace(/[\\]/g, '/').split('/').pop())) {
  const check = process.argv.includes('--check');
  const golden = process.argv.includes('--golden');
  if (check || golden) {
    const expected = expectedFixture();
    const stale = [];
    for (const lang of LANGS) {
      const file = join(fixtureDir, `expected_${lang}.json`);
      if (golden) { writeFileSync(file, expected[lang]); continue; }
      let current = '';
      try { current = readFileSync(file, 'utf8'); } catch { /* missing */ }
      if (current.replace(/\r\n/g, '\n') !== expected[lang].replace(/\r\n/g, '\n')) stale.push(`expected_${lang}.json`);
    }
    if (check && stale.length) {
      console.error(`fixture is out of date (${stale.join(', ')}). Run: node scripts/words/compile.mjs --golden`);
      process.exit(1);
    }
    console.log(golden ? 'fixture lists written' : 'fixture lists are up to date');
  } else {
    const { files, underfilled, weakAtNormal } = compileAll(loadMap());
    const total = LANGS.map((lang) => `${lang} ${flatLists(files, lang).length}`).join(', ');
    console.log(`the app will compile: ${total} entries`);
    if (underfilled.length) console.log(`not playable (fewer than 3 clues): ${underfilled.length}\n - ${underfilled.slice(0, 15).join('\n - ')}`);
    console.log(`fewer than 3 clues at help level normal (the app falls back to all clues): ${weakAtNormal.length}`);
    const v = spawnSync(process.execPath, [join(root, 'scripts/words/export-viewer.mjs')], { stdio: 'inherit' }); // keep docs/word-map in step
    if (v.status !== 0) process.exit(v.status ?? 1);
  }
}

#!/usr/bin/env node
/**
 * Compiles the concept map (data/files/concepts) into the flat word lists the app reads
 * (data/files/src/main/assets/{category}_{lang}.json) and refreshes words_version.txt.
 *
 *   node scripts/words/compile.mjs            # write the assets
 *   node scripts/words/compile.mjs --check    # exit 1 when the assets are not what the sources compile to
 *
 * Rules: docs/studies/word-collection/concept-map-spec.md, section 6. Edit the sources, never the generated assets.
 */
import { readdirSync, readFileSync, writeFileSync, unlinkSync } from 'node:fs';
import { join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { assetsDir, compileAll, loadMap, root, serialize } from './lib.mjs';

const check = process.argv.includes('--check');
const { files, underfilled, weakAtNormal } = compileAll(loadMap());

const expected = new Map(Object.entries(files).map(([name, entries]) => [`${name}.json`, serialize(entries)]));
const existing = readdirSync(assetsDir).filter((f) => /_(en|es)\.json$/.test(f));
const problems = [];
for (const [name, content] of expected) {
  let current = null;
  try { current = readFileSync(join(assetsDir, name), 'utf8').replace(/^﻿/, ''); } catch { /* missing */ }
  // the repository stores LF and a Windows checkout may turn it into CRLF: compare without line endings
  if (current === null || current.replace(/\r\n/g, '\n') !== content.replace(/\r\n/g, '\n')) problems.push(name);
}
for (const name of existing) if (!expected.has(name)) problems.push(`${name} (no longer produced)`);

if (underfilled.length) console.log(`not emitted (fewer than 3 clues): ${underfilled.length}\n - ${underfilled.slice(0, 15).join('\n - ')}`);

if (weakAtNormal.length) console.log(`fewer than 3 clues at help level normal (the app falls back to all clues): ${weakAtNormal.length}`);

if (check) {
  if (problems.length) {
    console.error(`assets are out of date with data/files/concepts (${problems.length} file(s), e.g. ${problems.slice(0, 3).join(', ')}). Run: node scripts/words/compile.mjs`);
    process.exit(1);
  }
  console.log('assets match the concept map');
} else {
  for (const [name, content] of expected) writeFileSync(join(assetsDir, name), content);
  for (const name of existing) if (!expected.has(name)) unlinkSync(join(assetsDir, name));
  const r = spawnSync(process.execPath, [join(root, 'scripts/word-study/update-words-version.mjs')], { stdio: 'inherit' });
  if (r.status !== 0) process.exit(r.status ?? 1);
  const v = spawnSync(process.execPath, [join(root, 'scripts/words/export-viewer.mjs')], { stdio: 'inherit' }); // keep docs/word-map in step
  if (v.status !== 0) process.exit(v.status ?? 1);
  console.log(`wrote ${expected.size} files, ${[...expected.values()].length} lists`);
}

#!/usr/bin/env node
/**
 * Writes data/files/src/main/assets/words_version.txt, the version of the bundled word lists.
 *
 *   node scripts/word-study/update-words-version.mjs           # update the file
 *   node scripts/word-study/update-words-version.mjs --check   # exit 1 when it is out of date
 *
 * The app compares this version with the one stored on the device and, when they differ, replaces the words in
 * its database. Run it every time a word list changes (WordAssetsValidationTest fails otherwise).
 *
 * Version = first 16 hex chars of SHA-256 over, for each `*_en.json` / `*_es.json` sorted by name,
 * `<name>\n<content without BOM and \r>\n`. The same algorithm lives in WordAssetsValidationTest.
 */
import { createHash } from 'node:crypto';
import { readdirSync, readFileSync, writeFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const assets = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'data/files/src/main/assets');
const target = join(assets, 'words_version.txt');

const hash = createHash('sha256');
for (const name of readdirSync(assets).filter((f) => /_(en|es)\.json$/.test(f)).sort()) {
  const content = readFileSync(join(assets, name), 'utf8').replace(/^﻿/, '').replace(/\r/g, '');
  hash.update(`${name}\n${content}\n`);
}
const version = hash.digest('hex').slice(0, 16);

if (process.argv.includes('--check')) {
  const current = existsSync(target) ? readFileSync(target, 'utf8').trim() : '';
  if (current !== version) {
    console.error(`words_version.txt is out of date (has "${current}", expected "${version}"). Run: node scripts/word-study/update-words-version.mjs`);
    process.exit(1);
  }
  console.log(`words_version.txt is up to date (${version})`);
} else {
  writeFileSync(target, version + '\n');
  console.log(`words_version.txt = ${version}`);
}

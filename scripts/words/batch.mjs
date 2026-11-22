#!/usr/bin/env node
/**
 * Applies many edit.mjs commands at once: node scripts/words/batch.mjs <file.jsonl> [--dir <concepts folder>]
 * With --dir the commands run on a copy of data/files/concepts (a dry run that leaves the real data alone).
 * Each line is a JSON array with the arguments of one command, e.g. ["label","casa","es","Hogar"].
 * A failing line is reported and skipped; the rest still apply.
 */
import { readFileSync } from 'node:fs';
import { run } from './edit.mjs';

const di = process.argv.indexOf('--dir');
const dir = di > 0 ? process.argv[di + 1] : undefined;
const lines = readFileSync(process.argv[2], 'utf8').split(/\r?\n/).filter((l) => l.trim());
let ok = 0;
const failed = [];
for (const [i, line] of lines.entries()) {
  try { run(JSON.parse(line), dir); ok++; } catch (e) { failed.push(`line ${i + 1}: ${e.message} :: ${line.slice(0, 140)}`); }
}
console.log(`applied ${ok} of ${lines.length}`);
for (const f of failed) console.log('FAILED', f);

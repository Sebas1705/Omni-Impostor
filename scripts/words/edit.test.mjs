// Run with: node --test scripts/words/edit.test.mjs
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { run } from './edit.mjs';

const setup = () => {
  const dir = mkdtempSync(join(tmpdir(), 'words-'));
  const concepts = [
    { id: 'casa', labels: { es: { text: 'Casa' }, en: { text: 'House' } }, categories: ['home_and_furniture'], hints: { es: [{ t: 'Puerta' }, { t: 'Tejado' }, { t: 'Ventana' }] } },
    { id: 'hogar', labels: { es: { text: 'Hogar' }, en: { text: 'Home' } }, categories: ['home_and_furniture'], hints: { es: [{ t: 'Familia' }] } },
  ];
  writeFileSync(join(dir, 'concepts.jsonl'), concepts.map((c) => JSON.stringify(c)).join('\n') + '\n');
  writeFileSync(join(dir, 'links.jsonl'), JSON.stringify({ from: 'casa', to: 'hogar', type: 'association', w: 3 }) + '\n');
  return dir;
};
const read = (dir, f) => readFileSync(join(dir, f), 'utf8').split('\n').filter(Boolean).map(JSON.parse);

test('a link can be typed, removed and added again', () => {
  const dir = setup();
  run(['type', 'casa', 'es', 'Hogar', 'synonym'], dir);
  assert.equal(read(dir, 'links.jsonl')[0].type, 'synonym');
  run(['unlink', 'casa', 'hogar'], dir);
  assert.equal(read(dir, 'links.jsonl').length, 0);
  run(['link', 'hogar', 'casa', 'synonym'], dir);
  assert.equal(read(dir, 'links.jsonl')[0].from, 'hogar');
});

test('hints are added, typed and removed', () => {
  const dir = setup();
  run(['add-hint', 'casa', 'es', 'Jardín'], dir);
  run(['type', 'casa', 'es', 'jardin', 'part'], dir);
  assert.deepEqual(read(dir, 'concepts.jsonl')[0].hints.es.at(-1), { t: 'Jardín', type: 'part' });
  run(['remove-hint', 'casa', 'es', 'Jardín'], dir);
  assert.equal(read(dir, 'concepts.jsonl')[0].hints.es.length, 3);
});

test('labels, categories and proper name flag change; delete drops the links', () => {
  const dir = setup();
  run(['label', 'casa', 'en', 'Houses'], dir);
  run(['categories', 'casa', 'home_and_furniture,architecture_test'], dir);
  run(['translatable', 'hogar', 'false'], dir);
  const [casa, hogar] = read(dir, 'concepts.jsonl');
  assert.equal(casa.labels.en.text, 'Houses');
  assert.deepEqual(casa.categories, ['architecture_test', 'home_and_furniture']);
  assert.equal(hogar.translatable, false);
  assert.match(run(['delete', 'hogar'], dir), /deleted hogar and 1 link/);
  assert.equal(read(dir, 'links.jsonl').length, 0);
});

test('bad input is rejected', () => {
  const dir = setup();
  assert.throws(() => run(['label', 'casa', 'es', 'Una casa muy grande'], dir), /1 to 3 words/);
  assert.throws(() => run(['add-hint', 'casa', 'es', 'puerta'], dir), /already exists/);
  assert.throws(() => run(['type', 'casa', 'es', 'Puerta', 'nonsense'], dir), /unknown type/);
  assert.throws(() => run(['show', 'nope'], dir), /unknown concept/);
});

// Run with: node --test scripts/words
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { RATING, compileConcept, indexMap, inDegrees, ratingFactor, wilsonLowerBound } from './lib.mjs';
import { mapRatings } from './ratings.mjs';

const concepts = [
  { id: 'leon', labels: { es: { text: 'León' }, en: { text: 'Lion' } }, categories: ['animals'],
    hints: { es: [{ t: 'Melena' }, { t: 'Rugido' }, { t: 'Selva' }, { t: 'Garras' }], en: [{ t: 'Mane' }, { t: 'Roar' }, { t: 'Jungle' }] } },
  { id: 'felino', labels: { es: { text: 'Felino' }, en: { text: 'Feline' } }, categories: ['animals'], hints: {} },
];
const links = [{ from: 'leon', to: 'felino', type: 'broader', w: 3 }];
const map = (ratings) => ({ concepts, links, byId: new Map(concepts.map((c) => [c.id, c])), ratings });
const compile = (m, lang = 'es', level = 'high') => compileConcept(concepts[0], lang, level, indexMap(m), inDegrees(m.links));

test('rating factor is neutral with few votes and drops for disliked clues', () => {
  assert.equal(ratingFactor(1, 3), 1);
  assert.equal(ratingFactor(0, RATING.minVotes - 1), 1);
  assert.ok(ratingFactor(40, 2) > 0.9);
  assert.ok(ratingFactor(2, 30) < 0.4);
  assert.ok(ratingFactor(0, 20) <= 0.3);
});

test('wilson lower bound grows with evidence', () => {
  assert.ok(wilsonLowerBound(9, 10) < wilsonLowerBound(90, 100));
  assert.equal(wilsonLowerBound(0, 0), 0);
});

test('a clue players dislike is dropped while enough clues remain', () => {
  const plain = compile(map(new Map())).clues;
  assert.ok(plain.includes('Garras'));
  const rated = compile(map(new Map([['leon|es|garras', ratingFactor(1, 40)]]))).clues;
  assert.ok(!rated.includes('Garras'));
  assert.equal(rated.length, plain.length - 1);
});

test('a mildly disliked clue only moves down', () => {
  const rated = compile(map(new Map([['leon|es|melena', ratingFactor(12, 8)]]))).clues;
  assert.ok(rated.includes('Melena'));
  assert.ok(rated.indexOf('Melena') > rated.indexOf('Rugido'));
});

test('ratings map onto concepts by word, category and clue text', () => {
  const { rows, unmatched } = mapRatings(
    [
      { word: 'León', category: 'ANIMALS', clue: 'Melena', positive: 4, negative: 1 },
      { word: 'León', category: 'ANIMALS', clue: 'melena', positive: 2, negative: 0 },
      { word: 'Lion', category: 'ANIMALS', clue: 'Feline', positive: 3, negative: 0 },
      { word: 'León', category: 'SPORTS', clue: 'Melena', positive: 9, negative: 9 },
      { word: 'Gone', category: 'ANIMALS', clue: 'x', positive: 1, negative: 0 },
    ],
    { concepts, links, byId: new Map(concepts.map((c) => [c.id, c])) }
  );
  assert.deepEqual(rows.map((r) => [r.id, r.lang, r.pos, r.neg]), [['leon', 'en', 3, 0], ['leon', 'es', 6, 1]]);
  assert.equal(unmatched.length, 2);
});

test('the fixture lists the Kotlin compiler is tested against are up to date', async () => {
  const { expectedFixture, fixtureDir } = await import('./compile.mjs');
  const { readFileSync } = await import('node:fs');
  const { join } = await import('node:path');
  const expected = expectedFixture();
  for (const lang of ['es', 'en']) {
    const current = readFileSync(join(fixtureDir, `expected_${lang}.json`), 'utf8').replace(/\r\n/g, '\n');
    assert.equal(current, expected[lang].replace(/\r\n/g, '\n'), `expected_${lang}.json is stale: run node scripts/words/compile.mjs --golden`);
  }
});

#!/usr/bin/env node
/**
 * One-off data cleanup for phase 0 of the word collection study (see docs/studies/word-collection).
 *
 *   node scripts/word-study/phase0-fixes.mjs          # rewrites the word assets in place
 *   node scripts/word-study/phase0-fixes.mjs --dry    # only prints what would change
 *
 * Mechanical fixes (objective):
 *   - drop a clue that equals its word (it would reveal the word to the impostor)
 *   - drop repeated clues; split clues that glue several clues with 「」 separators
 *   - merge duplicate words of the same list (accent variants such as Pele / Pelé)
 *   - move parentheses out of words (the alias becomes a clue)
 * Proposed content (to be reviewed by the owner): the new clues in ADD below, written by the assistant.
 *
 * Only the lines of changed entries are rewritten, so the file format (CRLF, one entry per line) is kept.
 */
import { readdirSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const assets = join(root, 'data/files/src/main/assets');
const dry = process.argv.includes('--dry');
const reviewIdx = process.argv.indexOf('--review');
const reviewPath = reviewIdx > 0 ? process.argv[reviewIdx + 1] : null;
const changes = [];

const norm = (s) => s.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase().trim().replace(/\s+/g, ' ');

// ---- words whose parentheses become part of the clues
const PAREN = {
  'Electric Vehicle (EV)': { word: 'Electric Vehicle', add: ['EV'] },
  'Vehículo Eléctrico (EV)': { word: 'Vehículo Eléctrico', add: ['EV'] },
  'Mindfulness (Atención Plena)': { word: 'Mindfulness', add: ['Atención Plena'] },
  'Jessica Drew (Spider-Woman)': { word: 'Jessica Drew', add: [] },
  'ATV (All-Terrain Vehicle)': { word: 'ATV', add: ['All-Terrain Vehicle'] },
  'Autobus (Double Decker)': { word: 'Double Decker Bus', add: ['London'] },
};

// ---- proposed extra clues: "<file without .json>|<word>" -> clues (a leading "-Historia" style entry drops that clue)
const fashionEn = {
  Kimono: ['Japan', 'Obi Belt', 'Silk'],
  Sari: ['India', 'Six Yards', 'Blouse'],
  Kilt: ['Scotland', 'Bagpipes', 'Highlands'],
  'Bowler Hat': ['Derby', 'London', 'Gentleman'],
  'Top Hat': ['Magician', 'Abraham Lincoln', 'Tuxedo'],
  'Cloche Hat': ['Flapper', 'Art Deco', 'Close-fitting'],
  'Panama Hat': ['Summer', 'Ecuador', 'Tropical'],
  'Newsboy Cap': ['Peaky Blinders', 'Tweed', 'Paperboy'],
  'Trilby Hat': ['Fedora', 'Felt', 'Jazz'],
  'Gatsby Cap': ['Tweed', 'Golfer', 'Great Gatsby'],
  'Homburg Hat': ['Churchill', 'Felt', 'Rolled Brim'],
  'Pillbox Hat': ['Jackie Kennedy', 'Cylindrical', 'Veil'],
  Sombrero: ['Mariachi', 'Sun', 'Fiesta'],
  Ushanka: ['Winter', 'Fur', 'Soviet'],
  Balaclava: ['Skiing', 'Winter', 'Motorcycle'],
};
const kitchenEn = {
  'Pizza Stone': ['Baking', 'Crispy Crust', 'Ceramic'],
  'Cherry Pitter': ['Remove Stones'],
  'Apple Corer': ['Remove Seeds'],
  'Corn Stripper': ['Cob'],
  'Avocado Slicer': ['Guacamole'],
  'Pineapple Corer': ['Rings'],
  'Melon Baller': ['Fruit Salad'],
  'Butter Curler': ['Garnish'],
  'Ice Cream Scoop': ['Dessert'],
  'Pizza Cutter': ['Wheel'],
  'Cake Tester': ['Skewer'],
  'Meat Thermometer': ['Temperature'],
  'Candy Thermometer': ['Sugar'],
  'Deep Fryer': ['Oil'],
  'Electric Skillet': ['Frying Pan'],
  'Food Dehydrator': ['Dried Fruit'],
  'Yogurt Maker': ['Dairy'],
};
const onlineRoles = {
  Mercenario: ['Dinero', 'Guerra', 'Contrato', 'Armas'],
  Secuestrador: ['Rescate', 'Rehén', 'Delito', 'Víctima'],
  Extorsionista: ['Chantaje', 'Amenazas', 'Dinero', 'Delito'],
  Corrupto: ['Soborno', 'Político', 'Dinero', 'Poder'],
  'Lavador de dinero': ['Blanqueo', 'Ilegal', 'Banco', 'Delito'],
  Narcotraficante: ['Droga', 'Cártel', 'Contrabando', 'Policía'],
  'Terrorista internacional': ['Atentado', 'Violencia', 'Amenaza', 'Seguridad'],
  Cibercriminal: ['Hackeo', 'Internet', 'Fraude', 'Virus'],
  Hacker: ['Ordenador', 'Programación', 'Seguridad', 'Código'],
  'Espía cibernético': ['Datos', 'Internet', 'Secreto', 'Gobierno'],
  'Troll de internet': ['Provocar', 'Foros', 'Redes sociales', 'Burlas'],
  Influencer: ['Seguidores', 'Marcas', 'Fotos', 'Publicidad'],
  Youtuber: ['Vídeos', 'Canal', 'Suscriptores', 'Cámara'],
  Streamer: ['Directo', 'Twitch', 'Videojuegos', 'Seguidores'],
  Blogger: ['Blog', 'Artículos', 'Escribir', 'Internet'],
  Podcaster: ['Audio', 'Micrófono', 'Episodios', 'Entrevistas'],
  Vlogger: ['Vídeo diario', 'Cámara', 'Viajes', 'Canal'],
  TikToker: ['Vídeos cortos', 'Bailes', 'Tendencias', 'Móvil'],
  Instagrammer: ['Fotos', 'Stories', 'Filtros', 'Seguidores'],
  Twittero: ['Tuits', 'Hashtag', 'Opiniones', 'Tendencias'],
  Facebooker: ['Amigos', 'Muro', 'Publicaciones', 'Red social'],
  'LinkedIn influencer': ['Empleo', 'Profesional', 'Networking', 'Contactos'],
  Snapchatter: ['Mensajes efímeros', 'Filtros', 'Stories', 'Amigos'],
  Redditor: ['Foros', 'Subreddit', 'Votos', 'Comunidad'],
  'Tumblr user': ['Blog', 'Imágenes', 'Reblog', 'Estética'],
  'Pinterest user': ['Tableros', 'Inspiración', 'Imágenes', 'Ideas'],
  'GitHub contributor': ['Código', 'Repositorio', 'Programación', 'Open source'],
  'Stack Overflow user': ['Preguntas', 'Respuestas', 'Programación', 'Código'],
};
const ADD = {};
for (const [w, c] of Object.entries(fashionEn)) ADD[`fashion_and_clothing_en|${w}`] = c;
for (const [w, c] of Object.entries(kitchenEn)) ADD[`kitchen_and_cookware_en|${w}`] = c;
for (const [w, c] of Object.entries(onlineRoles)) ADD[`professions_es|${w}`] = { add: c, drop: ['Historia'] };
Object.assign(ADD, {
  'fashion_and_clothing_es|Kilt': ['Escocia', 'Gaita', 'Cuadros'],
  'fashion_and_clothing_es|Top Hat': ['Chistera', 'Mago', 'Esmoquin'],
  'health_and_medicine_es|Alzheimer': ['Demencia', 'Olvido', 'Ancianos'],
  'health_and_medicine_es|Parkinson': ['Temblor', 'Dopamina', 'Rigidez'],
  'health_and_medicine_es|Esclerosis Múltiple': ['Mielina', 'Brotes', 'Fatiga'],
  'history_en|Senator': ['Senate', 'Toga', 'Law', 'Republic'],
  'mythology_and_fantasy_en|Ra': ['Pharaoh', 'Eye of Ra', 'Falcon'],
  'mythology_and_fantasy_en|Pentagram': ['Witchcraft', 'Wicca', 'Circle'],
  'nature_and_weather_en|Asteroid Belt': ['Jupiter', 'Mars', 'Ceres'],
  'nature_and_weather_en|Parallel Universe': ['Fiction', 'Physics', 'Another Dimension'],
  'school_and_education_es|Impresora 3D': ['Filamento', 'Plástico', 'Capas'],
  'school_and_education_es|Exposición Oral': ['Diapositivas', 'Nervios', 'Clase'],
  'science_chemistry_en|Amino Acid': ['Biochemistry', 'Peptide', 'Nutrition'],
  'science_physics_en|Time Travel Paradox': ['Causality', 'Loop', 'Past'],
  'science_physics_en|Inflation Theory': ['Universe', 'Cosmology', 'Alan Guth'],
  'superheroes_and_comics_en|Energy Blast': ['Beam', 'Superpower', 'Explosion'],
  'superheroes_and_comics_en|Silver Surfer': ['Surfboard', 'Galactus', 'Herald'],
  'superheroes_and_comics_en|Civil War': ['Iron Man', 'Captain America', 'Registration Act'],
  'superheroes_and_comics_en|Secret Wars': ['Beyonder', 'Battleworld', 'Crossover'],
  'superheroes_and_comics_en|House of M': ['Scarlet Witch', 'Mutants', 'No More Mutants'],
  'superheroes_and_comics_en|Age of Apocalypse': ['X-Men', 'Mutants', 'Magneto'],
  'travel_and_tourism_en|Volunteer Tourism': ['Charity'],
});

// ---- helpers
const SEPARATOR = /」「/; // 」「
const cleanClue = (c) => c.replace(/^「/, '').replace(/」$/, '').trim();

function fixEntry(file, entry) {
  let { word, clues } = entry;
  const extra = [];
  const paren = PAREN[word];
  if (paren) {
    word = paren.word;
    extra.push(...paren.add);
  }
  const rule = ADD[`${file}|${word}`] ?? ADD[`${file}|${entry.word}`];
  const drop = new Set(Array.isArray(rule) ? [] : (rule?.drop ?? []).map(norm));
  extra.push(...(Array.isArray(rule) ? rule : (rule?.add ?? [])));
  const result = [];
  const seen = new Set([norm(word)]); // a clue equal to the word is dropped
  for (const raw of [...clues.flatMap((c) => c.split(SEPARATOR)), ...extra]) {
    const clue = cleanClue(raw);
    const key = norm(clue);
    if (!clue || seen.has(key) || drop.has(key)) continue;
    seen.add(key);
    result.push(clue);
  }
  return { word, clues: result, category: entry.category };
}

const serialize = (e) => JSON.stringify({ word: e.word, clues: e.clues, category: e.category });

// ---- run
let changedFiles = 0, changedEntries = 0, removedEntries = 0;
const problems = [];
for (const name of readdirSync(assets).filter((f) => /_(en|es)\.json$/.test(f)).sort()) {
  const file = name.replace(/\.json$/, '');
  const path = join(assets, name);
  const raw = readFileSync(path, 'utf8');
  const bom = raw.startsWith('﻿') ? '﻿' : '';
  const text = raw.slice(bom.length);
  const eol = text.includes('\r\n') ? '\r\n' : '\n';
  const lines = text.split(eol);
  const first = lines.findIndex((l) => l.trimStart().startsWith('{"word"'));
  let last = first;
  while (last + 1 < lines.length && lines[last + 1].trimStart().startsWith('{"word"')) last++;
  const indent = lines[first].match(/^\s*/)[0];
  const originals = lines.slice(first, last + 1).map((l) => ({ line: l, entry: JSON.parse(l.trim().replace(/,$/, '')) }));

  // merge duplicates inside the list (accent variants): keep the variant with more diacritics, union the clues
  const byKey = new Map();
  const order = [];
  for (const o of originals) {
    const key = norm(PAREN[o.entry.word]?.word ?? o.entry.word);
    if (!byKey.has(key)) { byKey.set(key, { ...o, merged: false }); order.push(key); continue; }
    const kept = byKey.get(key);
    const accents = (s) => (s.normalize('NFD').match(/\p{M}/gu) ?? []).length;
    if (accents(o.entry.word) > accents(kept.entry.word)) kept.entry = { ...kept.entry, word: o.entry.word };
    kept.entry = { ...kept.entry, clues: [...kept.entry.clues, ...o.entry.clues] };
    kept.merged = true;
    removedEntries++;
  }

  const out = [];
  for (const key of order) {
    const o = byKey.get(key);
    const fixed = fixEntry(file, o.entry);
    if (fixed.clues.length < 3) problems.push(`${file}: "${fixed.word}" has only ${fixed.clues.length} clues`);
    const unchanged = !o.merged && serialize(fixed) === serialize(o.entry);
    if (!unchanged) {
      changedEntries++;
      changes.push({ file, before: o.entry, after: fixed, merged: o.merged });
    }
    out.push(unchanged ? o.line.replace(/,$/, '') : indent + serialize(fixed));
  }
  const newLines = [...lines.slice(0, first), ...out.map((l, i) => (i < out.length - 1 ? l + ',' : l)), ...lines.slice(last + 1)];
  const newText = bom + newLines.join(eol);
  if (newText !== raw) {
    changedFiles++;
    if (!dry) writeFileSync(path, newText);
  }
}
if (reviewPath) {
  const md = ['# Phase 0 data changes to review', '',
    'Generated by `scripts/word-study/phase0-fixes.mjs --review`. Mechanical fixes are objective; the clues marked **+** are proposed by the assistant and need a human review (accept, edit or delete).', ''];
  let current = '';
  for (const c of changes.sort((a, b) => a.file.localeCompare(b.file) || a.after.word.localeCompare(b.after.word))) {
    if (c.file !== current) { current = c.file; md.push('', `## ${current}`, ''); }
    const had = new Set(c.before.clues.flatMap((x) => x.split(SEPARATOR)).map((x) => norm(cleanClue(x))));
    const has = new Set(c.after.clues.map(norm));
    const added = c.after.clues.filter((x) => !had.has(norm(x)));
    const removed = c.before.clues.filter((x) => !has.has(norm(x)) && !x.includes('」'));
    const notes = [];
    if (c.before.word !== c.after.word) notes.push(`renamed from "${c.before.word}"`);
    if (c.merged) notes.push('merged duplicate');
    if (c.before.clues.some((x) => x.includes('」'))) notes.push('glued clues split');
    md.push(`- **${c.after.word}**${notes.length ? ' (' + notes.join(', ') + ')' : ''}: ${added.length ? '**+** ' + added.join(', ') : ''}${removed.length ? (added.length ? ' · ' : '') + '**−** ' + removed.join(', ') : ''}${!added.length && !removed.length ? 'clues tidied' : ''} → [${c.after.clues.join(' | ')}]`);
  }
  writeFileSync(reviewPath, md.join('\n') + '\n');
  console.log(`review written to ${reviewPath} (${changes.length} entries)`);
}
console.log(`${dry ? '[dry run] ' : ''}files changed: ${changedFiles}, entries changed: ${changedEntries}, entries merged away: ${removedEntries}`);
if (problems.length) { console.log('PROBLEMS:'); problems.forEach((p) => console.log(' - ' + p)); process.exitCode = 1; }

#!/usr/bin/env node
/**
 * Small corrections of the concept map without editing JSON by hand. Each command changes
 * data/files/concepts/{concepts,links}.jsonl; afterwards run `node scripts/words/compile.mjs`.
 *
 *   node scripts/words/edit.mjs show <id>
 *   node scripts/words/edit.mjs label <id> <lang> <text>               set or change the label of a language
 *   node scripts/words/edit.mjs categories <id> <cat1,cat2,...>        replace the categories
 *   node scripts/words/edit.mjs translatable <id> <true|false>         proper name flag
 *   node scripts/words/edit.mjs add-hint <id> <lang> <text> [type]      add a text clue
 *   node scripts/words/edit.mjs remove-hint <id> <lang> <text>          remove a text clue
 *   node scripts/words/edit.mjs type <id> <lang> <clue> <type>          type a clue (hint or link): association|synonym|broader|contrast|part|context
 *   node scripts/words/edit.mjs link <from> <to> [type]                 add a link (the label of <to> becomes a clue of <from>)
 *   node scripts/words/edit.mjs unlink <from> <to>                      remove a link
 *   node scripts/words/edit.mjs delete <id>                             delete a concept and every link that touches it
 *
 * <id> is the id shown in docs/word-map/index.html. Quote texts that contain spaces.
 */
import { writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { REVEAL, cmp, conceptsDir, labelOf, loadMap, norm, tokens } from './lib.mjs';

export function run(argv, dir = conceptsDir) {
  const [cmd, id, ...a] = argv;
  const map = loadMap(dir);
  const c = map.byId.get(id);
  const need = (cond, msg) => { if (!cond) throw new Error(msg); };
  const cleanText = (t) => { need(t && t.trim() && tokens(t).length <= 3 && !/[()「」|;]/.test(t), `"${t}" must be 1 to 3 words without parentheses`); return t.trim(); };
  const isType = (t) => t in REVEAL;
  if (cmd !== 'link' && cmd !== 'unlink') need(c, `unknown concept "${id}"`);
  let message = 'ok';
  switch (cmd) {
    case 'show':
      return JSON.stringify({ concept: c, linksOut: map.links.filter((l) => l.from === id), linksIn: map.links.filter((l) => l.to === id).map((l) => l.from) }, null, 1);
    case 'label': {
      const [lang, text] = a;
      c.labels[lang] = { ...(c.labels[lang] ?? {}), text: cleanText(text) };
      if (c.todo) { c.todo = c.todo.filter((l) => l !== lang); if (!c.todo.length) delete c.todo; }
      break;
    }
    case 'categories': c.categories = [...new Set(a[0].split(',').map((x) => x.trim()).filter(Boolean))].sort(cmp); need(c.categories.length, 'at least one category'); break;
    case 'translatable': c.translatable = a[0] === 'false' ? false : undefined; if (c.translatable === undefined) delete c.translatable; break;
    case 'add-hint': {
      const [lang, text, type = 'association'] = a;
      need(isType(type), `unknown type ${type}`);
      c.hints ??= {};
      c.hints[lang] ??= [];
      need(!c.hints[lang].some((h) => norm(h.t) === norm(text)), 'that clue already exists');
      c.hints[lang].push(type === 'association' ? { t: cleanText(text) } : { t: cleanText(text), type });
      break;
    }
    case 'remove-hint': {
      const [lang, text] = a;
      const before = c.hints?.[lang]?.length ?? 0;
      need(before, `no ${lang} hints`);
      c.hints[lang] = c.hints[lang].filter((h) => norm(h.t) !== norm(text));
      need(c.hints[lang].length < before, 'no such hint (a clue that is a link is removed with unlink)');
      break;
    }
    case 'type': {
      const [lang, clue, type] = a;
      need(isType(type), `unknown type ${type}`);
      const hint = (c.hints?.[lang] ?? []).find((h) => norm(h.t) === norm(clue));
      if (hint) { if (type === 'association') delete hint.type; else hint.type = type; break; }
      const link = map.links.find((l) => l.from === id && (!l.langs || l.langs.includes(lang)) && norm(labelOf(map.byId.get(l.to), lang)?.text ?? '') === norm(clue));
      need(link, `"${clue}" is not a ${lang} clue of ${id}`);
      link.type = type;
      break;
    }
    case 'link': {
      const [to, type = 'association'] = a;
      need(map.byId.get(id) && map.byId.get(to) && id !== to, 'both concepts must exist and differ');
      need(isType(type), `unknown type ${type}`);
      need(!map.links.some((l) => l.from === id && l.to === to), 'that link already exists');
      map.links.push({ from: id, to, type, w: 3 });
      break;
    }
    case 'unlink': {
      const n = map.links.length;
      map.links = map.links.filter((l) => !(l.from === id && l.to === a[0]));
      need(map.links.length < n, 'no such link');
      break;
    }
    case 'delete': {
      map.concepts = map.concepts.filter((x) => x.id !== id);
      const n = map.links.length;
      map.links = map.links.filter((l) => l.from !== id && l.to !== id);
      message = `deleted ${id} and ${n - map.links.length} link(s)`;
      break;
    }
    default: throw new Error('usage: edit.mjs show|label|categories|translatable|add-hint|remove-hint|type|link|unlink|delete <id> ...');
  }
  const line = (o) => JSON.stringify(o);
  writeFileSync(join(dir, 'concepts.jsonl'), [...map.concepts].sort((x, y) => cmp(x.id, y.id)).map(line).join('\n') + '\n');
  writeFileSync(join(dir, 'links.jsonl'), [...map.links].sort((x, y) => cmp(x.from, y.from) || cmp(x.to, y.to)).map(line).join('\n') + '\n');
  return `${message}. Now run: node scripts/words/compile.mjs`;
}

if (process.argv[1] && import.meta.url.endsWith(process.argv[1].replace(/\\/g, '/').split('/').pop())) {
  try { console.log(run(process.argv.slice(2))); } catch (e) { console.error(e.message); process.exit(1); }
}

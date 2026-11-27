#!/usr/bin/env node
/**
 * Writes a read-only viewer of the whole concept map: docs/word-map/index.html (one self-contained page).
 *
 *   node scripts/words/export-viewer.mjs
 *
 * Every concept with its labels, categories, clues (hints and links, with their type) and the concepts that use it as a
 * clue. Search and filter in the page; to correct something see docs/word-map/README.md (edit.mjs or the JSON Lines
 * files). The page is generated: regenerate it after changing the data. GitHub Pages serves `main:/docs`, so the merged
 * version is also at https://sebas1705.github.io/Omni-Impostor/word-map/
 */
import { mkdirSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { cmp, labelOf, loadMap, root } from './lib.mjs';

const map = loadMap();
const incoming = new Map();
for (const l of map.links) incoming.set(l.to, (incoming.get(l.to) ?? 0) + 1);

const rows = map.concepts.map((c) => {
  const out = map.links.filter((l) => l.from === c.id).map((l) => [l.to, l.type ?? 'association', l.langs ?? []]);
  return {
    id: c.id,
    es: c.labels.es?.text ?? null,
    en: c.labels.en?.text ?? null,
    al: Object.fromEntries(Object.entries(c.labels).filter(([, l]) => l.aliases).map(([k, l]) => [k, l.aliases])),
    cats: c.categories,
    tr: c.translatable === false ? 0 : 1,
    co: c.clueOnly ? 1 : 0,
    h: Object.fromEntries(Object.entries(c.hints ?? {}).map(([k, a]) => [k, a.map((h) => [h.t, h.type ?? 'association'])])),
    out: out.map(([to, type, langs]) => [to, type, langs, labelOf(map.byId.get(to), 'es')?.text ?? labelOf(map.byId.get(to), 'en')?.text ?? to]),
    inn: incoming.get(c.id) ?? 0,
  };
}).sort((a, b) => cmp(a.id, b.id));
const categories = [...new Set(rows.flatMap((r) => r.cats))].sort(cmp);

const html = `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Omni-Impostor word map</title>
<style>
:root { --bg:#f7f9fc; --card:#fff; --ink:#1c2430; --mute:#5b6675; --line:#d8dee8; --acc:#1565c0; --syn:#b3261e; --bro:#8a5a00; --con:#6b2fa0; }
@media (prefers-color-scheme: dark) { :root { --bg:#10151c; --card:#18202a; --ink:#e6ebf2; --mute:#9aa6b6; --line:#2a3544; --acc:#6aa9ff; --syn:#ff8a80; --bro:#ffcc66; --con:#d2a8ff; } }
* { box-sizing:border-box; }
body { margin:0; background:var(--bg); color:var(--ink); font:15px/1.45 system-ui,-apple-system,"Segoe UI",Roboto,sans-serif; }
header { position:sticky; top:0; z-index:2; background:var(--bg); border-bottom:1px solid var(--line); padding:12px 16px; }
h1 { font-size:18px; margin:0 0 8px; }
.bar { display:flex; flex-wrap:wrap; gap:8px; align-items:center; }
input,select { background:var(--card); color:var(--ink); border:1px solid var(--line); border-radius:8px; padding:8px 10px; font:inherit; }
input[type=search] { flex:1 1 220px; min-width:0; }
main { max-width:980px; margin:0 auto; padding:12px 16px 40px; }
.count { color:var(--mute); margin:8px 0; }
.card { background:var(--card); border:1px solid var(--line); border-radius:12px; padding:12px 14px; margin:10px 0; }
.title { display:flex; flex-wrap:wrap; gap:6px 12px; align-items:baseline; }
.title b { font-size:17px; }
.id { font:12px ui-monospace,Consolas,monospace; color:var(--mute); }
.chips span { display:inline-block; font-size:12px; border:1px solid var(--line); border-radius:999px; padding:1px 8px; margin:2px 4px 2px 0; color:var(--mute); }
.lang { margin-top:6px; }
.lang > i { font-style:normal; font-weight:600; color:var(--mute); margin-right:6px; }
.clue { display:inline-block; margin:2px 6px 2px 0; padding:1px 8px; border-radius:8px; border:1px solid var(--line); }
.clue.synonym { color:var(--syn); border-color:var(--syn); }
.clue.broader { color:var(--bro); border-color:var(--bro); }
.clue.contrast { color:var(--con); border-color:var(--con); }
.clue small { color:var(--mute); }
.pager { display:flex; gap:8px; align-items:center; margin:12px 0; }
button { background:var(--acc); color:#fff; border:0; border-radius:8px; padding:8px 14px; font:inherit; cursor:pointer; }
button:disabled { opacity:.4; cursor:default; }
details { margin-top:10px; color:var(--mute); }
code { font:13px ui-monospace,Consolas,monospace; }
</style>
</head>
<body>
<header>
  <h1>Omni-Impostor word map <span class="count" id="total"></span></h1>
  <div class="bar">
    <input type="search" id="q" placeholder="Search id, word or clue" aria-label="Search">
    <select id="cat" aria-label="Category"><option value="">All categories</option>${categories.map((c) => `<option>${c}</option>`).join('')}</select>
    <select id="lang" aria-label="Language"><option value="">Any language</option><option value="both">Spanish and English</option><option value="esonly">Spanish only</option><option value="name">Proper name (not translatable)</option></select>
    <select id="type" aria-label="Clue type"><option value="">Any clue type</option><option>synonym</option><option>broader</option><option>contrast</option></select>
    <select id="flag" aria-label="Filter"><option value="">No other filter</option><option value="multi">In several categories</option><option value="fewclues">Fewer than 4 clues in a language</option><option value="alias">Has aliases</option><option value="hub">Used as a clue by 10+ words</option></select>
  </div>
</header>
<main>
  <div class="count" id="count"></div>
  <div id="list"></div>
  <div class="pager"><button id="prev">Previous</button><span id="page"></span><button id="next">Next</button></div>
  <details><summary>How to correct something</summary>
    <p>This page is read-only and generated. Data lives in <code>data/files/concepts/concepts.jsonl</code> (concepts, labels, clues) and <code>links.jsonl</code> (links between concepts). Edit them (or use <code>node scripts/words/edit.mjs</code>, see <code>docs/word-map/README.md</code>), then run <code>node scripts/words/compile.mjs</code> and open a pull request. You can also just tell the assistant the <code>id</code> and what to change.</p>
    <p>Clue colours: <span class="clue synonym">synonym</span> <span class="clue broader">broader</span> <span class="clue contrast">contrast</span> (plain clues have no colour). Arrow lines are links to other concepts; <small>es/en</small> marks a link valid in one language only.</p>
  </details>
</main>
<script>
const DATA = ${JSON.stringify(rows).replace(/</g, '\\u003c')};
const $ = (id) => document.getElementById(id);
const norm = (s) => (s || '').normalize('NFD').replace(/\\p{M}/gu, '').toLowerCase();
const PAGE = 40;
let page = 0, view = DATA;
const esc = (s) => String(s).replace(/[&<>"]/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]));
const text = (r) => norm([r.id, r.es, r.en, ...Object.values(r.h).flat().map((h) => h[0]), ...r.out.map((o) => o[3])].join(' '));
DATA.forEach((r) => { r.t = text(r); });
function filter() {
  const q = norm($('q').value.trim()), cat = $('cat').value, lang = $('lang').value, type = $('type').value, flag = $('flag').value;
  view = DATA.filter((r) => {
    if (q && !r.t.includes(q)) return false;
    if (cat && !r.cats.includes(cat)) return false;
    if (lang === 'both' && !(r.es && r.en)) return false;
    if (lang === 'esonly' && !(r.es && !r.en)) return false;
    if (lang === 'name' && r.tr) return false;
    if (type && !(Object.values(r.h).flat().some((h) => h[1] === type) || r.out.some((o) => o[1] === type))) return false;
    if (flag === 'multi' && r.cats.length < 2) return false;
    if (flag === 'alias' && !Object.keys(r.al).length) return false;
    if (flag === 'hub' && r.inn < 10) return false;
    if (flag === 'fewclues' && !['es', 'en'].some((l) => (r[l] || !r.tr) && ((r.h[l] || []).length + r.out.filter((o) => !o[2].length || o[2].includes(l)).length) < 4)) return false;
    return true;
  });
  page = 0; render();
}
function clues(r, l) {
  const hints = (r.h[l] || []).map((h) => '<span class="clue ' + h[1] + '">' + esc(h[0]) + '</span>').join('');
  const links = r.out.filter((o) => !o[2].length || o[2].includes(l)).map((o) => '<span class="clue ' + o[1] + '">&rarr; ' + esc(o[3]) + (o[2].length ? ' <small>' + o[2].join('/') + '</small>' : '') + '</span>').join('');
  return hints + links;
}
function card(r) {
  const langs = [['es', 'Spanish'], ['en', 'English']].filter(([l]) => r[l] || !r.tr);
  return '<div class="card"><div class="title"><b>' + esc(r.es || r.en) + '</b>' +
    (r.es && r.en && r.es !== r.en ? '<span>' + esc(r.en) + '</span>' : '') +
    '<span class="id">' + esc(r.id) + '</span></div>' +
    '<div class="chips">' + r.cats.map((c) => '<span>' + esc(c) + '</span>').join('') + (r.tr ? '' : '<span>proper name</span>') +
    (r.inn ? '<span>' + r.inn + ' words use it as a clue</span>' : '') + '</div>' +
    langs.map(([l, name]) => '<div class="lang"><i>' + name + (r.al[l] ? ' (also: ' + esc(r.al[l].join(', ')) + ')' : '') + '</i>' + clues(r, l) + '</div>').join('') + '</div>';
}
function render() {
  const pages = Math.max(1, Math.ceil(view.length / PAGE));
  $('count').textContent = view.length + ' concepts';
  $('list').innerHTML = view.slice(page * PAGE, (page + 1) * PAGE).map(card).join('') || '<p>Nothing matches.</p>';
  $('page').textContent = 'Page ' + (page + 1) + ' of ' + pages;
  $('prev').disabled = page === 0; $('next').disabled = page >= pages - 1;
}
['q', 'cat', 'lang', 'type', 'flag'].forEach((id) => $(id).addEventListener('input', filter));
$('prev').onclick = () => { page--; render(); scrollTo(0, 0); };
$('next').onclick = () => { page++; render(); scrollTo(0, 0); };
$('total').textContent = '(' + DATA.length + ' concepts)';
render();
</script>
</body>
</html>
`;
const out = join(root, 'docs/word-map');
mkdirSync(out, { recursive: true });
writeFileSync(join(out, 'index.html'), html);
console.log(`docs/word-map/index.html written: ${rows.length} concepts, ${(html.length / 1048576).toFixed(1)} MB`);

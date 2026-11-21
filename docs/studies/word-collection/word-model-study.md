# Word collection study: shape, clue graph, categories

Status: draft for discussion. Nothing in the app or the word files has been changed by this study. Data: `data/files/src/main/assets/*_{en,es}.json` at `main` (v1.4.1), 6,586 entries
(EN 3,464 / ES 3,122), analysed with `scripts/word-study/analyze.mjs` (matching ignores case, accents and leading articles;
no plural/lemma handling, so every "is this clue also a word" figure is a lower bound).
Supporting data is in `data/` (generated, see `README.md`): `issues.csv`, `duplicate-words.csv`, `category-candidates.csv`,
`hub-clues.csv`, `hub-clues-not-words.csv`, plus `collection-summary.md` with the per-category counts.

How the game uses the data today: civilians see the secret word, each impostor sees one clue
(`clue[playerIndex % clues.size]`); the round word is `words.random()` over the entries of the selected categories.

## 1. Words: a word or a short phrase, at most 3

**The rule already holds.** Counting whitespace-separated tokens: 4,763 entries have 1, 1,451 have 2, 372 have 3,
none has more. Clues also never exceed 3 tokens (29,197 / 1,813 / 461).

What is actually inconsistent:

| Pattern | Entries | Examples |
|---|---:|---|
| Alias or gloss in parentheses (really 4+ tokens) | 6 | `Electric Vehicle (EV)`, `Jessica Drew (Spider-Woman)`, `Mindfulness (Atención Plena)` |
| Leading article | 65 (29 EN, 36 ES) | `The Hobbit`, `La Metamorfosis` (3 also exist without it) |
| Digits / symbols (`&`, `/`, `.`, `'s`) | 46 / 16 / 6 / 26 | `French 75`, `H&M`, `Downey Jr.`, `McDonald's` |
| Three tokens with a connector (`and`, `de`, `the`) | 196 of 372 | `Copa de Vino`, `Gin and Tonic` |
| Technical jargon phrases > 24 chars (mostly technology, school, science) | 41 | `Evolutionary Developmental Genetics`, `5G-Enabled Healthcare Telemetry` |

Not covered by token counts: whether the entry is a *recognisable* word for players. The 41 long jargon phrases and
the 176 entries with three content words are the review queue for that.

**Proposal.** Keep "max 3 tokens, one concept" and make it enforceable: no parentheses (aliases go to a separate
`aliases` field), articles dropped from the stored form, symbols only when part of a proper name. Add a validator
(unit test over the JSON) so the rule cannot regress. The same validator should enforce the rules found in the first
pass: no clue equal to the word (78 today), at least 3 clues (40 violate), no repeated clue (3) and no duplicate in a list (4).

## 2. Clues as words: from lists to a map

### What the data says

- Clue slots: EN 15,501, ES 14,187 (excluding self references).
- **~31 % of clues are themselves a word in the collection** (EN 4,787, ES 4,528). Only **~11 % of those links are
  reciprocal**, i.e. ~270 mutual pairs per language. The `casa` ↔ `hogar` pattern is rare today.
- The example in the request does not exist yet: `casa`, `hogar`, `vivienda`, `apartamento`, `house`, `home`,
  `apartment`, `building` are **not words**, although `casa` is used as a clue by 54 ES entries.
- The links already form **one connected map**: 98 % of headwords (EN 3,064 of 3,113) sit in a single component. It is
  connected mostly through generic hubs.
- **Hubs:** clues used by 10+ entries are only ~300 distinct strings per language but fill **38-40 % of all clue
  slots** (`store` ×107, `water` ×87, `power`, `paper`, `space`, `history` ×102 in ES). More than half of all distinct
  clues are used exactly once (EN 2,872 of 5,207). 180 EN / 168 ES of the hub clues are not words themselves
  (`data/hub-clues-not-words.csv`): natural candidates to become words.
- **Derived reverse links:** if A lists B, B could list A. This would add 4,255 EN / 4,010 ES links (33-35 % of words
  would gain at least one). On a hand-reviewed sample of 18 EN and 18 ES one-way links, roughly half of the reverse
  directions are good clues (`python→snake`, `accountability→responsibility`, `semana santa→pascua`) and half are bad
  (`macy's→star`, `donkey→strength`, `harry potter→gafas`). Typical failure: **specific → general** is a good clue, the
  reverse (general → specific entity) usually is not. Incoming neighbours of `Guitar` include `Elvis Presley`
  (acceptable) and `Coco` (poor). Reverse links must not be applied automatically.

### Options

| Option | Idea | Pros | Cons |
|---|---|---|---|
| A. Flat lists (today) | word + text clues | simple, already shipped | duplicates, no relations, no quality signal |
| B. Flat lists + derived reverse links | keep JSON, compute neighbours at import | no authoring change | ~50 % precision, hub noise, nothing to curate |
| **C. Concept map (recommended)** | nodes = concepts (senses) with labels per language and category tags; weighted, typed edges between concepts; text-only clues stay as leaf attributes | one place per concept, quality controllable, enables new modes | needs a migration and curation tooling |

Recommended shape for C (small: ~6.5k nodes, ~31k edges, a few hundred KB):

```json
{ "id": "lion", "labels": {"en": "Lion", "es": "León"}, "categories": ["animals", "mythology_and_fantasy"],
  "edges": [ {"to": "predator", "type": "category-of", "w": 0.9},
             {"text": {"en": "Mane", "es": "Melena"}, "type": "attribute", "w": 0.8} ] }
```

- Edge types give control over **difficulty**: a synonym (`hogar` for `casa`) tells the impostor almost everything;
  an attribute or association tells less. Weight and type decide which clue an impostor gets.
- Hubs get a low weight (inverse of how many concepts use them), so `store` stops being a "good" clue.
- Compile to the current flat form at build/import time first, so the app and Couchbase need no change in phase 1.
- Keep the clue-rating telemetry (`clue_ratings` in Firestore) as the quality signal for edge weights.

## 3. A word in as many categories as it semantically belongs to

### Today

- **Multi-category words already exist, but as duplicated entries:** 327 EN / 292 ES words appear in 2-4 categories
  (about 10.5 % of words); 351 EN / 314 ES extra entries (a few more are repeated inside the same category). Top overlaps: celebrities+history (27), adult_18_plus+food_and_drinks
  (25), health_and_medicine+professions (24), office+school (15), nature+space (17 ES).
- Of those duplicates only 46 EN / 12 ES have identical clues; **248 EN / 247 ES have partially overlapping clues** (same
  sense, drifted copies); **33 per language have completely different clues** and are **different senses (homonyms)**:
  `Space` (board-game square vs universe), `Mechanic` (game mechanic vs job), `Flamenco` (bird vs dance),
  `Medusa` (jellyfish vs myth), `Painting`, `Shazam`.
- **Selection bias:** the game picks a random *entry*, so a word listed twice is twice as likely whenever both
  categories are selected.

### What the map adds

Using only graph evidence (a word whose neighbours are mostly words of another category: ≥3 neighbours and ≥40 %),
there are **136 EN / 145 ES candidate memberships** (`data/category-candidates.csv`). Judged on the top of the list, it is a
useful review queue but **not an auto-apply rule** (roughly half are right):

- good: `Kitchen → kitchen_and_cookware`, `Space → space_and_astronomy`, `Car → cars_and_motorsport`,
  `Insecto → animals`, `Alcohol → adult_18_plus`;
- noise from hubs: `Historia → professions`, `Italia → stores_and_retail`, `Hospital → professions`.

### Consequences for the model

1. **The unit must be the sense, not the string.** A "word in many categories" is one concept with several tags;
   a homonym is two concepts with the same label. Clues that are words (`Space`) must point to a concept, not a string,
   or the map links unrelated senses.
2. Category tags are a set on the concept, merged from the duplicates (≈620 entries collapse into their concept).
   Selection then samples **concepts** uniformly.
3. `adult_18_plus` behaves like a content rating, not a topic (alcohol, party words overlap with food_and_drinks).
   It is better as an attribute (`mature: true`) combined with normal categories.
4. Generic words that are also category names (`Historia`, `Ciencia`, `Space`, `Clothing`) have 40-100 neighbours; they
   are labels for a topic and should be modelled as such, not as ordinary concepts.

## Recommended path (nothing is changed yet)

| Phase | Work | Risk |
|---|---|---|
| 0 | Validator for the shape rules (section 1) + fix the 78 clue-equals-word, 40 short and 4 repeated entries | none, data only |
| 1 | Add stable ids; merge same-sense duplicates into one entry with `categories[]`; keep homonyms as separate ids; compile back to flat for the app; uniform concept sampling | low (import format stays) |
| 2 | Introduce edges with type and weight (derive from current clues + hub weights), review queue for category candidates and reverse links | medium (curation effort) |
| 3 | Tooling: LLM-assisted proposals reviewed by hand, edge weights calibrated with `clue_ratings`, optional new game modes (difficulty, "similar word" impostor) | open |

## Decisions needed

1. Approve the shape rule and the validator (phase 0). Do articles stay in the displayed name?
2. Option C (concept map) as the target? Or stop at B?
3. EN and ES lists are not translations (3,113 vs 2,808 distinct words). Should a concept id align both languages
   (a word then exists in both), or do the two languages stay independent maps?
4. How much should a synonym clue be allowed (easier for impostors)? Per-edge-type difficulty policy.
5. Who reviews: manual, LLM proposals reviewed in batches, or players' ratings only?

## Limits of this study

Counts are exact; the quality estimates (≈50 % for reverse links and category candidates) come from small
hand-judged samples and need a proper review pass or the existing `clue_ratings` data. Plural/lemma variants
(`dog`/`dogs`) are not merged, so word-clue overlap is understated. Semantic judgement on samples is mine, not a
native-speaker panel.

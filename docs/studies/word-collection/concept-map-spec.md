# Concept map: detailed design

Status: phase 1 implemented (see section 12). Follows `word-model-study.md` (option C). Nothing in the app or the word files changes with this
document. The sizes quoted come from `data/concept-dry-run.md` (script `scripts/word-study/concept-dry-run.mjs`), the rules
can be tried with `scripts/word-study/concept-compile-prototype.mjs` on `concept-map-example/`.

## 1. Decisions this design implements

| Question | Decision |
|---|---|
| Shape of a word | One concept, at most 3 words, articles stay in the label (`The Hobbit`). Aliases go to a separate field, never in parentheses. |
| Model | Concept map: concepts (senses) with category tags, typed weighted links between concepts, plain-text hints as leaves. |
| Languages | **One map.** A concept carries a label per language. **Spanish (`es`) is mandatory**, every other language is optional. Looking for clues is filtered by language. |
| Synonyms as clues | Allowed, gated by a difficulty setting (impostor help). |
| Review | AI proposes, the owner reviews in batches. Proposals never reach the app until approved. |
| English-only words | The AI proposes a Spanish label; the owner reviews it. |

## 2. Entities and files

Source of truth lives **outside** `src/main/assets`, so it is not shipped; the app keeps consuming the flat files that the
compiler writes into the assets.

```
data/files/concepts/
  concepts.jsonl      one concept per line, sorted by id
  links.jsonl         one link per line, sorted by from, to
data/files/src/main/assets/{category}_{lang}.json     generated (same format as today)
data/files/src/main/assets/words_version.txt          generated (existing script)
```

JSON Lines keeps diffs and review batches line based, like the current one-entry-per-line files.

### 2.1 Concept (`concepts.jsonl`)

```json
{"id":"leon","labels":{"es":{"text":"León"},"en":{"text":"Lion","aliases":["Lions"]}},
 "categories":["animals"],
 "hints":{"es":[{"t":"Melena","type":"part","w":4},{"t":"Selva","type":"context"}],
          "en":[{"t":"Mane","type":"part","w":4},{"t":"Jungle","type":"context"}]}}
```

| Field | Type | Rules |
|---|---|---|
| `id` | string | Stable forever. `slug` of the Spanish label (`leon`; of the English one while the concept is `todo`); homonyms add a sense tag: `espacio~universo`, `espacio~casilla`. Pattern `^[a-z0-9]+(-[a-z0-9]+)*(~[a-z0-9]+(-[a-z0-9]+)*)?$`. Labels may change, the id never does (links and ratings point at it). |
| `labels` | map lang to label | `es` required (see `todo`). Other keys are ISO 639-1 codes. A concept without a label in a language is **not a word in that language** and cannot be a clue there. |
| `labels.<l>.text` | string | 1 to 3 words, no parentheses, not blank. This is what players see. |
| `labels.<l>.aliases` | string[] | Optional. Never shown. Used only to reject clues that give the word away. |
| `categories` | string[] | At least 1, each an existing category slug. A word belongs to every category where it fits; the first is the primary one. |
| `hints` | map lang to hint[] | Plain-text clues that are not concepts (yet). Per language, because text is language specific. |
| `kind` | `"thing"` (default) or `"topic"` | `topic` marks very general words (`Historia`, `Ciencia`, `Space`) that are labels of a topic more than things; they are valid targets of `broader` links but are reviewed before being a secret word. |
| `todo` | string[] | Languages still missing while the concept is being completed, e.g. `["es"]` for a migrated English-only word. The release check fails if any concept has `todo`. |
| `status` | `approved` (default) / `proposed` | `proposed` = written by the AI, not reviewed. |

Hint: `t` (text, 1 to 3 words), `type` (default `association`), `w` (1 to 5, default 3), `status`.

### 2.2 Link (`links.jsonl`)

```json
{"from":"leon","to":"felino","type":"broader","w":4}
{"from":"casa","to":"hogar","type":"synonym","w":4}
{"from":"hogar","to":"casa","type":"synonym","w":4}
```

| Field | Rules |
|---|---|
| `from`, `to` | Concept ids. No self links. `(from,to)` is unique. |
| `type` | One of the types in 3. Default `association`. |
| `w` | Integer 1 to 5, default 3. |
| `langs` | Optional. Restricts the link to some languages (`["en"]`); absent = every language where both concepts have a label. |
| `status` | `approved` (default) or `proposed`. |

## 3. Link semantics

**A link `from -> to` means: "the label of `to` is a good clue for `from`".** Direction is part of the meaning, so reverse
links are never implied. This is the fix for the study's finding that reversing links is right about half of the time
(`python -> snake` good, `donkey -> strength` bad): a reciprocal pair is two explicit links, like `casa <-> hogar`.

| Type | Meaning | Example | Reveal |
|---|---|---|---:|
| `association` | Loosely related (default, what all migrated clues start as) | `playa -> sombrilla` | 1 |
| `context` | Where, when or what for | `leon -> selva` | 1 |
| `part` | Part, feature or typical attribute | `leon -> melena` | 2 |
| `broader` | `to` is a more general kind of `from` | `leon -> felino` | 2 |
| `contrast` | Opposite or foil | `frio -> calor` | 2 |
| `synonym` | Same or near-same meaning | `casa -> hogar` | 3 |

`reveal` is how much a clue tells the impostor. It drives the **impostor help** setting:

| Help level | Clues allowed |
|---|---|
| `low` | reveal 1 |
| `normal` (default) | reveal 1 to 2 |
| `high` | reveal 1 to 3 (synonyms too) |

Weight `w` says how good a clue is within its type (1 weak, 5 excellent). The score used to rank clues is
`w / 5 * hubFactor * ratingFactor`:

- `hubFactor = min(1, sqrt(8 / inDegree))` where `inDegree` is the number of links pointing at the target concept. A concept
  used as a clue by 100 others (`store`, `history`) gets about 0.28, so it stops being a "good" clue. Text hints are not
  hubs (factor 1).
- `ratingFactor` is 1 until the ratings calibration (phase 3) fills it from `clue_ratings`.

### Several languages, one map

Links connect concepts, not strings, so one link serves every language. With two concepts `casa` (es *Casa*, en *House*) and
`hogar` (es *Hogar*, en *Home*), the single pair `casa <-> hogar` (synonym) is the synonym link of *casa/hogar* **and** of
*house/home*. Two words of one language that mean the same are two concepts joined by a `synonym` link, not one concept
with two labels (`aliases` only hold spellings the player should not be given as clues).

Language filtering: for language `L`, a clue from a link is offered only when the target has a label in `L` (and the link is
not restricted away from `L`). A concept with no English label is not a word in English and never appears as an English clue
(in the example, `tablero` has no `en` label, so `espacio~casilla -> tablero` is silently unusable in English; the compiler
reports it).

## 4. Hubs

The dry run found that, once a clue that is a word is turned into a link, hub concepts (10 or more incoming links) are only
109 EN / 117 ES and make up about 15 % of the clue slots. The hub factor only *reorders* a concept's clues. Because the app
gives an impostor `clue[playerIndex % size]`, reordering alone changes nothing; the policy is therefore:

1. Rank by score, keep at most 8.
2. While more than 4 clues remain, drop the ones scoring under 0.2 (a hub used as weak association).
3. Never go below 3. A concept that cannot reach 3 usable clues is reported and not emitted for that language/help level.

A later phase can make the runtime pick clues by weight instead of by index; the data does not need to change for that.

## 5. Categories

- `categories` is a set on the concept. Duplicated entries of one sense collapse into one concept; **homonyms stay separate
  concepts** with their own id and categories (`espacio~universo` in space and nature, `espacio~casilla` in board games).
- `adult_18_plus` stays a normal category tag in phases 1 and 2. The study suggested treating it as a content rating
  instead; that needs UI work and is an open question (section 10).
- Category candidates suggested by the graph (136 EN / 145 ES, about half right) enter as `proposed` and are reviewed.

## 6. Compiler: concept map to the flat files

`node scripts/words/compile.mjs` (to be written in phase 1; `scripts/word-study/concept-compile-prototype.mjs` is the
reference) writes `{category}_{lang}.json` for every language and category, then runs `update-words-version.mjs`.

For each concept `C`, language `L` that `C` labels, and level `normal`:

1. Candidates: links from `C` whose target has an `L` label and that are `approved` and not restricted away from `L`, plus
   the approved `L` hints of `C`.
2. Keep those whose reveal is allowed at the level; drop any whose normalised text equals the label or an alias of `C` in `L`
   (a clue must not give the word away) and duplicates by normalised text.
3. Score, rank, cap (section 4).
4. Emit one flat entry `{word, clues, category}` for every category of `C`, plus the new field `conceptId`.

Why a field per entry and one entry per category: existing ratings use the key `category_word_clue`, which therefore keeps
working for every migrated word and clue text; the extra field is ignored by today's parser (`ignoreUnknownKeys`).

**The app change in phase 1:** the round word is `words.random()` over entries, which counts a multi-category concept once
per selected category. `SearchWordsUseCase` now returns one entry per distinct `conceptId` (entries without one are never
merged), so the pick is uniform over concepts; `WordJson`, `WordDoc` and `WordModel` carry the nullable `conceptId`.

**Impostor help in the app (phase 2):** the compiler emits all clues up to level `high` and a parallel array `reveal`
(`"clues":["Jardín","Hogar"],"reveal":[1,3]`); the app filters by the setting. Old builds ignore `reveal`.

## 7. Validation (CI, extends `WordAssetsValidationTest`)

On the source files: unique ids and id pattern; links reference existing concepts; no self or duplicate links; `es` label
present unless `todo`; label and hint text 1 to 3 words, not blank, no parentheses, no separators (`「」|;`); categories exist
and are non-empty; weights 1 to 5; known link types; two concepts with the same label in a language must differ by a sense
tag; every `(concept, language)` compiles to at least 3 clues at `normal`; `proposed` records are never compiled.
Release check: no `todo`, and `words_version.txt` matches the compiled output. The existing validator keeps checking the
compiled files, so both layers are covered.

## 8. Review workflow

1. A generator (AI-assisted, run by the owner or a session) writes proposal records into `data/files/concepts/proposals/`:
   Spanish labels (with translated clues) for English-only words, clue types, later category candidates, reverse-link
   suggestions and homonym disambiguation. Proposals live in their own files, so `concepts.jsonl` and `links.jsonl` only hold
   approved data (phase 2 therefore did not need the `status` field in the sources).
2. Records are grouped in batches of about 50 in one file per kind (`proposals/<kind>-<n>.jsonl`) with the evidence next to
   each (neighbours, source entries).
3. The owner accepts, edits or rejects each line. A small tool applies the decisions (`status` removed or the record
   deleted) and re-runs the validator.
4. Only `approved` data is compiled. Reverse-link suggestions follow the direction rule: `synonym` reverse always proposed,
   `association` reverse proposed for review, `broader` reverse never (specific to general is the good direction).

## 9. Migration (phase 1) and what the dry run measured

Algorithm: group entries per language by normalised word; entries that share at least one clue are one sense (merge, union of
clues), the rest are homonyms with separate ids; a clue that equals a single concept's label becomes a link (migrated links get
`langs: [<their language>]` so nothing is silently extended across languages), otherwise a hint; link type
`association`, `w` 3; everything migrated is `approved` because it is shipped data. Then pair English and Spanish concepts.

| Measure (dry run on v1.4.1 lists) | EN | ES |
|---|---:|---:|
| Entries today | 3,464 | 3,122 |
| Concepts after merging same-sense duplicates | 3,143 | 2,839 |
| of which homonyms | 60 | 62 |
| Links to a single concept / text-only hints | 4,661 / 10,714 | 4,374 / 9,659 |
| Clues pointing at a homonym (need disambiguation) | 126 | 154 |
| Pairs that are already reciprocal | 255 | 246 |

Pairing the languages: 721 EN concepts have an identical Spanish label and a shared category (auto-pair, which covers many
proper names), 11 need a look, and **2,411 English-only concepts need a proposed Spanish label** reviewed by the owner, about
48 batches of 50. The merged map would have about 5,250 concepts. Until an English-only concept is completed it carries
`todo: ["es"]`, keeps its English label, and compiles for English exactly as today.

Safety nets: the dry run proves no clue is lost (31,471 clues checked, 0 lost) and the migration must keep that check as a
test; the compiled flat files are diffed against the current ones and every difference is explained by the merge or by
phase 0 cleanups; word and clue text of unmerged entries stays identical so `clue_ratings` still match; the app is tested on
both emulators (es and en, upgrade from the current release thanks to the versioned import).

## 10. Open questions

1. `adult_18_plus`: keep as a category, or turn it into an audience attribute combined with normal categories?
2. `kind: "topic"` concepts (`Historia`, `Space`): keep them as selectable secret words, or only as clue targets?
3. Do links migrated from one language get extended to the others (`langs` removed) automatically after review, or one by one?
4. Third language: the schema allows it; adding one only means adding labels and hints. Not planned.
5. Where do the AI proposals run (owner machine, CI with a secret in Doppler)? The proposals contain no personal data.

## 11. Phases

| Phase | Work | App change |
|---|---|---|
| 1 | Migration script, `concepts/` source files, compiler, validator, uniform concept sampling, `conceptId` field | one line (sampling) |
| 2 | Link types and weights proposed and reviewed, ES labels for English-only words, impostor help setting with `reveal` | setting + clue filter |
| 3 | Ratings calibration (`ratingFactor` from `clue_ratings`, Wilson lower bound), runtime weighted clue pick, optional "similar word" modes | optional |

Limits: the dry-run figures use the v1.4.1 lists and string matching without lemmas; the hub constants (`8`, `0.2`) and the
reveal values per type are starting points to calibrate with reviewed data and ratings.

## 12. Phase 1 as built

| Piece | Where |
|---|---|
| Shared rules (ranking, caps, serialization) | `scripts/words/lib.mjs` |
| One-off migration (refuses to run again) | `scripts/words/migrate.mjs` |
| Compiler, `--check` for CI | `scripts/words/compile.mjs` |
| Before/after comparison of two list directories | `scripts/words/compare.mjs` |
| Source validator | `ConceptSourcesValidationTest` (data:files) |
| Uniform sampling test | `SearchWordsUseCaseTest` (domain:usescases:game) |
| CI | job `word_lists` in `validate.yml` |

Differences from the design found while building it:

- The clue cap is 8, not 6: with 6, merging duplicate entries dropped about 520 clues per language; with 8 only the hub
  floor drops anything (about 160 EN / 170 ES clues, mostly `alcohol`, `water`, `glass`). They stay in the sources as links or
  hints, so raising a weight brings them back.
- Several English senses of one word that all match the same Spanish sense are merged into one concept (a brand listed in
  two categories with different clues, for example `Puma`); otherwise the compiled list had the word twice.
- A clue with a parenthetical (`Hierbas (Anís)`) is split into two clues.

Result on the v1.4.1 lists after the phase 0 cleanup: 5,256 concepts (719 paired EN/ES, 2,116 Spanish only, 2,421 English
only with `todo: ["es"]`), 8,970 links, 20,736 hints, 0 clues lost (31,609 checked). Compared with the lists before, 594 EN
and 592 ES entries changed (merged duplicates and extra categories after pairing), 21 EN and 25 ES entries were added (a
paired word gained the categories of its other-language side), none removed.

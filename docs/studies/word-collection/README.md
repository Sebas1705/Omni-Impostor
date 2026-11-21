# Word collection study

Study of how the game's words are structured and what a better structure could be. Everything produced while doing the
study lives in this repository.

| File | What it is |
|---|---|
| [`word-model-study.md`](word-model-study.md) | The study: word shape (max 3), clues as a map, words in many categories, options, recommendation, decisions needed |
| [`concept-map-spec.md`](concept-map-spec.md) | Detailed design of the concept map: entities and files, link types, languages, compiler to the flat files, validation, review workflow, migration, phases |
| [`concept-map-example/`](concept-map-example) | Small example of the source files (`concepts.jsonl`, `links.jsonl`) used to try the compiler rules |
| [`data/concept-dry-run.md`](data/concept-dry-run.md) | What the migration would produce from today's lists (generated) |
| [`collection-summary.md`](collection-summary.md) | Entries per category and language (generated) |
| [`data/issues.csv`](data/issues.csv) | Entries that break a shape rule: clue equals word, fewer than 3 clues, repeated clue, duplicate in a list, parentheses, more than 3 words |
| [`data/duplicate-words.csv`](data/duplicate-words.csv) | Words listed in several categories, with whether their clue lists look like the same sense or a homonym |
| [`data/category-candidates.csv`](data/category-candidates.csv) | Category memberships suggested by the clue graph (a review queue, not an auto-apply list) |
| [`data/hub-clues.csv`](data/hub-clues.csv), [`data/hub-clues-not-words.csv`](data/hub-clues-not-words.csv) | Generic clues used by 10+ entries, and which of them are not words yet |

## Regenerate

The files in `data/` and `collection-summary.md` are generated from `data/files/src/main/assets/*_{en,es}.json`:

```powershell
node scripts/word-study/analyze.mjs
```

It also writes `data/word-collection.csv` (every word with its clues, git-ignored because it is large and derivable).
Requires Node 18+; it has no dependencies and changes no source file.

## Status

Draft for discussion. The study changes nothing in the app or in the word files; the proposed phases and the open
decisions are at the end of `word-model-study.md`.

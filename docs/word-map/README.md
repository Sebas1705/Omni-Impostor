# Word map: where to see and correct the data

| What | Where |
|---|---|
| **Browse everything** (search, filters, every clue with its type) | [`index.html`](index.html), open the file in a browser. Once merged it is also served at <https://sebas1705.github.io/Omni-Impostor/word-map/> |
| The data itself | [`data/files/concepts/concepts.jsonl`](../../data/files/concepts/concepts.jsonl) (one concept per line: labels, categories, clues) and [`links.jsonl`](../../data/files/concepts/links.jsonl) (concept to concept clues) |
| What the app ships (generated, do not edit) | `data/files/src/main/assets/{category}_{es,en}.json` |
| Design and decisions | [`docs/studies/word-collection/concept-map-spec.md`](../studies/word-collection/concept-map-spec.md) |
| Migration report (numbers, what is unreviewed) | [`docs/studies/word-collection/migration-report.md`](../studies/word-collection/migration-report.md) |

`index.html` is generated: `node scripts/words/compile.mjs` rewrites it together with the lists (or run `node scripts/words/export-viewer.mjs`).

## Correcting something

Find the concept in the viewer (each card shows its `id`), then either tell the assistant the `id` and the change, or do it yourself:

```powershell
node scripts/words/edit.mjs show <id>                      # see the raw concept and its links
node scripts/words/edit.mjs label <id> es "Nuevo nombre"   # change a label (1 to 4 words)
node scripts/words/edit.mjs add-hint <id> es "Pista"       # add a text clue
node scripts/words/edit.mjs remove-hint <id> es "Pista"    # remove a text clue
node scripts/words/edit.mjs type <id> es "Pista" synonym   # type a clue: association, synonym, broader, contrast, part, context
node scripts/words/edit.mjs link <from> <to> synonym       # the label of <to> becomes a clue of <from>
node scripts/words/edit.mjs unlink <from> <to>
node scripts/words/edit.mjs categories <id> animals,nature_and_weather
node scripts/words/edit.mjs translatable <id> false        # proper name: the same text in every language
node scripts/words/edit.mjs delete <id>                    # remove a concept and the links to it
```

After editing:

```powershell
node scripts/words/compile.mjs                 # regenerates the lists the app ships and words_version.txt
node scripts/words/export-viewer.mjs           # refreshes this page
node --test scripts/words/words.test.mjs scripts/words/edit.test.mjs
```

Then run the unit tests of `data:files` (`ConceptSourcesValidationTest`, `WordAssetsValidationTest`) and open a pull request.
Installed apps replace their stored words by themselves when `words_version.txt` changes. Never edit the files under
`src/main/assets`: CI regenerates them from the concepts and fails if they differ.

# Migration report: word lists to the concept map

Status: finished and merged to `main` (pull requests #25 to #30, 2026-11-22). Design: [`concept-map-spec.md`](concept-map-spec.md).
Browse and correct the data: [`docs/word-map/`](../../word-map/README.md).

## Before and after

| | Before (v1.4.1) | After |
|---|---:|---:|
| Source of the lists | 88 hand-edited JSON files | one concept map, lists generated from it |
| Entries shipped, Spanish | 3,122 | **4,403** |
| Entries shipped, English | 3,464 | **3,531** |
| Concepts | n/a (6,586 separate entries) | **3,981**: 2,831 with Spanish and English, 856 Spanish only, 294 English-only proper names that reuse their name in Spanish |
| Words in several categories | copies that drift apart | 392 concepts with several categories under one id |
| Clues that are themselves words | text only | 6,731 links between concepts |
| Typed clues | none | hints: 719 synonym, 1,758 broader, 10 contrast; links: 108 synonym, 571 broader, 9 contrast |
| Impostor help setting | none | low / normal / high, driven by the clue types |

Also shipped on the way: cleanup and validators of the word lists (78 clues that gave the word away removed), versioned re-import
of the words when the app updates (and the `deleteAll` bug that kept the old words), uniform choice of the round word over
concepts, weighted draw of the impostor clue, rating calibration tooling, the `translatable` flag for proper names, the browsable
viewer and `edit.mjs`.

## What came from where, and what nobody has proofread

| Data | How it was produced | Reviewed |
|---|---|---|
| The original 3,464 + 3,122 entries and their clues | migrated by script, no clue lost (31,609 checked) | by the project over time |
| About 1,150 new Spanish labels with 4 to 5 translated clues each, and 1,263 English words folded into Spanish concepts | translated by AI (sonnet agents), checked by script (1 to 3 words, no hint equal to the label, no clash) | **only the 51 conflicts**; the rest of the translations are unreviewed |
| Merging of English synonyms into one concept (the second word becomes an alias) | script, rules agreed with the owner | the groups were reviewed with the owner |
| 87 clues written in phase 0 (`phase0-review.md`) | assistant | accepted by the owner |
| 3,338 clue types (synonym, broader, contrast) | AI typing pass plus an independent strict review pass | **sampled, not line by line** |
| Clue types part and context | dropped (3,661 discarded) | not applicable |
| Cleanup of `professions_es` and the generic clue Expansión | decided with the owner | yes |

## Where I would look first (known weak spots)

1. **Spanish labels that are workarounds.** When the natural translation clashed with another concept or exceeded 3 words, the
   agents chose a variant. Some of these are really duplicates that should be merged or renamed:

| Spanish label | English | id |
|---|---|---|
| Fuerza física | Strength | `strength` |
| Energía física | Energy | `energy` |
| Batería musical | Drums | `drums` |
| Bebida energizante | Energy Drink | `energy-drink` |
| Médico urólogo | Urologist | `urologist` |
| Médico digestivo | Gastroenterologist | `gastroenterologist` |
| Médico patólogo | Pathologist | `pathologist` |
| Vía rápida | Highway | `highway` |
| Relato mítico | Myth | `myth` |
| Energía agotable | Non-renewable Energy | `non-renewable-energy` |
| Energía atómica | Nuclear Energy | `nuclear-energy` |
| Fiesta cervecera | Oktoberfest | `oktoberfest~holidays-and-celebrations` |
| Estatua Libertad | Statue of Liberty | `statue-of-liberty` |
| Día del Planeta | Earth Day | `earth-day` |
| Fiesta Diwali | Diwali | `diwali~holidays-and-celebrations` |
| Fábula con moraleja | Fable | `fable` |
| Geotermia | Geothermal Energy | `geothermal-energy` |
| Órgano corporal | Organ | `organ~science-biology` |
| Paz mundial | Peace | `peace` |
| Actividad radiactiva | Radioactivity | `radioactivity` |
| Distorsión de realidad | Reality Warping | `reality-warping` |
| Fuentes renovables | Renewable Energy | `renewable-energy~science-physics` |
| Saga mítica | Saga | `saga~mythology-and-fantasy` |
| Vela deportiva | Sailing | `sailing` |
| Shazam superhéroe | Shazam | `shazam~superheroes-and-comics` |
| Sábanas | Sheet | `sheet` |
| Firmamento | Sky | `sky` |
| Calendario de pared | Wall Calendar | `wall-calendar` |
| Nube informática | Cloud | `cloud~technology` |
| Año Nuevo lunar | Chinese New Year | `chinese-new-year` |
| Ciclovía | Bicycle Lane | `bicycle-lane` |
| Teoría de supercuerdas | String Theory | `string-theory` |
| Puente Einstein-Rosen | Wormhole | `wormhole` |
| Planeta Mercurio | Mercury | `mercury~space-and-astronomy` |
| Lucifer Morningstar | Lucifer | `lucifer~superheroes-and-comics` |
| Metamorfosis animal | Metamorphosis | `metamorphosis` |
| Multiverso de cómics | Multiverse | `multiverse~superheroes-and-comics` |
| Plataforma gamificada | Gamified Learning Platform | `gamified-learning-platform` |
| Plataforma online | Online Learning Platform | `online-learning-platform` |
| Sombrero de vestir | Hat | `hat` |
| Línea del Ecuador | Equator | `equator` |
| Director escolar | Principal | `principal` |
| Gorra Gatsby | Gatsby Cap | `gatsby-cap` |
| Tecnología urbana | Smart City Technology | `smart-city-technology` |
| Agrotecnología inteligente | Smart Agriculture Technology | `smart-agriculture-technology` |
| Dispositivo domótico | Smart Home Device | `smart-home-device` |
| Gafas VR | Virtual Reality Headset | `virtual-reality-headset` |
| Terapia virtual | Virtual Reality Therapy | `virtual-reality-therapy` |
| Espacio-tiempo | Time-Space Continuum | `time-space-continuum` |
| Motorista Fantasma | Ghost Rider | `ghost-rider` |
| Sublimación inversa | Deposition | `deposition` |
| Filibusterismo | Filibuster | `filibuster` |
| Evo-devo | Evolutionary Developmental Biology | `evolutionary-developmental-biology` |
| Casa de papel | Money Heist | `money-heist` |
| Día de bromas | April Fools' Day | `april-fools-day` |
| Quad | ATV | `atv` |
| Mochileo | Backpacking | `backpacking` |
| Carpeta con pinza | Clipboard | `clipboard` |
| Tuning | Car Customization | `car-customization` |
| Cabildeo | Lobbying | `lobbying` |
| Tren bala | High-Speed Train | `high-speed-train` |
| Alunizaje | Moon Landing | `moon-landing` |
| Aguardiente casero | Moonshine | `moonshine` |
| Gorra Gatsby | Gatsby Cap | `gatsby-cap` |
| Sustentabilidad | Sustainability | `sustainability` |
| Energía del viento | Wind Energy | `wind-energy` |
| Copa para vino | Wine Glass | `wine-glass` |
| Fuerza | Force | `fuerza~science-physics` |
| Batería | Battery | `bateria~cars-and-motorsport` |

   The 3-word limit also forces unnatural labels (`Estatua Libertad`, `Día del Planeta`, `Casa de papel`).
2. **Clue types.** `broader` is generous (about 1,760 hints): brands to "Store", occupations, product categories. The review pass
   removed genres and manufacturers, but a native speaker would still change some. Filter the viewer by clue type to read them.
3. **Translated clues** of the 2,421 concepts that were English-only: natural, but not proofread. Filter "Spanish and English" in
   the viewer and read the Spanish rows.
4. **Hubs.** 173 concepts are the clue of 10 or more words (Store, Tienda, Cocktail, País...). Their weight is lowered
   automatically, but nobody decided which of those links are good.

## Not done or not verified

- The player ratings (`clue_ratings`) are not fetched: it needs your own Google credentials, so the rating factor is neutral for now.
- Not tested with a real release AAB. The English text of the new impostor help setting was not seen on a device (tested in Spanish).
- 856 concepts are Spanish only (no English word); English stays optional by design.
- Open design questions: treat `adult_18_plus` as a content rating instead of a category; keep generic topic words (`Historia`, `Space`) as secret words.
- The optional "similar word" impostor mode was not built.

## Verification done

Unit tests of the data validators, the tooling (`node --test`), the use cases and the offline game; CI green on every pull request;
upgrade from the previous build and games played on the emulator in Spanish and English (the number of clues given at low, normal
and high help matched the data).

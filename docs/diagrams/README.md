# Architecture Diagrams

Folder: `docs/diagrams`

PlantUML (`.puml`) diagrams describing the multi-module architecture and data flows:

- `00-modulos-dependencias.puml` — Module map (core, data, domain, feature, app).
- `01-capas-flujo.puml` — Layer diagram and dependency flow (Clean Architecture).
- `02-secuencia-flujo-uso.puml` — Typical sequence: UI → UseCase → Repo → DataSource.
- `03-core-paquetes.puml` — Packages and components of the `core` module.
- `04-domain-paquetes.puml` — Packages and classes of the `domain` module.
- `05-data-paquetes.puml` — Packages and structures of the `data` module.
- `06-feature-paquetes.puml` — Example packages inside a `feature` module.
- `07-modelos-clases.puml` — Class diagram example for models and mappers.

## Rendering

### Option 1: VS Code + PlantUML extension
Install the PlantUML extension and Graphviz locally; open any `.puml` file and use the preview pane.

### Option 2: Command line
```bash
cd docs/diagrams
plantuml *.puml
```

### Option 3: Online
Paste the contents of any `.puml` file at [plantuml.com/plantuml](https://www.plantuml.com/plantuml/uml/).

## Notes

- Diagrams reflect the multi-module structure declared in `settings.gradle.kts`.
- For exact class diagrams derived from real code (actual classes and relationships), the Kotlin packages can be analyzed and the diagrams regenerated from `build-logic`.

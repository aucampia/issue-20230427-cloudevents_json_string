# ...

```bash
poetry install
poetry run aucampia-issue-cejsonstring-cli


poetry run poe help
poetry run poe validate-fix && poetry run poe validate

```

## Using docker devtools

```bash
make -C devtools -B
docker compose build


docker compose run --rm python-devtools run poe help
docker compose run --rm python-devtools run poe validate-fix && poetry run poe validate

```

## Updating from template base

```bash
pipx run --spec=cruft cruft update
```

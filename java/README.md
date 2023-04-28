```bash
## Run the game with random seed
./gradlew run

## Run with a specific seed
./gradlew run --args='1'

## format the code
./gradlew spotlessApply

## Run all checks
./gradlew check

## (re-)Run tests
./gradlew --rerun-tasks test --info

## Run jar direct
./gradlew build
java -jar build/libs/aucampia-example-eating_monsters.jar

## Generate docs
./gradlew javadoc
```

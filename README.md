# HolyModeration — buildable sources

Fabric mod for Minecraft **1.21.11** with Mojang mappings.

## Requirements

- Java 21+
- Internet (Gradle downloads Minecraft + Fabric on first run)

## Build

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
```

Output JAR:

```
build/libs/HolyModeration-2.10alpha.jar
```

Run client:

```bash
./gradlew runClient
```

## Config paths

Cross-platform config directory:

```
~/.config/fabric/holymoderation/          # macOS/Linux (Fabric config dir)
config/holymoderation/                    # relative to game directory
```

Files:

- `config.json` — mod settings
- `checktwinks.txt` — twinks check input
- `results.txt` — twinks check output
- `temp/` — temporary twinks files

## Bundled assets

Sounds are bundled in the mod JAR:

```
src/main/resources/assets/holymoderation/sounds/
```

## Versions

| Component | Version |
|-----------|---------|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.141.6+1.21.11 |
| Loom | 1.14.7 |
| Java | 21 |

## External services

- Journal API: `https://journal.holyworld.me/srv/api/v1/` (requires API token via `/hm setapitoken`)
- No runtime dependencies on GitHub

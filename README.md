# HolyModeration

Fabric-мод для модерации на **Minecraft 1.21.11**.

## Требования

| Компонент | Версия |
|-----------|--------|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.19.3+ |
| Fabric API | 0.141.6+1.21.11 |
| Java (для сборки) | 21+ |

## Установка

1. Установи [Fabric Loader](https://fabricmc.net/use/) для Minecraft **1.21.11**
2. Скачай **Fabric API** (`0.141.6+1.21.11`) и положи в `mods/`
3. Скачай `HolyModeration-21-11.jar` из [Releases](https://github.com/xvdosha-alt/HolyModeration/releases)
4. Положи JAR в папку `mods/` клиента
5. Запусти игру

```
.minecraft/
  mods/
    fabric-api-0.141.6+1.21.11.jar
    HolyModeration-21-11.jar
```

## Первый запуск

### API-токен журнала

Для работы с журналом проверок нужен токен:

```
/hm setapitoken <токен>
```

После установки токена мод попросит перезайти на сервер.

### Конфиг

Настройки сохраняются в:

```
~/.config/fabric/holymoderation/config.json     # macOS / Linux
config/holymoderation/config.json               # рядом с игрой (Windows)
```

Дополнительные файлы:

- `checktwinks.txt` — список игроков для проверки твинков
- `results.txt` — результат проверки твинков
- `temp/` — временные файлы

## Основные команды

| Команда | Описание |
|---------|----------|
| `/hm` | Справка по командам |
| `/hm enable` / `/hm disable` | Включить / выключить мод |
| `/frz <ник>` | Начать проверку (заморозка) |
| `/unfrz` | Закончить проверку |
| `/hm startcheckout <ник> <причина>` | Внести проверку в журнал |
| `/hm endcheckout <результат>` | Завершить проверку в журнале |
| `/hm twinks` | Проверка твинков |
| `/hm spy <ник>` | Следить за игроком |
| `/hm stats` | Статистика проверок |
| `/hm textadd <текст>` | Добавить авто-текст для проверки |
| `/hm textslist` | Список авто-текстов |

### Причины проверки (журнал)

`report`, `checkout`, `autobuy`, `autosell`, `customka`, `personal`, `toManyChecks`, `candidate`

### Результаты проверки (журнал)

`clean`, `ban`, `autobuy`, `autosell`

## Как работает проверка

1. `/frz <ник>` — начинается проверка, игрок замораживается
2. Через несколько секунд в чат приходят кнопки для внесения проверки в журнал
3. Настроенные авто-тексты отправляются игроку через `/msg`
4. `/unfrz` — проверка завершается, появляются кнопки:
   - Закончить с результатом «чистый»
   - Закончить с «бан» (+/- снос стеша)
   - Закончить с «автобай» / «автоселл»

## Сборка из исходников

```bash
git clone https://github.com/xvdosha-alt/HolyModeration.git
cd HolyModeration
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
./gradlew build
```

Готовый JAR:

```
build/libs/HolyModeration-21-11.jar
```

Запуск клиента для отладки:

```bash
./gradlew runClient
```

## Внешние сервисы

- Journal API: `https://journal.holyworld.me/srv/api/v1/`
- Требует API-токен (`/hm setapitoken`)

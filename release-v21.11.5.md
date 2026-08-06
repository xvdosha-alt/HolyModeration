![kick](https://github.com/xvdosha-alt/HolyModeration/releases/download/v21.11.5/release-kick.png)

## HolyModeration · v21.11.5

Стабильный релиз мода для модерации на **HolyWorld** · **Minecraft 1.21.11**

---

Мой кик из КП — **не конец разработок**. HolyModeration продолжает жить и обновляться. По вашим **анонимным наводкам** сюда будут добавляться новые функции — пишите, что нужно модерам, без привязки к «официальной» команде.

---

### Новое

**Journal API отключён**
- API-ключи journal больше не работают — мод не блокируется из‑за `/hm setapitoken`
- VK только через `/hm setvk vk.com/id123` → `config/holymoderation/config.json`
- `/hm me` — ник + сохранённый VK; `/hm stats` — недоступно без journal
- Проверки (`startcheckout` / `endcheckout`) работают без записи в journal

**Bare-сборка (опционально)**
- `./gradlew build -Pbare=true` → `HolyModeration-bare-21-11.jar`
- Без HUD, тостов, звуков, маркера CHECK и `[HM]` в чате

---

### Установка

| | |
|---|---|
| **Minecraft** | 1.21.11 |
| **Fabric Loader** | 0.19.3+ |
| **Fabric API** | 0.141.6+1.21.11 |

1. Скачай **`HolyModeration-21-11.jar`**
2. Положи в `.minecraft/mods/`
3. `/hm setvk vk.com/id<твой_id>`
4. `/hm enable`

---

<details>
<summary>Changelog с v21.11.4</summary>

- Journal API полностью отключён (`ModBuild.JOURNAL_API = false`)
- Мод разблокируется на HW без API-токена
- `/hm setapitoken` → подсказка использовать `/hm setvk`
- Checkout без отправки в journal
- Bare-сборка для stealth-режима

</details>

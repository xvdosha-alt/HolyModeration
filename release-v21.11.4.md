![kick](https://github.com/xvdosha-alt/HolyModeration/releases/download/v21.11.4/release-kick.png)

## HolyModeration · v21.11.4

Стабильный релиз мода для модерации на **HolyWorld** · **Minecraft 1.21.11**

---

Мой кик из КП — **не конец разработок**. HolyModeration продолжает жить и обновляться. По вашим **анонимным наводкам** сюда будут добавляться новые функции — пишите, что нужно модерам, без привязки к «официальной» команде.

---

### Новое

**VK без API журнала**
- `/hm setvk vk.com/id123` — сохраняется в `config/holymoderation/config.json`
- Подставляется в `/banip`, `/ban`, `/hm sban` и другие наказания

**Сканер /dupeip (VaguriDupeIP встроен)**
- `/hm autodupeip` + `/frz` — автопоиск забаненных твинов
- Reverse-check, расчёт срока, окно подтверждения `/banip`
- Отдельный мод VaguriDupeIP не нужен

**Проверки**
- Лив игрока → сразу кнопки «Закончить проверку»
- Autovanish/autogm3 после лива и `/unfrz`
- Внос проверки: ник копируется в буфер (без спама кнопками в чате)
- Кнопки «Закончить проверку» — в чате как раньше

**HUD и команды**
- Перетаскиваемые панели SPY / CHECK
- Tab-подсказки `/hm sban` (`30d`/`20d` + причины)
- VK в ручных `/banip`, `/ban`, `/mute`

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

---

<details>
<summary>Changelog с v21.11.2</summary>

- VaguriDupeIP встроен в мод
- `/hm setvk` — VK в конфиге навсегда
- Ник в буфер вместо кнопок «Внести проверку»
- Лив на проверке, autovanish, draggable HUD
- Tab `/hm sban`, VK в ручных банах

</details>

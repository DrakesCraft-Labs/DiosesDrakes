<p align="center">
  <img src="https://raw.githubusercontent.com/DrakesCraft-Labs/DiosesDrakes/main/diosesdrakes_banner.svg" width="100%" alt="DiosesDrakes banner" />
</p>

# DiosesDrakes

**The persistent divine-progression system for DrakesCraft on Paper/Purpur 1.21.11.** Players choose a pantheon and patron, unlock an intentional loadout of blessings, earn favor through approved boss content, and take part in the permanent Convergence anchors.

## Start here

- Run **`/dioses`** to open the pantheon and active-path menu.
- Run **`/dioses ayuda`** to open the in-game Convergence guide before selecting, spending, or renouncing anything.
- Run **`/dioses estado`** to see the active patron, current favor and upkeep state.
- Run **`/dioses libro`** for the portable Codex.

The menus are intentionally informational before they are transactional: they explain the permanent cost of a patron choice, skill requirements, favor sources, protection boundaries and anchor mechanics.

## The divine loop

1. Choose one pantheon, then one patron.
2. Unlock and equip a small, deliberate set of passive, active, stance and combat skills.
3. Defeat eligible Odysseia bosses to gain durable favor for the selected patron.
4. Spend favor at public Convergence anchors to support a pantheon.
5. Pay configured weekly upkeep to keep the divine path usable when the economy module is enabled.

Renouncing a patron intentionally clears favor, nodes and relics tied to that path. The configured 48-hour cooldown prevents faction hopping from becoming an exploit.

## Pantheons and Convergence

The foundation includes Greek, Nordic, Egyptian and Celtic pantheons. They are not flattened into one list: a player selects a pantheon first, then a deity with its own skill path.

Convergence anchors are permanent public locations. They do not claim blocks or damage survival worlds. Players offer their own active-patron favor to an anchor, which tracks the public contribution and pantheon dominance. Odysseia remains responsible for boss encounters; DiosesDrakes owns favor, skills, upkeep and Convergence state.

## Skills and safety

- Every patron path presents its nodes in the active-path menu, including state, type, tier, prerequisites and unlock information.
- Skills must be unlocked and equipped; players are not meant to have every ability active at once.
- Cooldowns, passives and ability execution are owned by the divine services and are audited.
- Normal claims are the default safety boundary. The dedicated divine PvP experience belongs in a deliberately configured arena, not ordinary protected survival land.
- Slimefun integration is limited to supported energy or machine bridges. DiosesDrakes never mutates Slimefun core storage.

## Arcana bridge

ArcanaDrakes may read the public `DivineAccess` Bukkit service to display a player's patron and current favor, then award a small Arcana-only resonance bonus for configured compatible pairings. Arcana never writes divine data, creates favor, spends favor or switches patrons.

## Configuration and operations

Routine balance belongs in `plugins/DiosesDrakes/config.yml`: economy/upkeep, Convergence anchors, boss-favor multipliers, protection integration and enabled subsystems. Keep secrets and production databases out of Git.

```bash
mvn test
mvn package
```

Back up the current JAR, config and `diosesdrakes.db` before deployment. Replace one JAR during a planned restart, then verify startup logs, `/dioses ayuda`, `/dioses estado`, an active skill menu and a non-commercial boss reward test.

## Authorship

Created for DrakesCraft by **JackStar**.

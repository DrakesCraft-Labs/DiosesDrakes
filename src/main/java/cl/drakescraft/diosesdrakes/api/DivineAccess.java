package cl.drakescraft.diosesdrakes.api;

import cl.drakescraft.diosesdrakes.model.GodId;

import java.util.Optional;
import java.util.UUID;

/** Stable Bukkit service consumed by Odysseia, SF Core and trusted addons. */
public interface DivineAccess {
    Optional<GodId> activeGod(UUID playerId);

    boolean hasEquippedSkill(UUID playerId, String skillId);

    /**
     * Awards favor for an external boss result. Implementations must make this idempotent
     * using the boss instance and player identifiers, because callers may retry after failures.
     */
    DivineBossReward rewardBossVictory(DivineBossVictory victory);

    /** Returns the favor accumulated on the player's currently selected divine branch. */
    int currentFavor(UUID playerId);
}

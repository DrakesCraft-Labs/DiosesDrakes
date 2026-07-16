package cl.drakescraft.diosesdrakes.api;

import cl.drakescraft.diosesdrakes.model.GodId;

import java.util.Optional;
import java.util.UUID;

/** Stable Bukkit service consumed by Odysseia, SF Core and trusted addons. */
public interface DivineAccess {
    Optional<GodId> activeGod(UUID playerId);

    boolean hasEquippedSkill(UUID playerId, String skillId);
}

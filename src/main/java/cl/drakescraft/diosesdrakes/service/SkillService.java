package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.time.Duration;
import java.util.UUID;
import java.util.Set;

/** Central authorization point for every divine effect. */
public final class SkillService {
    private final DivineRepository repository;
    private final ProfileService profiles;
    private final LoadoutService loadout;
    private final CooldownService cooldowns;

    public SkillService(DivineRepository repository, ProfileService profiles, LoadoutService loadout, CooldownService cooldowns) {
        this.repository = repository;
        this.profiles = profiles;
        this.loadout = loadout;
        this.cooldowns = cooldowns;
    }

    public void grant(UUID playerId, String skillId) throws SQLException {
        SkillDefinition skill = definition(skillId);
        DivineProfile profile = profiles.profile(playerId);
        if (profile.activeGod() != skill.god()) {
            throw new IllegalStateException("La habilidad no pertenece al dios activo del jugador.");
        }
        repository.unlockSkill(playerId, skill.god(), skill.id(), Instant.now());
    }

    public void equip(UUID playerId, String skillId) throws SQLException {
        SkillDefinition skill = definition(skillId);
        if (!isUnlockedForActiveGod(playerId, skill)) {
            throw new IllegalStateException("Debes desbloquear esta habilidad antes de equiparla.");
        }
        loadout.equip(playerId, skill.id());
    }

    public void unequip(UUID playerId, String skillId) throws SQLException {
        loadout.unequip(playerId, skillId);
    }

    /** Returns the persisted loadout so the GUI can describe the player's real state. */
    public Set<String> equipped(UUID playerId) throws SQLException {
        return loadout.equipped(playerId);
    }

    /** Checks ownership without silently equipping or activating the blessing. */
    public boolean isUnlocked(UUID playerId, String skillId) throws SQLException {
        return repository.hasUnlockedSkill(playerId, definition(skillId).id());
    }

    public boolean isEquippedAndUsable(UUID playerId, String skillId) {
        try {
            SkillDefinition skill = definition(skillId);
            DivineProfile profile = profiles.profile(playerId);
            return !profile.upkeepSuspended() && isUnlockedForActiveGod(playerId, skill)
                    && loadout.equipped(playerId).contains(skill.id());
        } catch (SQLException | IllegalArgumentException exception) {
            return false;
        }
    }

    public ActivationResult tryActivate(UUID playerId, String skillId, Instant now) {
        SkillDefinition skill = definition(skillId);
        if (!isEquippedAndUsable(playerId, skillId)) {
            return ActivationResult.denied("La habilidad no esta equipada o disponible.");
        }
        if (skill.cooldownSeconds() <= 0) {
            return ActivationResult.denied("Esta bendicion es pasiva.");
        }
        Duration cooldown = Duration.ofSeconds(skill.cooldownSeconds());
        if (!cooldowns.tryStart(playerId, skill.id(), cooldown, now)) {
            long seconds = Math.max(1, cooldowns.remaining(playerId, skill.id(), now).toSeconds());
            return ActivationResult.denied("Cooldown activo: " + seconds + "s.");
        }
        return ActivationResult.started(skill.durationSeconds());
    }

    private boolean isUnlockedForActiveGod(UUID playerId, SkillDefinition skill) throws SQLException {
        DivineProfile profile = profiles.profile(playerId);
        return profile.activeGod() == skill.god() && repository.hasUnlockedSkill(playerId, skill.id());
    }

    private SkillDefinition definition(String skillId) {
        return SkillCatalog.find(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad desconocida."));
    }

    public record ActivationResult(boolean started, int durationSeconds, String message) {
        static ActivationResult started(int durationSeconds) {
            return new ActivationResult(true, durationSeconds, "Habilidad activada.");
        }

        static ActivationResult denied(String message) {
            return new ActivationResult(false, 0, message);
        }
    }
}

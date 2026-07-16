package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

/** Central authorization point for every divine effect. */
public final class SkillService {
    private final DivineRepository repository;
    private final ProfileService profiles;
    private final LoadoutService loadout;

    public SkillService(DivineRepository repository, ProfileService profiles, LoadoutService loadout) {
        this.repository = repository;
        this.profiles = profiles;
        this.loadout = loadout;
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

    private boolean isUnlockedForActiveGod(UUID playerId, SkillDefinition skill) throws SQLException {
        DivineProfile profile = profiles.profile(playerId);
        return profile.activeGod() == skill.god() && repository.hasUnlockedSkill(playerId, skill.id());
    }

    private SkillDefinition definition(String skillId) {
        return SkillCatalog.find(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad desconocida."));
    }
}

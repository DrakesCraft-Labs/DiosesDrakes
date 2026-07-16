package cl.drakescraft.diosesdrakes.api;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public final class DiosesDrakesAccess implements DivineAccess {
    private final ProfileService profiles;
    private final SkillService skills;

    public DiosesDrakesAccess(ProfileService profiles, SkillService skills) {
        this.profiles = profiles;
        this.skills = skills;
    }

    @Override
    public Optional<GodId> activeGod(UUID playerId) {
        try {
            return profiles.profile(playerId).activeGodOptional();
        } catch (SQLException exception) {
            return Optional.empty();
        }
    }

    @Override
    public boolean hasEquippedSkill(UUID playerId, String skillId) {
        return skills.isEquippedAndUsable(playerId, skillId);
    }
}

package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.model.SkillType;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Limits equipped skills so a player cannot keep every blessing active. */
public final class LoadoutService {
    private static final EnumMap<SkillType, Integer> LIMITS = new EnumMap<>(SkillType.class);
    static {
        LIMITS.put(SkillType.PASSIVE, 2);
        LIMITS.put(SkillType.ACTIVE, 2);
        LIMITS.put(SkillType.STANCE, 1);
    }

    private final DivineRepository repository;

    public LoadoutService(DivineRepository repository) {
        this.repository = repository;
    }

    public Set<String> equipped(UUID playerId) throws SQLException {
        return repository.loadout(playerId);
    }

    public void equip(UUID playerId, String skillId) throws SQLException {
        SkillDefinition requested = SkillCatalog.find(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Habilidad desconocida."));
        Set<String> equipped = new HashSet<>(repository.loadout(playerId));
        if (equipped.contains(requested.id())) {
            return;
        }
        long sameType = equipped.stream().map(SkillCatalog::find).flatMap(java.util.Optional::stream)
                .filter(skill -> skill.type() == requested.type()).count();
        if (sameType >= LIMITS.get(requested.type())) {
            throw new IllegalStateException("Limite de " + requested.type() + " alcanzado.");
        }
        equipped.add(requested.id());
        repository.replaceLoadout(playerId, equipped);
    }

    public void unequip(UUID playerId, String skillId) throws SQLException {
        Set<String> equipped = new HashSet<>(repository.loadout(playerId));
        equipped.remove(skillId);
        repository.replaceLoadout(playerId, equipped);
    }
}

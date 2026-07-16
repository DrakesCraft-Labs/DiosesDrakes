package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void hephaestusBlessingMustBeUnlockedAndEquippedBeforeItWorks() throws Exception {
        UUID playerId = UUID.randomUUID();
        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            ProfileService profiles = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));
            SkillService skills = new SkillService(repository, profiles, new LoadoutService(repository), new CooldownService());
            profiles.selectGod(playerId, GodId.HEPHAESTUS, Instant.now());

            assertFalse(skills.isEquippedAndUsable(playerId, "hephaestus.forja_viva"));
            skills.grant(playerId, "hephaestus.forja_viva");
            assertFalse(skills.isEquippedAndUsable(playerId, "hephaestus.forja_viva"));
            skills.equip(playerId, "hephaestus.forja_viva");
            assertTrue(skills.isEquippedAndUsable(playerId, "hephaestus.forja_viva"));
        }
    }

    @Test
    void activeBlessingsRespectTheirCooldown() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-16T20:00:00Z");
        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("cooldown.db"))) {
            ProfileService profiles = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));
            SkillService skills = new SkillService(repository, profiles, new LoadoutService(repository), new CooldownService());
            profiles.selectGod(playerId, GodId.HEPHAESTUS, now);
            skills.grant(playerId, "hephaestus.pulso_de_red");
            skills.equip(playerId, "hephaestus.pulso_de_red");

            assertTrue(skills.tryActivate(playerId, "hephaestus.pulso_de_red", now).started());
            assertFalse(skills.tryActivate(playerId, "hephaestus.pulso_de_red", now.plusSeconds(10)).started());
            assertTrue(skills.tryActivate(playerId, "hephaestus.pulso_de_red", now.plusSeconds(46)).started());
        }
    }

    @Test
    void thirdPassiveCannotBeEquipped() throws Exception {
        UUID playerId = UUID.randomUUID();
        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            LoadoutService loadout = new LoadoutService(repository);
            repository.findOrCreate(playerId);
            loadout.equip(playerId, "hephaestus.forja_viva");
            loadout.equip(playerId, "zeus.chispa_regia");

            assertThrows(IllegalStateException.class, () -> loadout.equip(playerId, "hera.velo_del_hogar"));
        }
    }
}

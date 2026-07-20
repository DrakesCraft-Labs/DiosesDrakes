package cl.drakescraft.diosesdrakes.catalog;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillCatalogTest {
    @Test
    void everyGodAndTitanHasAFifteenNodeProgression() {
        for (GodId god : GodId.values()) {
            assertEquals(15, SkillCatalog.forGod(god).size(), god + " needs a fifteen-node path");
        }
    }

    @Test
    void everyPermanentPantheonHasDistinctPatronsAndCompleteBranches() {
        for (var pantheon : cl.drakescraft.diosesdrakes.model.PantheonId.values()) {
            var patrons = java.util.Arrays.stream(GodId.values()).filter(god -> god.pantheon() == pantheon).toList();
            assertTrue(patrons.size() >= 5, pantheon + " needs enough patron choice to be meaningful");
            assertTrue(patrons.stream().allMatch(god -> SkillCatalog.forGod(god).size() == 15));
        }
    }

    @Test
    void activeSkillsAlwaysDeclareCooldowns() {
        assertEquals(0, SkillCatalog.all().stream()
                .filter(skill -> skill.type() == SkillType.ACTIVE)
                .filter(skill -> skill.cooldownSeconds() <= 0)
                .count());
    }

    @Test
    void everyBranchHasAControlledActivePassiveAndStanceLoadout() {
        for (GodId god : GodId.values()) {
            var branch = SkillCatalog.forGod(god);
            assertEquals(4, branch.stream().filter(skill -> skill.type() == SkillType.PASSIVE).count());
            assertEquals(7, branch.stream().filter(skill -> skill.type() == SkillType.ACTIVE).count());
            assertEquals(4, branch.stream().filter(skill -> skill.type() == SkillType.STANCE).count());
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 8 && skill.cooldownSeconds() >= 480));
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 9 && skill.durationSeconds() > 0));
        }
    }

    @Test
    void hephaestusBranchHasCostsAndOrderedPrerequisites() {
        var forge = SkillCatalog.find("hephaestus.forja_viva").orElseThrow();
        var pulse = SkillCatalog.find("hephaestus.pulso_de_red").orElseThrow();
        var sight = SkillCatalog.find("hephaestus.ojo_de_mena").orElseThrow();

        assertTrue(forge.unlockCost() > 0);
        assertEquals(java.util.List.of(forge.id()), pulse.prerequisites());
        assertEquals(java.util.List.of(pulse.id()), sight.prerequisites());
    }

    @Test
    void canonicalTitansHaveCompletePaidBranches() {
        var titans = java.util.List.of(
                GodId.OCEANUS, GodId.COEUS, GodId.CRIUS, GodId.HYPERION, GodId.IAPETUS, GodId.CRONUS,
                GodId.THEIA, GodId.RHEA, GodId.THEMIS, GodId.MNEMOSYNE, GodId.PHOEBE, GodId.TETHYS
        );
        assertEquals(12, titans.size());
        for (GodId titan : titans) {
            var branch = SkillCatalog.forGod(titan);
            assertEquals(15, branch.size(), titan + " needs a complete fifteen-node branch");
            assertTrue(branch.stream().allMatch(skill -> skill.unlockCost() > 0));
            assertTrue(branch.stream().filter(skill -> skill.tier() > 1).allMatch(skill -> !skill.prerequisites().isEmpty()));
        }
    }

    @Test
    void completeAscensionIsARealLateGameMoneySink() {
        double total = SkillCatalog.forGod(GodId.ZEUS).stream().mapToDouble(skill -> skill.unlockCost()).sum();
        assertTrue(total >= 360_000_000.0D);
        assertEquals(36_000_000.0D, SkillCatalog.forGod(GodId.ZEUS).stream()
                .filter(skill -> skill.tier() == 10).findFirst().orElseThrow().unlockCost());
    }

    @Test
    void everyPatronHasFistWeaponDashAndGuardTechniques() {
        for (GodId god : GodId.values()) {
            var branch = SkillCatalog.forGod(god);
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 11 && skill.id().endsWith("_punos")));
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 12 && skill.id().endsWith("_arma")));
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 13 && skill.id().endsWith("_carrera")));
            assertTrue(branch.stream().anyMatch(skill -> skill.tier() == 14 && skill.id().endsWith("_guardia")));
        }
    }
}

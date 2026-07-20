package cl.drakescraft.diosesdrakes.catalog;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillCatalogTest {
    @Test
    void everyGodAndTitanHasATenNodeProgression() {
        for (GodId god : GodId.values()) {
            assertEquals(10, SkillCatalog.forGod(god).size(), god + " needs a ten-node path");
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
            assertEquals(3, branch.stream().filter(skill -> skill.type() == SkillType.PASSIVE).count());
            assertEquals(4, branch.stream().filter(skill -> skill.type() == SkillType.ACTIVE).count());
            assertEquals(3, branch.stream().filter(skill -> skill.type() == SkillType.STANCE).count());
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
            assertEquals(10, branch.size(), titan + " needs a complete ten-node branch");
            assertTrue(branch.stream().allMatch(skill -> skill.unlockCost() > 0));
            assertTrue(branch.stream().filter(skill -> skill.tier() > 1).allMatch(skill -> !skill.prerequisites().isEmpty()));
        }
    }

    @Test
    void completeAscensionIsARealLateGameMoneySink() {
        double total = SkillCatalog.forGod(GodId.ZEUS).stream().mapToDouble(skill -> skill.unlockCost()).sum();
        assertTrue(total >= 70_000_000.0D);
        assertEquals(36_000_000.0D, SkillCatalog.forGod(GodId.ZEUS).stream()
                .filter(skill -> skill.tier() == 10).findFirst().orElseThrow().unlockCost());
    }
}

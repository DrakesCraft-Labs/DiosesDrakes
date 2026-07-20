package cl.drakescraft.diosesdrakes.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpkeepCostPolicyTest {
    @Test
    void maintenanceScalesWithActualUnlockedPowerAndHonoursTheCap() {
        double early = UpkeepCostPolicy.calculate(5_000.0D, 2.5D, 2_500_000.0D,
                Set.of("hephaestus.forja_viva", "hephaestus.pulso_de_red"));
        double complete = UpkeepCostPolicy.calculate(5_000.0D, 2.5D, 2_500_000.0D,
                cl.drakescraft.diosesdrakes.catalog.SkillCatalog.forGod(
                        cl.drakescraft.diosesdrakes.model.GodId.HEPHAESTUS).stream().map(skill -> skill.id()).collect(java.util.stream.Collectors.toSet()));

        assertEquals(5_000.0D + Set.of("hephaestus.forja_viva", "hephaestus.pulso_de_red").stream()
                .flatMap(skillId -> cl.drakescraft.diosesdrakes.catalog.SkillCatalog.find(skillId).stream())
                .mapToDouble(skill -> skill.unlockCost()).sum() * 0.025D, early);
        assertTrue(complete > 1_750_000.0D);
        assertTrue(complete <= 2_500_000.0D);
    }
}

package cl.drakescraft.diosesdrakes.catalog;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillCatalogTest {
    @Test
    void everyInitialGodHasSeveralDocumentedSkills() {
        for (GodId god : GodId.values()) {
            assertTrue(SkillCatalog.forGod(god).size() >= 3, god + " needs several skills");
        }
    }

    @Test
    void activeSkillsAlwaysDeclareCooldowns() {
        assertEquals(0, SkillCatalog.all().stream()
                .filter(skill -> skill.type() == SkillType.ACTIVE)
                .filter(skill -> skill.cooldownSeconds() <= 0)
                .count());
    }
}

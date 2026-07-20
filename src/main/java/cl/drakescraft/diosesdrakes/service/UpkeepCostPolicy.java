package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;

import java.util.Set;

/** Calculates an explicit weekly offering from the power the player has actually unlocked. */
public final class UpkeepCostPolicy {
    private UpkeepCostPolicy() {
    }

    public static double calculate(double baseCost, double investedPercent, double maxCost, Set<String> unlockedSkillIds) {
        double invested = unlockedSkillIds.stream()
                .flatMap(skillId -> SkillCatalog.find(skillId).stream())
                .mapToDouble(skill -> skill.unlockCost())
                .sum();
        double cost = Math.max(0.0D, baseCost) + Math.max(0.0D, investedPercent) / 100.0D * invested;
        return Math.min(Math.max(0.0D, maxCost), cost);
    }
}

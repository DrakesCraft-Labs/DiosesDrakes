package cl.drakescraft.diosesdrakes.model;

import java.util.List;

public record SkillDefinition(
        String id,
        GodId god,
        String name,
        SkillType type,
        String description,
        int cooldownSeconds,
        int durationSeconds,
        int tier,
        double unlockCost,
        List<String> prerequisites
) {
    public SkillDefinition {
        if (unlockCost < 0) {
            throw new IllegalArgumentException("unlockCost cannot be negative");
        }
        prerequisites = List.copyOf(prerequisites);
    }

    public String informationLine() {
        String cooldown = cooldownSeconds > 0 ? cooldownSeconds + "s de cooldown" : "sin cooldown";
        String duration = durationSeconds > 0 ? durationSeconds + "s de duracion" : "efecto permanente al equipar";
        return type + " | " + cooldown + " | " + duration;
    }

    public String unlockInformation() {
        return unlockCost > 0 ? "Ofrenda: " + unlockCost + " monedas" : "Sin ofrenda monetaria";
    }
}

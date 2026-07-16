package cl.drakescraft.diosesdrakes.model;

public record SkillDefinition(
        String id,
        GodId god,
        String name,
        SkillType type,
        String description,
        int cooldownSeconds,
        int durationSeconds,
        int tier
) {
    public String informationLine() {
        String cooldown = cooldownSeconds > 0 ? cooldownSeconds + "s de cooldown" : "sin cooldown";
        String duration = durationSeconds > 0 ? durationSeconds + "s de duracion" : "efecto permanente al equipar";
        return type + " | " + cooldown + " | " + duration;
    }
}

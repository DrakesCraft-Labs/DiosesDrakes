package cl.drakescraft.diosesdrakes.service;

import org.bukkit.entity.Player;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Writes compact activation records for staff review without flooding the server log. */
public final class DivineAbilityAuditLogger {
    private final Path directory;

    public DivineAbilityAuditLogger(Path directory) {
        this.directory = directory;
    }

    public void record(Player player, String skillId, String result) {
        String line = "{\"at\":\"" + java.time.Instant.now() + "\",\"player\":\""
                + player.getUniqueId() + "\",\"skill\":\"" + escape(skillId) + "\",\"result\":\""
                + escape(result) + "\"}" + System.lineSeparator();
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("abilities-" + LocalDate.now(ZoneOffset.UTC) + ".jsonl"), line,
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // Gameplay remains available; economic transactions keep their own durable audit path.
        }
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
    }
}

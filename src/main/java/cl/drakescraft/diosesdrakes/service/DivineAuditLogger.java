package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.TransactionState;
import cl.drakescraft.diosesdrakes.model.TransactionType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Writes one compact JSON object per transaction without console spam. */
public final class DivineAuditLogger {
    private final Path directory;
    private final Object lock = new Object();

    public DivineAuditLogger(Path directory) {
        this.directory = directory;
    }

    public void record(UUID transactionId, UUID playerId, TransactionType type,
                       TransactionState state, double amount, String detail) {
        String line = "{"
                + "\"timestamp\":\"" + escape(OffsetDateTime.now().toString()) + "\","
                + "\"transaction_id\":\"" + transactionId + "\","
                + "\"player_uuid\":\"" + playerId + "\","
                + "\"type\":\"" + type + "\","
                + "\"state\":\"" + state + "\","
                + "\"amount\":" + amount + ","
                + "\"detail\":\"" + escape(detail) + "\""
                + "}" + System.lineSeparator();

        synchronized (lock) {
            try {
                Files.createDirectories(directory);
                Files.writeString(directory.resolve("transactions-" + LocalDate.now() + ".jsonl"), line,
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException exception) {
                throw new IllegalStateException("No se pudo escribir la auditoria divina", exception);
            }
        }
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }
}

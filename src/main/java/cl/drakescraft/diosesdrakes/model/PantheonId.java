package cl.drakescraft.diosesdrakes.model;

import java.util.Locale;
import java.util.Optional;

/** A durable cultural pantheon. New expansions extend this enum without flattening the selection UI. */
public enum PantheonId {
    GREEK("Olimpo y Titanes", "Orden, destino, mar, guerra y submundo."),
    NORDIC("Asgard", "Runas, juramentos, tormenta, caceria y Ragnarok."),
    EGYPTIAN("Duat", "Sol, juicio, muerte, desierto y renacimiento."),
    CELTIC("Tuatha de Danann", "Naturaleza viva, bruma, caza y pactos." );

    private final String displayName;
    private final String description;

    PantheonId(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public static Optional<PantheonId> fromStorage(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(valueOf(value.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}

package cl.drakescraft.diosesdrakes.model;

import java.util.Locale;
import java.util.Optional;

public enum GodId {
    HEPHAESTUS("Hefesto");

    private final String displayName;

    GodId(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Optional<GodId> fromStorage(String value) {
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

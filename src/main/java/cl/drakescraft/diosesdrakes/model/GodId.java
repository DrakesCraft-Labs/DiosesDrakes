package cl.drakescraft.diosesdrakes.model;

import java.util.Locale;
import java.util.Optional;

public enum GodId {
    ZEUS("Zeus"), HERA("Hera"), POSEIDON("Poseidon"), DEMETER("Demeter"),
    ATHENA("Atenea"), APOLLO("Apolo"), ARTEMIS("Artemisa"), ARES("Ares"),
    APHRODITE("Afrodita"), HEPHAESTUS("Hefesto"), HERMES("Hermes"), HESTIA("Hestia"),
    HADES("Hades"), PERSEPHONE("Persefone"), HECATE("Hecate"), DIONYSUS("Dionisio"),
    EROS("Eros"), NIKE("Nike"), NEMESIS("Nemesis"), MORPHEUS("Morfeo"),
    HELIOS("Helios"), SELENE("Selene"), TYCHE("Tique");

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

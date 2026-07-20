package cl.drakescraft.diosesdrakes.model;

import java.util.Locale;
import java.util.Optional;

public enum GodId {
    ZEUS("Zeus", PantheonId.GREEK), HERA("Hera", PantheonId.GREEK), POSEIDON("Poseidon", PantheonId.GREEK), DEMETER("Demeter", PantheonId.GREEK),
    ATHENA("Atenea"), APOLLO("Apolo"), ARTEMIS("Artemisa"), ARES("Ares"),
    APHRODITE("Afrodita"), HEPHAESTUS("Hefesto"), HERMES("Hermes"), HESTIA("Hestia"),
    HADES("Hades", PantheonId.GREEK), PERSEPHONE("Persefone", PantheonId.GREEK), HECATE("Hecate", PantheonId.GREEK), DIONYSUS("Dionisio", PantheonId.GREEK),
    EROS("Eros"), NIKE("Nike"), NEMESIS("Nemesis"), MORPHEUS("Morfeo"),
    HELIOS("Helios"), SELENE("Selene"), TYCHE("Tique"),
    OCEANUS("Oceano", PantheonId.GREEK), COEUS("Ceo", PantheonId.GREEK), CRIUS("Crio", PantheonId.GREEK), HYPERION("Hiperion", PantheonId.GREEK),
    IAPETUS("Japeto"), CRONUS("Cronos"), THEIA("Tea"), RHEA("Rea"),
    THEMIS("Temis", PantheonId.GREEK), MNEMOSYNE("Mnemosine", PantheonId.GREEK), PHOEBE("Febe", PantheonId.GREEK), TETHYS("Tetis", PantheonId.GREEK),

    THOR("Thor", PantheonId.NORDIC), ODIN("Odin", PantheonId.NORDIC), LOKI("Loki", PantheonId.NORDIC),
    HEIMDALL("Heimdall", PantheonId.NORDIC), FREYJA("Freyja", PantheonId.NORDIC), TYR("Tyr", PantheonId.NORDIC),

    RA("Ra", PantheonId.EGYPTIAN), ANUBIS("Anubis", PantheonId.EGYPTIAN), ISIS("Isis", PantheonId.EGYPTIAN),
    SET("Set", PantheonId.EGYPTIAN), BASTET("Bastet", PantheonId.EGYPTIAN), HORUS("Horus", PantheonId.EGYPTIAN),

    MORRIGAN("Morrigan", PantheonId.CELTIC), LUGH("Lugh", PantheonId.CELTIC), BRIGID("Brigid", PantheonId.CELTIC),
    CERNUNNOS("Cernunnos", PantheonId.CELTIC), DAGDA("Dagda", PantheonId.CELTIC);

    private final String displayName;
    private final PantheonId pantheon;

    GodId(String displayName) {
        this(displayName, PantheonId.GREEK);
    }

    GodId(String displayName, PantheonId pantheon) {
        this.displayName = displayName;
        this.pantheon = pantheon;
    }

    public String displayName() {
        return displayName;
    }

    public PantheonId pantheon() {
        return pantheon;
    }

    public boolean isTitan() {
        return pantheon == PantheonId.GREEK && ordinal() >= OCEANUS.ordinal() && ordinal() <= TETHYS.ordinal();
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

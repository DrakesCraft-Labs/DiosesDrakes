package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ProfileService {
    private static final ZoneId ZONA_CLT = ZoneId.of("America/Santiago");
    private static final DateTimeFormatter FORMATO_CLT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZONA_CLT);

    private final DivineRepository repository;
    private final Duration renunciationCooldown;
    private final Duration upkeepPeriod;

    public ProfileService(DivineRepository repository, Duration renunciationCooldown, Duration upkeepPeriod) {
        this.repository = repository;
        this.renunciationCooldown = renunciationCooldown;
        this.upkeepPeriod = upkeepPeriod;
    }

    public DivineProfile profile(UUID playerId) throws SQLException {
        return repository.findOrCreate(playerId);
    }

    public DivineProfile selectGod(UUID playerId, GodId god, Instant now) throws SQLException {
        DivineProfile profile = profile(playerId);
        if (!profile.canChooseGod(now)) {
            if (profile.activeGod() != null) {
                throw new IllegalStateException("Ya tienes un dios activo: " + profile.activeGod().displayName() + ".");
            }
            throw new IllegalStateException("Todavia sigues en cooldown de renuncia. Podras elegir de nuevo "
                    + describeRemaining(profile, now) + ".");
        }

        repository.selectGod(playerId, god, now, now.plus(upkeepPeriod));
        return repository.find(playerId).orElseThrow(() -> new SQLException("Profile disappeared after selection"));
    }

    public DivineProfile renounce(UUID playerId, Instant now) throws SQLException {
        DivineProfile profile = profile(playerId);
        if (!profile.canRenounce(now)) {
            if (profile.activeGod() == null) {
                throw new IllegalStateException("No tienes un dios activo al cual renunciar.");
            }
            throw new IllegalStateException("La renuncia todavia no esta disponible. Podras renunciar de nuevo "
                    + describeRemaining(profile, now) + ".");
        }

        repository.renounceGod(playerId, now.plus(renunciationCooldown));
        return repository.find(playerId).orElseThrow(() -> new SQLException("Profile disappeared after renunciation"));
    }

    public static String formatClt(Instant instant) {
        return FORMATO_CLT.format(instant) + " CLT";
    }

    private static String describeRemaining(DivineProfile profile, Instant now) {
        Duration remaining = profile.cooldownRemaining(now);
        long horas = remaining.toHours();
        long minutos = remaining.toMinutesPart();
        return "en " + horas + "h " + minutos + "m (" + FORMATO_CLT.format(profile.renounceAvailableAt()) + " CLT)";
    }
}

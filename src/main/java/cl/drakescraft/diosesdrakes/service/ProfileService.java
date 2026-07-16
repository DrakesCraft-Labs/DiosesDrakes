package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public final class ProfileService {
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
            throw new IllegalStateException("El jugador ya tiene un dios activo o sigue en cooldown.");
        }

        repository.selectGod(playerId, god, now, now.plus(upkeepPeriod));
        return repository.find(playerId).orElseThrow(() -> new SQLException("Profile disappeared after selection"));
    }

    public DivineProfile renounce(UUID playerId, Instant now) throws SQLException {
        DivineProfile profile = profile(playerId);
        if (!profile.canRenounce(now)) {
            throw new IllegalStateException("La renuncia todavia no esta disponible.");
        }

        repository.renounceGod(playerId, now.plus(renunciationCooldown));
        return repository.find(playerId).orElseThrow(() -> new SQLException("Profile disappeared after renunciation"));
    }
}

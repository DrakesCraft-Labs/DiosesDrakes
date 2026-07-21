package cl.drakescraft.diosesdrakes.service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks active-skill cooldowns without scheduling one task per player. */
public final class CooldownService {
    private final Map<UUID, Map<String, Instant>> expiryByPlayer = new HashMap<>();

    public synchronized Duration remaining(UUID playerId, String skillId, Instant now) {
        Instant expiry = expiryByPlayer.getOrDefault(playerId, Map.of()).get(skillId);
        if (expiry == null || !now.isBefore(expiry)) {
            return Duration.ZERO;
        }
        return Duration.between(now, expiry);
    }

    public synchronized boolean tryStart(UUID playerId, String skillId, Duration cooldown, Instant now) {
        if (!remaining(playerId, skillId, now).isZero()) {
            return false;
        }
        expiryByPlayer.computeIfAbsent(playerId, ignored -> new HashMap<>())
                .put(skillId, now.plus(cooldown));
        return true;
    }

    public synchronized void clear(UUID playerId) {
        expiryByPlayer.remove(playerId);
    }
}

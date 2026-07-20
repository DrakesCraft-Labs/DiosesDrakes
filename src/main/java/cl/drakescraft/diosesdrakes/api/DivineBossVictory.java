package cl.drakescraft.diosesdrakes.api;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Immutable, cross-plugin description of one player's contribution to a boss defeat. */
public record DivineBossVictory(UUID bossInstanceId, UUID playerId, String bossId,
                                double contribution, double totalContribution,
                                int participantCount, Instant defeatedAt) {
    public DivineBossVictory {
        Objects.requireNonNull(bossInstanceId, "bossInstanceId");
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(bossId, "bossId");
        Objects.requireNonNull(defeatedAt, "defeatedAt");
        if (bossId.isBlank() || bossId.length() > 64) {
            throw new IllegalArgumentException("bossId must contain between 1 and 64 characters");
        }
        if (!Double.isFinite(contribution) || contribution < 0.0D
                || !Double.isFinite(totalContribution) || totalContribution < 0.0D
                || participantCount < 1) {
            throw new IllegalArgumentException("Boss contribution values are invalid");
        }
    }

    public double contributionShare() {
        return totalContribution <= 0.0D ? 1.0D : Math.min(1.0D, contribution / totalContribution);
    }
}

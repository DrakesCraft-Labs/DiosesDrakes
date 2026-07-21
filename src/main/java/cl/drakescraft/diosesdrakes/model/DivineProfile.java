package cl.drakescraft.diosesdrakes.model;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public record DivineProfile(
        UUID playerId,
        GodId activeGod,
        Instant selectedAt,
        Instant renounceAvailableAt,
        Instant upkeepDueAt,
        boolean upkeepSuspended
) {
    public DivineProfile {
        if (playerId == null) {
            throw new IllegalArgumentException("playerId is required");
        }
    }

    public Optional<GodId> activeGodOptional() {
        return Optional.ofNullable(activeGod);
    }

    public boolean canChooseGod(Instant now) {
        return activeGod == null && (renounceAvailableAt == null || !now.isBefore(renounceAvailableAt));
    }

    public boolean canRenounce(Instant now) {
        return activeGod != null && (renounceAvailableAt == null || !now.isBefore(renounceAvailableAt));
    }
}

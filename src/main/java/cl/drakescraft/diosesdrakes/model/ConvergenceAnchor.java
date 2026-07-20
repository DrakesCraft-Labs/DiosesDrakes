package cl.drakescraft.diosesdrakes.model;

import java.time.Instant;
import java.util.Map;

/** Immutable public anchor state. It has no owner and never modifies blocks or claims. */
public record ConvergenceAnchor(
        String id,
        String worldName,
        int blockX,
        int blockY,
        int blockZ,
        PantheonId dominantPantheon,
        Instant createdAt,
        Instant updatedAt,
        Map<PantheonId, Integer> offerings
) {
    public int favorOf(PantheonId pantheon) {
        return offerings.getOrDefault(pantheon, 0);
    }
}

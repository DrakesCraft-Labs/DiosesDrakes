package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.PantheonId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConvergenceServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void offeringDebitsOnlyTheActiveBranchAndCanFlipAPermanentAnchor() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-20T16:00:00Z");
        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("convergence.db"))) {
            ProfileService profiles = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));
            profiles.selectGod(playerId, GodId.THOR, now);
            repository.awardBossFavor(UUID.randomUUID(), playerId, GodId.THOR, "thor", 100, 1, 1, now);

            ConvergenceService convergence = new ConvergenceService(repository, profiles, true, 3, 25, 20);
            convergence.createAnchor("valhalla", "pvpdivino", 10, 80, -10, PantheonId.GREEK, now);
            ConvergenceService.OfferResult offer = convergence.offer(playerId, "valhalla", 50, now.plusSeconds(1));

            assertTrue(offer.dominanceChanged());
            assertEquals(PantheonId.NORDIC, offer.anchor().dominantPantheon());
            assertEquals(50, offer.anchor().favorOf(PantheonId.NORDIC));
            assertEquals(50, repository.favor(playerId, GodId.THOR));
            assertThrows(IllegalStateException.class, () -> convergence.offer(playerId, "valhalla", 75, now.plusSeconds(2)));
        }
    }
}

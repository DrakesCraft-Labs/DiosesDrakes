package cl.drakescraft.diosesdrakes.storage;

import cl.drakescraft.diosesdrakes.model.GodId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DivineBossFavorTest {
    @TempDir
    Path tempDirectory;

    @Test
    void bossFavorIsIdempotentAndRenunciationClearsBranchProgress() throws Exception {
        UUID playerId = UUID.randomUUID();
        UUID bossId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-20T16:00:00Z");
        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("favor.db"))) {
            repository.findOrCreate(playerId);
            repository.selectGod(playerId, GodId.ZEUS, now, now.plusSeconds(604800));

            assertTrue(repository.awardBossFavor(bossId, playerId, GodId.ZEUS, "thor", 90, 100.0D, 100.0D, now).granted());
            assertFalse(repository.awardBossFavor(bossId, playerId, GodId.ZEUS, "thor", 90, 100.0D, 100.0D, now).granted());
            assertEquals(90, repository.favor(playerId, GodId.ZEUS));

            repository.renounceGod(playerId, now.plusSeconds(172800));
            assertEquals(0, repository.favor(playerId, GodId.ZEUS));
        }
    }
}

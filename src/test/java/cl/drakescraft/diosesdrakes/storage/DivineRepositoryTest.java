package cl.drakescraft.diosesdrakes.storage;

import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.TransactionState;
import cl.drakescraft.diosesdrakes.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DivineRepositoryTest {
    @TempDir
    Path tempDirectory;

    @Test
    void selectionAndRenunciationPersistExpectedState() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-16T18:00:00Z");

        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            DivineProfile created = repository.findOrCreate(playerId);
            assertTrue(created.activeGodOptional().isEmpty());

            repository.selectGod(playerId, GodId.HEPHAESTUS, now, now.plusSeconds(604800));
            repository.unlockSkill(playerId, GodId.HEPHAESTUS, "hephaestus.forja_viva", now);
            repository.replaceLoadout(playerId, Set.of("hephaestus.forja_viva"));
            assertTrue(repository.hasUnlockedSkill(playerId, "hephaestus.forja_viva"));
            DivineProfile selected = repository.find(playerId).orElseThrow();
            assertEquals(GodId.HEPHAESTUS, selected.activeGod());

            Instant cooldownUntil = now.plusSeconds(172800);
            repository.renounceGod(playerId, cooldownUntil);
            DivineProfile renounced = repository.find(playerId).orElseThrow();
            assertTrue(renounced.activeGodOptional().isEmpty());
            assertEquals(cooldownUntil, renounced.renounceAvailableAt());
            assertTrue(repository.loadout(playerId).isEmpty());
            assertFalse(renounced.canChooseGod(now));
            assertTrue(renounced.canChooseGod(cooldownUntil));
        }
    }

    @Test
    void transactionStateCanMoveFromPreparedToCommitted() throws Exception {
        UUID playerId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            repository.createPreparedTransaction(transactionId, playerId, TransactionType.SKILL_UNLOCK, 100.0, "hephaestus:test");
            repository.updateTransactionState(transactionId, TransactionState.COMMITTED, "hephaestus:test");
        }
    }
}

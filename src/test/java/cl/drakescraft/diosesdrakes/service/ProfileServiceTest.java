package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.GodId;
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

class ProfileServiceTest {
    @TempDir
    Path tempDirectory;

    @Test
    void formatCltUsesFixedOffsetRegardlessOfChileanDst() {
        // 2026-01-15 es verano chileno (America/Santiago estaria en UTC-3 con DST activo).
        // La hora canonica del proyecto es UTC-4 fija: NO debe coincidir con America/Santiago aqui.
        Instant veranoAustral = Instant.parse("2026-01-15T12:00:00Z");
        assertEquals("2026-01-15 08:00 CLT", ProfileService.formatClt(veranoAustral));

        // 2026-07-15 es invierno chileno (America/Santiago sin DST, tambien UTC-4): debe coincidir.
        Instant inviernoAustral = Instant.parse("2026-07-15T12:00:00Z");
        assertEquals("2026-07-15 08:00 CLT", ProfileService.formatClt(inviernoAustral));
    }

    @Test
    void selectGodMessageReportsSubMinuteRemainingAsZero() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-16T18:00:00Z");

        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            ProfileService service = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));
            service.selectGod(playerId, GodId.HEPHAESTUS, now);
            service.renounce(playerId, now);

            Instant justBeforeCooldownEnds = now.plusSeconds(172800).minusSeconds(30);
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> service.selectGod(playerId, GodId.HEPHAESTUS, justBeforeCooldownEnds));
            assertTrue(exception.getMessage().contains("en 0h 0m"), exception.getMessage());
        }
    }

    @Test
    void renounceWithoutActiveGodReportsClearMessage() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-16T18:00:00Z");

        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            ProfileService service = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> service.renounce(playerId, now));
            assertTrue(exception.getMessage().contains("No tienes"), exception.getMessage());
        }
    }

    @Test
    void selectGodMessageEmbedsExactClockTimeAtFixedOffset() throws Exception {
        UUID playerId = UUID.randomUUID();
        Instant now = Instant.parse("2026-07-16T18:00:00Z");

        try (DivineRepository repository = new DivineRepository(tempDirectory.resolve("dioses.db"))) {
            ProfileService service = new ProfileService(repository, Duration.ofHours(48), Duration.ofDays(7));
            service.selectGod(playerId, GodId.HEPHAESTUS, now);
            service.renounce(playerId, now);

            Instant tenHoursIntoCooldown = now.plusSeconds(36000);
            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> service.selectGod(playerId, GodId.HEPHAESTUS, tenHoursIntoCooldown));
            Instant cooldownEnd = now.plusSeconds(172800);
            assertTrue(exception.getMessage().contains("en 38h 0m"), exception.getMessage());
            assertTrue(exception.getMessage().contains(ProfileService.formatClt(cooldownEnd)), exception.getMessage());
        }
    }
}

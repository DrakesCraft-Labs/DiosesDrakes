package cl.drakescraft.diosesdrakes;

import cl.drakescraft.diosesdrakes.service.UpkeepService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.time.Instant;

/** Checks overdue upkeep as soon as the player returns, not only on the timed sweep. */
final class UpkeepListener implements Listener {
    private final UpkeepService upkeep;

    UpkeepListener(UpkeepService upkeep) {
        this.upkeep = upkeep;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (upkeep == null) {
            return;
        }
        UpkeepService.Settlement settlement = upkeep.settle(event.getPlayer(), Instant.now());
        if (settlement.status() != UpkeepService.Status.NOT_DUE && !settlement.message().isBlank()) {
            event.getPlayer().sendMessage("[Dioses] " + settlement.message());
        }
    }
}

package cl.drakescraft.diosesdrakes.listener;

import cl.drakescraft.bosses.api.BossVictoryEvent;
import cl.drakescraft.diosesdrakes.api.DivineBossVictory;
import cl.drakescraft.diosesdrakes.service.BossFavorService;
import java.util.Map;
import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Converts the public boss result into idempotent divine favor records. */
public final class BossVictoryListener implements Listener {
    private final BossFavorService favor;

    public BossVictoryListener(BossFavorService favor) {
        this.favor = favor;
    }

    @EventHandler
    public void onBossVictory(BossVictoryEvent event) {
        Map<UUID, Double> contributions = event.getContributions();
        if (contributions.isEmpty()) return;
        double total = contributions.values().stream().mapToDouble(Double::doubleValue).sum();
        int participants = contributions.size();
        contributions.forEach((playerId, contribution) -> favor.reward(new DivineBossVictory(
                event.getBossInstanceId(), playerId, event.getBossId(), contribution, total,
                participants, event.getDefeatedAt())));
    }
}

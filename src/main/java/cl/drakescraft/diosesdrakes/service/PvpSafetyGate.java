package cl.drakescraft.diosesdrakes.service;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Prevents survival powers from being activated while either player is in a PvP exchange. */
public final class PvpSafetyGate implements Listener {
    private static final Duration COMBAT_WINDOW = Duration.ofSeconds(20);
    private final Map<UUID, Instant> combatUntil = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public synchronized void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = playerFrom(event.getDamager());
        Player victim = event.getEntity() instanceof Player player ? player : null;
        if (attacker == null || victim == null || attacker.equals(victim)) {
            return;
        }
        Instant until = Instant.now().plus(COMBAT_WINDOW);
        combatUntil.put(attacker.getUniqueId(), until);
        combatUntil.put(victim.getUniqueId(), until);
    }

    public synchronized boolean inCombat(Player player, Instant now) {
        Instant until = combatUntil.get(player.getUniqueId());
        if (until == null || !now.isBefore(until)) {
            combatUntil.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    private Player playerFrom(Entity entity) {
        if (entity instanceof Player player) {
            return player;
        }
        if (entity instanceof org.bukkit.entity.Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            return shooter instanceof Player player ? player : null;
        }
        return null;
    }
}

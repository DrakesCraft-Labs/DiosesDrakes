package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.GodId;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds short-lived vanilla scenes with Paper display entities. Every scene is
 * capped, non-persistent and explicitly removed so it cannot leave entity debris.
 */
public final class DivineCinematicService implements Listener {
    private final JavaPlugin plugin;
    private final boolean displaysEnabled;
    private final int maxDisplaysPerScene;
    private final float viewRange;
    private final Map<UUID, Scene> activeScenes = new HashMap<>();

    public DivineCinematicService(JavaPlugin plugin, boolean displaysEnabled, int maxDisplaysPerScene, float viewRange) {
        this.plugin = plugin;
        this.displaysEnabled = displaysEnabled;
        this.maxDisplaysPerScene = Math.max(0, Math.min(12, maxDisplaysPerScene));
        this.viewRange = Math.max(8.0F, Math.min(64.0F, viewRange));
    }

    /** Shows the opening pulse used by every active or stance ability. */
    public void announce(Player player, GodId god) {
        Location center = player.getLocation().add(0, 1.0, 0);
        player.getWorld().spawnParticle(Particle.DUST, center, 24, 0.6, 0.7, 0.6, 0,
                new Particle.DustOptions(colorFor(god), 1.25F));
        player.getWorld().spawnParticle(Particle.ENCHANT, center, 16, 0.5, 0.65, 0.5, 0.08);
        player.playSound(center, Sound.BLOCK_BEACON_POWER_SELECT, 0.65F, god.isTitan() ? 0.76F : 1.18F);
    }

    /** A purely visual explosion. It never calls World#createExplosion and cannot alter terrain. */
    public void strike(Player player, GodId god, Monster target, double damage) {
        clear(player.getUniqueId());
        UUID sceneId = UUID.randomUUID();
        Location center = target.getLocation().add(0, Math.max(0.8, target.getHeight() * 0.5), 0);
        List<Entity> displays = new ArrayList<>();
        if (displaysEnabled) {
            int count = Math.min(4, maxDisplaysPerScene);
            for (int index = 0; index < count; index++) {
                double angle = (Math.PI * 2D * index) / Math.max(1, count);
                Location point = center.clone().add(Math.cos(angle) * 1.15D, 0.15D, Math.sin(angle) * 1.15D);
                BlockDisplay display = spawnBlock(point, materialFor(god), 0.26F);
                if (display != null) {
                    displays.add(display);
                    animate(display, 0.65F, 10, (float) (angle + Math.PI));
                }
            }
        }
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (target.isValid()) {
                target.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
                target.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, center, 48, 0.8, 1.0, 0.8, 0.15);
                target.getWorld().spawnParticle(Particle.DUST, center, 32, 0.85, 0.9, 0.85, 0,
                        new Particle.DustOptions(colorFor(god), 1.6F));
                target.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 0.58F, damage >= 100 ? 0.62F : 1.15F);
            }
            remove(displays);
            clear(player.getUniqueId(), sceneId);
        }, 12L);
        activeScenes.put(player.getUniqueId(), new Scene(sceneId, displays, task));
    }

    /** Creates a short client-side hit-stop cue around a PvE target without changing blocks or other players. */
    public void impact(Player player, GodId god, Monster target, boolean heavy) {
        Location center = target.getLocation().add(0, Math.max(0.7, target.getHeight() * 0.5), 0);
        target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, center, 1, 0, 0, 0, 0);
        target.getWorld().spawnParticle(Particle.CRIT, center, heavy ? 28 : 16, 0.45, 0.55, 0.45, 0.1);
        target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, center, heavy ? 18 : 9, 0.35, 0.45, 0.35, 0.04);
        target.getWorld().spawnParticle(Particle.DUST, center, heavy ? 18 : 10, 0.45, 0.6, 0.45, 0,
                new Particle.DustOptions(colorFor(god), heavy ? 1.45F : 1.1F));
        target.getWorld().playSound(center, heavy ? Sound.ENTITY_PLAYER_ATTACK_STRONG : Sound.ENTITY_PLAYER_ATTACK_CRIT,
                heavy ? 0.9F : 0.65F, heavy ? 0.72F : 1.18F);
    }

    /** Visual feedback for a successful guard, kept local to the defending player. */
    public void guard(Player player, GodId god) {
        Location center = player.getLocation().add(0, 1.0, 0);
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, center, 28, 0.55, 0.8, 0.55, 0.15);
        player.getWorld().spawnParticle(Particle.DUST, center, 18, 0.45, 0.7, 0.45, 0,
                new Particle.DustOptions(colorFor(god), 1.3F));
        player.playSound(center, Sound.ITEM_SHIELD_BLOCK, 0.82F, 0.82F);
    }

    /** Surrounds the player with a client-interpolated ring for a controlled duration. */
    public void domain(Player player, GodId god, int seconds) {
        clear(player.getUniqueId());
        UUID sceneId = UUID.randomUUID();
        Location center = player.getLocation().add(0, 0.15, 0);
        List<Entity> displays = new ArrayList<>();
        if (displaysEnabled) {
            int count = Math.min(8, maxDisplaysPerScene);
            for (int index = 0; index < count; index++) {
                double angle = (Math.PI * 2D * index) / Math.max(1, count);
                Location point = center.clone().add(Math.cos(angle) * 2.35D, 0.15D, Math.sin(angle) * 2.35D);
                BlockDisplay display = spawnBlock(point, materialFor(god), 0.32F);
                if (display != null) {
                    displays.add(display);
                    animate(display, 0.78F, 20, (float) (angle + Math.PI));
                }
            }
        }
        player.getWorld().spawnParticle(Particle.ENCHANT, center.add(0, 0.7, 0), 42, 2.3, 0.55, 2.3, 0.08);
        player.playSound(center, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 0.58F, god.isTitan() ? 0.68F : 1.03F);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> clear(player.getUniqueId(), sceneId),
                Math.max(3, seconds) * 20L);
        activeScenes.put(player.getUniqueId(), new Scene(sceneId, displays, task));
    }

    /** Gives flight a visible but lightweight trail, without spawning any mutable world object. */
    public void flight(Player player, GodId god, int seconds) {
        clear(player.getUniqueId());
        UUID playerId = player.getUniqueId();
        UUID sceneId = UUID.randomUUID();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.isFlying()) {
                    cancel();
                    clear(playerId, sceneId);
                    return;
                }
                Location trail = player.getLocation().add(0, 0.35, 0);
                player.getWorld().spawnParticle(Particle.END_ROD, trail, 4, 0.28, 0.18, 0.28, 0.02);
                player.getWorld().spawnParticle(Particle.DUST, trail, 5, 0.35, 0.22, 0.35, 0,
                        new Particle.DustOptions(colorFor(god), 1.05F));
            }
        }.runTaskTimer(plugin, 0L, 6L);
        BukkitTask cleanup = plugin.getServer().getScheduler().runTaskLater(plugin, () -> clear(playerId, sceneId),
                Math.max(2, seconds) * 20L);
        activeScenes.put(playerId, new Scene(sceneId, List.of(), new CombinedTask(task, cleanup)));
    }

    /** Marks the avatar form with a halo. It is visual-only and disappears on every cleanup path. */
    public void avatar(Player player, GodId god, int seconds) {
        clear(player.getUniqueId());
        UUID sceneId = UUID.randomUUID();
        Location center = player.getLocation().add(0, 2.7, 0);
        List<Entity> displays = new ArrayList<>();
        if (displaysEnabled) {
            int count = Math.min(6, maxDisplaysPerScene);
            for (int index = 0; index < count; index++) {
                double angle = (Math.PI * 2D * index) / Math.max(1, count);
                Location point = center.clone().add(Math.cos(angle) * 1.25D, Math.sin(angle) * 0.25D, 0);
                BlockDisplay display = spawnBlock(point, materialFor(god), 0.22F);
                if (display != null) {
                    displays.add(display);
                    animate(display, 0.52F, 16, (float) (angle + Math.PI));
                }
            }
        }
        player.getWorld().spawnParticle(Particle.FIREWORK, center, 36, 0.8, 1.0, 0.8, 0.05);
        player.playSound(center, Sound.ITEM_TOTEM_USE, 0.55F, god.isTitan() ? 0.72F : 1.0F);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> clear(player.getUniqueId(), sceneId),
                Math.max(3, seconds) * 20L);
        activeScenes.put(player.getUniqueId(), new Scene(sceneId, displays, task));
    }

    private BlockDisplay spawnBlock(Location location, Material material, float scale) {
        if (!location.getChunk().isLoaded()) {
            return null;
        }
        return location.getWorld().spawn(location, BlockDisplay.class, display -> {
            display.setBlock(material.createBlockData());
            display.setPersistent(false);
            display.setInvulnerable(true);
            display.setGravity(false);
            display.setViewRange(viewRange);
            display.setGlowColorOverride(Color.WHITE);
            display.setTransformation(transform(scale, 0.0F));
        });
    }

    private void animate(BlockDisplay display, float scale, int ticks, float rotation) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (!display.isValid()) {
                return;
            }
            display.setInterpolationDelay(0);
            display.setInterpolationDuration(Math.max(1, ticks));
            display.setTransformation(transform(scale, rotation));
        });
    }

    private Transformation transform(float scale, float rotation) {
        return new Transformation(
                new Vector3f(-scale / 2.0F, -scale / 2.0F, -scale / 2.0F),
                new AxisAngle4f(rotation, 0.0F, 1.0F, 0.0F),
                new Vector3f(scale, scale, scale),
                new AxisAngle4f()
        );
    }

    private void clear(UUID playerId) {
        Scene scene = activeScenes.remove(playerId);
        if (scene == null) {
            return;
        }
        scene.task().cancel();
        remove(scene.entities());
    }

    /** Only a scene may clean itself; an older delayed task cannot erase its replacement. */
    private void clear(UUID playerId, UUID expectedSceneId) {
        Scene scene = activeScenes.get(playerId);
        if (scene == null || !scene.id().equals(expectedSceneId)) {
            return;
        }
        clear(playerId);
    }

    private void remove(List<? extends Entity> entities) {
        entities.forEach(entity -> {
            if (entity.isValid()) {
                entity.remove();
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clear(event.getPlayer().getUniqueId());
    }

    /** Removes all temporary scenes before the plugin closes or reloads. */
    public void shutdown() {
        new ArrayList<>(activeScenes.keySet()).forEach(this::clear);
    }

    private Material materialFor(GodId god) {
        return switch (god) {
            case POSEIDON, OCEANUS, TETHYS -> Material.PRISMARINE;
            case DEMETER, PERSEPHONE, DIONYSUS, RHEA, APHRODITE, EROS, TYCHE -> Material.MOSS_BLOCK;
            case HADES, HECATE, MORPHEUS, SELENE, PHOEBE -> Material.CRYING_OBSIDIAN;
            case HEPHAESTUS, HESTIA, IAPETUS, CRONUS -> Material.COPPER_BLOCK;
            case ZEUS, APOLLO, HELIOS, HYPERION, THEIA, ARES, NIKE, NEMESIS -> Material.AMETHYST_BLOCK;
            default -> Material.SEA_LANTERN;
        };
    }

    private Color colorFor(GodId god) {
        return switch (god) {
            case POSEIDON, OCEANUS, TETHYS -> Color.fromRGB(54, 185, 255);
            case ZEUS, APOLLO, HELIOS, HYPERION, THEIA -> Color.fromRGB(255, 213, 92);
            case HADES, HECATE, MORPHEUS, SELENE, PHOEBE -> Color.fromRGB(164, 109, 255);
            case DEMETER, RHEA, DIONYSUS, PERSEPHONE -> Color.fromRGB(112, 221, 112);
            case ARES, NEMESIS, NIKE -> Color.fromRGB(241, 94, 94);
            default -> Color.fromRGB(233, 228, 255);
        };
    }

    private record Scene(UUID id, List<Entity> entities, BukkitTask task) { }

    /** Cancels both the repeating trail and its delayed cleanup as one scene task. */
    private record CombinedTask(BukkitTask first, BukkitTask second) implements BukkitTask {
        @Override public int getTaskId() { return first.getTaskId(); }
        @Override public org.bukkit.plugin.Plugin getOwner() { return first.getOwner(); }
        @Override public boolean isSync() { return first.isSync(); }
        @Override public void cancel() { first.cancel(); second.cancel(); }
        @Override public boolean isCancelled() { return first.isCancelled() && second.isCancelled(); }
    }
}

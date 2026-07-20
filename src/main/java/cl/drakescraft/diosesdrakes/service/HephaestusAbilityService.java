package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.integration.ProtectionGate;
import cl.drakescraft.diosesdrakes.integration.SlimefunEnergyBridge;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Executes the implemented Hefesto powers after SkillService authorizes them. */
public final class HephaestusAbilityService {
    public static final String NETWORK_PULSE = "hephaestus.pulso_de_red";
    public static final String ORE_SIGHT = "hephaestus.ojo_de_mena";
    private static final EnumSet<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE, Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE, Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE, Material.ANCIENT_DEBRIS);

    private final JavaPlugin plugin;
    private final SkillService skills;
    private final ProtectionGate protections;
    private final SlimefunEnergyBridge energy;
    private final DivineAbilityAuditLogger audit;
    private final int energyRadius;
    private final int maxMachines;
    private final int energyPerMachine;
    private final int pulseIntervalSeconds;
    private final int oreRadius;
    private final int maxOreMarkers;
    private final Map<UUID, BukkitTask> pulseTasks = new HashMap<>();

    public HephaestusAbilityService(JavaPlugin plugin, SkillService skills, ProtectionGate protections,
                                   SlimefunEnergyBridge energy, DivineAbilityAuditLogger audit, int energyRadius, int maxMachines,
                                   int energyPerMachine, int pulseIntervalSeconds, int oreRadius, int maxOreMarkers) {
        this.plugin = plugin;
        this.skills = skills;
        this.protections = protections;
        this.energy = energy;
        this.audit = audit;
        this.energyRadius = energyRadius;
        this.maxMachines = maxMachines;
        this.energyPerMachine = energyPerMachine;
        this.pulseIntervalSeconds = pulseIntervalSeconds;
        this.oreRadius = oreRadius;
        this.maxOreMarkers = maxOreMarkers;
    }

    public UseResult use(Player player, String skillId) {
        UseResult result = switch (skillId) {
            case NETWORK_PULSE -> startPulse(player);
            case ORE_SIGHT -> revealOres(player);
            default -> UseResult.denied("Esta habilidad todavia no tiene un efecto implementado.");
        };
        audit.record(player, skillId, result.started() ? "started" : "denied:" + result.message());
        return result;
    }

    private UseResult startPulse(Player player) {
        if (findChargeableMachines(player).isEmpty()) {
            return UseResult.denied("No hay maquinas Slimefun permitidas y con espacio de energia cerca.");
        }
        SkillService.ActivationResult activation = skills.tryActivate(player.getUniqueId(), NETWORK_PULSE, Instant.now());
        if (!activation.started()) {
            return UseResult.denied(activation.message());
        }

        UUID playerId = player.getUniqueId();
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int remaining = Math.max(1, activation.durationSeconds() / pulseIntervalSeconds);

            @Override
            public void run() {
                Player current = plugin.getServer().getPlayer(playerId);
                if (current == null || !current.isOnline() || remaining-- <= 0) {
                    BukkitTask active = pulseTasks.remove(playerId);
                    if (active != null) {
                        active.cancel();
                    }
                    return;
                }
                int granted = findChargeableMachines(current).stream().mapToInt(block -> energy.addEnergy(block, energyPerMachine)).sum();
                if (granted > 0) {
                    current.sendActionBar(net.kyori.adventure.text.Component.text("Pulso de Red: +" + granted + " J"));
                }
            }
        }, 1L, pulseIntervalSeconds * 20L);
        pulseTasks.put(playerId, task);
        return UseResult.started("Pulso de Red activo durante " + activation.durationSeconds() + " segundos.");
    }

    private UseResult revealOres(Player player) {
        List<Block> ores = findVisibleOres(player);
        if (ores.isEmpty()) {
            return UseResult.denied("No se detectaron minerales permitidos en los chunks ya cargados.");
        }
        SkillService.ActivationResult activation = skills.tryActivate(player.getUniqueId(), ORE_SIGHT, Instant.now());
        if (!activation.started()) {
            return UseResult.denied(activation.message());
        }

        ores.forEach(block -> player.sendBlockChange(block.getLocation(), Material.GLOWSTONE.createBlockData()));
        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> ores.forEach(block -> player.sendBlockChange(block.getLocation(), block.getBlockData())),
                activation.durationSeconds() * 20L);
        return UseResult.started("Ojo de Mena ha marcado " + ores.size() + " minerales durante " + activation.durationSeconds() + " segundos.");
    }

    private List<Block> findChargeableMachines(Player player) {
        List<Block> machines = new ArrayList<>();
        int baseX = player.getLocation().getBlockX();
        int baseY = player.getLocation().getBlockY();
        int baseZ = player.getLocation().getBlockZ();
        for (int x = baseX - energyRadius; x <= baseX + energyRadius && machines.size() < maxMachines; x++) {
            for (int y = baseY - energyRadius; y <= baseY + energyRadius && machines.size() < maxMachines; y++) {
                for (int z = baseZ - energyRadius; z <= baseZ + energyRadius && machines.size() < maxMachines; z++) {
                    if (!player.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (protections.canInteract(player, block) && energy.canReceiveEnergy(block)) {
                        machines.add(block);
                    }
                }
            }
        }
        return machines;
    }

    private List<Block> findVisibleOres(Player player) {
        List<Block> ores = new ArrayList<>();
        int baseX = player.getLocation().getBlockX();
        int baseY = player.getLocation().getBlockY();
        int baseZ = player.getLocation().getBlockZ();
        for (int x = baseX - oreRadius; x <= baseX + oreRadius && ores.size() < maxOreMarkers; x++) {
            for (int y = Math.max(player.getWorld().getMinHeight(), baseY - oreRadius); y <= Math.min(player.getWorld().getMaxHeight() - 1, baseY + oreRadius) && ores.size() < maxOreMarkers; y++) {
                for (int z = baseZ - oreRadius; z <= baseZ + oreRadius && ores.size() < maxOreMarkers; z++) {
                    if (!player.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                        continue;
                    }
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (ORES.contains(block.getType()) && protections.canInteract(player, block)) {
                        ores.add(block);
                    }
                }
            }
        }
        return ores;
    }

    public record UseResult(boolean started, String message) {
        public static UseResult started(String message) {
            return new UseResult(true, message);
        }

        public static UseResult denied(String message) {
            return new UseResult(false, message);
        }
    }
}

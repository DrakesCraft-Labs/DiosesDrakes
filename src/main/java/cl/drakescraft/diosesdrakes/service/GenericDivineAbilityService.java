package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.integration.ProtectionGate;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.model.SkillType;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Executes survival-safe divine powers. World mutation is always gated by the player's protections. */
public final class GenericDivineAbilityService implements Listener {
    private final JavaPlugin plugin;
    private final SkillService skills;
    private final HephaestusAbilityService hephaestus;
    private final PvpSafetyGate pvp;
    private final ProtectionGate protections;
    private final DivineCinematicService cinematics;
    private final Map<UUID, FlightState> flights = new HashMap<>();
    private final Map<UUID, AvatarState> avatars = new HashMap<>();
    private final Map<UUID, GuardState> guards = new HashMap<>();

    public GenericDivineAbilityService(JavaPlugin plugin, SkillService skills, HephaestusAbilityService hephaestus,
                                       PvpSafetyGate pvp, ProtectionGate protections) {
        this.plugin = plugin;
        this.skills = skills;
        this.hephaestus = hephaestus;
        this.pvp = pvp;
        this.protections = protections;
        this.cinematics = new DivineCinematicService(
                plugin,
                plugin.getConfig().getBoolean("visuals.display-entities.enabled", true),
                plugin.getConfig().getInt("visuals.display-entities.max-displays-per-scene", 8),
                (float) plugin.getConfig().getDouble("visuals.display-entities.view-range", 32.0D)
        );
    }

    public DivineCinematicService cinematics() {
        return cinematics;
    }

    /** Cleans temporary flight, avatar and display state before a plugin reload or shutdown. */
    public void shutdown() {
        flights.forEach((playerId, state) -> {
            state.task().cancel();
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                restoreFlight(player, state);
            }
        });
        avatars.forEach((playerId, state) -> {
            state.task().cancel();
            Player player = plugin.getServer().getPlayer(playerId);
            if (player != null) {
                restoreScale(player, state.scale());
            }
        });
        flights.clear();
        avatars.clear();
        guards.clear();
        cinematics.shutdown();
    }

    /** Restores temporary player state before it can be persisted by a disconnect. */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        FlightState flight = flights.remove(playerId);
        if (flight != null) {
            flight.task().cancel();
            restoreFlight(event.getPlayer(), flight);
        }
        AvatarState avatar = avatars.remove(playerId);
        if (avatar != null) {
            avatar.task().cancel();
            restoreScale(event.getPlayer(), avatar.scale());
        }
        guards.remove(playerId);
    }

    /** Authorizes the skill first, then routes it to its real effect family. */
    public UseResult use(Player player, String skillId) {
        SkillDefinition skill = SkillCatalog.find(skillId).orElse(null);
        if (skill == null) {
            return UseResult.denied("No existe esa habilidad.");
        }
        if (skill.id().equals(HephaestusAbilityService.NETWORK_PULSE) || skill.id().equals(HephaestusAbilityService.ORE_SIGHT)) {
            HephaestusAbilityService.UseResult result = hephaestus.use(player, skillId);
            return new UseResult(result.started(), result.message());
        }
        if (skill.type() == SkillType.PASSIVE) {
            return UseResult.denied("Esta bendicion es pasiva; equipala para mantenerla activa.");
        }
        if (pvp.inCombat(player, Instant.now())) {
            return UseResult.denied("Las bendiciones de supervivencia se bloquean durante combate PvP.");
        }
        String combatRequirement = combatRequirement(player, skill);
        if (combatRequirement != null) {
            return UseResult.denied(combatRequirement);
        }
        if (requiresMonster(skill) && nearestAllowedMonster(player, targetRange(player, skill)) == null) {
            return UseResult.denied("No hay una criatura hostil accesible fuera de una proteccion ajena.");
        }

        SkillService.ActivationResult activation = skills.tryActivate(player.getUniqueId(), skill.id(), Instant.now());
        if (!activation.started()) {
            return UseResult.denied(activation.message());
        }
        apply(player, skill, Math.max(1, activation.durationSeconds()));
        announceActivation(player, skill, activation.durationSeconds());
        return UseResult.started(skill.name() + " activo durante " + activation.durationSeconds() + " segundos.");
    }

    /** Preserves the early branches while turning tiers four through nine into distinct power milestones. */
    private void apply(Player player, SkillDefinition skill, int seconds) {
        if (skill.tier() >= 11) {
            applyCombat(player, skill, seconds);
            return;
        }
        if (skill.tier() >= 4) {
            applyAscension(player, skill, seconds);
            return;
        }
        switch (skill.god()) {
            case POSEIDON, OCEANUS, TETHYS -> effects(player, seconds, 0, PotionEffectType.WATER_BREATHING, PotionEffectType.DOLPHINS_GRACE);
            case ZEUS, HERMES, CRIUS, CRONUS, SELENE -> effects(player, seconds, 0, PotionEffectType.SPEED, PotionEffectType.SLOW_FALLING);
            case APOLLO, HELIOS, HYPERION, THEIA -> effects(player, seconds, 0, PotionEffectType.NIGHT_VISION, PotionEffectType.FIRE_RESISTANCE);
            case HESTIA, HADES, PERSEPHONE -> effects(player, seconds, 0, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.RESISTANCE);
            case DEMETER, RHEA, DIONYSUS -> effects(player, seconds, 0, PotionEffectType.REGENERATION, PotionEffectType.SATURATION);
            case HERA, ATHENA, ARES, IAPETUS, THEMIS, PHOEBE -> effects(player, seconds, 0, PotionEffectType.RESISTANCE, PotionEffectType.ABSORPTION);
            case ARTEMIS -> trackNearestMonster(player);
            case APHRODITE -> calmNearbyMonsters(player);
            case HECATE, MORPHEUS -> effects(player, seconds, 0, PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION);
            case EROS, NIKE, NEMESIS, TYCHE -> effects(player, seconds, 0, PotionEffectType.LUCK, PotionEffectType.REGENERATION);
            case COEUS -> showCoordinates(player);
            case MNEMOSYNE -> showLocationMemory(player);
            default -> effects(player, seconds, 0, PotionEffectType.REGENERATION);
        }
        if (skill.id().equals("hermes.ascenso_de_icaro")) {
            grantFlight(player, skill.god(), Math.min(seconds, 4));
        }
    }

    /** Runs the endgame tiers: strike, travel, domain, execution and avatar. */
    private void applyAscension(Player player, SkillDefinition skill, int seconds) {
        switch (skill.tier()) {
            case 5 -> strikeCreature(player, skill.god(), 24.0D);
            case 6 -> grantMobility(player, skill.god(), seconds);
            case 7 -> establishDomain(player, skill.god(), seconds);
            case 8 -> strikeCreature(player, skill.god(), 100.0D);
            case 9 -> becomeAvatar(player, skill.god(), seconds);
            default -> effects(player, Math.max(8, seconds), 1, primaryEffect(skill.god()));
        }
    }

    /** Uses an effect-only lightning strike so it cannot ignite terrain or damage another player. */
    private void strikeCreature(Player player, GodId god, double damage) {
        Monster target = nearestAllowedMonster(player, 14.0D);
        if (target == null) {
            player.sendActionBar(Component.text("El poder busca una criatura hostil cercana."));
            return;
        }
        target.getWorld().strikeLightningEffect(target.getLocation());
        target.damage(damage, player);
        cinematics.strike(player, god, target, damage);
        player.sendActionBar(Component.text(damage >= 100 ? "Veredicto divino: 100 de dano PvE." : "Descarga divina sobre " + target.getType() + "."));
    }

    /** Runs the late combat disciplines: bare fists, weapon styles, dash and a one-hit monster guard. */
    private void applyCombat(Player player, SkillDefinition skill, int seconds) {
        switch (skill.tier()) {
            case 11 -> fistTechnique(player, skill.god());
            case 12 -> weaponTechnique(player, skill.god());
            case 13 -> divineDash(player, skill.god(), seconds);
            case 14 -> activateGuard(player, skill, seconds);
            default -> effects(player, Math.max(8, seconds), 2, PotionEffectType.STRENGTH, PotionEffectType.SPEED);
        }
    }

    private void fistTechnique(Player player, GodId god) {
        Monster target = nearestAllowedMonster(player, 6.0D);
        if (target == null) {
            return;
        }
        player.swingMainHand();
        target.damage(18.0D, player);
        impactStop(target, 4);
        cinematics.impact(player, god, target, false);
        player.sendActionBar(Component.text("Impacto de punos: " + target.getType() + "."));
    }

    private void weaponTechnique(Player player, GodId god) {
        WeaponStyle style = weaponStyle(player.getInventory().getItemInMainHand().getType());
        Monster target = nearestAllowedMonster(player, style == WeaponStyle.SPEAR ? 9.0D : 6.5D);
        if (target == null) {
            return;
        }
        player.swingMainHand();
        double damage = switch (style) {
            case SPEAR -> 34.0D;
            case MACE -> 36.0D + Math.min(36.0D, player.getFallDistance() * 6.0D);
            case AXE -> 46.0D;
            case SWORD -> 32.0D;
            default -> 0.0D;
        };
        target.damage(damage, player);
        impactStop(target, style == WeaponStyle.AXE || style == WeaponStyle.MACE ? 8 : 5);
        if (style == WeaponStyle.SPEAR) {
            Vector thrust = player.getLocation().getDirection().setY(0).normalize().multiply(1.15D).setY(0.15D);
            target.setVelocity(thrust);
        } else if (style == WeaponStyle.SWORD) {
            player.getWorld().getNearbyEntities(target.getLocation(), 2.2D, 1.5D, 2.2D,
                            entity -> entity instanceof Monster && entity != target && protections.canAffect(player, entity.getLocation()))
                    .forEach(entity -> ((Monster) entity).damage(12.0D, player));
        }
        cinematics.impact(player, god, target, style == WeaponStyle.AXE || style == WeaponStyle.MACE);
        player.sendActionBar(Component.text(weaponLabel(style) + " divino: " + Math.round(damage) + " de dano PvE."));
    }

    private void divineDash(Player player, GodId god, int seconds) {
        Vector direction = player.getLocation().getDirection().setY(0.0D);
        if (direction.lengthSquared() > 0.001D) {
            player.setVelocity(direction.normalize().multiply(1.28D).setY(Math.max(0.12D, player.getVelocity().getY())));
        }
        effects(player, Math.max(3, seconds), 2, PotionEffectType.SPEED, PotionEffectType.SLOW_FALLING);
        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(0, 0.2, 0), 20, 0.45, 0.18, 0.45, 0.08);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7F, 1.1F);
        player.sendActionBar(Component.text("Carrera divina activada."));
    }

    private void activateGuard(Player player, SkillDefinition skill, int seconds) {
        guards.put(player.getUniqueId(), new GuardState(skill.id(), skill.god(), Instant.now().plusSeconds(Math.max(3, seconds))));
        effects(player, Math.max(3, seconds), 1, PotionEffectType.RESISTANCE);
        cinematics.domain(player, skill.god(), Math.min(5, seconds));
        player.sendActionBar(Component.text("Guardia lista: bloquea el proximo golpe de una criatura."));
    }

    /** The guard only applies to monster damage and cannot affect normal PvP. */
    @EventHandler(ignoreCancelled = true)
    public void onMonsterDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getDamager() instanceof Monster)) {
            return;
        }
        GuardState guard = guards.get(player.getUniqueId());
        if (guard == null || Instant.now().isAfter(guard.expiresAt()) || !skills.isEquippedAndUsable(player.getUniqueId(), guard.skillId())) {
            guards.remove(player.getUniqueId());
            return;
        }
        guards.remove(player.getUniqueId());
        event.setDamage(event.getDamage() * 0.35D);
        cinematics.guard(player, guard.god());
        player.sendActionBar(Component.text("Guardia divina: impacto absorbido."));
    }

    /** Grants a timed flight state and restores exactly the player's former flight permissions afterwards. */
    private void grantFlight(Player player, GodId god, int seconds) {
        FlightState previous = flights.remove(player.getUniqueId());
        if (previous != null) {
            previous.task().cancel();
            restoreFlight(player, previous);
        }
        boolean allowed = player.getAllowFlight();
        boolean flying = player.isFlying();
        player.setAllowFlight(true);
        player.setFlying(true);
        effects(player, seconds + 4, 4, PotionEffectType.SPEED, PotionEffectType.SLOW_FALLING);
        cinematics.flight(player, god, seconds);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            FlightState state = flights.remove(player.getUniqueId());
            if (state == null || !player.isOnline()) {
                return;
            }
            restoreFlight(player, state);
        }, Math.max(1, seconds) * 20L);
        flights.put(player.getUniqueId(), new FlightState(allowed, flying, player.getGameMode(), task));
    }

    /** Only wind patrons fly. Other domains receive movement matching their mythology. */
    private void grantMobility(Player player, GodId god, int seconds) {
        if (switch (god) { case HERMES, ARTEMIS, SELENE, CRIUS -> true; default -> false; }) {
            grantFlight(player, god, seconds);
            return;
        }
        switch (domainFor(god)) {
            case WATER -> effects(player, seconds, 1, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.WATER_BREATHING, PotionEffectType.SPEED);
            case SHADOW -> effects(player, seconds, 1, PotionEffectType.INVISIBILITY, PotionEffectType.SPEED, PotionEffectType.SLOW_FALLING);
            case FORGE -> effects(player, seconds, 1, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HASTE, PotionEffectType.SPEED);
            default -> effects(player, seconds, 1, PotionEffectType.SPEED, PotionEffectType.JUMP_BOOST, PotionEffectType.SLOW_FALLING);
        }
        divineDash(player, god, Math.min(seconds, 5));
    }

    /** Restores flight only when the game mode has not changed underneath the temporary ability. */
    private void restoreFlight(Player player, FlightState state) {
        if (player.getGameMode() != state.gameMode()) {
            return;
        }
        player.setFlying(state.wasFlying() && state.wasAllowed());
        player.setAllowFlight(state.wasAllowed());
    }

    /** Applies a god-specific local domain without changing the global world weather or bypassing a claim. */
    private void establishDomain(Player player, GodId god, int seconds) {
        cinematics.domain(player, god, seconds);
        switch (domainFor(god)) {
            case WEATHER -> {
                player.setPlayerWeather(WeatherType.DOWNFALL);
                plugin.getServer().getScheduler().runTaskLater(plugin, player::resetPlayerWeather, seconds * 20L);
                effects(player, seconds, 1, PotionEffectType.SPEED, PotionEffectType.RESISTANCE);
            }
            case GROWTH -> growNearbyNature(player);
            case WATER -> effects(player, seconds, 1, PotionEffectType.WATER_BREATHING, PotionEffectType.DOLPHINS_GRACE, PotionEffectType.REGENERATION);
            case LIGHT -> effects(player, seconds, 1, PotionEffectType.NIGHT_VISION, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.SPEED);
            case SHADOW -> effects(player, seconds, 1, PotionEffectType.INVISIBILITY, PotionEffectType.SLOW_FALLING, PotionEffectType.RESISTANCE);
            case FORGE -> effects(player, seconds, 1, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HASTE, PotionEffectType.RESISTANCE);
            default -> effects(player, seconds, 1, PotionEffectType.RESISTANCE, PotionEffectType.REGENERATION);
        }
    }

    /** Bone-meals existing crops and saplings only where the player can interact, then grows decorative grass safely. */
    private void growNearbyNature(Player player) {
        int changed = 0;
        int baseX = player.getLocation().getBlockX();
        int baseY = player.getLocation().getBlockY();
        int baseZ = player.getLocation().getBlockZ();
        Material shortGrass = Material.matchMaterial("SHORT_GRASS");
        for (int x = baseX - 5; x <= baseX + 5; x++) {
            for (int z = baseZ - 5; z <= baseZ + 5; z++) {
                if (!player.getWorld().isChunkLoaded(x >> 4, z >> 4)) {
                    continue;
                }
                for (int y = baseY - 3; y <= baseY + 3; y++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (!protections.canInteract(player, block)) {
                        continue;
                    }
                    if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge()) {
                        block.applyBoneMeal(BlockFace.UP);
                        changed++;
                    } else if (block.getType().name().endsWith("_SAPLING")) {
                        block.applyBoneMeal(BlockFace.UP);
                        changed++;
                    } else if (shortGrass != null && block.getType() == Material.GRASS_BLOCK
                            && block.getRelative(BlockFace.UP).getType().isAir() && changed < 32) {
                        block.getRelative(BlockFace.UP).setType(shortGrass);
                        changed++;
                    }
                }
            }
        }
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 0.5, 0), 80, 4.5, 1.1, 4.5, 0.1);
        player.sendActionBar(Component.text("Dominio verdante: " + changed + " plantas respondieron a tu llamada."));
    }

    /** Enlarges the player through the native scale attribute and always restores the original value. */
    private void becomeAvatar(Player player, GodId god, int seconds) {
        AvatarState previous = avatars.remove(player.getUniqueId());
        if (previous != null) {
            previous.task().cancel();
            restoreScale(player, previous.scale());
        }
        Attribute scaleAttribute = scaleAttribute();
        AttributeInstance scale = scaleAttribute == null ? null : player.getAttribute(scaleAttribute);
        if (scale == null) {
            player.sendActionBar(Component.text("Tu cliente no admite escala divina; recibes la forma de batalla sin cambio visual."));
            effects(player, seconds, 2, PotionEffectType.RESISTANCE, PotionEffectType.STRENGTH, PotionEffectType.ABSORPTION);
            return;
        }
        double originalScale = scale.getBaseValue();
        scale.setBaseValue(Math.min(1.85D, Math.max(1.35D, originalScale * 1.65D)));
        effects(player, seconds, 2, PotionEffectType.RESISTANCE, PotionEffectType.STRENGTH, PotionEffectType.ABSORPTION);
        cinematics.avatar(player, god, seconds);
        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            AvatarState state = avatars.remove(player.getUniqueId());
            if (state != null && player.isOnline()) {
                restoreScale(player, state.scale());
            }
        }, seconds * 20L);
        avatars.put(player.getUniqueId(), new AvatarState(originalScale, task));
    }

    /** Restores scale defensively because custom clients or future Paper APIs may omit the attribute. */
    private void restoreScale(Player player, double scale) {
        Attribute scaleAttribute = scaleAttribute();
        AttributeInstance attribute = scaleAttribute == null ? null : player.getAttribute(scaleAttribute);
        if (attribute != null) {
            attribute.setBaseValue(scale);
        }
    }

    /** Paper 1.21.11 exposes SCALE directly; the null check at use sites still protects unusual entity types. */
    private Attribute scaleAttribute() {
        return Attribute.SCALE;
    }

    private void effects(Player player, int seconds, int amplifier, PotionEffectType... types) {
        for (PotionEffectType type : types) {
            player.addPotionEffect(new PotionEffect(type, Math.max(1, seconds) * 20, amplifier, true, true, true));
        }
    }

    private boolean isStrike(SkillDefinition skill) {
        return skill.tier() == 5 || skill.tier() == 8;
    }

    private boolean requiresMonster(SkillDefinition skill) {
        return isStrike(skill) || skill.tier() == 11 || skill.tier() == 12;
    }

    private double targetRange(Player player, SkillDefinition skill) {
        if (skill.tier() == 12 && weaponStyle(player.getInventory().getItemInMainHand().getType()) == WeaponStyle.SPEAR) {
            return 9.0D;
        }
        return switch (skill.tier()) {
            case 11 -> 6.0D;
            case 12 -> 6.5D;
            default -> 14.0D;
        };
    }

    private Monster nearestAllowedMonster(Player player, double range) {
        return player.getWorld().getNearbyEntities(player.getLocation(), range, Math.min(9.0D, range), range,
                        entity -> entity instanceof Monster && protections.canAffect(player, entity.getLocation()))
                .stream().map(entity -> (Monster) entity)
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
                .orElse(null);
    }

    private String combatRequirement(Player player, SkillDefinition skill) {
        if (skill.tier() == 11 && !player.getInventory().getItemInMainHand().getType().isAir()) {
            return "La tecnica de punos requiere la mano principal vacia.";
        }
        if (skill.tier() == 12 && weaponStyle(player.getInventory().getItemInMainHand().getType()) == WeaponStyle.NONE) {
            return "La tecnica de arma requiere espada, hacha, maza o lanza en la mano principal.";
        }
        return null;
    }

    private WeaponStyle weaponStyle(Material material) {
        String name = material.name();
        if (name.endsWith("_SPEAR")) return WeaponStyle.SPEAR;
        if (material == Material.MACE) return WeaponStyle.MACE;
        if (name.endsWith("_AXE")) return WeaponStyle.AXE;
        if (name.endsWith("_SWORD")) return WeaponStyle.SWORD;
        return WeaponStyle.NONE;
    }

    private String weaponLabel(WeaponStyle style) {
        return switch (style) {
            case SPEAR -> "Estocada de lanza";
            case MACE -> "Aplastamiento de maza";
            case AXE -> "Golpe de hacha";
            case SWORD -> "Arco de espada";
            default -> "Impacto";
        };
    }

    private void impactStop(Monster target, int ticks) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Math.max(1, ticks), 10, true, false, false));
    }

    private PotionEffectType primaryEffect(GodId god) {
        return switch (domainFor(god)) {
            case WATER -> PotionEffectType.WATER_BREATHING;
            case GROWTH -> PotionEffectType.REGENERATION;
            case LIGHT -> PotionEffectType.NIGHT_VISION;
            case SHADOW -> PotionEffectType.INVISIBILITY;
            case FORGE -> PotionEffectType.HASTE;
            default -> PotionEffectType.RESISTANCE;
        };
    }

    private Domain domainFor(GodId god) {
        return switch (god) {
            case ZEUS, ARES, NIKE, NEMESIS -> Domain.WEATHER;
            case POSEIDON, OCEANUS, TETHYS -> Domain.WATER;
            case DEMETER, PERSEPHONE, DIONYSUS, RHEA, APHRODITE, EROS, TYCHE -> Domain.GROWTH;
            case APOLLO, HELIOS, HYPERION, THEIA, PHOEBE -> Domain.LIGHT;
            case HADES, HECATE, MORPHEUS -> Domain.SHADOW;
            case HEPHAESTUS, HESTIA, IAPETUS, CRONUS -> Domain.FORGE;
            default -> Domain.WARD;
        };
    }

    private void trackNearestMonster(Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 48, 32, 48, entity -> entity instanceof Monster)
                .stream().map(entity -> (LivingEntity) entity)
                .min(Comparator.comparingDouble(entity -> entity.getLocation().distanceSquared(player.getLocation())))
                .ifPresentOrElse(monster -> player.sendActionBar(Component.text("Rastro: " + monster.getType() + " a "
                                + Math.round(monster.getLocation().distance(player.getLocation())) + " bloques.")),
                        () -> player.sendActionBar(Component.text("No hay criaturas hostiles cercanas.")));
    }

    private void calmNearbyMonsters(Player player) {
        player.getWorld().getNearbyEntities(player.getLocation(), 12, 8, 12, entity -> entity instanceof Monster)
                .forEach(entity -> ((Monster) entity).setTarget(null));
    }

    private void showCoordinates(Player player) {
        var location = player.getLocation();
        player.sendActionBar(Component.text("Eje celeste: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
    }

    private void showLocationMemory(Player player) {
        var location = player.getLocation();
        player.sendActionBar(Component.text("Recuerdo: " + location.getWorld().getName() + " "
                + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ()));
    }

    private void announceActivation(Player player, SkillDefinition skill, int seconds) {
        Color color = colorFor(skill.god());
        player.getWorld().spawnParticle(Particle.DUST, player.getLocation().add(0, 1.0, 0), 28,
                0.65, 0.75, 0.65, 0, new Particle.DustOptions(color, 1.25F));
        player.getWorld().spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1.0, 0), 18,
                0.55, 0.65, 0.55, 0.08);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.65F, pitchFor(skill.god()));
        cinematics.announce(player, skill.god());
        player.sendActionBar(Component.text(skill.name() + " | " + seconds + " s | recarga " + skill.cooldownSeconds() + " s"));
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

    private float pitchFor(GodId god) {
        return god.isTitan() ? 0.76F : 1.18F;
    }

    private enum Domain { WEATHER, WATER, GROWTH, LIGHT, SHADOW, FORGE, WARD }

    private record FlightState(boolean wasAllowed, boolean wasFlying, org.bukkit.GameMode gameMode, BukkitTask task) { }
    private record AvatarState(double scale, BukkitTask task) { }
    private record GuardState(String skillId, GodId god, Instant expiresAt) { }
    private enum WeaponStyle { NONE, SWORD, AXE, MACE, SPEAR }

    public record UseResult(boolean started, String message) {
        public static UseResult started(String message) {
            return new UseResult(true, message);
        }

        public static UseResult denied(String message) {
            return new UseResult(false, message);
        }
    }
}

package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.model.SkillType;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/** Executes safe shared effect families for every non-Hefesto active or stance. */
public final class GenericDivineAbilityService {
    private final SkillService skills;
    private final HephaestusAbilityService hephaestus;
    private final PvpSafetyGate pvp;

    public GenericDivineAbilityService(SkillService skills, HephaestusAbilityService hephaestus, PvpSafetyGate pvp) {
        this.skills = skills;
        this.hephaestus = hephaestus;
        this.pvp = pvp;
    }

    public UseResult use(Player player, String skillId) {
        SkillDefinition skill = SkillCatalog.find(skillId).orElse(null);
        if (skill == null) {
            return UseResult.denied("No existe esa habilidad.");
        }
        if (skill.god() == GodId.HEPHAESTUS) {
            HephaestusAbilityService.UseResult result = hephaestus.use(player, skillId);
            return new UseResult(result.started(), result.message());
        }
        if (skill.type() == SkillType.PASSIVE) {
            return UseResult.denied("Esta bendicion es pasiva; equipala para mantenerla activa.");
        }
        if (pvp.inCombat(player, Instant.now())) {
            return UseResult.denied("Las bendiciones de supervivencia se bloquean durante combate PvP.");
        }

        SkillService.ActivationResult activation = skills.tryActivate(player.getUniqueId(), skill.id(), Instant.now());
        if (!activation.started()) {
            return UseResult.denied(activation.message());
        }
        apply(player, skill, Math.max(1, activation.durationSeconds()));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.6F, 1.2F);
        return UseResult.started(skill.name() + " activo durante " + activation.durationSeconds() + " segundos.");
    }

    private void apply(Player player, SkillDefinition skill, int seconds) {
        switch (skill.god()) {
            case POSEIDON, OCEANUS, TETHYS -> effects(player, seconds, PotionEffectType.WATER_BREATHING, PotionEffectType.DOLPHINS_GRACE);
            case ZEUS, HERMES, CRIUS, CRONUS, SELENE -> effects(player, seconds, PotionEffectType.SPEED, PotionEffectType.SLOW_FALLING);
            case APOLLO, HELIOS, HYPERION, THEIA -> effects(player, seconds, PotionEffectType.NIGHT_VISION, PotionEffectType.FIRE_RESISTANCE);
            case HESTIA, HADES, PERSEPHONE -> effects(player, seconds, PotionEffectType.FIRE_RESISTANCE, PotionEffectType.RESISTANCE);
            case DEMETER, RHEA, DIONYSUS -> effects(player, seconds, PotionEffectType.REGENERATION, PotionEffectType.SATURATION);
            case HERA, ATHENA, ARES, IAPETUS, THEMIS, PHOEBE -> effects(player, seconds, PotionEffectType.RESISTANCE, PotionEffectType.ABSORPTION);
            case ARTEMIS -> trackNearestMonster(player);
            case APHRODITE -> calmNearbyMonsters(player);
            case HECATE, MORPHEUS -> effects(player, seconds, PotionEffectType.INVISIBILITY, PotionEffectType.NIGHT_VISION);
            case EROS, NIKE, NEMESIS, TYCHE -> effects(player, seconds, PotionEffectType.LUCK, PotionEffectType.REGENERATION);
            case COEUS -> showCoordinates(player);
            case MNEMOSYNE -> showLocationMemory(player);
            default -> effects(player, seconds, PotionEffectType.REGENERATION);
        }
    }

    private void effects(Player player, int seconds, PotionEffectType... types) {
        for (PotionEffectType type : types) {
            player.addPotionEffect(new PotionEffect(type, seconds * 20, 0, true, true, true));
        }
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

    public record UseResult(boolean started, String message) {
        public static UseResult started(String message) {
            return new UseResult(true, message);
        }

        public static UseResult denied(String message) {
            return new UseResult(false, message);
        }
    }
}

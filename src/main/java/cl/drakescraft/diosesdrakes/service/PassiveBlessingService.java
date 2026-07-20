package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.model.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;

/** Refreshes short, self-only passive effects; unequipping naturally lets them expire. */
public final class PassiveBlessingService {
    private final SkillService skills;
    private final PvpSafetyGate pvp;

    public PassiveBlessingService(SkillService skills, PvpSafetyGate pvp) {
        this.skills = skills;
        this.pvp = pvp;
    }

    public void refresh(Player player) {
        if (pvp.inCombat(player, Instant.now())) {
            return;
        }
        for (SkillDefinition skill : skills.equippedUsable(player.getUniqueId())) {
            if (skill.type() == SkillType.PASSIVE && skill.god() != GodId.HEPHAESTUS) {
                apply(player, skill);
            }
        }
    }

    /** Refreshes the equipped passive without leaving a permanent effect after it is unequipped. */
    private void apply(Player player, SkillDefinition skill) {
        GodId god = skill.god();
        PotionEffectType type = switch (god) {
            case POSEIDON, OCEANUS, TETHYS -> PotionEffectType.WATER_BREATHING;
            case ZEUS, HERA, ATHENA, ARES, IAPETUS, RHEA, THEMIS -> PotionEffectType.RESISTANCE;
            case DEMETER, DIONYSUS, PERSEPHONE, MNEMOSYNE -> PotionEffectType.REGENERATION;
            case APOLLO, HELIOS, HYPERION, THEIA, COEUS, PHOEBE, SELENE -> PotionEffectType.NIGHT_VISION;
            case ARTEMIS, HERMES, CRIUS, CRONUS -> PotionEffectType.SPEED;
            case HESTIA, HADES -> PotionEffectType.FIRE_RESISTANCE;
            case HECATE, MORPHEUS -> PotionEffectType.INVISIBILITY;
            case APHRODITE, EROS, NIKE, NEMESIS, TYCHE -> PotionEffectType.LUCK;
            default -> PotionEffectType.REGENERATION;
        };
        int amplifier = skill.tier() >= 10 ? 2 : skill.tier() >= 4 ? 1 : 0;
        player.addPotionEffect(new PotionEffect(type, 160, amplifier, true, false, true));
    }
}

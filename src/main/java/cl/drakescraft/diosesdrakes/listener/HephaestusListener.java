package cl.drakescraft.diosesdrakes.listener;

import cl.drakescraft.diosesdrakes.service.SkillService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceExtractEvent;

/** First implemented blessing: controlled vanilla furnace experience. */
public final class HephaestusListener implements Listener {
    private static final String FORGE_LIVING = "hephaestus.forja_viva";
    private final SkillService skills;

    public HephaestusListener(SkillService skills) {
        this.skills = skills;
    }

    @EventHandler(ignoreCancelled = true)
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        if (!skills.isEquippedAndUsable(event.getPlayer().getUniqueId(), FORGE_LIVING)) {
            return;
        }
        int original = event.getExpToDrop();
        if (original > 0) {
            // Experience is integral; flooring avoids turning a one-XP extraction into two.
            event.setExpToDrop(original + (int) Math.floor(original * 0.25D));
        }
    }
}

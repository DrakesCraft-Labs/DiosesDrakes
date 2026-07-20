package cl.drakescraft.diosesdrakes.menu;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.service.DivineTransactionService;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.time.Instant;

/** Handles generic patron selection and the active patron's branch without hard-coded gods. */
public final class PantheonMenuListener implements Listener {
    private final ProfileService profiles;
    private final SkillService skills;
    private final DivineTransactionService transactions;

    public PantheonMenuListener(ProfileService profiles, SkillService skills, DivineTransactionService transactions) {
        this.profiles = profiles;
        this.skills = skills;
        this.transactions = transactions;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof PantheonMenuHolder holder)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player) || event.getRawSlot() < 0) {
            return;
        }
        if (holder.view() == PantheonMenuHolder.View.SELECTION) {
            chooseGod(player, event.getRawSlot());
            return;
        }
        String skillId = holder.skillAt(event.getRawSlot());
        if (skillId != null) {
            toggleSkill(player, skillId);
        }
    }

    private void chooseGod(org.bukkit.entity.Player player, int slot) {
        GodId[] gods = GodId.values();
        if (slot < 0 || slot >= gods.length) {
            return;
        }
        try {
            GodId god = gods[slot];
            profiles.selectGod(player.getUniqueId(), god, Instant.now());
            player.sendMessage(god.displayName() + " ha aceptado tu ofrenda. Tu senda ha comenzado.");
            PantheonMenu.open(player, profiles, skills);
        } catch (IllegalStateException exception) {
            player.sendMessage(exception.getMessage());
        } catch (Exception exception) {
            player.sendMessage("No se pudo seleccionar el patron. El staff debe revisar la base divina.");
        }
    }

    private void toggleSkill(org.bukkit.entity.Player player, String skillId) {
        try {
            if (!skills.isUnlocked(player.getUniqueId(), skillId)) {
                SkillService.PurchaseResult result = skills.purchase(player, skillId, transactions);
                player.sendMessage("[Dioses] " + result.message());
            } else if (skills.equipped(player.getUniqueId()).contains(skillId)) {
                skills.unequip(player.getUniqueId(), skillId);
                player.sendMessage("Bendicion desequipada: " + skillId + ".");
            } else {
                skills.equip(player.getUniqueId(), skillId);
                player.sendMessage("Bendicion equipada: " + skillId + ".");
            }
            PantheonMenu.open(player, profiles, skills);
        } catch (Exception exception) {
            player.sendMessage("No se pudo actualizar tu carga divina.");
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PantheonMenuHolder) {
            event.setCancelled(true);
        }
    }
}

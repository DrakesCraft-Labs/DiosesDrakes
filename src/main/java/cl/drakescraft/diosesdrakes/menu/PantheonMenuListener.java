package cl.drakescraft.diosesdrakes.menu;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.service.DivineTransactionService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.time.Instant;

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
        if (event.getView().getTopInventory().getHolder() instanceof PantheonMenuHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player)) {
                return;
            }
            if (event.getRawSlot() >= 12 && event.getRawSlot() <= 14) {
                toggleHephaestusSkill(player, event.getRawSlot());
                return;
            }
            if (event.getRawSlot() != 11) {
                return;
            }
            try {
                profiles.selectGod(player.getUniqueId(), GodId.HEPHAESTUS, Instant.now());
                player.sendMessage("Hefesto ha aceptado tu ofrenda. Tu progreso industrial ha comenzado.");
                PantheonMenu.open(player, profiles, skills);
            } catch (IllegalStateException exception) {
                player.sendMessage(exception.getMessage());
            } catch (Exception exception) {
                player.sendMessage("No se pudo seleccionar a Hefesto. El staff debe revisar la base divina.");
            }
        }
    }

    private void toggleHephaestusSkill(org.bukkit.entity.Player player, int slot) {
        SkillDefinition skill = SkillCatalog.forGod(GodId.HEPHAESTUS).stream().skip(slot - 12L).findFirst().orElse(null);
        if (skill == null) {
            return;
        }
        try {
            if (!skills.isUnlocked(player.getUniqueId(), skill.id())) {
                SkillService.PurchaseResult result = skills.purchase(player, skill.id(), transactions);
                player.sendMessage("[Dioses] " + result.message());
                PantheonMenu.open(player, profiles, skills);
                return;
            }
            if (skills.equipped(player.getUniqueId()).contains(skill.id())) {
                skills.unequip(player.getUniqueId(), skill.id());
                player.sendMessage("Bendicion desequipada: " + skill.name() + ".");
            } else {
                skills.equip(player.getUniqueId(), skill.id());
                player.sendMessage("Bendicion equipada: " + skill.name() + ".");
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

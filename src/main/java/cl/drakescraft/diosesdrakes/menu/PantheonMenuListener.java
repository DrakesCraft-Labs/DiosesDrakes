package cl.drakescraft.diosesdrakes.menu;

import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import java.time.Instant;

public final class PantheonMenuListener implements Listener {
    private final ProfileService profiles;

    public PantheonMenuListener(ProfileService profiles) {
        this.profiles = profiles;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PantheonMenuHolder) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof org.bukkit.entity.Player player) || event.getRawSlot() != 11) {
                return;
            }
            try {
                profiles.selectGod(player.getUniqueId(), GodId.HEPHAESTUS, Instant.now());
                player.sendMessage("Hefesto ha aceptado tu ofrenda. Tu progreso industrial ha comenzado.");
                PantheonMenu.open(player, profiles);
            } catch (IllegalStateException exception) {
                player.sendMessage(exception.getMessage());
            } catch (Exception exception) {
                player.sendMessage("No se pudo seleccionar a Hefesto. El staff debe revisar la base divina.");
            }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof PantheonMenuHolder) {
            event.setCancelled(true);
        }
    }
}

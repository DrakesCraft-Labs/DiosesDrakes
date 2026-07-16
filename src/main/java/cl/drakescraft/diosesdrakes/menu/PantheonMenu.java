package cl.drakescraft.diosesdrakes.menu;

import net.kyori.adventure.text.Component;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class PantheonMenu {
    private PantheonMenu() {
    }

    public static void open(Player player, ProfileService profiles) {
        Inventory inventory = Bukkit.createInventory(new PantheonMenuHolder(), 27, Component.text("DiosesDrakes"));
        ItemStack background = item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, background);
        }

        DivineProfile profile;
        try {
            profile = profiles.profile(player.getUniqueId());
        } catch (Exception exception) {
            player.sendMessage("No se pudo cargar tu perfil divino.");
            return;
        }

        inventory.setItem(11, hephaestus(profile));
        inventory.setItem(13, item(Material.BOOK, "Guia del Panteon", List.of(
                "La guia y el arbol completo llegaran con el nucleo.",
                "Las habilidades nunca ignoraran protecciones."
        )));
        inventory.setItem(15, item(Material.BARRIER, "Dioses futuros", List.of(
                "El panteon se incorporara dios por dios.",
                "Tu progreso sera persistente y auditable."
        )));
        player.openInventory(inventory);
    }

    private static ItemStack hephaestus(DivineProfile profile) {
        if (profile.activeGodOptional().isPresent()) {
            return item(Material.ANVIL, "Hefesto", List.of(
                    "Tu dios actual: " + profile.activeGod().displayName(),
                    "La rama industrial se esta preparando."
            ));
        }
        return item(Material.ANVIL, "Hefesto", List.of(
                "Primer dios en construccion.",
                "Haz clic para iniciar tu camino industrial.",
                "Cambiar de dios destruye el progreso anterior."
        ));
    }

    private static ItemStack item(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(lore.stream().map(Component::text).toList());
        stack.setItemMeta(meta);
        return stack;
    }
}

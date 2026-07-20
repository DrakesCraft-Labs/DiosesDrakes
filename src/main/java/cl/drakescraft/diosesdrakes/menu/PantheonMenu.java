package cl.drakescraft.diosesdrakes.menu;

import net.kyori.adventure.text.Component;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
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

    public static void open(Player player, ProfileService profiles, SkillService skills) {
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
        if (profile.activeGodOptional().isPresent()) {
            renderHephaestusSkills(inventory, player, skills);
            inventory.setItem(22, item(Material.RED_DYE, "Renunciar a tu dios", List.of(
                    "Esta accion elimina todo tu progreso actual.",
                    "Usa /dioses renunciar confirmar para continuar."
            )));
        }
        inventory.setItem(4, item(Material.BOOK, "Guia del Panteon", List.of(
                "La guia y el arbol completo llegaran con el nucleo.",
                "Las habilidades nunca ignoraran protecciones."
        )));
        inventory.setItem(6, item(Material.BARRIER, "Dioses futuros", List.of(
                "El panteon se incorporara dios por dios.",
                "Tu progreso sera persistente y auditable."
        )));
        player.openInventory(inventory);
    }

    private static void renderHephaestusSkills(Inventory inventory, Player player, SkillService skills) {
        int[] slots = {12, 13, 14};
        int index = 0;
        for (SkillDefinition skill : SkillCatalog.forGod(cl.drakescraft.diosesdrakes.model.GodId.HEPHAESTUS)) {
            if (index >= slots.length) {
                break;
            }
            try {
                boolean unlocked = skills.isUnlocked(player.getUniqueId(), skill.id());
                boolean equipped = skills.equipped(player.getUniqueId()).contains(skill.id());
                Material material = unlocked ? (equipped ? Material.LIME_DYE : Material.GOLD_INGOT) : Material.GRAY_DYE;
                String state = equipped ? "Equipada" : unlocked ? "Desbloqueada" : "Bloqueada";
                inventory.setItem(slots[index++], item(material, skill.name(), List.of(
                        state,
                        skill.description(),
                        skill.informationLine(),
                        skill.unlockInformation(),
                        skill.prerequisites().isEmpty() ? "Sin prerrequisitos." : "Requiere: " + String.join(", ", skill.prerequisites()),
                        unlocked ? "Clic para equipar o desequipar." : "Clic para entregar la ofrenda."
                )));
            } catch (Exception exception) {
                inventory.setItem(slots[index++], item(Material.BARRIER, skill.name(), List.of("No se pudo cargar esta habilidad.")));
            }
        }
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

package cl.drakescraft.diosesdrakes.menu;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Renders either the full pantheon selection or the three-node branch of the active patron. */
public final class PantheonMenu {
    private PantheonMenu() {
    }

    public static void open(Player player, ProfileService profiles, SkillService skills) {
        try {
            DivineProfile profile = profiles.profile(player.getUniqueId());
            if (profile.activeGod() == null) {
                openSelection(player);
            } else {
                openSkills(player, profile, skills);
            }
        } catch (Exception exception) {
            player.sendMessage("No se pudo cargar tu perfil divino.");
        }
    }

    private static void openSelection(Player player) {
        Inventory inventory = Bukkit.createInventory(new PantheonMenuHolder(PantheonMenuHolder.View.SELECTION, Map.of()), 54,
                Component.text("El Panteon"));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        int slot = 0;
        for (GodId god : GodId.values()) {
            Material icon = god.isTitan() ? Material.AMETHYST_SHARD : Material.NETHER_STAR;
            inventory.setItem(slot++, item(icon, god.displayName(), List.of(
                    god.isTitan() ? "Titan: fuerza cosmica primordial." : "Dios del panteon de DrakesCraft.",
                    "Elegirlo elimina el progreso solo al renunciar a tu patron actual.",
                    "Tres nodos: pasiva, activa y postura.", "Clic para iniciar esta senda."
            )));
        }
        inventory.setItem(49, item(Material.WRITTEN_BOOK, "Codice del Panteon", List.of(
                "Un solo patron activo.", "Renunciar borra el arbol y aplica 48 horas.",
                "Las bendiciones respetan protecciones y mantenimiento.", "Pide el libro con /dioses libro."
        )));
        player.openInventory(inventory);
    }

    private static void openSkills(Player player, DivineProfile profile, SkillService skills) {
        Map<Integer, String> skillBySlot = new HashMap<>();
        PantheonMenuHolder holder = new PantheonMenuHolder(PantheonMenuHolder.View.SKILLS, skillBySlot);
        Inventory inventory = Bukkit.createInventory(holder, 27, Component.text(profile.activeGod().displayName() + " - Senda"));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }

        int[] slots = {11, 13, 15};
        int index = 0;
        for (SkillDefinition skill : SkillCatalog.forGod(profile.activeGod())) {
            if (index >= slots.length) {
                break;
            }
            int slot = slots[index++];
            skillBySlot.put(slot, skill.id());
            try {
                boolean unlocked = skills.isUnlocked(player.getUniqueId(), skill.id());
                boolean equipped = skills.equipped(player.getUniqueId()).contains(skill.id());
                Material material = unlocked ? (equipped ? Material.LIME_DYE : Material.GOLD_INGOT) : Material.GRAY_DYE;
                String state = equipped ? "Equipada" : unlocked ? "Desbloqueada" : "Bloqueada";
                inventory.setItem(slot, item(material, skill.name(), List.of(
                        "Estado: " + state, "Tipo: " + skill.type() + " | Nivel " + skill.tier(), skill.description(),
                        skill.informationLine(), skill.unlockInformation(),
                        skill.prerequisites().isEmpty() ? "Sin prerrequisitos." : "Requiere: " + String.join(", ", skill.prerequisites()),
                        unlocked ? "Clic para equipar o desequipar." : "Clic para entregar la ofrenda."
                )));
            } catch (Exception exception) {
                inventory.setItem(slot, item(Material.BARRIER, skill.name(), List.of("No se pudo cargar esta habilidad.")));
            }
        }
        inventory.setItem(22, item(Material.RED_DYE, "Renunciar a " + profile.activeGod().displayName(), List.of(
                "Elimina todo tu progreso actual.", "Usa /dioses renunciar confirmar."
        )));
        player.openInventory(inventory);
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

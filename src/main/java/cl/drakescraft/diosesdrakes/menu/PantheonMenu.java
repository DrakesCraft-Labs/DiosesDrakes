package cl.drakescraft.diosesdrakes.menu;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.PantheonId;
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
                openPantheons(player);
            } else {
                openSkills(player, profile, skills);
            }
        } catch (Exception exception) {
            player.sendMessage("No se pudo cargar tu perfil divino.");
        }
    }

    private static void openPantheons(Player player) {
        Map<Integer, String> pantheonBySlot = new HashMap<>();
        Inventory inventory = Bukkit.createInventory(new PantheonMenuHolder(PantheonMenuHolder.View.PANTHEONS, pantheonBySlot), 27,
                Component.text("La Convergencia"));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        int[] slots = {10, 12, 14, 16};
        PantheonId[] pantheons = PantheonId.values();
        for (int index = 0; index < pantheons.length; index++) {
            PantheonId pantheon = pantheons[index];
            int slot = slots[index];
            pantheonBySlot.put(slot, pantheon.name());
            inventory.setItem(slot, item(iconFor(pantheon), pantheon.displayName(), List.of(
                    pantheon.description(),
                    "Elige primero un panteon y luego un patron.",
                    "Cambiar de patron exige renunciar: borra favor, nodos y reliquias ligadas.",
                    "Clic para ver sus deidades."
            )));
        }
        inventory.setItem(22, item(Material.WRITTEN_BOOK, "Codice de la Convergencia", List.of(
                "Un patron activo dentro de un panteon.", "Renunciar borra favor, nodos y reliquias de esa senda.",
                "El cooldown de cambio es de 48 horas.", "Las anclas son publicas, persistentes y no reclaman terreno."
        )));
        player.openInventory(inventory);
    }

    public static void openDeities(Player player, PantheonId pantheon) {
        Map<Integer, String> godBySlot = new HashMap<>();
        PantheonMenuHolder holder = new PantheonMenuHolder(PantheonMenuHolder.View.DEITIES, godBySlot);
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text(pantheon.displayName() + " - Patrones"));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }
        int slot = 10;
        for (GodId god : GodId.values()) {
            if (god.pantheon() != pantheon) {
                continue;
            }
            if (slot == 17) {
                slot = 19;
            }
            if (slot >= 44) {
                break;
            }
            godBySlot.put(slot, god.name());
            Material icon = god.isTitan() ? Material.AMETHYST_SHARD : Material.NETHER_STAR;
            inventory.setItem(slot++, item(icon, god.displayName(), List.of(
                    god.isTitan() ? "Titan primordial del panteon griego." : "Patron de " + pantheon.displayName() + ".",
                    "15 nodos: pasivas, activas, posturas y tecnicas de combate.",
                    "Clic para jurar tu senda."
            )));
        }
        inventory.setItem(49, item(Material.ARROW, "Volver a panteones", List.of("Clic para regresar.")));
        player.openInventory(inventory);
    }

    private static void openSkills(Player player, DivineProfile profile, SkillService skills) {
        Map<Integer, String> skillBySlot = new HashMap<>();
        PantheonMenuHolder holder = new PantheonMenuHolder(PantheonMenuHolder.View.SKILLS, skillBySlot);
        Inventory inventory = Bukkit.createInventory(holder, 54, Component.text(profile.activeGod().displayName() + " - Senda"));
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, item(Material.BLACK_STAINED_GLASS_PANE, " ", List.of()));
        }

        int[] slots = {10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38};
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
        inventory.setItem(49, item(Material.RED_DYE, "Renunciar a " + profile.activeGod().displayName(), List.of(
                "Elimina todo tu progreso actual.", "Usa /dioses renunciar confirmar."
        )));
        player.openInventory(inventory);
    }

    private static Material iconFor(PantheonId pantheon) {
        return switch (pantheon) {
            case GREEK -> Material.NETHER_STAR;
            case NORDIC -> Material.LIGHTNING_ROD;
            case EGYPTIAN -> Material.SUNFLOWER;
            case CELTIC -> Material.MOSS_BLOCK;
        };
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

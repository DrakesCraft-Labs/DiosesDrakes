package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/** Creates a compact in-game codex without storing player state in the book itself. */
public final class DivineCodexService {
    private final ProfileService profiles;

    public DivineCodexService(ProfileService profiles) {
        this.profiles = profiles;
    }

    public void give(Player player) {
        try {
            DivineProfile profile = profiles.profile(player.getUniqueId());
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            meta.title(Component.text("Codice de DiosesDrakes"));
            meta.author(Component.text("DrakesCraft"));
            meta.pages(pages(profile));
            book.setItemMeta(meta);

            player.getInventory().addItem(book).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            player.sendMessage("[Dioses] Recibiste el Codice Divino. Usa /dioses info <habilidad> para el detalle puntual.");
        } catch (Exception exception) {
            player.sendMessage("[Dioses] No se pudo generar el Codice. El staff debe revisar la base divina.");
        }
    }

    private List<Component> pages(DivineProfile profile) {
        List<Component> pages = new ArrayList<>();
        pages.add(Component.text("CODICE DIVINO\n\nElige un patron, desbloquea su senda y equipa solo las bendiciones que quieras llevar.\n\nLas pasivas funcionan equipadas. Activas y posturas se usan con /dioses usar <id>."));
        pages.add(Component.text("REGLAS\n\nUn patron activo. Renunciar borra su progreso y abre un periodo de 48 horas.\n\nLas protecciones, el combate PvP y el mantenimiento semanal se respetan siempre."));
        pages.add(Component.text("TU SENDA\n\n" + (profile.activeGod() == null
                ? "Aun no has elegido patron. Abre /dioses para conocer el panteon."
                : profile.activeGod().displayName() + " te ha elegido. Las siguientes paginas resumen tus tres nodos.")));
        if (profile.activeGod() != null) {
            for (SkillDefinition skill : SkillCatalog.forGod(profile.activeGod())) {
                pages.add(Component.text(skill.name().toUpperCase() + "\n\n" + skill.description() + "\n\n"
                        + skill.informationLine() + "\nCosto: " + Math.round(skill.unlockCost()) + " Dragmas\n"
                        + "Comando: /dioses usar " + skill.id()));
            }
        }
        pages.add(Component.text("EL PANTEON\n\nLa guia completa de cada dios, titan y habilidad vive en web.drakescraft.cl/dioses.html.\n\nEl libro se puede pedir de nuevo con /dioses libro."));
        return List.copyOf(pages);
    }
}

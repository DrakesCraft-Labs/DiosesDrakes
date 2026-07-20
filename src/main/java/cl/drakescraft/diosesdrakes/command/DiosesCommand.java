package cl.drakescraft.diosesdrakes.command;

import cl.drakescraft.diosesdrakes.menu.PantheonMenu;
import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import cl.drakescraft.diosesdrakes.service.DivineTransactionService;
import cl.drakescraft.diosesdrakes.service.HephaestusAbilityService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.time.Instant;

public final class DiosesCommand implements CommandExecutor, TabCompleter {
    private final ProfileService profiles;
    private final SkillService skills;
    private final DivineTransactionService transactions;
    private final HephaestusAbilityService hephaestus;

    public DiosesCommand(ProfileService profiles, SkillService skills, DivineTransactionService transactions,
                         HephaestusAbilityService hephaestus) {
        this.profiles = profiles;
        this.skills = skills;
        this.transactions = transactions;
        this.hephaestus = hephaestus;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando solo puede usarse dentro del servidor.");
            return true;
        }

        if (!player.hasPermission("diosesdrakes.use")) {
            player.sendMessage("No tienes permiso para abrir el panteon.");
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("renunciar") && args[1].equalsIgnoreCase("confirmar")) {
            renounce(player);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            showSkillInfo(player, args[1]);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("equipar")) {
            equip(player, args[1]);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("desequipar")) {
            unequip(player, args[1]);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("desbloquear")) {
            unlock(player, args[1]);
            return true;
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("usar")) {
            use(player, args[1]);
            return true;
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("otorgar")) {
            grant(player, args[2], args[3]);
            return true;
        }

        PantheonMenu.open(player, profiles, skills);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("info", "desbloquear", "equipar", "desequipar", "usar", "renunciar");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("desbloquear")
                || args[0].equalsIgnoreCase("equipar") || args[0].equalsIgnoreCase("desequipar")
                || args[0].equalsIgnoreCase("usar"))) {
            return SkillCatalog.all().stream().map(SkillDefinition::id).sorted().toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("renunciar")) {
            return List.of("confirmar");
        }
        return Collections.emptyList();
    }

    private void renounce(Player player) {
        try {
            DivineProfile profile = profiles.renounce(player.getUniqueId(), Instant.now());
            player.sendMessage("Renunciaste a tu dios. El panteon estara disponible nuevamente desde "
                    + profile.renounceAvailableAt() + ".");
        } catch (IllegalStateException exception) {
            player.sendMessage(exception.getMessage());
        } catch (Exception exception) {
            player.sendMessage("No se pudo completar la renuncia. El staff debe revisar la auditoria.");
        }
    }

    private void showSkillInfo(Player player, String skillId) {
        SkillCatalog.find(skillId).ifPresentOrElse(skill -> {
            player.sendMessage("[Dioses] " + skill.name() + " | " + skill.god().displayName());
            player.sendMessage(skill.description());
            player.sendMessage(skill.informationLine() + " | nivel " + skill.tier());
            player.sendMessage("Debe desbloquearse y equiparse en una ranura disponible.");
        }, () -> player.sendMessage("No existe una habilidad con ese identificador."));
    }

    private void equip(Player player, String skillId) {
        try {
            skills.equip(player.getUniqueId(), skillId);
            player.sendMessage("Habilidad equipada: " + skillId + ".");
        } catch (Exception exception) {
            player.sendMessage(exception.getMessage());
        }
    }

    private void unequip(Player player, String skillId) {
        try {
            skills.unequip(player.getUniqueId(), skillId);
            player.sendMessage("Habilidad desequipada: " + skillId + ".");
        } catch (Exception exception) {
            player.sendMessage("No se pudo desequipar la habilidad.");
        }
    }

    private void unlock(Player player, String skillId) {
        SkillService.PurchaseResult result = skills.purchase(player, skillId, transactions);
        player.sendMessage("[Dioses] " + result.message());
    }

    private void use(Player player, String skillId) {
        HephaestusAbilityService.UseResult result = hephaestus.use(player, skillId.toLowerCase(java.util.Locale.ROOT));
        player.sendMessage("[Dioses] " + result.message());
    }

    private void grant(Player sender, String targetName, String skillId) {
        if (!sender.hasPermission("diosesdrakes.admin")) {
            sender.sendMessage("No tienes permiso administrativo.");
            return;
        }
        Player target = sender.getServer().getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage("El jugador debe estar conectado para recibir una habilidad de prueba.");
            return;
        }
        try {
            skills.grant(target.getUniqueId(), skillId);
            sender.sendMessage("Habilidad otorgada a " + target.getName() + ".");
            target.sendMessage("Has desbloqueado " + skillId + ". Equipa la habilidad con /dioses equipar.");
        } catch (Exception exception) {
            sender.sendMessage(exception.getMessage());
        }
    }
}

package cl.drakescraft.diosesdrakes.command;

import cl.drakescraft.diosesdrakes.menu.PantheonMenu;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.service.ProfileService;
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

    public DiosesCommand(ProfileService profiles) {
        this.profiles = profiles;
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

        PantheonMenu.open(player, profiles);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("renunciar");
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
}

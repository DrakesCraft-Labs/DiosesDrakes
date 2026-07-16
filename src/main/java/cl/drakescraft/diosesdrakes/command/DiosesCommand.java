package cl.drakescraft.diosesdrakes.command;

import cl.drakescraft.diosesdrakes.menu.PantheonMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class DiosesCommand implements CommandExecutor, TabCompleter {
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

        PantheonMenu.open(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}

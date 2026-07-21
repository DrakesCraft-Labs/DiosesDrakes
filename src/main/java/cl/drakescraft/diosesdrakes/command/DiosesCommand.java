package cl.drakescraft.diosesdrakes.command;

import cl.drakescraft.diosesdrakes.menu.PantheonMenu;
import cl.drakescraft.diosesdrakes.catalog.SkillCatalog;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.SkillDefinition;
import cl.drakescraft.diosesdrakes.service.ProfileService;
import cl.drakescraft.diosesdrakes.service.SkillService;
import cl.drakescraft.diosesdrakes.service.DivineTransactionService;
import cl.drakescraft.diosesdrakes.service.GenericDivineAbilityService;
import cl.drakescraft.diosesdrakes.service.DivineCodexService;
import cl.drakescraft.diosesdrakes.service.ConvergenceService;
import cl.drakescraft.diosesdrakes.service.BossFavorService;
import cl.drakescraft.diosesdrakes.model.ConvergenceAnchor;
import cl.drakescraft.diosesdrakes.model.PantheonId;
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
    private final GenericDivineAbilityService abilities;
    private final DivineCodexService codex;
    private final ConvergenceService convergence;
    private final BossFavorService bossFavor;

    public DiosesCommand(ProfileService profiles, SkillService skills, DivineTransactionService transactions,
                         GenericDivineAbilityService abilities, DivineCodexService codex) {
        this(profiles, skills, transactions, abilities, codex, null, null);
    }

    public DiosesCommand(ProfileService profiles, SkillService skills, DivineTransactionService transactions,
                         GenericDivineAbilityService abilities, DivineCodexService codex, ConvergenceService convergence,
                         BossFavorService bossFavor) {
        this.profiles = profiles;
        this.skills = skills;
        this.transactions = transactions;
        this.abilities = abilities;
        this.codex = codex;
        this.convergence = convergence;
        this.bossFavor = bossFavor;
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
        if (args.length == 1 && args[0].equalsIgnoreCase("libro")) {
            codex.give(player);
            return true;
        }
        if (args.length == 1 && (args[0].equalsIgnoreCase("estado") || args[0].equalsIgnoreCase("favor"))) {
            showStatus(player);
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
        if (args.length >= 1 && (args[0].equalsIgnoreCase("ancla") || args[0].equalsIgnoreCase("anclas"))) {
            handleAnchor(player, args);
            return true;
        }

        PantheonMenu.open(player, profiles, skills);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("estado", "favor", "info", "desbloquear", "equipar", "desequipar", "usar", "libro", "renunciar", "ancla");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("desbloquear")
                || args[0].equalsIgnoreCase("equipar") || args[0].equalsIgnoreCase("desequipar")
                || args[0].equalsIgnoreCase("usar"))) {
            return SkillCatalog.all().stream().map(SkillDefinition::id).sorted().toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("renunciar")) {
            return List.of("confirmar");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("ancla") || args[0].equalsIgnoreCase("anclas"))) {
            return List.of("lista", "estado", "ofrendar", "crear");
        }
        if (args.length == 4 && args[0].equalsIgnoreCase("ancla") && args[1].equalsIgnoreCase("crear")) {
            return java.util.Arrays.stream(PantheonId.values()).map(Enum::name).toList();
        }
        return Collections.emptyList();
    }

    private void handleAnchor(Player player, String[] args) {
        if (convergence == null) {
            player.sendMessage("La Convergencia aun no esta disponible.");
            return;
        }
        String action = args.length < 2 ? "lista" : args[1].toLowerCase(java.util.Locale.ROOT);
        try {
            switch (action) {
                case "lista" -> listAnchors(player);
                case "estado" -> {
                    if (args.length < 3) {
                        player.sendMessage("Uso: /dioses ancla estado <id>");
                    } else {
                        showAnchor(player, convergence.anchor(args[2]));
                    }
                }
                case "ofrendar" -> {
                    if (args.length < 4) {
                        player.sendMessage("Uso: /dioses ancla ofrendar <id> <favor>. Minimo " + convergence.minimumOffering() + ".");
                    } else {
                        int amount = Integer.parseInt(args[3]);
                        ConvergenceService.OfferResult result = convergence.offer(player.getUniqueId(), args[2], amount, Instant.now());
                        player.sendMessage("Ofrendaste " + result.spentFavor() + " de favor a " + result.anchor().id() + " para "
                                + result.offeredPantheon().displayName() + ".");
                        if (result.dominanceChanged()) {
                            player.sendMessage("La ancla ahora reconoce a " + result.anchor().dominantPantheon().displayName() + ".");
                        }
                    }
                }
                case "crear" -> createAnchor(player, args);
                default -> player.sendMessage("Uso: /dioses ancla <lista|estado|ofrendar|crear>.");
            }
        } catch (NumberFormatException exception) {
            player.sendMessage("La ofrenda debe ser un numero entero.");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            player.sendMessage(exception.getMessage());
        } catch (Exception exception) {
            player.sendMessage("No se pudo consultar la Convergencia. El staff debe revisar la base divina.");
        }
    }

    private void createAnchor(Player player, String[] args) throws Exception {
        if (!player.hasPermission("diosesdrakes.admin")) {
            player.sendMessage("No tienes permiso administrativo para crear anclas.");
            return;
        }
        if (args.length < 4) {
            player.sendMessage("Uso: /dioses ancla crear <id> <panteon>.");
            return;
        }
        PantheonId pantheon = PantheonId.fromStorage(args[3])
                .orElseThrow(() -> new IllegalArgumentException("Panteon invalido."));
        org.bukkit.Location location = player.getLocation();
        ConvergenceAnchor anchor = convergence.createAnchor(args[2], location.getWorld().getName(),
                location.getBlockX(), location.getBlockY(), location.getBlockZ(), pantheon, Instant.now());
        player.sendMessage("Ancla permanente " + anchor.id() + " creada en " + anchor.worldName() + " "
                + anchor.blockX() + ", " + anchor.blockY() + ", " + anchor.blockZ() + ". No altera ni reclama bloques.");
    }

    private void listAnchors(Player player) throws Exception {
        List<ConvergenceAnchor> anchors = convergence.anchors();
        if (anchors.isEmpty()) {
            player.sendMessage("Aun no hay anclas: un admin debe crearlas en zonas publicas preparadas.");
            return;
        }
        player.sendMessage("Anclas permanentes de la Convergencia:");
        for (ConvergenceAnchor anchor : anchors) {
            player.sendMessage("- " + anchor.id() + " | " + (anchor.dominantPantheon() == null ? "sin dominio" : anchor.dominantPantheon().displayName())
                    + " | " + anchor.worldName() + " " + anchor.blockX() + "," + anchor.blockY() + "," + anchor.blockZ());
        }
    }

    private void showAnchor(Player player, ConvergenceAnchor anchor) {
        player.sendMessage("Ancla " + anchor.id() + " | dominio: "
                + (anchor.dominantPantheon() == null ? "sin dominio" : anchor.dominantPantheon().displayName()));
        for (PantheonId pantheon : PantheonId.values()) {
            player.sendMessage("  " + pantheon.displayName() + ": " + anchor.favorOf(pantheon) + " favor publico.");
        }
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

    /** Shows only durable progression state, never recalculates or changes favor. */
    private void showStatus(Player player) {
        try {
            DivineProfile profile = profiles.profile(player.getUniqueId());
            if (profile.activeGod() == null) {
                player.sendMessage("[Dioses] Aun no has jurado lealtad a un patron.");
                return;
            }
            int favor = bossFavor == null ? 0 : bossFavor.currentFavor(player.getUniqueId());
            String upkeep = profile.upkeepSuspended()
                    ? "SUSPENDIDO"
                    : profile.upkeepDueAt() == null ? "sin mantenimiento"
                    : "vence " + profile.upkeepDueAt();
            player.sendMessage("[Dioses] Patron: " + profile.activeGod().displayName()
                    + " | Favor: " + favor + " | Mantenimiento: " + upkeep + ".");
        } catch (Exception exception) {
            player.sendMessage("[Dioses] No se pudo consultar tu estado divino.");
        }
    }

    private void showSkillInfo(Player player, String skillId) {
        SkillCatalog.find(skillId).ifPresentOrElse(skill -> {
            player.sendMessage("[Dioses] " + skill.name() + " | " + skill.god().displayName() + " | " + skill.type());
            player.sendMessage(skill.description());
            player.sendMessage(skill.informationLine() + " | nivel " + skill.tier() + " | costo "
                    + Math.round(skill.unlockCost()) + " Dragmas");
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
        GenericDivineAbilityService.UseResult result = abilities.use(player, skillId.toLowerCase(java.util.Locale.ROOT));
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

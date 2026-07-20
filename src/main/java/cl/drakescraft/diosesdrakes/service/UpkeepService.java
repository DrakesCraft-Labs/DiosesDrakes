package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.TransactionType;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.function.ToDoubleFunction;

/** Settles weekly divine upkeep once per due period, with a bounded grace window. */
public final class UpkeepService {
    private final DivineRepository repository;
    private final ProfileService profiles;
    private final DivineTransactionService transactions;
    private final Duration period;
    private final Duration grace;
    private final ToDoubleFunction<GodId> costForGod;
    private final double investedPercent;
    private final double maxCost;

    public UpkeepService(DivineRepository repository, ProfileService profiles, DivineTransactionService transactions,
                         Duration period, Duration grace, ToDoubleFunction<GodId> costForGod,
                         double investedPercent, double maxCost) {
        this.repository = repository;
        this.profiles = profiles;
        this.transactions = transactions;
        this.period = period;
        this.grace = grace;
        this.costForGod = costForGod;
        this.investedPercent = investedPercent;
        this.maxCost = maxCost;
    }

    public Settlement settle(Player player, Instant now) {
        try {
            DivineProfile profile = profiles.profile(player.getUniqueId());
            if (profile.activeGod() == null || profile.upkeepDueAt() == null || now.isBefore(profile.upkeepDueAt())) {
                return Settlement.notDue();
            }

            double amount = UpkeepCostPolicy.calculate(
                    costForGod.applyAsDouble(profile.activeGod()),
                    investedPercent,
                    maxCost,
                    repository.unlockedSkills(player.getUniqueId())
            );
            if (amount <= 0) {
                repository.renewUpkeep(player.getUniqueId(), now.plus(period));
                return Settlement.renewed("Tu mantenimiento divino fue renovado.");
            }

            String detail = "upkeep:" + profile.activeGod().name().toLowerCase() + ":" + profile.upkeepDueAt().toEpochMilli();
            DivineTransactionService.TransactionResult payment = transactions.charge(
                    player, TransactionType.WEEKLY_UPKEEP, amount, detail);
            if (payment.charged()) {
                repository.renewUpkeep(player.getUniqueId(), now.plus(period));
                return Settlement.renewed("Mantenimiento semanal pagado: " + amount + " monedas.");
            }

            if (!now.isBefore(profile.upkeepDueAt().plus(grace))) {
                repository.setUpkeepSuspended(player.getUniqueId(), true);
                return Settlement.suspended("Tus bendiciones quedaron suspendidas: " + payment.message());
            }
            return Settlement.grace("El mantenimiento vence pronto: " + payment.message());
        } catch (SQLException exception) {
            return Settlement.error("No se pudo revisar el mantenimiento divino.");
        }
    }

    public record Settlement(Status status, String message) {
        public static Settlement notDue() {
            return new Settlement(Status.NOT_DUE, "");
        }

        public static Settlement renewed(String message) {
            return new Settlement(Status.RENEWED, message);
        }

        public static Settlement grace(String message) {
            return new Settlement(Status.GRACE, message);
        }

        public static Settlement suspended(String message) {
            return new Settlement(Status.SUSPENDED, message);
        }

        public static Settlement error(String message) {
            return new Settlement(Status.ERROR, message);
        }
    }

    public enum Status {
        NOT_DUE,
        RENEWED,
        GRACE,
        SUSPENDED,
        ERROR
    }
}

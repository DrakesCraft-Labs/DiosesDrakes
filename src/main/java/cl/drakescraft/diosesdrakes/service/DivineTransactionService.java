package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.TransactionState;
import cl.drakescraft.diosesdrakes.model.TransactionType;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/** Coordinates Vault and SQLite with explicit recovery states instead of silent charges. */
public final class DivineTransactionService {
    private final DivineRepository repository;
    private final VaultEconomyGateway economy;
    private final DivineAuditLogger auditLogger;

    public DivineTransactionService(DivineRepository repository, VaultEconomyGateway economy, DivineAuditLogger auditLogger) {
        this.repository = repository;
        this.economy = economy;
        this.auditLogger = auditLogger;
    }

    public TransactionResult charge(Player player, TransactionType type, double amount, String detail) {
        if (amount <= 0) {
            return TransactionResult.notCharged("El monto debe ser mayor a cero.");
        }
        if (economy.balance(player) < amount) {
            return TransactionResult.notCharged("No tienes dinero suficiente.");
        }

        UUID transactionId = UUID.randomUUID();
        try {
            repository.createPreparedTransaction(transactionId, player.getUniqueId(), type, amount, detail);
            auditLogger.record(transactionId, player.getUniqueId(), type, TransactionState.PREPARED, amount, detail);
        } catch (SQLException | IllegalStateException exception) {
            return TransactionResult.notCharged("No se pudo preparar la transaccion.");
        }

        if (!economy.withdraw(player, amount)) {
            updateState(transactionId, player, type, amount, TransactionState.ROLLED_BACK, "vault-withdraw-failed");
            return TransactionResult.notCharged("El banco rechazo el cobro.");
        }

        try {
            repository.updateTransactionState(transactionId, TransactionState.COMMITTED, detail);
            auditLogger.record(transactionId, player.getUniqueId(), type, TransactionState.COMMITTED, amount, detail);
            return TransactionResult.charged(transactionId);
        } catch (SQLException | IllegalStateException exception) {
            boolean refunded = economy.deposit(player, amount);
            TransactionState state = refunded ? TransactionState.ROLLED_BACK : TransactionState.MANUAL_REVIEW;
            updateState(transactionId, player, type, amount, state,
                    refunded ? "database-commit-failed-refunded" : "database-commit-failed-refund-required");
            return TransactionResult.notCharged("La transaccion requiere revision administrativa.");
        }
    }

    private void updateState(UUID transactionId, Player player, TransactionType type,
                             double amount, TransactionState state, String detail) {
        try {
            repository.updateTransactionState(transactionId, state, detail);
        } catch (SQLException ignored) {
            state = TransactionState.MANUAL_REVIEW;
        }
        try {
            auditLogger.record(transactionId, player.getUniqueId(), type, state, amount, detail);
        } catch (IllegalStateException ignored) {
            // The durable database record is the primary recovery source.
        }
    }

    public record TransactionResult(boolean charged, UUID transactionId, String message) {
        public static TransactionResult charged(UUID transactionId) {
            return new TransactionResult(true, transactionId, "Cobro confirmado.");
        }

        public static TransactionResult notCharged(String message) {
            return new TransactionResult(false, null, message);
        }
    }
}

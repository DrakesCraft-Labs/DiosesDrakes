package cl.drakescraft.diosesdrakes.storage;

import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.GodId;
import cl.drakescraft.diosesdrakes.model.TransactionState;
import cl.drakescraft.diosesdrakes.model.TransactionType;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

/** Persists player progression and the durable side of economy transactions. */
public final class DivineRepository implements AutoCloseable {
    private final Connection connection;
    private final Object lock = new Object();

    public DivineRepository(Path databasePath) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toAbsolutePath());
        initialize();
    }

    public DivineProfile findOrCreate(UUID playerId) throws SQLException {
        synchronized (lock) {
            Optional<DivineProfile> existing = find(playerId);
            if (existing.isPresent()) {
                return existing.get();
            }

            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO divine_profiles (player_uuid, active_god, selected_at, renounce_available_at, upkeep_due_at, upkeep_suspended)
                    VALUES (?, NULL, NULL, NULL, NULL, 0)
                    """)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }
            return find(playerId).orElseThrow(() -> new SQLException("Profile insert did not persist"));
        }
    }

    public Optional<DivineProfile> find(UUID playerId) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT active_god, selected_at, renounce_available_at, upkeep_due_at, upkeep_suspended
                    FROM divine_profiles WHERE player_uuid = ?
                    """)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Optional.empty();
                    }
                    return Optional.of(new DivineProfile(
                            playerId,
                            GodId.fromStorage(result.getString("active_god")).orElse(null),
                            instant(result, "selected_at"),
                            instant(result, "renounce_available_at"),
                            instant(result, "upkeep_due_at"),
                            result.getBoolean("upkeep_suspended")
                    ));
                }
            }
        }
    }

    public void selectGod(UUID playerId, GodId god, Instant selectedAt, Instant upkeepDueAt) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE divine_profiles
                    SET active_god = ?, selected_at = ?, upkeep_due_at = ?, upkeep_suspended = 0
                    WHERE player_uuid = ? AND active_god IS NULL
                    """)) {
                statement.setString(1, god.name());
                statement.setLong(2, selectedAt.toEpochMilli());
                statement.setLong(3, upkeepDueAt.toEpochMilli());
                statement.setString(4, playerId.toString());
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Profile already has an active god");
                }
            }
        }
    }

    public void renounceGod(UUID playerId, Instant cooldownUntil) throws SQLException {
        synchronized (lock) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement clearSkills = connection.prepareStatement("DELETE FROM divine_skills WHERE player_uuid = ?");
                 PreparedStatement clearLoadout = connection.prepareStatement("DELETE FROM divine_loadout WHERE player_uuid = ?");
                 PreparedStatement clearFavor = connection.prepareStatement("DELETE FROM divine_favor WHERE player_uuid = ?");
                 PreparedStatement updateProfile = connection.prepareStatement("""
                         UPDATE divine_profiles
                         SET active_god = NULL, selected_at = NULL, upkeep_due_at = NULL,
                             upkeep_suspended = 0, renounce_available_at = ?
                         WHERE player_uuid = ? AND active_god IS NOT NULL
                         """)) {
                clearSkills.setString(1, playerId.toString());
                clearSkills.executeUpdate();
                clearLoadout.setString(1, playerId.toString());
                clearLoadout.executeUpdate();
                clearFavor.setString(1, playerId.toString());
                clearFavor.executeUpdate();
                updateProfile.setLong(1, cooldownUntil.toEpochMilli());
                updateProfile.setString(2, playerId.toString());
                if (updateProfile.executeUpdate() != 1) {
                    throw new SQLException("Profile has no active god to renounce");
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public void createPreparedTransaction(UUID transactionId, UUID playerId, TransactionType type, double amount, String detail) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO divine_transactions (transaction_id, player_uuid, transaction_type, state, amount, detail, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """)) {
                statement.setString(1, transactionId.toString());
                statement.setString(2, playerId.toString());
                statement.setString(3, type.name());
                statement.setString(4, TransactionState.PREPARED.name());
                statement.setDouble(5, amount);
                statement.setString(6, detail);
                statement.setLong(7, Instant.now().toEpochMilli());
                statement.executeUpdate();
            }
        }
    }

    public void updateTransactionState(UUID transactionId, TransactionState state, String detail) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE divine_transactions SET state = ?, detail = ? WHERE transaction_id = ?
                    """)) {
                statement.setString(1, state.name());
                statement.setString(2, detail);
                statement.setString(3, transactionId.toString());
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Transaction does not exist");
                }
            }
        }
    }

    /** Finds a completed payment so a retry can safely complete its pending game-side action. */
    public Optional<UUID> findCommittedTransaction(UUID playerId, TransactionType type, String detail) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT transaction_id FROM divine_transactions
                    WHERE player_uuid = ? AND transaction_type = ? AND state = ? AND detail = ?
                    ORDER BY created_at DESC LIMIT 1
                    """)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, type.name());
                statement.setString(3, TransactionState.COMMITTED.name());
                statement.setString(4, detail);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next() ? Optional.of(UUID.fromString(result.getString("transaction_id"))) : Optional.empty();
                }
            }
        }
    }

    public void renewUpkeep(UUID playerId, Instant nextDueAt) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE divine_profiles SET upkeep_due_at = ?, upkeep_suspended = 0
                    WHERE player_uuid = ? AND active_god IS NOT NULL
                    """)) {
                statement.setLong(1, nextDueAt.toEpochMilli());
                statement.setString(2, playerId.toString());
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Profile has no active god to renew");
                }
            }
        }
    }

    public void setUpkeepSuspended(UUID playerId, boolean suspended) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    UPDATE divine_profiles SET upkeep_suspended = ?
                    WHERE player_uuid = ? AND active_god IS NOT NULL
                    """)) {
                statement.setBoolean(1, suspended);
                statement.setString(2, playerId.toString());
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Profile has no active god to suspend");
                }
            }
        }
    }

    public Set<String> loadout(UUID playerId) throws SQLException {
        synchronized (lock) {
            Set<String> skills = new HashSet<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT skill_id FROM divine_loadout WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        skills.add(result.getString("skill_id"));
                    }
                }
            }
            return Set.copyOf(skills);
        }
    }

    public void replaceLoadout(UUID playerId, Set<String> skillIds) throws SQLException {
        synchronized (lock) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement clear = connection.prepareStatement("DELETE FROM divine_loadout WHERE player_uuid = ?");
                 PreparedStatement add = connection.prepareStatement("INSERT INTO divine_loadout (player_uuid, skill_id) VALUES (?, ?)")) {
                clear.setString(1, playerId.toString());
                clear.executeUpdate();
                for (String skillId : skillIds) {
                    add.setString(1, playerId.toString());
                    add.setString(2, skillId);
                    add.addBatch();
                }
                add.executeBatch();
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public void unlockSkill(UUID playerId, GodId god, String skillId, Instant unlockedAt) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT OR IGNORE INTO divine_skills (player_uuid, god_id, skill_id, unlocked_at)
                    VALUES (?, ?, ?, ?)
                    """)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, god.name());
                statement.setString(3, skillId);
                statement.setLong(4, unlockedAt.toEpochMilli());
                statement.executeUpdate();
            }
        }
    }

    public boolean hasUnlockedSkill(UUID playerId, String skillId) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("""
                    SELECT 1 FROM divine_skills WHERE player_uuid = ? AND skill_id = ?
                    """)) {
                statement.setString(1, playerId.toString());
                statement.setString(2, skillId);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
    }

    /** Returns the player's persisted branch state for maintenance and UI calculations. */
    public Set<String> unlockedSkills(UUID playerId) throws SQLException {
        synchronized (lock) {
            Set<String> skills = new HashSet<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT skill_id FROM divine_skills WHERE player_uuid = ?")) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        skills.add(result.getString("skill_id"));
                    }
                }
            }
            return Set.copyOf(skills);
        }
    }

    /** Stores one reward per boss instance/player pair and updates favor atomically. */
    public BossFavorResult awardBossFavor(UUID bossInstanceId, UUID playerId, GodId god, String bossId, int favor,
                                           double contribution, double totalContribution, Instant awardedAt) throws SQLException {
        synchronized (lock) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try (PreparedStatement event = connection.prepareStatement("""
                    INSERT OR IGNORE INTO divine_boss_rewards
                    (boss_instance_id, player_uuid, god_id, boss_id, favor, contribution, total_contribution, awarded_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """);
                 PreparedStatement upsert = connection.prepareStatement("""
                    INSERT INTO divine_favor (player_uuid, god_id, favor, updated_at) VALUES (?, ?, ?, ?)
                    ON CONFLICT(player_uuid, god_id) DO UPDATE SET favor = divine_favor.favor + excluded.favor,
                    updated_at = excluded.updated_at
                    """);
                 PreparedStatement select = connection.prepareStatement("SELECT favor FROM divine_favor WHERE player_uuid = ? AND god_id = ?")) {
                event.setString(1, bossInstanceId.toString());
                event.setString(2, playerId.toString());
                event.setString(3, god.name());
                event.setString(4, bossId);
                event.setInt(5, favor);
                event.setDouble(6, contribution);
                event.setDouble(7, totalContribution);
                event.setLong(8, awardedAt.toEpochMilli());
                boolean granted = event.executeUpdate() == 1;
                if (granted) {
                    upsert.setString(1, playerId.toString());
                    upsert.setString(2, god.name());
                    upsert.setInt(3, favor);
                    upsert.setLong(4, awardedAt.toEpochMilli());
                    upsert.executeUpdate();
                }
                select.setString(1, playerId.toString());
                select.setString(2, god.name());
                try (ResultSet result = select.executeQuery()) {
                    int total = result.next() ? result.getInt("favor") : 0;
                    connection.commit();
                    return new BossFavorResult(granted, total);
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public int favor(UUID playerId, GodId god) throws SQLException {
        synchronized (lock) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT favor FROM divine_favor WHERE player_uuid = ? AND god_id = ?")) {
                statement.setString(1, playerId.toString());
                statement.setString(2, god.name());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next() ? result.getInt("favor") : 0;
                }
            }
        }
    }

    private void initialize() throws SQLException {
        synchronized (lock) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA journal_mode=WAL");
                statement.execute("PRAGMA foreign_keys=ON");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_profiles (
                            player_uuid TEXT PRIMARY KEY,
                            active_god TEXT,
                            selected_at INTEGER,
                            renounce_available_at INTEGER,
                            upkeep_due_at INTEGER,
                            upkeep_suspended INTEGER NOT NULL DEFAULT 0
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_skills (
                            player_uuid TEXT NOT NULL,
                            god_id TEXT NOT NULL,
                            skill_id TEXT NOT NULL,
                            unlocked_at INTEGER NOT NULL,
                            PRIMARY KEY (player_uuid, god_id, skill_id),
                            FOREIGN KEY (player_uuid) REFERENCES divine_profiles(player_uuid) ON DELETE CASCADE
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_transactions (
                            transaction_id TEXT PRIMARY KEY,
                            player_uuid TEXT NOT NULL,
                            transaction_type TEXT NOT NULL,
                            state TEXT NOT NULL,
                            amount REAL NOT NULL,
                            detail TEXT NOT NULL,
                            created_at INTEGER NOT NULL
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_loadout (
                            player_uuid TEXT NOT NULL,
                            skill_id TEXT NOT NULL,
                            PRIMARY KEY (player_uuid, skill_id),
                            FOREIGN KEY (player_uuid) REFERENCES divine_profiles(player_uuid) ON DELETE CASCADE
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_favor (
                            player_uuid TEXT NOT NULL,
                            god_id TEXT NOT NULL,
                            favor INTEGER NOT NULL DEFAULT 0,
                            updated_at INTEGER NOT NULL,
                            PRIMARY KEY (player_uuid, god_id),
                            FOREIGN KEY (player_uuid) REFERENCES divine_profiles(player_uuid) ON DELETE CASCADE
                        )
                        """);
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS divine_boss_rewards (
                            boss_instance_id TEXT NOT NULL,
                            player_uuid TEXT NOT NULL,
                            god_id TEXT NOT NULL,
                            boss_id TEXT NOT NULL,
                            favor INTEGER NOT NULL,
                            contribution REAL NOT NULL,
                            total_contribution REAL NOT NULL,
                            awarded_at INTEGER NOT NULL,
                            PRIMARY KEY (boss_instance_id, player_uuid),
                            FOREIGN KEY (player_uuid) REFERENCES divine_profiles(player_uuid) ON DELETE CASCADE
                        )
                        """);
            }
        }
    }

    public record BossFavorResult(boolean granted, int totalFavor) { }

    private Instant instant(ResultSet result, String column) throws SQLException {
        long value = result.getLong(column);
        return result.wasNull() ? null : Instant.ofEpochMilli(value);
    }

    @Override
    public void close() throws SQLException {
        synchronized (lock) {
            connection.close();
        }
    }
}

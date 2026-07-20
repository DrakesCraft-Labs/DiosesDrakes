package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.api.DivineBossReward;
import cl.drakescraft.diosesdrakes.api.DivineBossVictory;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.PantheonId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.util.Map;

/** Awards durable favor from external boss systems without letting them mutate divine storage directly. */
public final class BossFavorService {
    private final DivineRepository repository;
    private final boolean enabled;
    private final int baseFavor;
    private final int minimumFavor;
    private final int maximumFavor;
    private final Map<String, Double> bossMultipliers;
    private final Map<String, PantheonId> bossAffinities;
    private final double matchingPantheonBonus;

    public BossFavorService(DivineRepository repository, boolean enabled, int baseFavor,
                            int minimumFavor, int maximumFavor, Map<String, Double> bossMultipliers,
                            Map<String, PantheonId> bossAffinities, double matchingPantheonBonus) {
        this.repository = repository;
        this.enabled = enabled;
        this.baseFavor = Math.max(0, baseFavor);
        this.minimumFavor = Math.max(0, minimumFavor);
        this.maximumFavor = Math.max(this.minimumFavor, maximumFavor);
        this.bossMultipliers = Map.copyOf(bossMultipliers);
        this.bossAffinities = Map.copyOf(bossAffinities);
        this.matchingPantheonBonus = Math.max(1.0D, matchingPantheonBonus);
    }

    public DivineBossReward reward(DivineBossVictory victory) {
        if (!enabled) {
            return new DivineBossReward(DivineBossReward.Status.DISABLED, 0, 0);
        }
        try {
            DivineProfile profile = repository.find(victory.playerId()).orElse(null);
            if (profile == null || profile.activeGod() == null) {
                return new DivineBossReward(DivineBossReward.Status.NO_ACTIVE_GOD, 0, 0);
            }
            if (profile.upkeepSuspended()) {
                return new DivineBossReward(DivineBossReward.Status.UPKEEP_SUSPENDED, 0, repository.favor(victory.playerId(), profile.activeGod()));
            }

            int favor = calculateFavor(victory, profile);
            DivineRepository.BossFavorResult result = repository.awardBossFavor(
                    victory.bossInstanceId(), victory.playerId(), profile.activeGod(), victory.bossId(), favor,
                    victory.contribution(), victory.totalContribution(), victory.defeatedAt());
            return new DivineBossReward(result.granted() ? DivineBossReward.Status.GRANTED : DivineBossReward.Status.ALREADY_GRANTED,
                    result.granted() ? favor : 0, result.totalFavor());
        } catch (SQLException exception) {
            return new DivineBossReward(DivineBossReward.Status.FAILED, 0, 0);
        }
    }

    public int currentFavor(java.util.UUID playerId) {
        try {
            DivineProfile profile = repository.find(playerId).orElse(null);
            return profile == null || profile.activeGod() == null ? 0 : repository.favor(playerId, profile.activeGod());
        } catch (SQLException exception) {
            return 0;
        }
    }

    private int calculateFavor(DivineBossVictory victory, DivineProfile profile) {
        double multiplier = bossMultipliers.getOrDefault(victory.bossId().toLowerCase(java.util.Locale.ROOT), 1.0D);
        PantheonId affinity = bossAffinities.get(victory.bossId().toLowerCase(java.util.Locale.ROOT));
        if (affinity != null && affinity == profile.activeGod().pantheon()) {
            multiplier *= matchingPantheonBonus;
        }
        double weightedShare = 0.5D + (0.5D * victory.contributionShare());
        long calculated = Math.round(baseFavor * Math.max(0.0D, multiplier) * weightedShare);
        return (int) Math.clamp(calculated, minimumFavor, maximumFavor);
    }
}

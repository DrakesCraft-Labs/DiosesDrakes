package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.model.ConvergenceAnchor;
import cl.drakescraft.diosesdrakes.model.DivineProfile;
import cl.drakescraft.diosesdrakes.model.PantheonId;
import cl.drakescraft.diosesdrakes.storage.DivineRepository;

import java.sql.SQLException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Permanent, opt-in public conflict. Favor is consumed atomically; anchors never claim or edit terrain. */
public final class ConvergenceService {
    private final DivineRepository repository;
    private final ProfileService profiles;
    private final boolean enabled;
    private final int maxAnchors;
    private final int minimumOffering;
    private final int dominanceMargin;

    public ConvergenceService(DivineRepository repository, ProfileService profiles, boolean enabled,
                              int maxAnchors, int minimumOffering, int dominanceMargin) {
        this.repository = repository;
        this.profiles = profiles;
        this.enabled = enabled;
        this.maxAnchors = Math.max(1, maxAnchors);
        this.minimumOffering = Math.max(1, minimumOffering);
        this.dominanceMargin = Math.max(0, dominanceMargin);
    }

    public ConvergenceAnchor createAnchor(String rawId, String worldName, int x, int y, int z,
                                           PantheonId initialPantheon, Instant now) throws SQLException {
        requireEnabled();
        String id = normalizeId(rawId);
        if (repository.listAnchors().size() >= maxAnchors) {
            throw new IllegalStateException("Ya existen las " + maxAnchors + " anclas permanentes configuradas.");
        }
        return repository.createAnchor(id, worldName, x, y, z, initialPantheon, now);
    }

    public List<ConvergenceAnchor> anchors() throws SQLException {
        return repository.listAnchors();
    }

    public ConvergenceAnchor anchor(String rawId) throws SQLException {
        return repository.findAnchor(normalizeId(rawId))
                .orElseThrow(() -> new IllegalArgumentException("No existe esa ancla de la Convergencia."));
    }

    public OfferResult offer(UUID playerId, String rawAnchorId, int amount, Instant now) throws SQLException {
        requireEnabled();
        if (amount < minimumOffering) {
            throw new IllegalArgumentException("La ofrenda minima es " + minimumOffering + " de favor.");
        }
        DivineProfile profile = profiles.profile(playerId);
        if (profile.activeGod() == null) {
            throw new IllegalStateException("Necesitas un patron activo para ofrendar favor.");
        }
        ConvergenceAnchor before = anchor(rawAnchorId);
        ConvergenceAnchor after = repository.offerAnchorFavor(before.id(), playerId, profile.activeGod(), amount, now);
        PantheonId winner = winningPantheon(after);
        PantheonId dominant = after.dominantPantheon();
        if (winner != null && winner != dominant && winsDecisively(after, winner)) {
            after = repository.updateAnchorDominance(after.id(), winner, now);
        }
        return new OfferResult(after, profile.activeGod().pantheon(), amount,
                before.dominantPantheon() != after.dominantPantheon());
    }

    public int minimumOffering() {
        return minimumOffering;
    }

    private PantheonId winningPantheon(ConvergenceAnchor anchor) {
        return anchor.offerings().entrySet().stream()
                .max(Comparator.<java.util.Map.Entry<PantheonId, Integer>>comparingInt(java.util.Map.Entry::getValue)
                        .thenComparing(entry -> entry.getKey().name()))
                .map(java.util.Map.Entry::getKey)
                .orElse(null);
    }

    private boolean winsDecisively(ConvergenceAnchor anchor, PantheonId candidate) {
        int candidateFavor = anchor.favorOf(candidate);
        int currentFavor = anchor.dominantPantheon() == null ? 0 : anchor.favorOf(anchor.dominantPantheon());
        return candidateFavor >= currentFavor + dominanceMargin;
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new IllegalStateException("La Convergencia esta desactivada por configuracion.");
        }
    }

    private String normalizeId(String rawId) {
        String id = rawId == null ? "" : rawId.toLowerCase(Locale.ROOT);
        if (!id.matches("[a-z0-9_-]{3,32}")) {
            throw new IllegalArgumentException("El ID del ancla debe usar 3-32 letras, numeros, guiones o guiones bajos.");
        }
        return id;
    }

    public record OfferResult(ConvergenceAnchor anchor, PantheonId offeredPantheon, int spentFavor,
                              boolean dominanceChanged) {
    }
}

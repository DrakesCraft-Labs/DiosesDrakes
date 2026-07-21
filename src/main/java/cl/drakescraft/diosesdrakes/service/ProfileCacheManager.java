package cl.drakescraft.diosesdrakes.service;

import cl.drakescraft.diosesdrakes.storage.DivineRepository;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/** Maintains an in-memory cache of player progression to prevent database I/O on the main thread during UI interactions. */
public final class ProfileCacheManager implements Listener {
    private final DivineRepository repository;
    private final Logger logger;
    
    private final Map<UUID, Set<String>> unlockedSkillsCache = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> equippedSkillsCache = new ConcurrentHashMap<>();

    public ProfileCacheManager(DivineRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        load(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        unload(event.getPlayer().getUniqueId());
    }

    public void load(UUID playerId) {
        try {
            unlockedSkillsCache.put(playerId, ConcurrentHashMap.newKeySet());
            unlockedSkillsCache.get(playerId).addAll(repository.unlockedSkills(playerId));
            
            equippedSkillsCache.put(playerId, ConcurrentHashMap.newKeySet());
            equippedSkillsCache.get(playerId).addAll(repository.loadout(playerId));
        } catch (SQLException exception) {
            logger.severe("No se pudo cargar la cache divina de " + playerId + ": " + exception.getMessage());
        }
    }

    public void unload(UUID playerId) {
        unlockedSkillsCache.remove(playerId);
        equippedSkillsCache.remove(playerId);
    }

    public boolean isUnlocked(UUID playerId, String skillId) {
        Set<String> unlocked = unlockedSkillsCache.get(playerId);
        if (unlocked == null) {
            try {
                return repository.hasUnlockedSkill(playerId, skillId);
            } catch (SQLException e) {
                return false;
            }
        }
        return unlocked.contains(skillId);
    }

    public Set<String> getEquipped(UUID playerId) {
        Set<String> equipped = equippedSkillsCache.get(playerId);
        if (equipped == null) {
            try {
                return repository.loadout(playerId);
            } catch (SQLException e) {
                return Set.of();
            }
        }
        return Set.copyOf(equipped);
    }
    
    public void addUnlocked(UUID playerId, String skillId) {
        Set<String> unlocked = unlockedSkillsCache.get(playerId);
        if (unlocked != null) {
            unlocked.add(skillId);
        }
    }
    
    public void addEquipped(UUID playerId, String skillId) {
        Set<String> equipped = equippedSkillsCache.get(playerId);
        if (equipped != null) {
            equipped.add(skillId);
        }
    }
    
    public void removeEquipped(UUID playerId, String skillId) {
        Set<String> equipped = equippedSkillsCache.get(playerId);
        if (equipped != null) {
            equipped.remove(skillId);
        }
    }
    
    public void clear(UUID playerId) {
        Set<String> unlocked = unlockedSkillsCache.get(playerId);
        if (unlocked != null) unlocked.clear();
        
        Set<String> equipped = equippedSkillsCache.get(playerId);
        if (equipped != null) equipped.clear();
    }
}

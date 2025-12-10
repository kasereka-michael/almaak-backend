package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.models.TrashItem;
import com.almaakcorp.entreprise.service_interface.TrashInterface;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrashImplementation implements TrashInterface {

    @Value("${trash.file.path:./data/trash.json}")
    private String trashFilePath;
    
    @Value("${trash.size.threshold.mb:4}")
    private long trashSizeThresholdMB;
    
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final long TRASH_SIZE_THRESHOLD_BYTES;

    public TrashImplementation() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.TRASH_SIZE_THRESHOLD_BYTES = 4L * 1024 * 1024; // 4MB in bytes
    }

    @PostConstruct
    public void init() {
        try {
            // Create data directory if it doesn't exist
            File dataDir = new File("./data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                log.info("Created data directory: {}", dataDir.getAbsolutePath());
            }
            
            // Create trash file if it doesn't exist
            File trashFile = new File(trashFilePath);
            if (!trashFile.exists()) {
                saveTrashItems(new ArrayList<>());
                log.info("Created trash file: {}", trashFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Failed to initialize trash service: {}", e.getMessage(), e);
        }
    }

    @Override
    public TrashItem moveToTrash(String entityType, String entityId, String entityName, 
                               Map<String, Object> entityData, String deletedBy, String deletedByName) {
        return moveToTrash(entityType, entityId, entityName, entityData, deletedBy, deletedByName, null);
    }

    @Override
    public TrashItem moveToTrash(String entityType, String entityId, String entityName, 
                               Map<String, Object> entityData, String deletedBy, String deletedByName, String reason) {
        lock.writeLock().lock();
        try {
            log.info("Starting moveToTrash: entityType={}, entityId={}, entityName={}, deletedBy={}", 
                    entityType, entityId, entityName, deletedBy);
            
            List<TrashItem> trashItems = loadTrashItems();
            log.info("Loaded {} existing trash items", trashItems.size());
            
            TrashItem trashItem = new TrashItem(entityType, entityId, entityName, entityData, deletedBy, deletedByName, reason);
            trashItems.add(trashItem);
            log.info("Created new trash item with ID: {}", trashItem.getId());
            
            saveTrashItems(trashItems);
            log.info("Saved {} trash items to file: {}", trashItems.size(), trashFilePath);
            
            log.info("Successfully moved item to trash: {} (ID: {}) by user: {}", entityName, entityId, deletedBy);
            
            // Check if trash size exceeds threshold
            if (isTrashSizeExceeded()) {
                log.warn("Trash file size has exceeded {}MB threshold. Current size: {}MB", 
                        trashSizeThresholdMB, getCurrentTrashSize() / (1024 * 1024));
            }
            
            return trashItem;
        } catch (Exception e) {
            log.error("Error in moveToTrash: {}", e.getMessage(), e);
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<TrashItem> getUserTrashItems(String username) {
        lock.readLock().lock();
        try {
            List<TrashItem> allItems = loadTrashItems();
            log.info("getUserTrashItems: Loaded {} total items from file", allItems.size());
            
            List<TrashItem> userItems = allItems.stream()
                    .filter(item -> {
                        boolean matches = item.getDeletedBy().equals(username);
                        log.debug("Item {} deleted by '{}', matches user '{}': {}", 
                                item.getEntityName(), item.getDeletedBy(), username, matches);
                        return matches;
                    })
                    .sorted((a, b) -> b.getDeletedAt().compareTo(a.getDeletedAt()))
                    .collect(Collectors.toList());
            
            log.info("getUserTrashItems: Found {} items for user '{}'", userItems.size(), username);
            return userItems;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<TrashItem> getAllTrashItems() {
        lock.readLock().lock();
        try {
            List<TrashItem> allItems = loadTrashItems();
            return allItems.stream()
                    .sorted((a, b) -> b.getDeletedAt().compareTo(a.getDeletedAt()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<TrashItem> getTrashItems(String username, boolean isAdmin, int page, int size) {
        lock.readLock().lock();
        try {
            log.info("Getting trash items for user: {}, isAdmin: {}, page: {}, size: {}", username, isAdmin, page, size);
            
            List<TrashItem> items = isAdmin ? getAllTrashItems() : getUserTrashItems(username);
            log.info("Found {} total items for user (isAdmin={})", items.size(), isAdmin);
            
            int start = page * size;
            int end = Math.min(start + size, items.size());
            
            if (start >= items.size()) {
                log.info("Start index {} >= items size {}, returning empty list", start, items.size());
                return new ArrayList<>();
            }
            
            List<TrashItem> result = items.subList(start, end);
            log.info("Returning {} items (from {} to {})", result.size(), start, end - 1);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<TrashItem> getTrashItemById(String trashId) {
        lock.readLock().lock();
        try {
            List<TrashItem> trashItems = loadTrashItems();
            return trashItems.stream()
                    .filter(item -> item.getId().equals(trashId))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Map<String, Object>> restoreFromTrash(String trashId, String username, boolean isAdmin) {
        lock.writeLock().lock();
        try {
            List<TrashItem> trashItems = loadTrashItems();
            Optional<TrashItem> itemToRestore = trashItems.stream()
                    .filter(item -> item.getId().equals(trashId))
                    .filter(item -> item.canBeViewedBy(username, isAdmin))
                    .findFirst();
            
            if (itemToRestore.isPresent()) {
                TrashItem item = itemToRestore.get();
                trashItems.removeIf(trashItem -> trashItem.getId().equals(trashId));
                saveTrashItems(trashItems);
                
                log.info("Restored item from trash: {} (ID: {}) by user: {}", 
                        item.getEntityName(), item.getEntityId(), username);
                
                return Optional.of(item.getEntityData());
            }
            
            return Optional.empty();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean permanentlyDelete(String trashId, boolean isAdmin) {
        if (!isAdmin) {
            log.warn("Non-admin user attempted to permanently delete trash item: {}", trashId);
            return false;
        }
        
        lock.writeLock().lock();
        try {
            List<TrashItem> trashItems = loadTrashItems();
            boolean removed = trashItems.removeIf(item -> item.getId().equals(trashId));
            
            if (removed) {
                saveTrashItems(trashItems);
                log.info("Permanently deleted item from trash: {}", trashId);
            }
            
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean clearAllTrash(boolean isAdmin) {
        if (!isAdmin) {
            log.warn("Non-admin user attempted to clear all trash");
            return false;
        }
        
        lock.writeLock().lock();
        try {
            saveTrashItems(new ArrayList<>());
            log.info("Cleared all trash items");
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean clearUserTrash(String username) {
        lock.writeLock().lock();
        try {
            List<TrashItem> trashItems = loadTrashItems();
            List<TrashItem> remainingItems = trashItems.stream()
                    .filter(item -> !item.getDeletedBy().equals(username))
                    .collect(Collectors.toList());
            
            saveTrashItems(remainingItems);
            log.info("Cleared trash items for user: {}", username);
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Map<String, Object> getTrashStatistics(String username, boolean isAdmin) {
        lock.readLock().lock();
        try {
            List<TrashItem> items = isAdmin ? getAllTrashItems() : getUserTrashItems(username);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalItems", items.size());
            stats.put("totalSizeBytes", items.stream().mapToLong(TrashItem::getSizeInBytes).sum());
            stats.put("currentFileSizeBytes", getCurrentTrashSize());
            stats.put("fileSizeThresholdBytes", getTrashSizeThreshold());
            stats.put("isFileSizeExceeded", isTrashSizeExceeded());
            
            // Group by entity type
            Map<String, Long> itemsByType = items.stream()
                    .collect(Collectors.groupingBy(TrashItem::getEntityType, Collectors.counting()));
            stats.put("itemsByType", itemsByType);
            
            // Group by deleted date (last 7 days)
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            long recentItems = items.stream()
                    .filter(item -> item.getDeletedAt().isAfter(weekAgo))
                    .count();
            stats.put("recentItemsCount", recentItems);
            
            if (isAdmin) {
                // Group by user (admin only)
                Map<String, Long> itemsByUser = items.stream()
                        .collect(Collectors.groupingBy(TrashItem::getDeletedBy, Collectors.counting()));
                stats.put("itemsByUser", itemsByUser);
            }
            
            return stats;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean isTrashSizeExceeded() {
        return getCurrentTrashSize() > getTrashSizeThreshold();
    }

    @Override
    public long getCurrentTrashSize() {
        try {
            File trashFile = new File(trashFilePath);
            return trashFile.exists() ? trashFile.length() : 0;
        } catch (Exception e) {
            log.error("Error getting trash file size: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public long getTrashSizeThreshold() {
        return TRASH_SIZE_THRESHOLD_BYTES;
    }

    private List<TrashItem> loadTrashItems() {
        try {
            File trashFile = new File(trashFilePath);
            if (!trashFile.exists()) {
                return new ArrayList<>();
            }
            
            String json = Files.readString(Paths.get(trashFilePath));
            if (json.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return objectMapper.readValue(json, new TypeReference<List<TrashItem>>() {});
        } catch (IOException e) {
            log.error("Error loading trash items: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    private void saveTrashItems(List<TrashItem> trashItems) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(trashItems);
            Files.writeString(Paths.get(trashFilePath), json);
        } catch (IOException e) {
            log.error("Error saving trash items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save trash items", e);
        }
    }
}
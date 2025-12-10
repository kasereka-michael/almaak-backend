package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.models.TrashItem;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TrashInterface {
    
    /**
     * Move an item to trash
     */
    TrashItem moveToTrash(String entityType, String entityId, String entityName, 
                         Map<String, Object> entityData, String deletedBy, String deletedByName);
    
    /**
     * Move an item to trash with reason
     */
    TrashItem moveToTrash(String entityType, String entityId, String entityName, 
                         Map<String, Object> entityData, String deletedBy, String deletedByName, String reason);
    
    /**
     * Get trash items for a specific user
     */
    List<TrashItem> getUserTrashItems(String username);
    
    /**
     * Get all trash items (admin only)
     */
    List<TrashItem> getAllTrashItems();
    
    /**
     * Get trash items with pagination
     */
    List<TrashItem> getTrashItems(String username, boolean isAdmin, int page, int size);
    
    /**
     * Get trash item by ID
     */
    Optional<TrashItem> getTrashItemById(String trashId);
    
    /**
     * Restore item from trash
     */
    Optional<Map<String, Object>> restoreFromTrash(String trashId, String username, boolean isAdmin);
    
    /**
     * Permanently delete item from trash (admin only)
     */
    boolean permanentlyDelete(String trashId, boolean isAdmin);
    
    /**
     * Clear all trash items (admin only)
     */
    boolean clearAllTrash(boolean isAdmin);
    
    /**
     * Clear user's trash items
     */
    boolean clearUserTrash(String username);
    
    /**
     * Get trash statistics
     */
    Map<String, Object> getTrashStatistics(String username, boolean isAdmin);
    
    /**
     * Check if trash file size exceeds limit
     */
    boolean isTrashSizeExceeded();
    
    /**
     * Get current trash file size in bytes
     */
    long getCurrentTrashSize();
    
    /**
     * Get trash size warning threshold (4MB)
     */
    long getTrashSizeThreshold();
}
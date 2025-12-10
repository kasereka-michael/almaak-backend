package com.almaakcorp.entreprise.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrashItem {
    
    private String id; // UUID for the trash item
    private String entityType; // Type of entity (Product, Customer, Invoice, etc.)
    private String entityId; // Original ID of the deleted entity
    private String entityName; // Display name for the entity
    private Map<String, Object> entityData; // Complete data of the deleted entity
    private String deletedBy; // Username/ID of user who deleted it
    private String deletedByName; // Display name of user who deleted it
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedAt;
    
    private String reason; // Optional reason for deletion
    private long sizeInBytes; // Size of the entity data in bytes
    
    // Constructor for easy creation
    public TrashItem(String entityType, String entityId, String entityName, 
                    Map<String, Object> entityData, String deletedBy, String deletedByName) {
        this.id = java.util.UUID.randomUUID().toString();
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
        this.entityData = entityData;
        this.deletedBy = deletedBy;
        this.deletedByName = deletedByName;
        this.deletedAt = LocalDateTime.now();
        this.sizeInBytes = calculateSize();
    }
    
    public TrashItem(String entityType, String entityId, String entityName, 
                    Map<String, Object> entityData, String deletedBy, String deletedByName, String reason) {
        this(entityType, entityId, entityName, entityData, deletedBy, deletedByName);
        this.reason = reason;
    }
    
    private long calculateSize() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = mapper.writeValueAsString(this.entityData);
            return json.getBytes("UTF-8").length;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Helper method to check if item can be viewed by user
    public boolean canBeViewedBy(String username, boolean isAdmin) {
        return isAdmin || this.deletedBy.equals(username);
    }
    
    // Helper method to check if item can be permanently deleted by user
    public boolean canBePermanentlyDeletedBy(boolean isAdmin) {
        return isAdmin;
    }
}
package com.almaakcorp.entreprise.repositories;

import com.almaakcorp.entreprise.models.DeliveryNotes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryNoteRepository extends JpaRepository<DeliveryNotes, Long>, JpaSpecificationExecutor<DeliveryNotes> {
        Optional<DeliveryNotes> findDeliveryNotesByTrackingNumber(String trackingNumber);

    DeliveryNotes findByDnId(long dnId);
}

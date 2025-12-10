package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.models.DeliveryNotes;

import java.util.List;
import java.util.Optional;

public interface DeliveryNoteInterface {

    boolean createDeliveryNote(DeliveryNotes deliveryNotes);
    boolean updateDeliveryNote(DeliveryNotes deliveryNotes);
    boolean deleteDeliveryNote(DeliveryNotes deliveryNotes);
    DeliveryNotes getDeliveryNoteById(int id);
    List<DeliveryNotes> getAllDeliveryNotes();
    Optional<DeliveryNotes> getDeliveryNoteByTrackingNumber(String trackNum);

}

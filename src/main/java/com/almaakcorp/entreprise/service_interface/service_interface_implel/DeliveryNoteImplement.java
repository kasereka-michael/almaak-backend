package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.models.DeliveryNotes;
import com.almaakcorp.entreprise.models.Products;
import com.almaakcorp.entreprise.repositories.DeliveryNoteRepository;
import com.almaakcorp.entreprise.repositories.ProductRepository;
import com.almaakcorp.entreprise.service_interface.DeliveryNoteInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryNoteImplement implements DeliveryNoteInterface {

    private final DeliveryNoteRepository deliveryNoteRepository;
    private final ProductRepository productRepository;



    @Override
    public boolean createDeliveryNote(DeliveryNotes deliveryNotes) {
        deliveryNoteRepository.save(deliveryNotes);
        return true;
    }

    @Override
    public boolean updateDeliveryNote(DeliveryNotes deliveryNotes) {
        try {
            deliveryNoteRepository.save(deliveryNotes);
            return true;
        } catch (Exception e) {
            log.error("Error updating delivery note: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteDeliveryNote(DeliveryNotes deliveryNotes) {
        deliveryNoteRepository.delete(deliveryNotes);
        return true;
    }

    @Override
    public DeliveryNotes getDeliveryNoteById(int id) {
        return deliveryNoteRepository.findByDnId(id);
    }

    @Override
    public List<DeliveryNotes> getAllDeliveryNotes() {
        return deliveryNoteRepository.findAll();
    }

    @Override
    public Optional<DeliveryNotes> getDeliveryNoteByTrackingNumber(String trackNum) {
        return Optional.of(deliveryNoteRepository.findDeliveryNotesByTrackingNumber(trackNum).orElseThrow());
    }



    List<Products> checkProductExistence(List<Products> products) {
        List<Products> existingProducts = new java.util.ArrayList<>();

        for (Products product : products) {
            if (product.getProductId() != null) {
                productRepository.findByProductId(product.getProductId())
                    .ifPresent(p -> existingProducts.add(p));
            } else if (product.getProductPartNumber() != null) {
                Products existingProduct = productRepository.findByProductPartNumber(product.getProductPartNumber());
                if (existingProduct != null) {
                    existingProducts.add(existingProduct);
                }
            }
        }

        return existingProducts;
    }
}

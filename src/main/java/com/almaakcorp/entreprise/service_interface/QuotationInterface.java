package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.models.Quotations;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface QuotationInterface {

    List<Quotations> getAllQuotations();

    Quotations getQuotationById(Long id);

    Quotations getQuotationByQuotationId(String quotationId);

    Quotations createQuotation(Quotations quotation);

    Quotations updateQuotation(Long id, Quotations quotation);

    void deleteQuotation(Long id);
    
    boolean moveQuotationToTrash(Long id, String deletedBy, String deletedByName);
    
    Quotations restoreQuotationFromTrash(Map<String, Object> quotationData);

    String getLastQuotationId();

    Page<Quotations> getQuotationsPaginated(int pageNo, int pageSize, String sortBy, String sortDir, String search, String quotationId, String status, LocalDate startDate, LocalDate endDate);
}

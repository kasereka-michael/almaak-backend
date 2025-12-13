package com.almaakcorp.entreprise.migration;

import com.almaakcorp.entreprise.migration.source.*;
import com.almaakcorp.entreprise.models.*;
import com.almaakcorp.entreprise.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    private final SourceProductRepository srcProductRepo;
    private final SourceQuotationRepository srcQuotationRepo;
    private final SourceQuotationItemRepository srcQuotationItemRepo;
    private final SourcePORepository srcPORepo;
    private final SourcePOExpenseRepository srcPOExpenseRepo;

    private final ProductRepository productRepo;
    private final QuotationRepository quotationRepo;
    private final PORepository poRepo;
    private final POExpenseRepository poExpenseRepo;

    @Transactional(transactionManager = "sourceTransactionManager", readOnly = true)
    public void migrateAll() {
        log.info("Starting migration from source DB to primary DB");
        migrateProducts();
        migrateQuotationsAndItems();
        migratePOsAndExpenses();
        log.info("Migration completed");
    }

    private void migrateProducts() {
        int page = 0; int size = 200; Page<Products> p;
        do {
            p = srcProductRepo.findAll(PageRequest.of(page, size));
            for (Products sp : p.getContent()) {
                // Upsert by productName (unique) or productId
                Optional<Products> existing = productRepo.findById(sp.getProductId());
                if (existing.isEmpty()) {
                    Products np = new Products();
                    // Copy fields
                    np.setProductId(null); // Let target DB generate IDs
                    np.setProductName(sp.getProductName());
                    np.setProductDescription(sp.getProductDescription());
                    np.setProductSKU(sp.getProductSKU());
                    np.setProductImage(sp.getProductImage());
                    np.setProductManufacturer(sp.getProductManufacturer());
                    np.setProductPartNumber(sp.getProductPartNumber());
                    np.setProductSellingPrice(sp.getProductSellingPrice());
                    np.setProductCostPrice(sp.getProductCostPrice());
                    np.setProductNormalPrice(sp.getProductNormalPrice());
                    np.setProductCurrentQuantity(sp.getProductCurrentQuantity());
                    np.setProductMinimumQuantity(sp.getProductMinimumQuantity());
                    np.setStorageLocation(sp.getStorageLocation());
                    np.setProductSupplierInfo(sp.getProductSupplierInfo());
                    np.setNotes(sp.getNotes());
                    np.setProductCategory(sp.getProductCategory());
                    np.setProductStatus(sp.getProductStatus());
                    productRepo.save(np);
                }
            }
            page++;
        } while (!p.isLast());
        log.info("Products migrated");
    }

    private void migrateQuotationsAndItems() {
        int page = 0; int size = 200; Page<Quotations> p;
        do {
            p = srcQuotationRepo.findAll(PageRequest.of(page, size));
            for (Quotations sq : p.getContent()) {
                Quotations tq = quotationRepo.findByQuotationId(sq.getQuotationId());
                if (tq == null) {
                    tq = new Quotations();
                }
                // Map fields (do not copy ID)
                tq.setQuotationId(sq.getQuotationId());
                tq.setCustomerId(sq.getCustomerId());
                tq.setCustomerName(sq.getCustomerName());
                tq.setCustomerEmail(sq.getCustomerEmail());
                tq.setCustomerAddress(sq.getCustomerAddress());
                tq.setReference(sq.getReference());
                tq.setAttention(sq.getAttention());
                tq.setValidUntil(sq.getValidUntil());
                tq.setDownloadPath(sq.getDownloadPath());
                tq.setStatus(sq.getStatus());
                tq.setNotes(sq.getNotes());
                tq.setTerms(sq.getTerms());
                tq.setSubtotal(sq.getSubtotal());
                tq.setTax(sq.getTax());
                tq.setTaxRate(sq.getTaxRate());
                tq.setDiscount(sq.getDiscount());
                tq.setDiscountType(sq.getDiscountType());
                tq.setTotalAmount(sq.getTotalAmount());
                tq.setExpectedIncome(sq.getExpectedIncome());
                tq.setEta(sq.getEta());
                tq = quotationRepo.save(tq);

                // Items: clear and reattach
                if (tq.getQuotationItems() != null) {
                    tq.getQuotationItems().clear();
                }
                if (sq.getQuotationItems() != null) {
                    for (QuotationItem si : sq.getQuotationItems()) {
                        QuotationItem ti = new QuotationItem();
                        ti.setQuotation(tq);
                        ti.setProduct(si.getProduct()); // assumes same product IDs; otherwise resolve by name/partNumber
                        ti.setQuantity(si.getQuantity());
                        ti.setUnitPrice(si.getUnitPrice());
                        ti.calculateTotalPrice();
                        tq.getQuotationItems().add(ti);
                    }
                }
                quotationRepo.save(tq);
            }
            page++;
        } while (!p.isLast());
        log.info("Quotations and items migrated");
    }

    private void migratePOsAndExpenses() {
        int page = 0; int size = 200; Page<PO> p;
        do {
            p = srcPORepo.findAll(PageRequest.of(page, size));
            for (PO spo : p.getContent()) {
                // Upsert by poNumber
                Optional<PO> existing = poRepo.findById(spo.getPoiId());
                PO tpo = existing.orElseGet(PO::new);
                // Map fields
                tpo.setPoNumber(spo.getPoNumber());
                tpo.setRfqNumber(spo.getRfqNumber());
                tpo.setQuotation(spo.getQuotation()); // optional
                tpo.setReceivedAt(spo.getReceivedAt());
                tpo.setDelivered(spo.getDelivered());
                tpo.setDeliveredAt(spo.getDeliveredAt());
                tpo.setIncome(spo.getIncome());
                tpo.setPoTotalAmount(spo.getPoTotalAmount());
                tpo.setPaid(spo.getPaid());
                tpo.setPaidAt(spo.getPaidAt());
                tpo = poRepo.save(tpo);

                // Expenses
                List<POExpense> exps = srcPOExpenseRepo.findByPo(spo);
                for (POExpense se : exps) {
                    POExpense te = new POExpense();
                    te.setPo(tpo);
                    te.setExpenseName(se.getExpenseName());
                    te.setAmount(se.getAmount());
                    poExpenseRepo.save(te);
                }
            }
            page++;
        } while (!p.isLast());
        log.info("POs and expenses migrated");
    }
}

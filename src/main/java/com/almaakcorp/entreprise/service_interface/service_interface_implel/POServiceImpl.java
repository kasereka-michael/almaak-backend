package com.almaakcorp.entreprise.service_interface.service_interface_implel;

import com.almaakcorp.entreprise.dto.PODTO;
import com.almaakcorp.entreprise.dto.POExpenseDTO;
import com.almaakcorp.entreprise.models.PO;
import com.almaakcorp.entreprise.models.POExpense;
import com.almaakcorp.entreprise.models.Quotations;
import com.almaakcorp.entreprise.repositories.POExpenseRepository;
import com.almaakcorp.entreprise.repositories.PORepository;
import com.almaakcorp.entreprise.repositories.QuotationRepository;
import com.almaakcorp.entreprise.service_interface.POService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class POServiceImpl implements POService {

    private final PORepository poRepository;
    private final POExpenseRepository expenseRepository;
    private final QuotationRepository quotationRepository;

    @Value("${app.po.files-dir:./po-files}")
    private String poFilesDir;

    public POServiceImpl(PORepository poRepository, POExpenseRepository expenseRepository, QuotationRepository quotationRepository) {
        this.poRepository = poRepository;
        this.expenseRepository = expenseRepository;
        this.quotationRepository = quotationRepository;
    }

    // Removed auto-generation: PO number must be provided by user

    private PODTO mapToDTO(PO po) {
        PODTO dto = new PODTO();
        dto.setPoiId(po.getPoiId());
        dto.setPoNumber(po.getPoNumber());
        dto.setRfqNumber(po.getRfqNumber());
        dto.setQuotationId(po.getQuotation() != null ? po.getQuotation().getId() : null);
        dto.setReceivedAt(po.getReceivedAt());
        dto.setDelivered(po.getDelivered());
        dto.setDeliveredAt(po.getDeliveredAt());
        dto.setIncome(po.getIncome());
        dto.setPoTotalAmount(po.getPoTotalAmount());
        dto.setPaid(po.getPaid());
        dto.setPaidAt(po.getPaidAt());
        if (po.getFilePath() != null) dto.setFileUrl("/api/po/v1/" + po.getPoiId() + "/file");
        return dto;
    }

    private void applyFromDTO(PO po, PODTO dto) {
        if (dto.getPoNumber() != null) po.setPoNumber(dto.getPoNumber());
        po.setRfqNumber(dto.getRfqNumber());
        if (dto.getQuotationId() != null) {
            Quotations q = quotationRepository.findById(dto.getQuotationId()).orElse(null);
            po.setQuotation(q);
        } else {
            po.setQuotation(null);
        }
        po.setReceivedAt(dto.getReceivedAt());
        po.setIncome(dto.getIncome());
        po.setPoTotalAmount(dto.getPoTotalAmount());
        if (dto.getDelivered() != null) {
            po.setDelivered(dto.getDelivered());
            if (Boolean.TRUE.equals(dto.getDelivered()) && po.getDeliveredAt() == null) {
                po.setDeliveredAt(Instant.now());
            } else if (Boolean.FALSE.equals(dto.getDelivered())) {
                po.setDeliveredAt(null);
            }
        }
        if (dto.getPaid() != null) {
            po.setPaid(dto.getPaid());
            if (Boolean.TRUE.equals(dto.getPaid()) && po.getPaidAt() == null) {
                po.setPaidAt(Instant.now());
            } else if (Boolean.FALSE.equals(dto.getPaid())) {
                po.setPaidAt(null);
            }
        }
    }

    private void ensureDir() throws IOException {
        Path dir = Paths.get(poFilesDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }

    private String storeFile(MultipartFile file, Long poId) throws IOException {
        ensureDir();
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String filename = "po-" + poId + ext;
        Path target = Paths.get(poFilesDir, filename);
        Files.write(target, file.getBytes());
        return target.toAbsolutePath().toString();
    }

    @Override
    public PODTO createPO(PODTO dto, MultipartFile file) throws Exception {
        if (dto.getPoNumber() == null || dto.getPoNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("PO number is required");
        }
        if (poRepository.existsByPoNumber(dto.getPoNumber())) {
            throw new IllegalArgumentException("PO number already exists");
        }
        PO po = new PO();
        applyFromDTO(po, dto);
        po = poRepository.save(po);
        if (file != null && !file.isEmpty()) {
            String path = storeFile(file, po.getPoiId());
            po.setFilePath(path);
        }
        return mapToDTO(poRepository.save(po));
    }

    @Override
    public PODTO updatePO(Long id, PODTO dto, MultipartFile file) throws Exception {
        PO po = poRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        if (dto.getPoNumber() == null || dto.getPoNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("PO number is required");
        }
        if (!dto.getPoNumber().equals(po.getPoNumber()) && poRepository.existsByPoNumber(dto.getPoNumber())) {
            throw new IllegalArgumentException("PO number already exists");
        }
        applyFromDTO(po, dto);
        if (file != null && !file.isEmpty()) {
            String path = storeFile(file, id);
            po.setFilePath(path);
        }
        return mapToDTO(poRepository.save(po));
    }

    @Override
    public void deletePO(Long id) {
        expenseRepository.findAll().stream().filter(e -> e.getPo() != null && id.equals(e.getPo().getPoiId()))
                .collect(Collectors.toList()).forEach(expenseRepository::delete);
        poRepository.deleteById(id);
    }

    @Override
    public PODTO getPO(Long id) {
        PO po = poRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        return mapToDTO(po);
    }

    @Override
    public List<PODTO> listPOs(int pageNo, int pageSize, String search) {
        return poRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    public String uploadPOFile(Long id, MultipartFile file) throws Exception {
        PO po = poRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        String path = storeFile(file, id);
        po.setFilePath(path);
        poRepository.save(po);
        return "/po/v1/" + id + "/file";
    }

    @Override
    public byte[] getPOFile(Long id) throws Exception {
        PO po = poRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        if (po.getFilePath() == null) throw new IllegalStateException("No file uploaded for this PO");
        Path p = Paths.get(po.getFilePath());
        return Files.readAllBytes(p);
    }

    @Override
    public List<POExpenseDTO> listExpenses(Long poId) {
        PO po = poRepository.findById(poId).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        return expenseRepository.findByPo(po).stream().map(e -> {
            POExpenseDTO dto = new POExpenseDTO();
            dto.setId(e.getId());
            dto.setPoId(poId);
            dto.setExpenseName(e.getExpenseName());
            dto.setAmount(e.getAmount());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public POExpenseDTO addExpense(Long poId, POExpenseDTO dto) {
        PO po = poRepository.findById(poId).orElseThrow(() -> new IllegalArgumentException("PO not found"));
        POExpense e = new POExpense();
        e.setPo(po);
        e.setExpenseName(dto.getExpenseName());
        e.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
        e = expenseRepository.save(e);
        dto.setId(e.getId());
        dto.setPoId(poId);
        return dto;
    }

    @Override
    public POExpenseDTO updateExpense(Long expenseId, POExpenseDTO dto) {
        POExpense e = expenseRepository.findById(expenseId).orElseThrow(() -> new IllegalArgumentException("Expense not found"));
        if (dto.getExpenseName() != null) e.setExpenseName(dto.getExpenseName());
        if (dto.getAmount() != null) e.setAmount(dto.getAmount());
        e = expenseRepository.save(e);
        POExpenseDTO out = new POExpenseDTO();
        out.setId(e.getId());
        out.setPoId(e.getPo().getPoiId());
        out.setExpenseName(e.getExpenseName());
        out.setAmount(e.getAmount());
        return out;
    }

    @Override
    public void deleteExpense(Long expenseId) {
        expenseRepository.deleteById(expenseId);
    }
}

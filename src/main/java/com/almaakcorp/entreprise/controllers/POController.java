package com.almaakcorp.entreprise.controllers;

import com.almaakcorp.entreprise.dto.PODTO;
import com.almaakcorp.entreprise.dto.POExpenseDTO;
import com.almaakcorp.entreprise.service_interface.POService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/po/v1")
public class POController {
// api/po/v1/save.
    private final POService poService;


    @PostMapping(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public PODTO create(@RequestPart("po") PODTO dto, @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return poService.createPO(dto, file);
    }

    @GetMapping("/find/{id}")
    public PODTO get(@PathVariable Long id) { return poService.getPO(id); }

    @GetMapping("/find-all")
    public List<PODTO> list(@RequestParam(defaultValue = "0") int pageNo,
                            @RequestParam(defaultValue = "20") int pageSize,
                            @RequestParam(required = false) String search) {
        return poService.listPOs(pageNo, pageSize, search);
    }

    @PutMapping(value = "/update/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public PODTO update(@PathVariable Long id,
                        @RequestPart("po") PODTO dto,
                        @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return poService.updatePO(id, dto, file);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) { poService.deletePO(id); }

    @PostMapping(value = "/{id}/upload-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws Exception {
        return poService.uploadPOFile(id, file);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) throws Exception {
        byte[] data = poService.getPOFile(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=po-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    // Expenses
    @GetMapping("/{poId}/expenses")
    public List<POExpenseDTO> listExpenses(@PathVariable Long poId) { return poService.listExpenses(poId); }

    @PostMapping("/{poId}/expenses")
    public POExpenseDTO addExpense(@PathVariable Long poId, @RequestBody POExpenseDTO dto) { return poService.addExpense(poId, dto); }

    @PutMapping("/expenses/{expenseId}")
    public POExpenseDTO updateExpense(@PathVariable Long expenseId, @RequestBody POExpenseDTO dto) { return poService.updateExpense(expenseId, dto); }

    @DeleteMapping("/expenses/{expenseId}")
    public void deleteExpense(@PathVariable Long expenseId) { poService.deleteExpense(expenseId); }
}

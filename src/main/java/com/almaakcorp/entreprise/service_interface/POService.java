package com.almaakcorp.entreprise.service_interface;

import com.almaakcorp.entreprise.dto.PODTO;
import com.almaakcorp.entreprise.dto.POExpenseDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface POService {
    PODTO createPO(PODTO dto, MultipartFile file) throws Exception;
    PODTO updatePO(Long id, PODTO dto, MultipartFile file) throws Exception;
    void deletePO(Long id);
    PODTO getPO(Long id);
    List<PODTO> listPOs(int pageNo, int pageSize, String search);

    // File operations
    String uploadPOFile(Long id, MultipartFile file) throws Exception;
    byte[] getPOFile(Long id) throws Exception;

    // Expenses
    List<POExpenseDTO> listExpenses(Long poId);
    POExpenseDTO addExpense(Long poId, POExpenseDTO dto);
    POExpenseDTO updateExpense(Long expenseId, POExpenseDTO dto);
    void deleteExpense(Long expenseId);
}

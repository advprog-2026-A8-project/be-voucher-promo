package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;

import java.time.LocalDateTime;
import java.util.List;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherRequest request);
    List<VoucherResponse> findAllVouchers();
    List<VoucherResponse> findAvailableVouchers();
    Voucher validateAndGetVoucher(String code, Double purchaseAmount);
    Double calculateDiscount(String code, Double purchaseAmount);
    void useVoucher(String code);

    String restoreVoucher(String code, String idempotencyKey);

    VoucherResponse updateVoucherAdmin(String code, Integer additionalQuota,
                                       LocalDateTime newExpiry, Boolean activeStatus);
    void deactivateExpiredVouchers();
}
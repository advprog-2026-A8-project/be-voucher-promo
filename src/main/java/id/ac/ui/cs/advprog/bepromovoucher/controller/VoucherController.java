package id.ac.ui.cs.advprog.bepromovoucher.controller;

import id.ac.ui.cs.advprog.bepromovoucher.dto.UpdateVoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_VALID = "valid";
    private static final String KEY_AMOUNT = "amount";

    private final VoucherService voucherService;

    @PostMapping("/admin/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> create(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VoucherResponse>> list() {
        return ResponseEntity.ok(voucherService.findAllVouchers());
    }

    @PatchMapping("/admin/update/{code}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VoucherResponse> updateByAdmin(
            @PathVariable String code,
            @RequestBody UpdateVoucherRequest body) {

        VoucherResponse response = voucherService.updateVoucherAdmin(
                code,
                body.getAdditionalQuota(),
                body.getNewExpiry(),
                body.getIsActive()
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<VoucherResponse>> available() {
        return ResponseEntity.ok(voucherService.findAvailableVouchers());
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(
            @RequestBody Map<String, Object> request) {

        if (!request.containsKey("code") || request.get("code") == null) {
            throw new IllegalArgumentException("Kode voucher wajib diisi");
        }
        if (!request.containsKey(KEY_AMOUNT) || request.get(KEY_AMOUNT) == null) {
            throw new IllegalArgumentException("Jumlah pembelian wajib diisi");
        }

        String code = request.get("code").toString();
        Double amount = Double.valueOf(request.get(KEY_AMOUNT).toString());
        Double discount = voucherService.calculateDiscount(code, amount);

        return ResponseEntity.ok(Map.of(
                KEY_VALID, true,
                "discountAmount", discount,
                "finalAmount", amount - discount
        ));
    }

    @PostMapping("/use")
    public ResponseEntity<Map<String, Object>> useVoucher(
            @RequestBody Map<String, String> request) {

        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode voucher wajib diisi");
        }

        voucherService.useVoucher(code);
        return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_MESSAGE, "Kuota voucher " + code + " berhasil dikurangi"
        ));
    }

    @PostMapping("/restore")
    public ResponseEntity<Map<String, Object>> restoreVoucher(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, String> request) {

        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Kode voucher wajib diisi");
        }

        String message = voucherService.restoreVoucher(code, idempotencyKey);
        return ResponseEntity.ok(Map.of(
                KEY_SUCCESS, true,
                KEY_MESSAGE, message
        ));
    }
}
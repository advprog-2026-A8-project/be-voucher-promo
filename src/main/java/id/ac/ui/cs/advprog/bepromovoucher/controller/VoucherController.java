package id.ac.ui.cs.advprog.bepromovoucher.controller;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private static final String KEY_SUCCESS = "success";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_VALID = "valid";

    private final VoucherService voucherService;

    @PostMapping("/admin/create")
    public ResponseEntity<VoucherResponse> create(@Valid @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @GetMapping("/admin/list")
    public ResponseEntity<List<VoucherResponse>> list() {
        return ResponseEntity.ok(voucherService.findAllVouchers());
    }

    @PatchMapping("/admin/update/{code}")
    public ResponseEntity<?> updateByAdmin(
            @PathVariable String code,
            @RequestBody Map<String, Object> body) {
        try {
            Integer addQuota = (Integer) body.get("additionalQuota");
            Boolean status = (Boolean) body.get("isActive");
            LocalDateTime expiry = body.containsKey("newExpiry")
                    ? LocalDateTime.parse(body.get("newExpiry").toString()) : null;

            return ResponseEntity.ok(voucherService.updateVoucherAdmin(code, addQuota, expiry, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_SUCCESS, false,
                    KEY_MESSAGE, e.getMessage()
            ));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<List<VoucherResponse>> available() {
        return ResponseEntity.ok(voucherService.findAvailableVouchers());
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(
            @RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Double amount = Double.valueOf(request.get("amount").toString());
            Double discount = voucherService.calculateDiscount(code, amount);

            return ResponseEntity.ok(Map.of(
                    KEY_VALID, true,
                    "discountAmount", discount,
                    "finalPrice", amount - discount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_VALID, false,
                    KEY_MESSAGE, e.getMessage() != null ? e.getMessage() : "Unknown error"
            ));
        }
    }

    @PostMapping("/use")
    public ResponseEntity<Map<String, Object>> useVoucher(
            @RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        KEY_SUCCESS, false,
                        KEY_MESSAGE, "Kode voucher wajib diisi"
                ));
            }
            voucherService.useVoucher(code);
            return ResponseEntity.ok(Map.of(
                    KEY_SUCCESS, true,
                    KEY_MESSAGE, "Kuota voucher " + code + " berhasil dikurangi"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    KEY_SUCCESS, false,
                    KEY_MESSAGE, e.getMessage()
            ));
        }
    }
}
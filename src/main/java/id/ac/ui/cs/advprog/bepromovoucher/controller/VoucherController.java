package id.ac.ui.cs.advprog.bepromovoucher.controller;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping("/create")
    public ResponseEntity<Voucher> create(@RequestBody VoucherRequest request) {
        return ResponseEntity.ok(voucherService.createVoucher(request));
    }

    @GetMapping("/list")
    public ResponseEntity<List<Voucher>> list() {
        return ResponseEntity.ok(voucherService.findAllVouchers());
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateVoucher(@RequestBody Map<String, Object> request) {
        try {
            String code = (String) request.get("code");
            Double amount = Double.valueOf(request.get("amount").toString());

            Double discount = voucherService.calculateDiscount(code, amount);

            Map<String, Object> response = Map.of(
                    "valid", true,
                    "discountAmount", discount,
                    "finalPrice", amount - discount
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "valid", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Unknown error"
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
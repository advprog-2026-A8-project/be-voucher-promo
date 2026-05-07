package id.ac.ui.cs.advprog.bepromovoucher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
class VoucherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VoucherService voucherService;

    @Autowired
    private ObjectMapper objectMapper;

    private VoucherResponse buildVoucherResponse(String code) {
        return VoucherResponse.builder()
                .code(code)
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(50000.0)
                .minPurchase(10000.0)
                .quota(100)
                .active(true)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .termsAndConditions("S&K berlaku")
                .build();
    }

    @Test
    void testCreateVoucherEndpoint() throws Exception {
        VoucherRequest request = new VoucherRequest();
        request.setCode("PROMO2026");
        request.setDiscountType("FIXED_AMOUNT");
        request.setDiscountValue(50000.0);
        request.setMinPurchase(10000.0);
        request.setQuota(100);
        request.setExpiryDate(LocalDateTime.now().plusDays(7));
        request.setTermsAndConditions("S&K berlaku");

        VoucherResponse response = buildVoucherResponse("PROMO2026");
        when(voucherService.createVoucher(any(VoucherRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/vouchers/admin/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROMO2026"));
    }

    @Test
    void testListVouchersEndpoint() throws Exception {
        when(voucherService.findAllVouchers()).thenReturn(
                Arrays.asList(buildVoucherResponse("V1"), buildVoucherResponse("V2"))
        );

        mockMvc.perform(get("/api/vouchers/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testAvailableVouchersEndpoint() throws Exception {
        when(voucherService.findAvailableVouchers()).thenReturn(
                Arrays.asList(buildVoucherResponse("DISKON10"), buildVoucherResponse("DISKON20"))
        );

        mockMvc.perform(get("/api/vouchers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void testAvailableVouchersEndpointEmpty() throws Exception {
        when(voucherService.findAvailableVouchers()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/vouchers/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }


    @Test
    void testValidateVoucherSuccess() throws Exception {
        String code = "DISKON50";
        Double amount = 100000.0;
        Double discount = 50000.0;

        java.util.Map<String, Object> requestBody = java.util.Map.of(
                "code", code,
                "amount", amount
        );

        when(voucherService.calculateDiscount(code, amount)).thenReturn(discount);

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(50000.0))
                .andExpect(jsonPath("$.finalPrice").value(50000.0));
    }

    @Test
    void testValidateVoucherFailed() throws Exception {
        String code = "KODE_SALAH";
        Double amount = 50000.0;
        String errorMessage = "Voucher tidak ditemukan!";

        java.util.Map<String, Object> requestBody = java.util.Map.of(
                "code", code,
                "amount", amount
        );

        when(voucherService.calculateDiscount(code, amount))
                .thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    void testUseVoucherSuccess() throws Exception {
        Map<String, String> requestBody = Map.of("code", "DISKON50");
        doNothing().when(voucherService).useVoucher("DISKON50");

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Kuota voucher DISKON50 berhasil dikurangi"));
    }

    @Test
    void testUseVoucherEmptyCode() throws Exception {
        Map<String, String> requestBody = Map.of("code", "");

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kode voucher wajib diisi"));

        verify(voucherService, never()).useVoucher(any());
    }

    @Test
    void testUseVoucherMissingCode() throws Exception {
        Map<String, String> requestBody = Map.of();

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kode voucher wajib diisi"));

        verify(voucherService, never()).useVoucher(any());
    }

    @Test
    void testUseVoucherServiceThrowsException() throws Exception {
        Map<String, String> requestBody = Map.of("code", "INVALID");
        doThrow(new IllegalArgumentException("Voucher tidak ditemukan"))
                .when(voucherService).useVoucher("INVALID");

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
    }

    @Test
    void testUseVoucherOutOfQuota() throws Exception {
        Map<String, String> requestBody = Map.of("code", "DISKON50");
        doThrow(new IllegalStateException("Kuota voucher habis!"))
                .when(voucherService).useVoucher("DISKON50");

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Kuota voucher habis!"));
    }

    @Test
    void testUpdateByAdminSuccess() throws Exception {
        VoucherResponse updated = buildVoucherResponse("DISKON50");

        Map<String, Object> requestBody = Map.of("additionalQuota", 50, "isActive", true);
        when(voucherService.updateVoucherAdmin(eq("DISKON50"), eq(50), isNull(), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"))
                .andExpect(jsonPath("$.quota").value(100))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void testUpdateByAdminWithNewExpiry() throws Exception {
        String expiryStr = "2027-01-01T00:00:00";
        LocalDateTime expiry = LocalDateTime.parse(expiryStr);

        VoucherResponse updated = buildVoucherResponse("DISKON50");

        Map<String, Object> requestBody = Map.of("newExpiry", expiryStr);
        when(voucherService.updateVoucherAdmin(eq("DISKON50"), isNull(), eq(expiry), isNull()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"));
    }

    @Test
    void testUpdateByAdminVoucherExpiredThrows() throws Exception {
        Map<String, Object> requestBody = Map.of("additionalQuota", 10);
        when(voucherService.updateVoucherAdmin(eq("EXPIRED"), eq(10), isNull(), isNull()))
                .thenThrow(new IllegalStateException("Tidak bisa mengubah voucher yang sudah kadaluwarsa"));

        mockMvc.perform(patch("/api/vouchers/admin/update/EXPIRED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Tidak bisa mengubah voucher yang sudah kadaluwarsa"));
    }

    @Test
    void testUpdateByAdminVoucherNotFound() throws Exception {
        Map<String, Object> requestBody = Map.of("additionalQuota", 10);
        when(voucherService.updateVoucherAdmin(eq("INVALID"), eq(10), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("Voucher tidak ditemukan"));

        mockMvc.perform(patch("/api/vouchers/admin/update/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
    }

    @PatchMapping("/admin/update/{code}")
    public ResponseEntity<?> updateByAdmin(
            @PathVariable String code,
            @RequestBody Map<String, Object> body) {
        try {
            Integer addQuota = (Integer) body.get("additionalQuota");
            Boolean status = (Boolean) body.get("isActive");
            LocalDateTime expiry = body.containsKey("newExpiry") ?
                    LocalDateTime.parse(body.get("newExpiry").toString()) : null;

            return ResponseEntity.ok(voucherService.updateVoucherAdmin(code, addQuota, expiry, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
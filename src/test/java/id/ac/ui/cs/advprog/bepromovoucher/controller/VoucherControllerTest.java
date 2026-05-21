package id.ac.ui.cs.advprog.bepromovoucher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bepromovoucher.config.TestSecurityConfig;
import id.ac.ui.cs.advprog.bepromovoucher.dto.UpdateVoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.ValidateVoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VoucherController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
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
    @WithMockUser(roles = "ADMIN")
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
    @WithMockUser(roles = "ADMIN")
    void testListVouchersEndpoint() throws Exception {
        when(voucherService.findAllVouchers()).thenReturn(
                Arrays.asList(buildVoucherResponse("V1"), buildVoucherResponse("V2"))
        );

        mockMvc.perform(get("/api/vouchers/admin/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateByAdminSuccess() throws Exception {
        VoucherResponse updated = buildVoucherResponse("DISKON50");
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setAdditionalQuota(50);
        request.setIsActive(true);

        when(voucherService.updateVoucherAdmin("DISKON50", 50, null, true))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateByAdminWithNewExpiry() throws Exception {
        LocalDateTime expiry = LocalDateTime.of(2027, 1, 1, 0, 0);
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setNewExpiry(expiry);

        VoucherResponse updated = buildVoucherResponse("DISKON50");
        when(voucherService.updateVoucherAdmin(
                eq("DISKON50"), isNull(), any(LocalDateTime.class), isNull()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateByAdminVoucherExpiredThrows() throws Exception {
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setAdditionalQuota(10);

        when(voucherService.updateVoucherAdmin(eq("EXPIRED"), eq(10), isNull(), isNull()))
                .thenThrow(new IllegalStateException(
                        "Tidak bisa mengubah voucher yang sudah kadaluwarsa"));

        mockMvc.perform(patch("/api/vouchers/admin/update/EXPIRED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "Tidak bisa mengubah voucher yang sudah kadaluwarsa"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateByAdminVoucherNotFound() throws Exception {
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setAdditionalQuota(10);

        when(voucherService.updateVoucherAdmin(eq("INVALID"), eq(10), isNull(), isNull()))
                .thenThrow(new IllegalArgumentException("Voucher tidak ditemukan"));

        mockMvc.perform(patch("/api/vouchers/admin/update/INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAdminEndpointWithUserRoleReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/vouchers/admin/list"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testAdminEndpointWithoutAuthReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/vouchers/admin/list"))
                .andExpect(status().isForbidden());
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

        ValidateVoucherRequest requestBody = new ValidateVoucherRequest();
        requestBody.setCode(code);
        requestBody.setAmount(amount);

        when(voucherService.calculateDiscount(code, amount)).thenReturn(discount);

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(50000.0))
                .andExpect(jsonPath("$.finalAmount").value(50000.0));
    }

    @Test
    void testValidateVoucherFailed() throws Exception {
        ValidateVoucherRequest requestBody = new ValidateVoucherRequest();
        requestBody.setCode("INVALID");
        requestBody.setAmount(50000.0);

        when(voucherService.calculateDiscount("INVALID", 50000.0))
                .thenThrow(new RuntimeException("Voucher tidak ditemukan"));

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
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
                .andExpect(jsonPath("$.message").value(
                        "Kuota voucher DISKON50 berhasil dikurangi"));
    }

    @Test
    void testUseVoucherEmptyCode() throws Exception {
        Map<String, String> requestBody = Map.of("code", "");

        mockMvc.perform(post("/api/vouchers/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
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
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Kuota voucher habis!"));
    }

    @Test
    void testRestoreVoucherSuccess() throws Exception {
        String idempotencyKey = "order-123-restore";
        Map<String, String> requestBody = Map.of("code", "DISKON50");

        when(voucherService.restoreVoucher("DISKON50", idempotencyKey))
                .thenReturn("Kuota voucher DISKON50 berhasil dikembalikan");

        mockMvc.perform(post("/api/vouchers/restore")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(
                        "Kuota voucher DISKON50 berhasil dikembalikan"));
    }

    @Test
    void testRestoreVoucherIdempotentReturnsSameResponse() throws Exception {
        String idempotencyKey = "order-123-restore";
        Map<String, String> requestBody = Map.of("code", "DISKON50");
        String expectedMessage = "Kuota voucher DISKON50 berhasil dikembalikan";

        when(voucherService.restoreVoucher("DISKON50", idempotencyKey))
                .thenReturn(expectedMessage);

        mockMvc.perform(post("/api/vouchers/restore")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedMessage));

        mockMvc.perform(post("/api/vouchers/restore")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(expectedMessage));

        verify(voucherService, times(2)).restoreVoucher("DISKON50", idempotencyKey);
    }

    @Test
    void testRestoreVoucherEmptyCode() throws Exception {
        Map<String, String> requestBody = Map.of("code", "");

        mockMvc.perform(post("/api/vouchers/restore")
                        .header("Idempotency-Key", "some-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Kode voucher wajib diisi"));

        verify(voucherService, never()).restoreVoucher(any(), any());
    }

    @Test
    void testRestoreVoucherNotFound() throws Exception {
        String idempotencyKey = "order-999-restore";
        Map<String, String> requestBody = Map.of("code", "INVALID");

        doThrow(new IllegalArgumentException("Voucher tidak ditemukan"))
                .when(voucherService).restoreVoucher("INVALID", idempotencyKey);

        mockMvc.perform(post("/api/vouchers/restore")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
    }
}
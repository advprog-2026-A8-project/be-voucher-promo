package id.ac.ui.cs.advprog.bepromovoucher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bepromovoucher.dto.UpdateVoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.ValidateVoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
    void testValidateVoucherSuccess() throws Exception {
        String code = "DISKON50";
        Double amount = 100000.0;
        Double discount = 50000.0;

        ValidateVoucherRequest requestBody = new ValidateVoucherRequest();
        requestBody.setCode(code);
        requestBody.setAmount(amount);

        when(voucherService.calculateDiscount(eq(code), eq(amount))).thenReturn(discount);

        mockMvc.perform(post("/api/vouchers/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.discountAmount").value(50000.0))
                .andExpect(jsonPath("$.finalPrice").value(50000.0));
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
    void testUpdateByAdminSuccess() throws Exception {
        VoucherResponse updated = buildVoucherResponse("DISKON50");
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setAdditionalQuota(50);
        request.setIsActive(true);

        when(voucherService.updateVoucherAdmin(eq("DISKON50"), eq(50), isNull(), eq(true)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"));
    }

    @Test
    void testUpdateByAdminWithNewExpiry() throws Exception {
        LocalDateTime expiry = LocalDateTime.of(2027, 1, 1, 0, 0);
        UpdateVoucherRequest request = new UpdateVoucherRequest();
        request.setNewExpiry(expiry);

        VoucherResponse updated = buildVoucherResponse("DISKON50");

        when(voucherService.updateVoucherAdmin(eq("DISKON50"), isNull(), any(LocalDateTime.class), isNull()))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/vouchers/admin/update/DISKON50")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("DISKON50"));
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
}
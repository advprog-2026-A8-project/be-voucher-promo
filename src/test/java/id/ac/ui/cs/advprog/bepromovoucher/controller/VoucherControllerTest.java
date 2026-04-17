package id.ac.ui.cs.advprog.bepromovoucher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void testCreateVoucherEndpoint() throws Exception {
        VoucherRequest request = new VoucherRequest();
        request.setCode("PROMO2026");
        request.setDiscountType("FIXED_AMOUNT");

        Voucher voucher = Voucher.builder().code("PROMO2026").build();

        when(voucherService.createVoucher(any(VoucherRequest.class))).thenReturn(voucher);

        mockMvc.perform(post("/api/vouchers/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("PROMO2026"));
    }

    @Test
    void testListVouchersEndpoint() throws Exception {
        when(voucherService.findAllVouchers()).thenReturn(Arrays.asList(new Voucher(), new Voucher()));

        mockMvc.perform(get("/api/vouchers/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
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
}
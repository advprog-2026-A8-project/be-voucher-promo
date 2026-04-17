package id.ac.ui.cs.advprog.bepromovoucher.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testHandleVoucherNotFoundException() throws Exception {
        String code = "PROMO-PALSU";
        String expectedMessage = "Voucher dengan kode " + code + " tidak ditemukan!";

        mockMvc.perform(get("/test/not-found/" + code)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    void testHandleInvalidVoucherException() throws Exception {
        mockMvc.perform(get("/test/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher tidak valid"));
    }

    @RestController
    static class TestController {
        @GetMapping("/test/not-found/{code}")
        public void triggerNotFound(@PathVariable String code) {
            throw new VoucherNotFoundException(code);
        }

        @GetMapping("/test/invalid")
        public void triggerInvalid() {
            throw new InvalidVoucherException("Voucher tidak valid");
        }
    }
}
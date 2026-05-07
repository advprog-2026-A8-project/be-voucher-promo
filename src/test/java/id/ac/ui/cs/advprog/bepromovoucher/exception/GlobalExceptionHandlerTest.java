package id.ac.ui.cs.advprog.bepromovoucher.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void testHandleIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test/illegal-argument")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Voucher tidak ditemukan"));
    }

    @Test
    void testHandleIllegalStateException() throws Exception {
        mockMvc.perform(get("/test/illegal-state")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Kuota voucher habis"));
    }

    @Test
    void testHandleMethodArgumentNotValidException() throws Exception {
        String requestBody = "{\"name\": \"\"}";

        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validasi gagal"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.name").value("Nama wajib diisi"));
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

        @GetMapping("/test/illegal-argument")
        public void triggerIllegalArgument() {
            throw new IllegalArgumentException("Voucher tidak ditemukan");
        }

        @GetMapping("/test/illegal-state")
        public void triggerIllegalState() {
            throw new IllegalStateException("Kuota voucher habis");
        }

        @PostMapping("/test/valid")
        public void triggerValidation(@Valid @RequestBody TestRequest request) {
            // intentionally empty — this method exists solely to trigger
            // @Valid validation. The exception is handled by
            // GlobalExceptionHandler before this method body executes.
        }
    }

    @Data
    static class TestRequest {
        @NotBlank(message = "Nama wajib diisi")
        private String name;
    }
}
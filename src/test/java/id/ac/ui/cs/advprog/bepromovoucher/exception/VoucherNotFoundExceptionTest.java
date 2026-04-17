package id.ac.ui.cs.advprog.bepromovoucher.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VoucherNotFoundExceptionTest {

    @Test
    void testExceptionMessageFormat() {
        String code = "DISKON-PALSU";
        VoucherNotFoundException exception = new VoucherNotFoundException(code);

        String expectedMessage = "Voucher dengan kode " + code + " tidak ditemukan!";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        VoucherNotFoundException exception = new VoucherNotFoundException("TEST");

        assertTrue(exception instanceof RuntimeException);
    }
}
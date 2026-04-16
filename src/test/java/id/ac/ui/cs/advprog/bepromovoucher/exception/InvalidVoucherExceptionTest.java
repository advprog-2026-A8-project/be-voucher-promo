package id.ac.ui.cs.advprog.bepromovoucher.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidVoucherExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Voucher sudah tidak berlaku";
        InvalidVoucherException exception = new InvalidVoucherException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidVoucherException exception = new InvalidVoucherException("Test");

        assertTrue(exception instanceof RuntimeException);
    }
}
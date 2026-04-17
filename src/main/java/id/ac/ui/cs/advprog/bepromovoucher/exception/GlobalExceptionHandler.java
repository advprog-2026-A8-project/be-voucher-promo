package id.ac.ui.cs.advprog.bepromovoucher.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VoucherNotFoundException.class)
    public ResponseEntity<Object> handleVoucherNotFound(VoucherNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidVoucherException.class)
    public ResponseEntity<Object> handleInvalidVoucher(InvalidVoucherException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
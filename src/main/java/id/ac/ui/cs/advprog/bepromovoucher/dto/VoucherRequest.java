package id.ac.ui.cs.advprog.bepromovoucher.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {
    private String code;
    private Integer quota;
    private Double discountValue;
    private Double minPurchase;
    private String discountType;
    private LocalDateTime expiryDate;
    private String termsAndConditions;
}
package id.ac.ui.cs.advprog.bepromovoucher.dto;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class VoucherResponse {
    private String code;
    private DiscountType discountType;
    private Double discountValue;
    private Double minPurchase;
    private LocalDateTime expiryDate;
    private Integer quota;
    private String termsAndConditions;
    private boolean active;
}
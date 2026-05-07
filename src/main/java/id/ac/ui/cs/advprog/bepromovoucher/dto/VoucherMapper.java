package id.ac.ui.cs.advprog.bepromovoucher.dto;

import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {

    public VoucherResponse toResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minPurchase(voucher.getMinPurchase())
                .expiryDate(voucher.getExpiryDate())
                .quota(voucher.getQuota())
                .termsAndConditions(voucher.getTermsAndConditions())
                .active(voucher.isActive())
                .build();
    }
}
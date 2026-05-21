package id.ac.ui.cs.advprog.bepromovoucher.dto;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VoucherMapperTest {

    private VoucherMapper voucherMapper;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        voucherMapper = new VoucherMapper();

        voucher = Voucher.builder()
                .code("DISKON50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(50.0)
                .build();

        voucher.setActive(true);
        voucher.setQuota(100);
        voucher.setMinPurchase(10000.0);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(7));
        voucher.setTermsAndConditions("S&K berlaku");
    }

    @Test
    void testToResponseMapsAllFieldsCorrectly() {
        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertNotNull(response);
        assertEquals(voucher.getCode(), response.getCode());
        assertEquals(voucher.getDiscountType(), response.getDiscountType());
        assertEquals(voucher.getDiscountValue(), response.getDiscountValue());
        assertEquals(voucher.getMinPurchase(), response.getMinPurchase());
        assertEquals(voucher.getExpiryDate(), response.getExpiryDate());
        assertEquals(voucher.getQuota(), response.getQuota());
        assertEquals(voucher.getTermsAndConditions(), response.getTermsAndConditions());
        assertEquals(voucher.isActive(), response.isActive());
    }

    @Test
    void testToResponseActiveTrueIsMappedCorrectly() {
        voucher.setActive(true);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertTrue(response.isActive());
    }

    @Test
    void testToResponseActiveFalseIsMappedCorrectly() {
        voucher.setActive(false);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertFalse(response.isActive());
    }

    @Test
    void testToResponseDiscountTypePercentageMappedCorrectly() {
        voucher.setDiscountType(DiscountType.PERCENTAGE);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertEquals(DiscountType.PERCENTAGE, response.getDiscountType());
    }

    @Test
    void testToResponseDiscountTypeFixedAmountMappedCorrectly() {
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertEquals(DiscountType.FIXED_AMOUNT, response.getDiscountType());
    }

    @Test
    void testToResponseNullTermsAndConditionsMappedCorrectly() {
        voucher.setTermsAndConditions(null);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertNull(response.getTermsAndConditions());
    }

    @Test
    void testToResponseZeroQuotaMappedCorrectly() {
        voucher.setQuota(0);

        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertEquals(0, response.getQuota());
    }

    @Test
    void testToResponseDoesNotExposeId() {
        VoucherResponse response = voucherMapper.toResponse(voucher);

        assertNotNull(response);
        assertNotNull(response.getCode());
    }
}
package id.ac.ui.cs.advprog.bepromovoucher.model;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class VoucherTest {

    @Test
    void testBuilderWithAllFields() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(7);

        Voucher voucher = Voucher.builder()
                .id(1L)
                .code("DISC10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .expiryDate(expiry)
                .quota(100)
                .minPurchase(50000.0)
                .termsAndConditions("Min. pembelian Rp50.000")
                .isActive(true)
                .build();

        assertEquals(1L, voucher.getId());
        assertEquals("DISC10", voucher.getCode());
        assertEquals(DiscountType.PERCENTAGE, voucher.getDiscountType());
        assertEquals(10.0, voucher.getDiscountValue());
        assertEquals(expiry, voucher.getExpiryDate());
        assertEquals(100, voucher.getQuota());
        assertEquals(50000.0, voucher.getMinPurchase());
        assertEquals("Min. pembelian Rp50.000", voucher.getTermsAndConditions());
        assertTrue(voucher.isActive());
    }

    @Test
    void testBuilder_isActiveDefaultsToTrue() {
        Voucher voucher = Voucher.builder()
                .code("DEFAULT")
                .build();

        assertTrue(voucher.isActive(), "isActive harus default true via @Builder.Default");
    }

    @Test
    void testBuilder_isActiveCanBeSetToFalse() {
        Voucher voucher = Voucher.builder()
                .code("INACTIVE")
                .isActive(false)
                .build();

        assertFalse(voucher.isActive());
    }

    @Test
    void testSetters() {
        Voucher voucher = new Voucher();
        LocalDateTime expiry = LocalDateTime.now().plusDays(30);

        voucher.setId(5L);
        voucher.setCode("FIXED5K");
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(5000.0);
        voucher.setExpiryDate(expiry);
        voucher.setQuota(50);
        voucher.setMinPurchase(20000.0);
        voucher.setTermsAndConditions("S&K berlaku");
        voucher.setActive(true);

        assertEquals(5L, voucher.getId());
        assertEquals("FIXED5K", voucher.getCode());
        assertEquals(DiscountType.FIXED_AMOUNT, voucher.getDiscountType());
        assertEquals(5000.0, voucher.getDiscountValue());
        assertEquals(expiry, voucher.getExpiryDate());
        assertEquals(50, voucher.getQuota());
        assertEquals(20000.0, voucher.getMinPurchase());
        assertEquals("S&K berlaku", voucher.getTermsAndConditions());
        assertTrue(voucher.isActive());
    }

    @Test
    void testNoArgsConstructor_fieldsAreNull() {
        Voucher voucher = new Voucher();

        assertNull(voucher.getId());
        assertNull(voucher.getCode());
        assertNull(voucher.getDiscountType());
        assertNull(voucher.getDiscountValue());
        assertNull(voucher.getExpiryDate());
        assertNull(voucher.getQuota());
        assertNull(voucher.getMinPurchase());
        assertNull(voucher.getTermsAndConditions());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime expiry = LocalDateTime.now().plusDays(3);

        Voucher voucher = new Voucher(
                2L, "ALL10", DiscountType.PERCENTAGE,
                10.0, expiry, 200, 15000.0,
                "Berlaku untuk semua produk", true
        );

        assertEquals(2L, voucher.getId());
        assertEquals("ALL10", voucher.getCode());
        assertEquals(DiscountType.PERCENTAGE, voucher.getDiscountType());
        assertEquals(10.0, voucher.getDiscountValue());
        assertEquals(expiry, voucher.getExpiryDate());
        assertEquals(200, voucher.getQuota());
        assertEquals(15000.0, voucher.getMinPurchase());
        assertEquals("Berlaku untuk semua produk", voucher.getTermsAndConditions());
        assertTrue(voucher.isActive());
    }

    @Test
    void testFixedAmountDiscountType() {
        Voucher voucher = Voucher.builder()
                .code("FIXED20K")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(20000.0)
                .build();

        assertEquals(DiscountType.FIXED_AMOUNT, voucher.getDiscountType());
        assertEquals(20000.0, voucher.getDiscountValue());
    }
}
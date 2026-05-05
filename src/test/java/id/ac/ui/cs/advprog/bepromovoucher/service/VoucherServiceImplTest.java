package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private VoucherRequest request;
    private Voucher voucher;

    @BeforeEach
    void setUp() {
        request = new VoucherRequest();
        request.setCode("DISKON50");
        request.setDiscountType("PERCENTAGE");
        request.setDiscountValue(50.0);
        request.setQuota(100);
        request.setMinPurchase(10000.0);
        request.setExpiryDate(LocalDateTime.now().plusDays(7));

        voucher = Voucher.builder()
                .code("DISKON50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(50.0)
                .build();

        voucher.setActive(true);
        voucher.setQuota(100);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(7));
        voucher.setMinPurchase(10000.0);
    }

    @Test
    void testCreateVoucherSuccess() {
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher created = voucherService.createVoucher(request);

        assertNotNull(created);
        assertEquals("DISKON50", created.getCode());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
    }

    @Test
    void testFindAllVouchers() {
        List<Voucher> vouchers = Arrays.asList(voucher, new Voucher());
        when(voucherRepository.findAll()).thenReturn(vouchers);

        List<Voucher> result = voucherService.findAllVouchers();

        assertEquals(2, result.size());
        verify(voucherRepository, times(1)).findAll();
    }

    @Test
    void testValidateVoucherNotFound() {
        when(voucherRepository.findByCode("INVALID")).thenReturn(java.util.Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            voucherService.calculateDiscount("INVALID", 100000.0);
        });

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
    }

    @Test
    void testValidateVoucherExpired() {
        voucher.setActive(true);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        voucher.setMinPurchase(1000.0);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            voucherService.calculateDiscount("DISKON50", 50000.0);
        });

        assertEquals("Voucher kadaluwarsa", exception.getMessage());
    }

    @Test
    void testValidateVoucherOutOfQuota() {
        voucher.setActive(true);
        voucher.setQuota(0);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(1000.0);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            voucherService.calculateDiscount("DISKON50", 50000.0);
        });

        assertEquals("Kuota voucher habis", exception.getMessage());
    }

    @Test
    void testValidateVoucherMinPurchaseNotMet() {
        voucher.setActive(true);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(100000.0);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            voucherService.calculateDiscount("DISKON50", 50000.0);
        });

        assertEquals("Minimal pembelian tidak terpenuhi", exception.getMessage());
    }

    @Test
    void testValidateVoucherIsActiveSuccess() {
        voucher.setActive(true);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(1000.0);
        voucher.setDiscountValue(10.0);
        voucher.setDiscountType(DiscountType.PERCENTAGE);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        assertDoesNotThrow(() -> {
            voucherService.calculateDiscount("DISKON50", 20000.0);
        });

        verify(voucherRepository, times(1)).findByCode("DISKON50");
    }

    @Test
    void testValidateVoucherIsInactiveThrowsException() {
        voucher.setActive(false);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(1000.0);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            voucherService.calculateDiscount("DISKON50", 20000.0);
        });

        assertEquals("Voucher tidak aktif", exception.getMessage());
    }

    @Test
    void testCalculateDiscountPercentageSuccess() {
        voucher.setActive(true);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(50000.0);
        voucher.setDiscountValue(10.0);
        voucher.setDiscountType(DiscountType.PERCENTAGE);

        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        Double discount = voucherService.calculateDiscount("DISKON50", 100000.0);

        assertEquals(10000.0, discount);
    }

    @Test
    void testCalculateDiscountFixedAmount() {
        voucher.setActive(true);
        voucher.setQuota(10);
        voucher.setExpiryDate(LocalDateTime.now().plusDays(1));
        voucher.setMinPurchase(50000.0);
        voucher.setDiscountValue(15000.0);
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);

        when(voucherRepository.findByCode("FIXED15")).thenReturn(java.util.Optional.of(voucher));
        Double discount = voucherService.calculateDiscount("FIXED15", 100000.0);
        assertEquals(15000.0, discount);
    }

    @Test
    void testUseVoucherSuccess() {
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        assertDoesNotThrow(() -> voucherService.useVoucher("DISKON50"));

        assertEquals(99, voucher.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUseVoucherNotFound() {
        when(voucherRepository.findByCodeWithLock("INVALID")).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.useVoucher("INVALID"));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherOutOfQuota() {
        voucher.setQuota(0);
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Kuota voucher habis!", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherInactive() {
        voucher.setActive(false);
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Voucher sudah tidak valid", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherExpired() {
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Voucher sudah tidak valid", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminNotFound() {
        when(voucherRepository.findByCode("INVALID")).thenReturn(java.util.Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.updateVoucherAdmin("INVALID", 10, null, null));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminAddQuotaSuccess() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher result = voucherService.updateVoucherAdmin("DISKON50", 20, null, null);

        assertEquals(120, result.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminAddQuotaOnExpiredVoucherThrows() {
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.updateVoucherAdmin("DISKON50", 10, null, null));

        assertEquals("Tidak bisa menambah kuota voucher yang sudah kadaluwarsa", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminZeroOrNegativeQuotaIgnored() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher result = voucherService.updateVoucherAdmin("DISKON50", 0, null, null);

        assertEquals(100, result.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminUpdateExpiry() {
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(30);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher result = voucherService.updateVoucherAdmin("DISKON50", null, newExpiry, null);

        assertEquals(newExpiry, result.getExpiryDate());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminSetActiveStatus() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher result = voucherService.updateVoucherAdmin("DISKON50", null, null, false);

        assertFalse(result.isActive());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminAllFieldsUpdated() {
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(14);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(java.util.Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        Voucher result = voucherService.updateVoucherAdmin("DISKON50", 50, newExpiry, false);

        assertEquals(150, result.getQuota());
        assertEquals(newExpiry, result.getExpiryDate());
        assertFalse(result.isActive());
        verify(voucherRepository, times(1)).save(voucher);
    }
}
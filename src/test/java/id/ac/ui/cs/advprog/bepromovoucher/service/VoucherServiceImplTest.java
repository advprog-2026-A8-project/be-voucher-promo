package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherMapper;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.IdempotencyRecord;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.repository.IdempotencyRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherMapper voucherMapper;

    @Mock
    private IdempotencyRepository idempotencyRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private VoucherRequest request;
    private Voucher voucher;
    private VoucherResponse voucherResponse;

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

        voucherResponse = VoucherResponse.builder()
                .code("DISKON50")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(50.0)
                .quota(100)
                .active(true)
                .minPurchase(10000.0)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .termsAndConditions("S&K berlaku")
                .build();
    }

    @Test
    void testCreateVoucherSuccess() {
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse created = voucherService.createVoucher(request);

        assertNotNull(created);
        assertEquals("DISKON50", created.getCode());
        verify(voucherRepository, times(1)).save(any(Voucher.class));
        verify(voucherMapper, times(1)).toResponse(any(Voucher.class));
    }

    @Test
    void testFindAllVouchers() {
        List<Voucher> vouchers = Arrays.asList(voucher, new Voucher());
        when(voucherRepository.findAll()).thenReturn(vouchers);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        List<VoucherResponse> result = voucherService.findAllVouchers();

        assertEquals(2, result.size());
        verify(voucherRepository, times(1)).findAll();
        verify(voucherMapper, times(2)).toResponse(any(Voucher.class));
    }

    @Test
    void testFindAvailableVouchers() {
        List<Voucher> available = Arrays.asList(voucher);
        when(voucherRepository.findAvailableVouchers(any(LocalDateTime.class))).thenReturn(available);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        List<VoucherResponse> result = voucherService.findAvailableVouchers();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
        verify(voucherRepository, times(1)).findAvailableVouchers(any(LocalDateTime.class));
    }

    @Test
    void testFindAvailableVouchersEmpty() {
        when(voucherRepository.findAvailableVouchers(any(LocalDateTime.class)))
                .thenReturn(List.of());

        List<VoucherResponse> result = voucherService.findAvailableVouchers();

        assertEquals(0, result.size());
        verify(voucherRepository, times(1)).findAvailableVouchers(any(LocalDateTime.class));
    }

    @Test
    void testValidateVoucherNotFound() {
        when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.calculateDiscount("INVALID", 100000.0));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
    }

    @Test
    void testValidateVoucherExpired() {
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateDiscount("DISKON50", 50000.0));

        assertEquals("Voucher kadaluwarsa", exception.getMessage());
    }

    @Test
    void testValidateVoucherOutOfQuota() {
        voucher.setQuota(0);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateDiscount("DISKON50", 50000.0));

        assertEquals("Kuota voucher habis", exception.getMessage());
    }

    @Test
    void testValidateVoucherMinPurchaseNotMet() {
        voucher.setMinPurchase(100000.0);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateDiscount("DISKON50", 50000.0));

        assertEquals("Minimal pembelian tidak terpenuhi", exception.getMessage());
    }

    @Test
    void testValidateVoucherIsActiveSuccess() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        assertDoesNotThrow(() -> voucherService.calculateDiscount("DISKON50", 20000.0));

        verify(voucherRepository, times(1)).findByCode("DISKON50");
    }

    @Test
    void testValidateVoucherIsInactiveThrowsException() {
        voucher.setActive(false);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                voucherService.calculateDiscount("DISKON50", 20000.0));

        assertEquals("Voucher tidak aktif", exception.getMessage());
    }

    @Test
    void testCalculateDiscountPercentageSuccess() {
        voucher.setDiscountValue(10.0);
        voucher.setMinPurchase(50000.0);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Double discount = voucherService.calculateDiscount("DISKON50", 100000.0);

        assertEquals(10000.0, discount);
    }

    @Test
    void testCalculateDiscountFixedAmount() {
        voucher.setDiscountType(DiscountType.FIXED_AMOUNT);
        voucher.setDiscountValue(15000.0);
        voucher.setMinPurchase(50000.0);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Double discount = voucherService.calculateDiscount("DISKON50", 100000.0);

        assertEquals(15000.0, discount);
    }

    @Test
    void testUseVoucherSuccess() {
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);

        assertDoesNotThrow(() -> voucherService.useVoucher("DISKON50"));

        assertEquals(99, voucher.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUseVoucherNotFound() {
        when(voucherRepository.findByCodeWithLock("INVALID")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.useVoucher("INVALID"));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherOutOfQuota() {
        voucher.setQuota(0);
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Kuota voucher habis!", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherInactive() {
        voucher.setActive(false);
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Voucher sudah tidak valid", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUseVoucherExpired() {
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(voucherRepository.findByCodeWithLock("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.useVoucher("DISKON50"));

        assertEquals("Voucher sudah tidak valid", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminNotFound() {
        when(voucherRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.updateVoucherAdmin("INVALID", 10, null, null));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminAddQuotaSuccess() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucherAdmin("DISKON50", 20, null, null);

        assertNotNull(result);
        assertEquals(120, voucher.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminAddQuotaOnExpiredVoucherThrows() {
        voucher.setExpiryDate(LocalDateTime.now().minusDays(1));
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));

        Exception exception = assertThrows(IllegalStateException.class, () ->
                voucherService.updateVoucherAdmin("DISKON50", 10, null, null));

        assertEquals("Tidak bisa mengubah voucher yang sudah kadaluwarsa", exception.getMessage());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void testUpdateVoucherAdminZeroOrNegativeQuotaIgnored() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucherAdmin("DISKON50", 0, null, null);

        assertNotNull(result);
        assertEquals(100, voucher.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminUpdateExpiry() {
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(30);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucherAdmin("DISKON50", null, newExpiry, null);

        assertNotNull(result);
        assertEquals(newExpiry, voucher.getExpiryDate());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminSetActiveStatus() {
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucherAdmin("DISKON50", null, null, false);

        assertNotNull(result);
        assertFalse(voucher.isActive());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testUpdateVoucherAdminAllFieldsUpdated() {
        LocalDateTime newExpiry = LocalDateTime.now().plusDays(14);
        when(voucherRepository.findByCode("DISKON50")).thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(voucherMapper.toResponse(any(Voucher.class))).thenReturn(voucherResponse);

        VoucherResponse result = voucherService.updateVoucherAdmin("DISKON50", 50, newExpiry, false);

        assertNotNull(result);
        assertEquals(150, voucher.getQuota());
        assertEquals(newExpiry, voucher.getExpiryDate());
        assertFalse(voucher.isActive());
        verify(voucherRepository, times(1)).save(voucher);
    }

    @Test
    void testDeactivateExpiredVouchers() {
        doNothing().when(voucherRepository).deactivateExpiredVouchers(any(LocalDateTime.class));

        assertDoesNotThrow(() -> voucherService.deactivateExpiredVouchers());

        verify(voucherRepository, times(1)).deactivateExpiredVouchers(any(LocalDateTime.class));
    }

    @Test
    void testRestoreVoucherSuccess() {
        String idempotencyKey = "order-123-restore";

        when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(voucherRepository.findByCodeWithLock("DISKON50"))
                .thenReturn(Optional.of(voucher));
        when(voucherRepository.save(any(Voucher.class))).thenReturn(voucher);
        when(idempotencyRepository.save(any(IdempotencyRecord.class)))
                .thenReturn(new IdempotencyRecord());

        String result = voucherService.restoreVoucher("DISKON50", idempotencyKey);

        assertEquals("Kuota voucher DISKON50 berhasil dikembalikan", result);
        assertEquals(101, voucher.getQuota());
        verify(voucherRepository, times(1)).save(voucher);
        verify(idempotencyRepository, times(1)).save(any(IdempotencyRecord.class));
    }

    @Test
    void testRestoreVoucherIdempotentReturnsSameResponse() {
        String idempotencyKey = "order-123-restore";
        String cachedMessage = "Kuota voucher DISKON50 berhasil dikembalikan";

        IdempotencyRecord existing = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .voucherCode("DISKON50")
                .responseMessage(cachedMessage)
                .build();

        when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.of(existing));

        String result = voucherService.restoreVoucher("DISKON50", idempotencyKey);

        assertEquals(cachedMessage, result);
        assertEquals(100, voucher.getQuota());
        verify(voucherRepository, never()).findByCodeWithLock(any());
        verify(voucherRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any());
    }

    @Test
    void testRestoreVoucherNotFound() {
        String idempotencyKey = "order-999-restore";

        when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                .thenReturn(Optional.empty());
        when(voucherRepository.findByCodeWithLock("INVALID"))
                .thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                voucherService.restoreVoucher("INVALID", idempotencyKey));

        assertEquals("Voucher tidak ditemukan", exception.getMessage());
        verify(voucherRepository, never()).save(any());
        verify(idempotencyRepository, never()).save(any());
    }
}
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
                .code(request.getCode())
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(50.0)
                .build();
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
}
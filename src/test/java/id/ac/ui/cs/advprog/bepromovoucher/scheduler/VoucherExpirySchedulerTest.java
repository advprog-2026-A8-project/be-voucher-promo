package id.ac.ui.cs.advprog.bepromovoucher.scheduler;

import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherExpirySchedulerTest {

    @Mock
    private VoucherService voucherService;

    @InjectMocks
    private VoucherExpiryScheduler scheduler;

    @Test
    void testRunDeactivateExpiredVouchersCallsService() {
        doNothing().when(voucherService).deactivateExpiredVouchers();

        scheduler.runDeactivateExpiredVouchers();

        verify(voucherService, times(1)).deactivateExpiredVouchers();
    }

    @Test
    void testRunDeactivateExpiredVouchersHandlesException() {
        doThrow(new RuntimeException("DB error")).when(voucherService).deactivateExpiredVouchers();

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> scheduler.runDeactivateExpiredVouchers());

        verify(voucherService, times(1)).deactivateExpiredVouchers();
    }
}

package id.ac.ui.cs.advprog.bepromovoucher.scheduler;

import id.ac.ui.cs.advprog.bepromovoucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherExpiryScheduler {

    private final VoucherService voucherService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runDeactivateExpiredVouchers() {
        log.info("[Scheduler] Starting deactivateExpiredVouchers job...");
        voucherService.deactivateExpiredVouchers();
        log.info("[Scheduler] Finished deactivateExpiredVouchers job.");
    }
}

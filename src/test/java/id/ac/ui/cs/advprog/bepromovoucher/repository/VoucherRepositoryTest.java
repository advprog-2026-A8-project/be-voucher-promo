package id.ac.ui.cs.advprog.bepromovoucher.repository;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class VoucherRepositoryTest {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        voucherRepository.deleteAll();

        voucherRepository.save(Voucher.builder()
                .code("ACTIVE10")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(10.0)
                .minPurchase(10000.0)
                .quota(100)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .termsAndConditions("S&K berlaku")
                .isActive(true)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("EXPIRED10")
                .discountType(DiscountType.FIXED_AMOUNT)
                .discountValue(5000.0)
                .minPurchase(10000.0)
                .quota(50)
                .expiryDate(LocalDateTime.now().minusDays(1))
                .termsAndConditions("S&K berlaku")
                .isActive(true)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("NOQUOTA")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(5.0)
                .minPurchase(10000.0)
                .quota(0)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .termsAndConditions("S&K berlaku")
                .isActive(true)
                .build());

        voucherRepository.save(Voucher.builder()
                .code("INACTIVE")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(15.0)
                .minPurchase(10000.0)
                .quota(50)
                .expiryDate(LocalDateTime.now().plusDays(7))
                .termsAndConditions("S&K berlaku")
                .isActive(false)
                .build());
    }

    @Test
    void testFindByCode_found() {
        Optional<Voucher> result = voucherRepository.findByCode("ACTIVE10");
        assertTrue(result.isPresent());
        assertEquals("ACTIVE10", result.get().getCode());
    }

    @Test
    void testFindByCode_notFound() {
        Optional<Voucher> result = voucherRepository.findByCode("NONEXISTENT");
        assertFalse(result.isPresent());
    }

    @Test
    @Transactional
    void testFindByCodeWithLock_returnsVoucher() {
        Optional<Voucher> result = voucherRepository.findByCodeWithLock("ACTIVE10");
        assertTrue(result.isPresent());
        assertEquals("ACTIVE10", result.get().getCode());
    }

    @Test
    void testFindAvailableVouchers_onlyReturnsActiveNonExpiredWithQuota() {
        List<Voucher> available = voucherRepository.findAvailableVouchers(LocalDateTime.now());

        assertEquals(1, available.size());
        assertEquals("ACTIVE10", available.get(0).getCode());

        assertTrue(available.stream().noneMatch(v -> v.getCode().equals("EXPIRED10")));
        assertTrue(available.stream().noneMatch(v -> v.getCode().equals("NOQUOTA")));
        assertTrue(available.stream().noneMatch(v -> v.getCode().equals("INACTIVE")));
    }

    @Test
    @Transactional
    void testDeactivateExpiredVouchers_doesNotAffectActiveVouchers() {
        voucherRepository.deactivateExpiredVouchers(LocalDateTime.now());
        voucherRepository.flush();

        Voucher active = voucherRepository.findByCode("ACTIVE10").orElseThrow();
        assertTrue(active.isActive(), "Active voucher should remain active");
    }

    @Test
    @Transactional
    void testFindAvailableVouchers_afterDeactivation_excludesExpired() {
        voucherRepository.deactivateExpiredVouchers(LocalDateTime.now());
        voucherRepository.flush();

        List<Voucher> available = voucherRepository.findAvailableVouchers(LocalDateTime.now());

        assertTrue(available.stream().allMatch(v ->
                v.isActive()
                        && v.getQuota() > 0
                        && v.getExpiryDate().isAfter(LocalDateTime.now())
        ));
        assertTrue(available.stream().noneMatch(v -> v.getCode().equals("EXPIRED10")));
    }
}
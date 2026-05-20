package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherMapper;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherResponse;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.IdempotencyRecord;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.repository.IdempotencyRepository;
import id.ac.ui.cs.advprog.bepromovoucher.repository.VoucherRepository;
import id.ac.ui.cs.advprog.bepromovoucher.strategy.DiscountStrategy;
import id.ac.ui.cs.advprog.bepromovoucher.strategy.DiscountStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private static final String VOUCHER_NOT_FOUND = "Voucher tidak ditemukan";

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;
    private final IdempotencyRepository idempotencyRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherRequest request) {
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()))
                .discountValue(request.getDiscountValue())
                .minPurchase(request.getMinPurchase())
                .expiryDate(request.getExpiryDate())
                .quota(request.getQuota())
                .termsAndConditions(request.getTermsAndConditions())
                .build();
        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    public List<VoucherResponse> findAllVouchers() {
        return voucherRepository.findAll()
                .stream()
                .map(voucherMapper::toResponse)
                .toList();
    }

    @Override
    public List<VoucherResponse> findAvailableVouchers() {
        return voucherRepository.findAvailableVouchers(LocalDateTime.now())
                .stream()
                .map(voucherMapper::toResponse)
                .toList();
    }

    @Override
    public Voucher validateAndGetVoucher(String code, Double purchaseAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(VOUCHER_NOT_FOUND));
        if (!voucher.isActive()) {
            throw new IllegalStateException("Voucher tidak aktif");
        }
        if (voucher.getQuota() <= 0) {
            throw new IllegalStateException("Kuota voucher habis");
        }
        if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Voucher kadaluwarsa");
        }
        if (purchaseAmount < voucher.getMinPurchase()) {
            throw new IllegalStateException("Minimal pembelian tidak terpenuhi");
        }
        return voucher;
    }

    @Override
    public Double calculateDiscount(String code, Double purchaseAmount) {
        Voucher voucher = validateAndGetVoucher(code, purchaseAmount);
        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(voucher.getDiscountType());
        return strategy.calculate(purchaseAmount, voucher.getDiscountValue());
    }

    @Override
    @Transactional
    public void useVoucher(String code) {
        Voucher voucher = voucherRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new IllegalArgumentException(VOUCHER_NOT_FOUND));
        if (voucher.getQuota() <= 0) {
            throw new IllegalStateException("Kuota voucher habis!");
        }
        if (!voucher.isActive() || voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Voucher sudah tidak valid");
        }
        voucher.setQuota(voucher.getQuota() - 1);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public String restoreVoucher(String code, String idempotencyKey) {
        Optional<IdempotencyRecord> existingRecord =
                idempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (existingRecord.isPresent()) {
            log.info("Idempotent restore request detected for key: {}", idempotencyKey);
            return existingRecord.get().getResponseMessage();
        }

        Voucher voucher = voucherRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new IllegalArgumentException(VOUCHER_NOT_FOUND));
        voucher.setQuota(voucher.getQuota() + 1);
        voucherRepository.save(voucher);

        String responseMessage = "Kuota voucher " + code + " berhasil dikembalikan";
        IdempotencyRecord idempotencyRecord = IdempotencyRecord.builder()
                .idempotencyKey(idempotencyKey)
                .voucherCode(code)
                .responseMessage(responseMessage)
                .build();
        idempotencyRepository.save(idempotencyRecord);

        log.info("Voucher {} restored with idempotency key: {}", code, idempotencyKey);
        return responseMessage;
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucherAdmin(String code, Integer additionalQuota,
                                              LocalDateTime newExpiry, Boolean activeStatus) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(VOUCHER_NOT_FOUND));
        if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Tidak bisa mengubah voucher yang sudah kadaluwarsa");
        }
        if (additionalQuota != null && additionalQuota > 0) {
            voucher.setQuota(voucher.getQuota() + additionalQuota);
        }
        if (newExpiry != null) {
            voucher.setExpiryDate(newExpiry);
        }
        if (activeStatus != null) {
            voucher.setActive(activeStatus);
        }
        return voucherMapper.toResponse(voucherRepository.save(voucher));
    }

    @Override
    @Transactional
    public void deactivateExpiredVouchers() {
        log.info("Deactivating expired vouchers at {}", LocalDateTime.now());
        voucherRepository.deactivateExpiredVouchers(LocalDateTime.now());
        log.info("Expired vouchers deactivated successfully.");
    }
}
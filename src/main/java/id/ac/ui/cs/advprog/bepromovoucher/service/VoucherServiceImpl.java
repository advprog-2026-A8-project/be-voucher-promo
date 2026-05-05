package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.*;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.repository.VoucherRepository;
import id.ac.ui.cs.advprog.bepromovoucher.strategy.DiscountStrategy;
import id.ac.ui.cs.advprog.bepromovoucher.strategy.FixedAmountDiscountStrategy;
import id.ac.ui.cs.advprog.bepromovoucher.strategy.PercentageDiscountStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional
    public Voucher createVoucher(VoucherRequest request) {
        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .discountType(DiscountType.valueOf(request.getDiscountType().toUpperCase()))
                .discountValue(request.getDiscountValue())
                .minPurchase(request.getMinPurchase())
                .expiryDate(request.getExpiryDate())
                .quota(request.getQuota())
                .termsAndConditions(request.getTermsAndConditions())
                .build();
        return voucherRepository.save(voucher);
    }

    @Override
    public List<Voucher> findAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher validateAndGetVoucher(String code, Double purchaseAmount) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Voucher tidak ditemukan"));

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

        DiscountStrategy strategy = (voucher.getDiscountType() == DiscountType.PERCENTAGE)
                ? new PercentageDiscountStrategy()
                : new FixedAmountDiscountStrategy();

        return strategy.calculate(purchaseAmount, voucher.getDiscountValue());
    }

    @Override
    @Transactional
    public void useVoucher(String code) {
        Voucher voucher = voucherRepository.findByCodeWithLock(code)
                .orElseThrow(() -> new IllegalArgumentException("Voucher tidak ditemukan"));

        if (voucher.getQuota() <= 0) {
            throw new IllegalStateException("Kuota voucher habis!");
        }

        if (!voucher.isActive() || voucher.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalStateException("Voucher sudah tidak valid");
        }

        voucher.setQuota(voucher.getQuota() - 1);
        voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher updateVoucherAdmin(String code, Integer additionalQuota, LocalDateTime newExpiry, Boolean activeStatus) {
        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Voucher tidak ditemukan"));

        if (additionalQuota != null && additionalQuota > 0) {
            if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Tidak bisa menambah kuota voucher yang sudah kadaluwarsa");
            }
            voucher.setQuota(voucher.getQuota() + additionalQuota);
        }

        if (newExpiry != null) {
            voucher.setExpiryDate(newExpiry);
        }

        if (activeStatus != null) {
            voucher.setActive(activeStatus);
        }

        return voucherRepository.save(voucher);
    }
}
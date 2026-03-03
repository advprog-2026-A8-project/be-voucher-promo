package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.dto.*;
import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}
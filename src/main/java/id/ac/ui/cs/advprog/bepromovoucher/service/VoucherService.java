package id.ac.ui.cs.advprog.bepromovoucher.service;

import id.ac.ui.cs.advprog.bepromovoucher.model.Voucher;
import id.ac.ui.cs.advprog.bepromovoucher.dto.VoucherRequest;
import java.util.List;

public interface VoucherService {
    Voucher createVoucher(VoucherRequest request);
    List<Voucher> findAllVouchers();
}
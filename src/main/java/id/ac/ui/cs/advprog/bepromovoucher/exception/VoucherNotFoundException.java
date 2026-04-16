package id.ac.ui.cs.advprog.bepromovoucher.exception;

public class VoucherNotFoundException extends RuntimeException {
    public VoucherNotFoundException(String code) {
        super("Voucher dengan kode " + code + " tidak ditemukan!");
    }
}
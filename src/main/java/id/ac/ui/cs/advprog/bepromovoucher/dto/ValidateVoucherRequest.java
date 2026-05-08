package id.ac.ui.cs.advprog.bepromovoucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ValidateVoucherRequest {
    @NotBlank(message = "Kode voucher wajib diisi")
    private String code;

    @NotNull(message = "Jumlah pembelian wajib diisi")
    private Double amount;
}
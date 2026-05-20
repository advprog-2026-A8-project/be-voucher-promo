package id.ac.ui.cs.advprog.bepromovoucher.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VoucherRequest {

    @NotBlank(message = "Kode voucher wajib diisi")
    private String code;

    @NotNull(message = "Kuota wajib diisi")
    @Min(value = 1, message = "Kuota minimal 1")
    private Integer quota;

    @NotNull(message = "Nilai diskon wajib diisi")
    private Double discountValue;

    @NotNull(message = "Minimal pembelian wajib diisi")
    private Double minPurchase;

    @NotBlank(message = "Tipe diskon wajib diisi")
    private String discountType;

    @NotNull(message = "Masa berlaku wajib diisi")
    @Future(message = "Masa berlaku harus di masa depan")
    private LocalDateTime expiryDate;

    @NotBlank(message = "Syarat penggunaan wajib diisi")
    private String termsAndConditions;
}
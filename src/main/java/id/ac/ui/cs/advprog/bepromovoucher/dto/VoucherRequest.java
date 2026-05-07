package id.ac.ui.cs.advprog.bepromovoucher.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
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
    @Positive(message = "Nilai diskon harus lebih dari 0")
    private Double discountValue;

    @NotNull(message = "Minimal pembelian wajib diisi")
    @PositiveOrZero(message = "Minimal pembelian tidak boleh negatif")
    private Double minPurchase;

    @NotBlank(message = "Tipe diskon wajib diisi")
    private String discountType;

    @NotNull(message = "Tanggal kadaluwarsa wajib diisi")
    @Future(message = "Tanggal kadaluwarsa harus di masa depan")
    private LocalDateTime expiryDate;

    @NotBlank(message = "Syarat dan ketentuan wajib diisi")
    private String termsAndConditions;
}
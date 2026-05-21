package id.ac.ui.cs.advprog.bepromovoucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoucherRequest {

    private Integer additionalQuota;

    private Boolean isActive;

    private LocalDateTime newExpiry;
}
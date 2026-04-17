package id.ac.ui.cs.advprog.bepromovoucher.model;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountValue;
    private LocalDateTime expiryDate;
    private Integer quota;
    private Double minPurchase;
    private String termsAndConditions;

    @Builder.Default
    private boolean isActive = true;
}
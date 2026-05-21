package id.ac.ui.cs.advprog.bepromovoucher.strategy;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import java.util.Map;

public class DiscountStrategyFactory {

    private static final Map<DiscountType, DiscountStrategy> STRATEGIES = Map.of(
            DiscountType.PERCENTAGE, new PercentageDiscountStrategy(),
            DiscountType.FIXED_AMOUNT, new FixedAmountDiscountStrategy()
    );

    private DiscountStrategyFactory() { }

    public static DiscountStrategy getStrategy(DiscountType type) {
        DiscountStrategy strategy = STRATEGIES.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Tipe diskon tidak dikenali: " + type);
        }
        return strategy;
    }
}
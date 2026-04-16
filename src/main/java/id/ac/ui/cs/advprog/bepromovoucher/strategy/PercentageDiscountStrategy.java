package id.ac.ui.cs.advprog.bepromovoucher.strategy;

public class PercentageDiscountStrategy implements DiscountStrategy {
    @Override
    public Double calculate(Double originalPrice, Double discountValue) {
        return originalPrice * (discountValue / 100);
    }
}
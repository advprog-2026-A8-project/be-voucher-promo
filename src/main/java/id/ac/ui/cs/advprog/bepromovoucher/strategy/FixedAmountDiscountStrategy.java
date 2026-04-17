package id.ac.ui.cs.advprog.bepromovoucher.strategy;

public class FixedAmountDiscountStrategy implements DiscountStrategy {
    @Override
    public Double calculate(Double originalPrice, Double discountValue) {
        return Math.min(originalPrice, discountValue);
    }
}
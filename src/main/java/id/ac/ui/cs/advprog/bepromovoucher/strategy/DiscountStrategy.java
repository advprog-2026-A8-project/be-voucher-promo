package id.ac.ui.cs.advprog.bepromovoucher.strategy;

public interface DiscountStrategy {
    Double calculate(Double originalPrice, Double discountValue);
}
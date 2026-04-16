package id.ac.ui.cs.advprog.bepromovoucher.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PercentageDiscountStrategyTest {

    private PercentageDiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PercentageDiscountStrategy();
    }

    @Test
    void testCalculateNormalPercentage() {
        Double originalPrice = 100000.0;
        Double discountValue = 10.0;

        Double result = strategy.calculate(originalPrice, discountValue);
        assertEquals(10000.0, result, "Diskon yang diberikan adalah 10% dari harga asli");
    }

    @Test
    void testCalculateZeroPercentage() {
        Double result = strategy.calculate(50000.0, 0.0);
        assertEquals(0.0, result, "Diskon dengan 0% seharusnya 0");
    }

    @Test
    void testCalculateFullDiscount() {
        Double result = strategy.calculate(75000.0, 100.0);
        assertEquals(75000.0, result, "Diskon 100% harus sama dengan harga asli");
    }

    @Test
    void testCalculateDecimalPercentage() {
        Double result = strategy.calculate(80000.0, 12.5);
        assertEquals(10000.0, result);
    }

    @Test
    void testCalculateZeroPrice() {
        Double result = strategy.calculate(0.0, 50.0);
        assertEquals(0.0, result, "Diskon untuk harga nol seharusnya nol");
    }
}
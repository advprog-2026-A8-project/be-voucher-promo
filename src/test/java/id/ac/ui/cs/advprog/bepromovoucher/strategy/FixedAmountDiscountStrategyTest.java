package id.ac.ui.cs.advprog.bepromovoucher.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FixedAmountDiscountStrategyTest {

    private DiscountStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FixedAmountDiscountStrategy();
    }

    @Test
    void testCalculateFixedAmountDiscount() {
        Double originalPrice = 100000.0;
        Double discountValue = 15000.0;

        Double result = strategy.calculate(originalPrice, discountValue);

        assertEquals(15000.0, result);
    }

    @Test
    void testCalculateFixedAmountHigherThanPrice() {
        Double originalPrice = 10000.0;
        Double discountValue = 20000.0;

        Double result = strategy.calculate(originalPrice, discountValue);

        assertTrue(result <= originalPrice);
    }
}
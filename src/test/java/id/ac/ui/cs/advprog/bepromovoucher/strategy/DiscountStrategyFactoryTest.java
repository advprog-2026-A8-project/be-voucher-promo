package id.ac.ui.cs.advprog.bepromovoucher.strategy;

import id.ac.ui.cs.advprog.bepromovoucher.enums.DiscountType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscountStrategyFactoryTest {

    @Test
    void testGetStrategyPercentageReturnsPercentageStrategy() {
        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(DiscountType.PERCENTAGE);

        assertNotNull(strategy);
        assertInstanceOf(PercentageDiscountStrategy.class, strategy);
    }

    @Test
    void testGetStrategyFixedAmountReturnsFixedAmountStrategy() {
        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(DiscountType.FIXED_AMOUNT);

        assertNotNull(strategy);
        assertInstanceOf(FixedAmountDiscountStrategy.class, strategy);
    }

    @Test
    void testGetStrategyWithInvalidTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                DiscountStrategyFactory.getStrategy(DiscountType.UNKNOWN));
    }

    @Test
    void testGetStrategyPercentageCalculatesCorrectly() {
        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(DiscountType.PERCENTAGE);

        Double result = strategy.calculate(100000.0, 10.0);

        assertEquals(10000.0, result);
    }

    @Test
    void testGetStrategyFixedAmountCalculatesCorrectly() {
        DiscountStrategy strategy = DiscountStrategyFactory.getStrategy(DiscountType.FIXED_AMOUNT);

        Double result = strategy.calculate(100000.0, 15000.0);

        assertEquals(15000.0, result);
    }

    @Test
    void testGetStrategySameInstanceReturnedForSameType() {
        DiscountStrategy first = DiscountStrategyFactory.getStrategy(DiscountType.PERCENTAGE);
        DiscountStrategy second = DiscountStrategyFactory.getStrategy(DiscountType.PERCENTAGE);

        assertSame(first, second);
    }

    @Test
    void testGetStrategyUnknownThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> DiscountStrategyFactory.getStrategy(DiscountType.UNKNOWN));
        assertTrue(exception.getMessage().contains("UNKNOWN"));
    }
}
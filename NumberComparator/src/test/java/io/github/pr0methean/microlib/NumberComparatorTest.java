package io.github.pr0methean.microlib;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import org.testng.annotations.Test;

public class NumberComparatorTest {

  private static final BigDecimal DOUBLE_MAX_VALUE_BD = new BigDecimal(Double.MAX_VALUE);
  private static final BigInteger DOUBLE_MAX_VALUE_BI = DOUBLE_MAX_VALUE_BD.toBigInteger();
  private static final BigDecimal POINT_ONE = new BigDecimal("0.1");
  private static final LongAdder LONG_MIN_ADDER = new LongAdder();
  private static final LongAdder LONG_MAX_ADDER = new LongAdder();

  static {
    LONG_MIN_ADDER.add(Long.MIN_VALUE);
    LONG_MAX_ADDER.add(Long.MAX_VALUE);
  }

  private static final List<? extends Number> ZEROES = ImmutableList.of(
      Long.valueOf(0),
      Integer.valueOf(0),
      Short.valueOf((short) 0),
      Byte.valueOf((byte) 0),
      Double.valueOf(0.0),
      Float.valueOf(0.0f),
      BigInteger.ZERO,
      BigDecimal.ZERO,
      new LongAccumulator((anyLeft, anyRight) -> 0, 0),
      new AtomicLong(0),
      new AtomicInteger(0),
      new LongAdder());

  private static final List<? extends Number> LONG_MIN_VALUES = ImmutableList.of(
      Long.valueOf(Long.MIN_VALUE),
      Double.valueOf(Long.MIN_VALUE),
      Float.valueOf(Long.MIN_VALUE),
      BigInteger.valueOf(Long.MIN_VALUE),
      new BigDecimal(Long.MIN_VALUE),
      new LongAccumulator(Math::max, Long.MIN_VALUE),
      LONG_MIN_ADDER);

  private static final List<? extends Number> LONG_MAX_VALUES = ImmutableList.of(
      Long.valueOf(Long.MAX_VALUE),
      BigInteger.valueOf(Long.MAX_VALUE),
      new BigDecimal(Long.MAX_VALUE),
      new LongAccumulator(Math::min, Long.MAX_VALUE),
      LONG_MAX_ADDER);

  private static final List<? extends Number> SORTED = ImmutableList.of(
      Double.NEGATIVE_INFINITY,
      DOUBLE_MAX_VALUE_BI.negate().subtract(BigInteger.ONE),
      DOUBLE_MAX_VALUE_BD.negate().subtract(POINT_ONE),
      -Double.MAX_VALUE,
      DOUBLE_MAX_VALUE_BD.negate().add(POINT_ONE),
      DOUBLE_MAX_VALUE_BI.negate().add(BigInteger.ONE),
      -Float.MAX_VALUE,
      Double.longBitsToDouble(Double.doubleToRawLongBits(Long.MIN_VALUE) + 1),
      Long.MIN_VALUE,
      Long.MIN_VALUE + 1,
      Double.longBitsToDouble(Double.doubleToRawLongBits(Long.MIN_VALUE) - 1),
      -(1L << 53) - 1,
      -0x1.0p53,
      -0x1.0p53 + 0.5,
      -(1L << 53) + 1,
      Integer.MIN_VALUE,
      Short.MIN_VALUE,
      Byte.MIN_VALUE,
      BigInteger.ONE.negate(),
      -Float.MIN_VALUE,
      -Double.MIN_VALUE,
      BigDecimal.ZERO,
      Double.MIN_VALUE,
      Float.MIN_VALUE,
      BigInteger.ONE,
      Byte.MAX_VALUE,
      Short.MAX_VALUE,
      Integer.MAX_VALUE,
      (1L << 53) - 1,
      0x1.0p53 - 0.5,
      0x1.0p53,
      (1L << 53) + 1,
      Double.longBitsToDouble(Double.doubleToRawLongBits(Long.MAX_VALUE) - 1),
      Long.MAX_VALUE - 1,
      Long.MAX_VALUE,
      Double.longBitsToDouble(Double.doubleToRawLongBits(Long.MAX_VALUE) + 1),
      Float.MAX_VALUE,
      DOUBLE_MAX_VALUE_BI.subtract(BigInteger.ONE),
      DOUBLE_MAX_VALUE_BD.subtract(POINT_ONE),
      Double.MAX_VALUE,
      DOUBLE_MAX_VALUE_BD.add(POINT_ONE),
      DOUBLE_MAX_VALUE_BI.add(BigInteger.ONE),
      Double.POSITIVE_INFINITY
  );

  private static String toStringWithClass(Object o) {
    return String.format("(%s) %s", o.getClass().getSimpleName(), o);
  }

  private static void assertAllEqual(Iterable<? extends Number> numbers) {
    for (Number n1 : numbers) {
      for (Number n2 : numbers) {
        assertEquals(NumberComparator.INSTANCE.compare(n1, n2), 0,
            String.format("Numbers are equal but compare as unequal (%s, %s)",
                toStringWithClass(n1), toStringWithClass(n2)));
      }
    }
  }

  private static void assertGreaterThanAny(Number greater, Iterable<? extends Number> less) {
    for (Number n : less) {
      assertTrue(NumberComparator.INSTANCE.compare(greater, n) > 0,
          String.format("compare(%s, %s) didn't return positive",
              toStringWithClass(greater), toStringWithClass(n)));
      assertTrue(NumberComparator.INSTANCE.compare(n, greater) < 0,
          String.format("compare(%s, %s) didn't return negative",
              toStringWithClass(n), toStringWithClass(greater)));
    }
  }

  private static void assertLessThanAny(Number less, Iterable<? extends Number> greater) {
    for (Number n : greater) {
      assertTrue(NumberComparator.INSTANCE.compare(less, n) < 0,
          String.format("compare(%s, %s) didn't return negative",
              toStringWithClass(less), toStringWithClass(n)));
      assertTrue(NumberComparator.INSTANCE.compare(n, less) > 0,
          String.format("compare(%s, %s) didn't return positive",
              toStringWithClass(n), toStringWithClass(less)));
    }
  }

  @Test public void testCompareZeroesEqual() {
    assertAllEqual(ZEROES);
  }

  @Test public void testCompareMinValuesEqual() {
    assertAllEqual(LONG_MIN_VALUES);
  }

  @Test public void testCompareMaxValuesEqual() {
    assertAllEqual(LONG_MAX_VALUES);
  }

  @Test public void testCompareToNegativeInfinity() {
    assertLessThanAny(Double.NEGATIVE_INFINITY, ZEROES);
    assertLessThanAny(Double.NEGATIVE_INFINITY, LONG_MIN_VALUES);
  }

  @Test public void testCompareToPositiveInfinity() {
    assertGreaterThanAny(Double.POSITIVE_INFINITY, ZEROES);
    assertGreaterThanAny(Double.POSITIVE_INFINITY, LONG_MIN_VALUES);
  }

  @Test public void testCompareToNan() {
    assertGreaterThanAny(Double.NaN, ZEROES);
    assertGreaterThanAny(Double.NaN, LONG_MIN_VALUES);
  }

  @Test(invocationCount = 10) public void testSort() throws NoSuchAlgorithmException {
    List<Number> copy = Arrays.asList(SORTED.toArray(new Number[0]));
    Collections.shuffle(copy, SecureRandom.getInstance("SHA1PRNG"));
    Collections.sort(copy, NumberComparator.INSTANCE);
    StringBuilder messageActualList = new StringBuilder("Numbers in unexpected order after sorting:");
    for (Number n : copy) {
      messageActualList.append(String.format("%n%s", toStringWithClass(n)));
    }
    assertEquals(copy, SORTED, messageActualList.toString());
  }
}
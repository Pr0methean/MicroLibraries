package io.github.pr0methean.microlib;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;

public class NumberComparator implements Comparator<Number> {

  private static final double LONG_MAX_PLUS_ONE_AS_DOUBLE = 0x1.0p63;
  private static final double LONG_MIN_AS_DOUBLE = Long.MIN_VALUE;
  /** Doubles larger than this have no fractional part. */
  private static final double DOUBLE_INTEGER_THRESHOLD = 0x1.0p53;
  private static final BigDecimal LONG_MAX_AS_BIG_DECIMAL = new BigDecimal(Long.MAX_VALUE);
  private static final BigDecimal LONG_MIN_AS_BIG_DECIMAL = new BigDecimal(Long.MIN_VALUE);
  private static final BigInteger LONG_MAX_AS_BIG_INTEGER = BigInteger.valueOf(Long.MAX_VALUE);
  private static final BigInteger LONG_MIN_AS_BIG_INTEGER = BigInteger.valueOf(Long.MIN_VALUE);
  private static final MathContext ROUND_DOWN = new MathContext(0, RoundingMode.FLOOR);

  public int compare(Number o1, Number o2) {
    if (o1 == o2) {
      return 0;
    }
    if (isBoxedIntegral(o1)) {
      return compareLong(o1.longValue(), o2);
    }
    if (isBoxedIntegral(o2)) {
      return -compareLong(o2.longValue(), o1);
    }
    if (o1 instanceof BigDecimal) {
      return compareBigDecimal((BigDecimal) o1, o2);
    }
    if (o2 instanceof BigDecimal) {
      return -compareBigDecimal((BigDecimal) o2, o1);
    }
    if (o1 instanceof BigInteger) {
      return compareBigInteger((BigInteger) o1, o2);
    }
    if (o2 instanceof BigInteger) {
      return -compareBigInteger((BigInteger) o2, o1);
    }
    return Double.compare(o1.doubleValue(), o2.doubleValue());
  }

  private static int compareLong(long o1, Number o2) {
    if (isBoxedIntegral(o2)) {
      return Long.compare(o1, o2.longValue());
    }
    if (o2 instanceof BigInteger) {
      return -compareBigInteger((BigInteger) o2, o1);
    }
    if (o2 instanceof BigDecimal) {
      return -compareBigDecimal((BigDecimal) o2, o1);
    }
    double o2d = o2.doubleValue();
    if (o2d >= LONG_MAX_PLUS_ONE_AS_DOUBLE) {
      return 1;
    }
    if (o2d < LONG_MIN_AS_DOUBLE) {
      return -1;
    }
    if (Math.abs(o2d) >= DOUBLE_INTEGER_THRESHOLD) {
      return Long.compare(o1, o2.longValue());
    }
    return Double.compare(o1, o2d);
  }

  private static int compareBigInteger(BigInteger o1, Number o2) {
    if (isBoxedIntegral(o2)) {
      if (o1.compareTo(LONG_MAX_AS_BIG_INTEGER) > 0) {
        return 1;
      }
      if (o1.compareTo(LONG_MIN_AS_BIG_INTEGER) < 0) {
        return -1;
      }
      return Long.compare(o1.longValue(), o2.longValue());
    }
    if (o2 instanceof BigInteger) {
      return o1.compareTo((BigInteger) o2);
    }
    if (o2 instanceof BigDecimal) {
      return -((BigDecimal) o2).compareTo(new BigDecimal(o1));
    }
    return compareBigDecimal(new BigDecimal(o1), o2);
  }

  private static int compareBigDecimal(BigDecimal o1, Number o2) {
    if (isBoxedIntegral(o2)) {
      if (o1.compareTo(LONG_MAX_AS_BIG_DECIMAL) > 0) {
        return 1;
      }
      if (o1.compareTo(LONG_MIN_AS_BIG_DECIMAL) < 0) {
        return -1;
      }
      int compare = Long.compare(o1.longValue(), o2.longValue());
      if (compare < 0) {
        return -1;
      }
      if (compare > 0) {
        return 1;
      }
      return o1.remainder(BigDecimal.ONE, ROUND_DOWN).compareTo(BigDecimal.ZERO) > 0 ? 1 : 0;
    }
    if (o2 instanceof BigDecimal) {
      return o1.compareTo((BigDecimal) o2);
    }
    if (o2 instanceof BigInteger) {
      return o1.compareTo(new BigDecimal((BigInteger) o2));
    }
    return o1.compareTo(new BigDecimal(o2.doubleValue()));
  }

  private static boolean isBoxedIntegral(Number number) {
    return number instanceof Long || number instanceof Integer || number instanceof Short
        || number instanceof Byte;
  }
}

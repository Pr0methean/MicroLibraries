package io.github.pr0methean.microlib;

import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public enum NumberComparator implements Comparator<Number> {
  INSTANCE;

  private static final double LONG_MAX_PLUS_ONE_AS_DOUBLE = 0x1.0p63;
  private static final double LONG_MIN_AS_DOUBLE = Long.MIN_VALUE;
  /** Doubles larger than this have no fractional part. */
  private static final double DOUBLE_INTEGER_THRESHOLD = 0x1.0p53;
  private static final BigDecimal LONG_MAX_AS_BIG_DECIMAL = new BigDecimal(Long.MAX_VALUE);
  private static final BigDecimal LONG_MIN_AS_BIG_DECIMAL = new BigDecimal(Long.MIN_VALUE);
  private static final BigInteger LONG_MAX_AS_BIG_INTEGER = BigInteger.valueOf(Long.MAX_VALUE);
  private static final BigInteger LONG_MIN_AS_BIG_INTEGER = BigInteger.valueOf(Long.MIN_VALUE);
  private static final MathContext ROUND_DOWN = new MathContext(0, RoundingMode.FLOOR);
  private static final List<Class<? extends Number>> INTEGRAL_CLASSES;

  static {
    ImmutableList.Builder<Class<? extends Number>> integralClasses = ImmutableList
        .<Class<? extends Number>>builder().add(
        Long.class, Integer.class, Short.class, Byte.class, AtomicLong.class, AtomicInteger.class);
    try {
      integralClasses.add(
          Class.forName("java.util.concurrent.atomic.LongAccumulator").asSubclass(Number.class));
      integralClasses.add(
          Class.forName("java.util.concurrent.atomic.LongAdder").asSubclass(Number.class));
    } catch (ClassNotFoundException ignored) {}
    INTEGRAL_CLASSES = integralClasses.build();
  }

  public int compare(Number o1, Number o2) {
    if (o1 == o2) {
      return 0;
    }
    if (convertibleToLong(o1)) {
      return compareLong(o1.longValue(), o2);
    }
    if (convertibleToLong(o2)) {
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
    if (convertibleToLong(o2)) {
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
      return -1; // o1 < o2
    }
    if (o2d < LONG_MIN_AS_DOUBLE) {
      return 1; // o1 > o2
    }
    if (Math.abs(o2d) >= DOUBLE_INTEGER_THRESHOLD) {
      return Long.compare(o1, o2.longValue());
    }
    return Double.compare(o1, o2d);
  }

  private static int compareBigInteger(BigInteger o1, Number o2) {
    if (convertibleToLong(o2)) {
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
    if (convertibleToLong(o2)) {
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
    final double o2d = o2.doubleValue();
    if (o2d == Double.POSITIVE_INFINITY || Double.isNaN(o2d)) {
      return -1;
    }
    if (o2d == Double.NEGATIVE_INFINITY) {
      return 1;
    }
    return o1.compareTo(new BigDecimal(o2d));
  }

  private static boolean convertibleToLong(Number number) {
    for (Class<? extends Number> clazz : INTEGRAL_CLASSES) {
      if (clazz.isInstance(number)) {
        return true;
      }
    }
    return false;
  }
}

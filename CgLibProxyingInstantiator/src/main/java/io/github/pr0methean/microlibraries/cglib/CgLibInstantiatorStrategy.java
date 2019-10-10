package io.github.pr0methean.microlibraries.cglib;

import java.lang.reflect.Modifier;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.InstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

/**
 * An {@link InstantiatorStrategy} that uses a {@link CgLibProxyingInstantiator} when instantiating
 * an abstract class or interface, and delegates to another {@link InstantiatorStrategy} otherwise.
 */
public class CgLibInstantiatorStrategy implements InstantiatorStrategy {
  private final InstantiatorStrategy delegate;

  /**
   * Creates an instance that uses a {@link StdInstantiatorStrategy} for concrete classes.
   */
  public CgLibInstantiatorStrategy() {
    this(new StdInstantiatorStrategy());
  }

  public CgLibInstantiatorStrategy(InstantiatorStrategy delegate) {
    this.delegate = delegate;
  }

  @Override
  public <T> ObjectInstantiator<T> newInstantiatorOf(Class<T> type) {
    if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
      return new CgLibProxyingInstantiator<>(type);
    }
    return delegate.newInstantiatorOf(type);
  }
}

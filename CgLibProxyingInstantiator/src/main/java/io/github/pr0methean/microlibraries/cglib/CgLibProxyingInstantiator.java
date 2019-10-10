package io.github.pr0methean.microlibraries.cglib;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * An instantiator that creates a Cglib proxy.
 *
 * @param <T> the type of object to create
 */
public class CgLibProxyingInstantiator<T> implements ObjectInstantiator<T> {

  private final Enhancer enhancer;

  public CgLibProxyingInstantiator(Class<T> type) {
    enhancer = new Enhancer();
    if (type.isInterface()) {
      enhancer.setInterfaces(new Class<?>[]{type});
    } else {
      enhancer.setSuperclass(type);
    }
    enhancer.setCallback((FixedValue) () -> null);
  }

  public T newInstance() {
    return (T) enhancer.create();
  }
}

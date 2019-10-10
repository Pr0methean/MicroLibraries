package io.github.pr0methean.microlibraries.cglib;

import java.util.AbstractList;
import org.testng.annotations.Test;

public class CgLibProxyingInstantiatorTest {

  @Test
  public void testInterface() {
    new CgLibProxyingInstantiator<>(Runnable.class).newInstance().run();
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testClass() {
    ((AbstractList<Object>) (new CgLibProxyingInstantiator<>(AbstractList.class).newInstance()))
        .add("an object");
  }
}
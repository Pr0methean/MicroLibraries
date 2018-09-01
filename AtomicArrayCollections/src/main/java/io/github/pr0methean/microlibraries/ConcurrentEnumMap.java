package io.github.pr0methean.microlibraries;

import com.google.common.collect.PeekingIterator;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Concurrent version of {@link java.util.EnumMap} backed by an {@link AtomicReferenceArray}.
 * Does not allow null values; setting an entry's value to null removes it from the map.
 *
 * @param <K>
 * @param <V>
 */
public class ConcurrentEnumMap<K extends Enum<K>, V> extends AbstractMap<K, V> {
  private final AtomicReferenceArray<V> values;
  private final Class<K> enumClass;
  private final int enumSize;

  protected final class MyEntry extends SimpleEntry<K, V> {
    protected MyEntry(K key) {
      super(key, get(key));
    }

    @Override public V getValue() {
      V out = get(getKey());
      if (out == null) {
        return super.getValue();
      }
      return out;
    }

    @Override public V setValue(V value) {
      super.setValue(value);
      return put(getKey(), value);
    }
  }

  public ConcurrentEnumMap(Class<K> enumClass) {
    this.enumClass = enumClass;
    enumSize = enumClass.getEnumConstants().length;
    values = new AtomicReferenceArray<>(enumSize);
  }

  @Override public V get(Object key) {
    return enumClass.isInstance(key) ? values.get(((Enum<K>) key).ordinal()) : null;
  }

  @Override public boolean containsKey(Object key) {
    return get(key) != null;
  }

  @Override public V put(K key, V value) {
    return values.getAndSet(key.ordinal(), value);
  }

  @Override public Set<Entry<K, V>> entrySet() {

    return new AbstractSet<Entry<K, V>>() {
      private Set<Entry<K, V>> snapshot() {
        Set<Entry<K, V>> snapshot = Collections.newSetFromMap(new IdentityHashMap<>());
        for (K key : enumClass.getEnumConstants()) {
          if (get(key) != null) {
            snapshot.add(new MyEntry(key));
          }
        }
        return snapshot;
      }

      @Override public Iterator<Entry<K, V>> iterator() {
        Iterator<Entry<K, V>> snapshotIterator = snapshot().iterator();
        return new Iterator<Entry<K, V>>() {
          Entry<K, V> lastReturnedEntry = null;

          @Override public boolean hasNext() {
            return snapshotIterator.hasNext();
          }

          @Override public Entry<K, V> next() {
            lastReturnedEntry = snapshotIterator.next();
            return lastReturnedEntry;
          }

          @Override public void remove() {
            if (lastReturnedEntry == null) {
              throw new IllegalStateException("remove() called before next()");
            }
            put(lastReturnedEntry.getKey(), null);
          }
        };
      }

      @Override public int size() {
        int size = 0;
        for (int i = 0; i < enumSize) {
          if (values.get(i) != null) {
            size++;
          }
        }
        return size;
      }
    };
  }

  @Override public boolean replace(K key, V oldValue, V newValue) {
    return values.compareAndSet(key.ordinal(), oldValue, newValue);
  }
}

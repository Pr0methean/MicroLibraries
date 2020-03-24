import java.util.AbstractList;
import java.util.List;
import javax.annotation.Nullable;

public class CharSequences {
  private static final int HASH_CODE_PRIME = 31;
  public static List<Character> asList(final CharSequence in) {
    return new AbstractList<Character>() {
      @Override public Character get(int index) {
        return in.charAt(index);
      }

      @Override public int size() {
        return in.length();
      }
    };
  }

  public static CharSequence forList(final List<Character> in) {
    return new CharSequence() {
      @Override public int length() {
        return in.size();
      }

      @Override public char charAt(int index) {
        return in.get(index);
      }

      @Override public CharSequence subSequence(int start, int end) {
        return forList(in.subList(start, end));
      }
    };
  }

  public static int hashCode(@Nullable CharSequence in) {
    if (in == null) {
      return 0;
    }
    int out = 1;
    for (int i = 0; i < in.length(); i++) {
      out *= HASH_CODE_PRIME;
      out += in.charAt(i);
    }
    return out;
  }

  public static long longHashCode(@Nullable CharSequence in) {
    if (in == null) {
      return 0;
    }
    long out = 1;
    for (int i = 0; i < in.length(); i++) {
      out *= HASH_CODE_PRIME;
      out += in.charAt(i);
    }
    return out;
  }
}

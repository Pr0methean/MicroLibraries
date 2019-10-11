import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Currying {
  public static <T,R> Function<T, Supplier<R>> curry(Function<T, R> input) {
    return param -> (() -> input.apply(param));
  }

  public static <T,U,R> Function<T, Function<U, R>> curry(BiFunction<T, U, R> input) {
    return param1 -> (param2 -> input.apply(param1, param2));
  }

  public static <T,U> Function<T, Consumer<U>> curry(BiConsumer<T, U> input) {
    return param1 -> (param2 -> input.accept(param1, param2));
  }
}

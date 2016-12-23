package org.cdi.further.camel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Exceptions {

    @FunctionalInterface
    public interface CheckedConsumer<T> {
        void accept(T t) throws Exception;
    }

    @FunctionalInterface
    public interface CheckedBiConsumer<T, U> {
        void accept(T t, U u) throws Exception;
    }

    public static <T, U> BiConsumer<T, U> rethrow(CheckedBiConsumer<T, U> consumer) {
        return (t, u) -> {
            try {
                consumer.accept(t, u);
            } catch (Exception cause) {
                throwAsUnchecked(cause);
            }
        };
    }

    public static <T> Consumer<T> rethrow(CheckedConsumer<T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
            } catch (Exception cause) {
                throwAsUnchecked(cause);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception exception) throws E {
        throw (E) exception;
    }
}

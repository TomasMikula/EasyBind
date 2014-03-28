package org.fxmisc.easybind;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import org.fxmisc.easybind.monadic.MonadicBinding;
import org.fxmisc.easybind.monadic.MonadicObservableValue;
import org.fxmisc.easybind.select.SelectBuilder;

/**
 * Methods for easy creation of bindings.
 */
public class EasyBind {

    @FunctionalInterface
    public interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface TetraFunction<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    @FunctionalInterface
    public interface PentaFunction<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    public interface HexaFunction<A, B, C, D, E, F, R> {
        R apply(A a, B b, C c, D d, E e, F f);
    }

    /**
     * Creates a thin wrapper around an observable value to make it monadic.
     * @param o ObservableValue to wrap
     * @return {@code o} if {@code o} is already monadic, or a thin monadic
     * wrapper around {@code o} otherwise.
     */
    public static <T> MonadicObservableValue<T> monadic(ObservableValue<T> o) {
        if(o instanceof MonadicObservableValue) {
            return (MonadicObservableValue<T>) o;
        } else {
            return new PreboundBinding<T>(o) {
                @Override
                protected T computeValue() {
                    return o.getValue();
                }
            };
        }
    }

    public static <T, U> MonadicBinding<U> map(
            ObservableValue<T> src,
            Function<T, U> f) {
        return new PreboundBinding<U>(src) {
            @Override
            protected U computeValue() {
                return f.apply(src.getValue());
            }
        };
    }

    public static <T, U> ObservableList<U> map(
            ObservableList<? extends T> sourceList,
            Function<? super T, ? extends U> f) {
        return new MappedList<>(sourceList, f);
    }

    public static <A, B, R> MonadicBinding<R> combine(
            ObservableValue<A> src1,
            ObservableValue<B> src2,
            BiFunction<A, B, R> f) {
        return new PreboundBinding<R>(src1, src2) {
            @Override
            protected R computeValue() {
                return f.apply(src1.getValue(), src2.getValue());
            }
        };
    }

    public static <A, B, C, R> MonadicBinding<R> combine(
            ObservableValue<A> src1,
            ObservableValue<B> src2,
            ObservableValue<C> src3,
            TriFunction<A, B, C, R> f) {
        return new PreboundBinding<R>(src1, src2, src3) {
            @Override
            protected R computeValue() {
                return f.apply(
                        src1.getValue(), src2.getValue(), src3.getValue());
            }
        };
    }

    public static <A, B, C, D, R> MonadicBinding<R> combine(
            ObservableValue<A> src1,
            ObservableValue<B> src2,
            ObservableValue<C> src3,
            ObservableValue<D> src4,
            TetraFunction<A, B, C, D, R> f) {
        return new PreboundBinding<R>(src1, src2, src3, src4) {
            @Override
            protected R computeValue() {
                return f.apply(
                        src1.getValue(), src2.getValue(),
                        src3.getValue(), src4.getValue());
            }
        };
    }

    public static <A, B, C, D, E, R> MonadicBinding<R> combine(
            ObservableValue<A> src1,
            ObservableValue<B> src2,
            ObservableValue<C> src3,
            ObservableValue<D> src4,
            ObservableValue<E> src5,
            PentaFunction<A, B, C, D, E, R> f) {
        return new PreboundBinding<R>(src1, src2, src3, src4, src5) {
            @Override
            protected R computeValue() {
                return f.apply(
                        src1.getValue(), src2.getValue(), src3.getValue(),
                        src4.getValue(), src5.getValue());
            }
        };
    }

    public static <A, B, C, D, E, F, R> MonadicBinding<R> combine(
            ObservableValue<A> src1,
            ObservableValue<B> src2,
            ObservableValue<C> src3,
            ObservableValue<D> src4,
            ObservableValue<E> src5,
            ObservableValue<F> src6,
            HexaFunction<A, B, C, D, E, F, R> f) {
        return new PreboundBinding<R>(src1, src2, src3, src4, src5, src6) {
            @Override
            protected R computeValue() {
                return f.apply(
                        src1.getValue(), src2.getValue(), src3.getValue(),
                        src4.getValue(), src5.getValue(), src6.getValue());
            }
        };
    }

    public static <T, R> MonadicBinding<R> combine(
            ObservableList<? extends ObservableValue<? extends T>> list,
            Function<? super Stream<T>, ? extends R> f) {
        return new ListCombinationBinding<>(list, f);
    }

    public static <T> SelectBuilder<T> select(ObservableValue<T> selectionRoot) {
        return SelectBuilder.startAt(selectionRoot);
    }
}

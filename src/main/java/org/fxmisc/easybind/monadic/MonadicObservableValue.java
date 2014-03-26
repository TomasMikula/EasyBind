package org.fxmisc.easybind.monadic;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.PreboundBinding;
import org.fxmisc.easybind.select.SelectBuilder;

/**
 * Adds monadic operations to {@link ObservableValue}.
 * @param <T>
 */
public interface MonadicObservableValue<T> extends ObservableValue<T> {

    /**
     * Checks whether this ObservableValue holds a (non-null) value.
     * @return {@code true} if this ObservableValue holds a (non-null) value,
     * {@code false} otherwise.
     */
    default boolean isPresent() {
        return getValue() != null;
    }

    /**
     * Inverse of {@link #isPresent()}.
     */
    default boolean isEmpty() {
        return getValue() == null;
    }

    /**
     * Invokes the given function if this ObservableValue holds a (non-null)
     * value.
     * @param f function to invoke on the value currently held by this
     * ObservableValue.
     */
    default void ifPresent(Consumer<? super T> f) {
        T val = getValue();
        if(val != null) {
            f.accept(val);
        }
    }

    /**
     * Returns the value currently held by this ObservableValue.
     * @throws NoSuchElementException if there is no value present.
     */
    default T getOrThrow() {
        T res = getValue();
        if(res != null) {
            return res;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the value currently held by this ObservableValue.
     * If this ObservableValue is empty, {@code other} is returned instead.
     * @param other value to return if there is no value present in this
     * ObservableValue.
     */
    default T getOrElse(T other) {
        T res = getValue();
        if(res != null) {
            return res;
        } else {
            return other;
        }
    }

    /**
     * Returns a new ObservableValue that holds the value held by this
     * ObservableValue, or {@code other} when this ObservableValue is empty.
     */
    default MonadicBinding<T> orElse(T other) {
        return new PreboundBinding<T>(this) {
            @Override
            protected T computeValue() {
                T val = MonadicObservableValue.this.getValue();
                return val != null ? val : other;
            }
        };
    }

    /**
     * Returns a new ObservableValue that holds the value held by this
     * ObservableValue, or the value held by {@code other} when this
     * ObservableValue is empty.
     */
    default MonadicBinding<T> orElse(ObservableValue<T> other) {
        return new FirstNonNullBinding<>(this, other);
    }

    /**
     * Returns a new ObservableValue that holds the same value
     * as this ObservableValue when the value satisfies the predicate
     * and is empty when this ObservableValue is empty or its value
     * does not satisfy the given predicate.
     * @param p
     */
    default MonadicBinding<T> filter(Predicate<? super T> p) {
        return new PreboundBinding<T>(this) {
            @Override
            protected T computeValue() {
                T val = MonadicObservableValue.this.getValue();
                return (val != null && p.test(val)) ? val : null;
            }
        };
    }

    /**
     * Returns a new ObservableValue that holds a mapping of the value held
     * by this ObservableValue, and is empty when this ObservableValue is empty.
     * @param f function to map the value held by this ObservableValue.
     */
    default <U> MonadicBinding<U> map(Function<? super T, ? extends U> f) {
        return new PreboundBinding<U>(this) {
            @Override
            protected U computeValue() {
                T baseVal = MonadicObservableValue.this.getValue();
                return baseVal != null ? f.apply(baseVal) : null;
            }
        };
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds
     * value {@code x}, holds the value held by {@code f(x)}, and is empty
     * when this ObservableValue is empty.
     * @param f
     */
    default <U> MonadicBinding<U> flatMap(Function<? super T, ObservableValue<U>> f) {
        return new FlatMapBinding<>(this, f);
    }

    /**
     * Starts a selection chain. A selection chain is just a more efficient
     * equivalent to a chain of flatMaps.
     * @param selector
     * @return
     */
    default <U> SelectBuilder<U> select(Function<? super T, ObservableValue<U>> selector) {
        return SelectBuilder.startAt(this).select(selector);
    }
}

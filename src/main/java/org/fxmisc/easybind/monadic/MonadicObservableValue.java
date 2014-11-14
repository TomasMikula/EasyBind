package org.fxmisc.easybind.monadic;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;
import org.fxmisc.easybind.select.SelectBuilder;

/**
 * Adds monadic operations to {@link ObservableValue}.
 */
public interface MonadicObservableValue<T> extends ObservableObjectValue<T> {

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
     * Returns an {@code Optional} describing the value currently held by this
     * ObservableValue, or and empty {@code Optional} if this ObservableValue
     * is empty.
     */
    default Optional<T> getOpt() {
        return Optional.ofNullable(getValue());
    }

    /**
     * Returns a new ObservableValue that holds the value held by this
     * ObservableValue, or {@code other} when this ObservableValue is empty.
     */
    default MonadicBinding<T> orElse(T other) {
        return EasyBind.orElse(this, other);
    }

    /**
     * Returns a new ObservableValue that holds the value held by this
     * ObservableValue, or the value held by {@code other} when this
     * ObservableValue is empty.
     */
    default MonadicBinding<T> orElse(ObservableValue<T> other) {
        return EasyBind.orElse(this, other);
    }

    /**
     * Returns a new ObservableValue that holds the same value
     * as this ObservableValue when the value satisfies the predicate
     * and is empty when this ObservableValue is empty or its value
     * does not satisfy the given predicate.
     */
    default MonadicBinding<T> filter(Predicate<? super T> p) {
        return EasyBind.filter(this, p);
    }

    /**
     * Returns a new ObservableValue that holds a mapping of the value held
     * by this ObservableValue, and is empty when this ObservableValue is empty.
     * @param f function to map the value held by this ObservableValue.
     */
    default <U> MonadicBinding<U> map(Function<? super T, ? extends U> f) {
        return EasyBind.map(this, f);
    }

    /**
     * Returns a new ObservableValue that, when this ObservableValue holds
     * value {@code x}, holds the value held by {@code f(x)}, and is empty
     * when this ObservableValue is empty.
     */
    default <U> MonadicBinding<U> flatMap(
            Function<? super T, ? extends ObservableValue<U>> f) {
        return EasyBind.flatMap(this, f);
    }

    /**
     * Similar to {@link #flatMap(Function)}, except the returned Binding is
     * also a Property. This means you can call {@code setValue()} and
     * {@code bind()} methods on the returned value, which delegate to the
     * currently selected Property.
     *
     * <p>As the value of this ObservableValue changes, so does the selected
     * Property. When the Property returned from this method is bound, as the
     * selected Property changes, the previously selected property is unbound
     * and the newly selected property is bound.
     *
     * <p>Note that if the currently selected property is {@code null}, then
     * calling {@code getValue()} on the returned value will return {@code null}
     * regardless of any prior call to {@code setValue()} or {@code bind()}.
     *
     * <p>Note that you need to retain a reference to the returned value to
     * prevent it from being garbage collected.
     */
    default <U> PropertyBinding<U> selectProperty(
            Function<? super T, ? extends Property<U>> f) {
        return EasyBind.selectProperty(this, f);
    }

    /**
     * Starts a selection chain. A selection chain is just a more efficient
     * equivalent to a chain of flatMaps.
     */
    default <U> SelectBuilder<U> select(Function<? super T, ObservableValue<U>> selector) {
        return SelectBuilder.startAt(this).select(selector);
    }

    /**
     * Adds an invalidation listener and returns a Subscription that can be
     * used to remove that listener.
     *
     * <pre>
     * {@code
     * Subscription s = observable.subscribe(obs -> doSomething());
     *
     * // later
     * s.unsubscribe();
     * }</pre>
     *
     * is equivalent to
     *
     * <pre>
     * {@code
     * InvalidationListener l = obs -> doSomething();
     * observable.addListener(l);
     *
     * // later
     * observable.removeListener();
     * }</pre>
     */
    default Subscription subscribe(InvalidationListener listener) {
        addListener(listener);
        return () -> removeListener(listener);
    }

    /**
     * Adds a change listener and returns a Subscription that can be
     * used to remove that listener. See the example at
     * {@link #subscribe(InvalidationListener)}.
     */
    default Subscription subscribe(ChangeListener<? super T> listener) {
        addListener(listener);
        return () -> removeListener(listener);
    }
}

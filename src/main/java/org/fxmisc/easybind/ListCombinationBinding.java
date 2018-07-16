package org.fxmisc.easybind;

import java.util.function.Function;
import java.util.stream.Stream;

import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.ObservableList;

class ListCombinationBinding<T, U> extends ObjectBinding<U> implements
        MonadicBinding<U> {

    private final InvalidationListener invalidationListener = obs -> invalidate();

    private final WeakInvalidationListener weakInvalidationListener = new WeakInvalidationListener(
            invalidationListener);

    private final ObservableList<? extends T> source;
    private final Function<? super Stream<T>, ? extends U> combiner;

    public ListCombinationBinding(
            ObservableList<? extends T> list,
            Function<? super Stream<T>, ? extends U> f) {
        source = list;
        combiner = f;

        source.addListener(weakInvalidationListener);
    }

    @Override
    protected U computeValue() {
        return combiner.apply(source.stream().map(Function.identity()));
    }

    @Override
    public void dispose() {
        source.removeListener(weakInvalidationListener);
    }

}

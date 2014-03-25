package org.fxmisc.easybind.monadic;

import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;

class FlatMapBinding<T, U> extends MonadicObjectBinding<U> {
    private final ObservableValue<T> src;
    private final Function<? super T, ObservableValue<U>> mapper;
    private final InvalidationListener mappedListener =
            new WeakInvalidationListener(obs -> mappedInvalidated());

    private ObservableValue<U> mapped = null;

    public FlatMapBinding(ObservableValue<T> src, Function<? super T, ObservableValue<U>> f) {
        this.src = src;
        this.mapper = f;
        src.addListener(new WeakInvalidationListener(obs -> srcInvalidated()));
    }

    @Override
    protected U computeValue() {
        if(mapped == null) {
            T baseVal = src.getValue();
            if(baseVal == null) {
                return null;
            } else {
                mapped = mapper.apply(baseVal);
                mapped.addListener(mappedListener);
            }
        }
        return mapped.getValue();
    }

    private void mappedInvalidated() {
        invalidate();
    }

    private void srcInvalidated() {
        if(mapped != null) {
            mapped.removeListener(mappedListener);
            mapped = null;
        }
        invalidate();
    }
}

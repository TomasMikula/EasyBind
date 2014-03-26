package org.fxmisc.easybind.monadic;

import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

class FlatMapBinding<T, U> extends ObjectBinding<U> implements MonadicBinding<U> {
    private final ObservableValue<T> src;
    private final Function<? super T, ObservableValue<U>> mapper;

    private final InvalidationListener mappedListener = obs -> mappedInvalidated();
    private final InvalidationListener weakMappedListener = new WeakInvalidationListener(mappedListener);

    private final InvalidationListener srcListener = obs -> srcInvalidated();
    private final InvalidationListener weakSrcListener = new WeakInvalidationListener(srcListener);

    private ObservableValue<U> mapped = null;

    public FlatMapBinding(ObservableValue<T> src, Function<? super T, ObservableValue<U>> f) {
        this.src = src;
        this.mapper = f;
        src.addListener(weakSrcListener);
    }

    @Override
    public void dispose() {
        src.removeListener(weakSrcListener);
        disposeMapped();
    }

    @Override
    protected U computeValue() {
        if(mapped == null) {
            T baseVal = src.getValue();
            if(baseVal == null) {
                return null;
            } else {
                mapped = mapper.apply(baseVal);
                mapped.addListener(weakMappedListener);
            }
        }
        return mapped.getValue();
    }

    private void mappedInvalidated() {
        invalidate();
    }

    private void disposeMapped() {
        if(mapped != null) {
            mapped.removeListener(weakMappedListener);
            mapped = null;
        }
    }

    private void srcInvalidated() {
        disposeMapped();
        invalidate();
    }
}

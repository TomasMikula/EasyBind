package org.fxmisc.easybind.monadic;

import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.Subscription;

class FlatMapBinding<T, U> extends ObjectBinding<U> implements MonadicBinding<U> {
    private final ObservableValue<T> src;
    private final Function<? super T, ObservableValue<U>> mapper;

    // need to retain strong reference to listeners, so that they don't get garbage collected
    private final InvalidationListener srcListener = obs -> srcInvalidated();
    private final InvalidationListener mappedListener = obs -> mappedInvalidated();

    private final InvalidationListener weakSrcListener = new WeakInvalidationListener(srcListener);
    private final InvalidationListener weakMappedListener = new WeakInvalidationListener(mappedListener);

    private ObservableValue<U> mapped = null;
    private Subscription mappedSubscription = null;

    public FlatMapBinding(ObservableValue<T> src, Function<? super T, ObservableValue<U>> f) {
        this.src = src;
        this.mapper = f;
        src.addListener(weakSrcListener);
    }

    @Override
    public final void dispose() {
        src.removeListener(weakSrcListener);
        disposeMapped();
    }

    @Override
    protected final U computeValue() {
        if(mapped == null) {
            T baseVal = src.getValue();
            if(baseVal == null) {
                return null;
            } else {
                mapped = mapper.apply(baseVal);
                mappedSubscription = observeMapped(mapped);
            }
        }
        return mapped.getValue();
    }

    protected Subscription observeMapped(ObservableValue<U> mapped) {
        mapped.addListener(weakMappedListener);
        return () -> mapped.removeListener(weakMappedListener);
    }

    private void disposeMapped() {
        if(mapped != null) {
            mappedSubscription.unsubscribe();
            mappedSubscription = null;
            mapped = null;
        }
    }

    private void mappedInvalidated() {
        invalidate();
    }

    private void srcInvalidated() {
        disposeMapped();
        invalidate();
    }
}

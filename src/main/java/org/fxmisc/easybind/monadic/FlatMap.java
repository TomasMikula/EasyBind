package org.fxmisc.easybind.monadic;

import java.util.function.Function;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.Subscription;

abstract class FlatMapBindingBase<T, U, O extends ObservableValue<U>>
extends ObjectBinding<U> implements MonadicBinding<U> {
    private final ObservableValue<T> src;
    private final Function<? super T, O> mapper;

    // need to retain strong reference to listeners, so that they don't get garbage collected
    private final InvalidationListener srcListener = obs -> srcInvalidated();
    private final InvalidationListener mappedListener = obs -> mappedInvalidated();

    private final InvalidationListener weakSrcListener = new WeakInvalidationListener(srcListener);
    private final InvalidationListener weakMappedListener = new WeakInvalidationListener(mappedListener);

    private O mapped = null;
    private Subscription mappedSubscription = null;

    public FlatMapBindingBase(ObservableValue<T> src, Function<? super T, O> f) {
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
        setupTargetObservable();
        return mapped != null ? mapped.getValue() : null;
    }

    protected final void setupTargetObservable() {
        if(mapped == null) {
            T baseVal = src.getValue();
            if(baseVal != null) {
                mapped = mapper.apply(baseVal);
                mappedSubscription = observeTargetObservable(mapped);
            }
        }
    }

    protected O getTargetObservable() {
        return mapped;
    }

    protected Subscription observeTargetObservable(O target) {
        target.addListener(weakMappedListener);
        return () -> target.removeListener(weakMappedListener);
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

    protected void srcInvalidated() {
        disposeMapped();
        invalidate();
    }
}

class FlatMapBinding<T, U> extends FlatMapBindingBase<T, U, ObservableValue<U>> {

    public FlatMapBinding(ObservableValue<T> src, Function<? super T, ObservableValue<U>> f) {
        super(src, f);
    }
}

class FlatMapProperty<T, U> extends FlatMapBindingBase<T, U, Property<U>> implements PropertyBinding<U> {
    private ObservableValue<? extends U> boundTo = null;

    public FlatMapProperty(ObservableValue<T> src, Function<? super T, Property<U>> f) {
        super(src, f);
    }

    @Override
    protected Subscription observeTargetObservable(Property<U> mapped) {
        if(boundTo != null) {
            mapped.bind(boundTo);
        }

        Subscription s1 = super.observeTargetObservable(mapped);
        Subscription s2 = () -> {
            if(boundTo != null) {
                mapped.unbind();
            }
        };

        return s1.and(s2);
    }

    @Override
    protected void srcInvalidated() {
        super.srcInvalidated();

        // if bound, make sure to rebind eagerly
        if(boundTo != null) {
            setupTargetObservable();
        }
    }

    @Override
    public void setValue(U value) {
        Property<U> target = getTargetObservable();
        if(target != null) {
            target.setValue(value);
        }
    }

    @Override
    public void bind(ObservableValue<? extends U> other) {
        Property<U> target = getTargetObservable();
        if(target != null) {
            target.bind(other);
        }
        boundTo = other;
    }

    @Override
    public boolean isBound() {
        return boundTo != null ||
                (getTargetObservable() != null && getTargetObservable().isBound());
    }

    @Override
    public void unbind() {
        Property<U> target = getTargetObservable();
        if(target != null) {
            target.unbind();
        }
        boundTo = null;
    }

    @Override
    public void bindBidirectional(Property<U> other) {
        Bindings.bindBidirectional(this, other);
    }

    @Override
    public void unbindBidirectional(Property<U> other) {
        Bindings.unbindBidirectional(this, other);
    }

    @Override
    public Object getBean() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
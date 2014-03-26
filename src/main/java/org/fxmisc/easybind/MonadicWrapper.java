package org.fxmisc.easybind;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.monadic.MonadicObservableValue;

class MonadicWrapper<T> implements MonadicObservableValue<T> {
    private final ObservableValue<T> delegate;

    public MonadicWrapper(ObservableValue<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T getValue() {
        return delegate.getValue();
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        delegate.addListener(wrap(listener));
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        delegate.removeListener(wrap(listener));
    }

    @Override
    public void addListener(InvalidationListener listener) {
        delegate.addListener(wrap(listener));
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        delegate.removeListener(wrap(listener));
    }

    private InvalidationListener wrap(InvalidationListener listener) {
        return new DelegatedInvalidationListener(this, listener);
    }

    private ChangeListener<T> wrap(ChangeListener<? super T> listener) {
        return new DelegatedChangeListener<>(this, listener);
    }
}

class DelegatedInvalidationListener implements InvalidationListener {
    private final Observable proxyObservable;
    private final InvalidationListener delegateListener;

    public DelegatedInvalidationListener(Observable proxyObservable, InvalidationListener delegateListener) {
        this.proxyObservable = proxyObservable;
        this.delegateListener = delegateListener;
    }

    @Override
    public void invalidated(Observable delegateObservable) {
        delegateListener.invalidated(proxyObservable);
    }

    @Override
    public boolean equals(Object that) {
        if(that instanceof DelegatedInvalidationListener) {
            DelegatedInvalidationListener thatListener = (DelegatedInvalidationListener) that;
            return thatListener.proxyObservable.equals(proxyObservable)
                    && thatListener.delegateListener.equals(delegateListener);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return delegateListener.hashCode();
    }
}

class DelegatedChangeListener<T> implements ChangeListener<T> {
    private final ObservableValue<T> proxyObservable;
    private final ChangeListener<? super T> delegateListener;

    public DelegatedChangeListener(ObservableValue<T> proxyObservable, ChangeListener<? super T> delegateListener) {
        this.proxyObservable = proxyObservable;
        this.delegateListener = delegateListener;
    }

    @Override
    public void changed(ObservableValue<? extends T> delegateObservable, T oldVal, T newVal) {
        delegateListener.changed(proxyObservable, oldVal, newVal);
    }

    @Override
    public boolean equals(Object that) {
        if(that instanceof DelegatedChangeListener) {
            DelegatedChangeListener<?> thatListener = (DelegatedChangeListener<?>) that;
            return thatListener.proxyObservable.equals(proxyObservable)
                    && thatListener.delegateListener.equals(delegateListener);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return delegateListener.hashCode();
    }
}
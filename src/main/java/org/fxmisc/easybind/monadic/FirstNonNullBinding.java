package org.fxmisc.easybind.monadic;

import javafx.beans.WeakInvalidationListener;
import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.PreboundBinding;

class FirstNonNullBinding<T> extends PreboundBinding<T> {
    private final ObservableValue<? extends T>[] chain;

    private int startAt = 0;

    @SafeVarargs
    public FirstNonNullBinding(ObservableValue<? extends T>... chain) {
        this.chain = chain;
        for(int i = 0; i < chain.length; ++i) {
            int index = i;
            chain[i].addListener(new WeakInvalidationListener(obs -> invalidated(index)));
        }
    }

    @Override
    protected T computeValue() {
        for(int i = startAt; i < chain.length; ++i) {
            T val = chain[i].getValue();
            if(val != null) {
                startAt = i;
                return val;
            }
        }
        startAt = chain.length;
        return null;
    }

    private void invalidated(int index) {
        if(index <= startAt) {
            startAt = index;
            invalidate();
        }
    }
}

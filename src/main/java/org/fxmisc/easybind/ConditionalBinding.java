package org.fxmisc.easybind;

import java.lang.ref.WeakReference;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

@Deprecated
class ConditionalBinding<T> implements Subscription, InvalidationListener,
        ChangeListener<Boolean> {

    private final WeakReference<Property<T>> target;
    private final ObservableValue<? extends T> source;
    private final ObservableValue<Boolean> condition;

    private boolean unsubscribed = false;

    public ConditionalBinding(
            Property<T> target,
            ObservableValue<? extends T> source,
            ObservableValue<Boolean> condition) {
        this.target = new WeakReference<>(target);
        this.source = source;
        this.condition = condition;

        // add an empty listener to target just to maintain a strong reference
        // to this object for the lifetime of target
        target.addListener(this);

        condition.addListener((ChangeListener<Boolean>) this);

        if(condition.getValue()) {
            target.bind(source);
        }
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> cond,
            Boolean wasTrue, Boolean isTrue) {
        Property<T> tgt = this.target.get();
        if(tgt == null) {
            condition.removeListener((ChangeListener<Boolean>) this);
        } else if(isTrue) {
            tgt.bind(source);
        } else {
            tgt.unbind();
        }
    }

    @Override
    public void invalidated(Observable obs) {
        // do nothing
    }

    @Override
    public void unsubscribe() {
        if(!unsubscribed) {
            condition.removeListener((ChangeListener<Boolean>) this);

            Property<T> tgt = this.target.get();
            if(tgt != null) {
                tgt.removeListener(this);
                tgt.unbind();
            }

            unsubscribed = true;
        }
    }
}

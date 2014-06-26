package org.fxmisc.easybind.monadic;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

public interface PropertyBinding<T>
extends Property<T>, MonadicBinding<T> {

    /**
     * Like {@link #bind(ObservableValue)}, plus whenever the underlying
     * property changes, the previous one is set to the provided value.
     */
    void bind(ObservableValue<? extends T> observable, T resetToOnUnbind);
}

package org.fxmisc.easybind.select;

import javafx.beans.value.ObservableValue;

import org.fxmisc.easybind.monadic.MonadicBinding;

class RootSelectedBuilder<T> implements ParentSelectedBuilder<T> {
    private final ObservableValue<T> root;

    public RootSelectedBuilder(ObservableValue<T> root) {
        this.root = root;
    }

    @Override
    public <U> MonadicBinding<U> create(
            NestedSelectionElementFactory<T, U> nestedSelectionFactory) {
        return new SelectObjectBinding<T, U>(root, nestedSelectionFactory);
    }
}
package org.fxmisc.easybind;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;

abstract class UnbindableObjectBinding<T> extends ObjectBinding<T> implements
        UnbindableBinding<T> {
    private final Observable[] dependencies;

    public UnbindableObjectBinding(Observable... dependencies) {
        this.dependencies = dependencies;
        bind(dependencies);
    }

    @Override
    public void unbind() {
        unbind(dependencies);
    }
}

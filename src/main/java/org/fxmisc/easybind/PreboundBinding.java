package org.fxmisc.easybind;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;

abstract class PreboundBinding<T> extends ObjectBinding<T> {
    private final Observable[] dependencies;

    public PreboundBinding(Observable... dependencies) {
        this.dependencies = dependencies;
        bind(dependencies);
    }

    @Override
    public void dispose() {
        unbind(dependencies);
    }
}

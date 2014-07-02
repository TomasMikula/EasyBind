package org.fxmisc.easybind;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;

import org.fxmisc.easybind.monadic.MonadicBinding;

/**
 * Object binding that binds to its dependencies on creation
 * and unbinds from them on dispose.
 */
public abstract class PreboundBinding<T> extends ObjectBinding<T> implements MonadicBinding<T> {
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

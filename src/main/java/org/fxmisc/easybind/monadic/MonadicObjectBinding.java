package org.fxmisc.easybind.monadic;

import javafx.beans.Observable;
import javafx.beans.binding.ObjectBinding;

abstract class MonadicObjectBinding<T> extends ObjectBinding<T>
        implements MonadicBinding<T> {

    public MonadicObjectBinding(Observable... dependencies) {
        bind(dependencies);
    }
}

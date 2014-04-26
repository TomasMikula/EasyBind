package org.fxmisc.easybind.monadic;

import javafx.beans.binding.Binding;

public interface MonadicBinding<T>
extends Binding<T>, MonadicObservableValue<T> {}

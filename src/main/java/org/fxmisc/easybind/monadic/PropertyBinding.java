package org.fxmisc.easybind.monadic;

import javafx.beans.property.Property;

public interface PropertyBinding<T>
extends Property<T>, MonadicBinding<T> {}

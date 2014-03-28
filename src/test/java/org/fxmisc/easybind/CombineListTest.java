package org.fxmisc.easybind;

import static org.junit.Assert.*;
import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

public class CombineListTest {

    @Test
    public void test() {
        ObservableList<Property<Integer>> list = FXCollections.observableArrayList();
        Binding<Integer> sum = EasyBind.combine(list, stream -> stream.reduce((a, b) -> a + b).orElse(0));

        Counter counter = new Counter();
        sum.addListener(obs -> counter.inc());

        assertEquals(0, sum.getValue().intValue());
        assertEquals(0, counter.getAndReset());

        Property<Integer> a = new SimpleObjectProperty<>(1);
        Property<Integer> b = new SimpleObjectProperty<>(2);
        Property<Integer> c = new SimpleObjectProperty<>(4);
        Property<Integer> d = new SimpleObjectProperty<>(8);

        // check that added items are reflected
        list.add(a);
        list.add(b);
        assertEquals(3, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());
        list.add(c);
        list.add(d);
        assertEquals(15, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());

        // check that changing an item's value is reflected
        b.setValue(16);
        assertEquals(29, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());

        // check that value removal is reflected
        list.remove(b);
        list.remove(d);
        assertEquals(5, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());

        // check that changing a removed item does not affect or invalidate the sum
        b.setValue(32);
        d.setValue(64);
        assertEquals(5, sum.getValue().intValue());
        assertEquals(0, counter.getAndReset());

        // check that no more invalidations arrive after disposal
        sum.dispose();
        a.setValue(2);
        c.setValue(8);
        assertEquals(0, counter.getAndReset());
    }

}

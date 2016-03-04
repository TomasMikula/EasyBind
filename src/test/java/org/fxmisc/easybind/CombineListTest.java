package org.fxmisc.easybind;

import static org.junit.Assert.*;
import javafx.beans.binding.Binding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

public class CombineListTest {

    @Test
    public void test() {
        ObservableList<Integer> list = FXCollections.observableArrayList();
        Binding<Integer> sum = EasyBind.weakCombine(list, stream -> stream.reduce((a, b) -> a + b).orElse(0));

        Counter counter = new Counter();
        sum.addListener(obs -> counter.inc());

        assertEquals(0, sum.getValue().intValue());
        assertEquals(0, counter.getAndReset());

        Integer a = 1;
        Integer b = 2;
        Integer c = 4;
        Integer d = 8;

        // check that added items are reflected
        list.add(a);
        list.add(b);
        assertEquals(3, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());
        list.add(c);
        list.add(d);
        assertEquals(15, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());

        // check that value removal is reflected
        list.remove(b);
        list.remove(d);
        assertEquals(5, sum.getValue().intValue());
        assertEquals(1, counter.getAndReset());

        // check that changing a removed item does not affect or invalidate the sum
        b = 32;
        d = 64;
        assertEquals(5, sum.getValue().intValue());
        assertEquals(0, counter.getAndReset());

        // check that no more invalidations arrive after disposal
        sum.dispose();
        a = 2;
        c = 8;
        assertEquals(0, counter.getAndReset());
    }

}

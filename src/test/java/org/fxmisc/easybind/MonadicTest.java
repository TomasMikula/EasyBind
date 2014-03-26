package org.fxmisc.easybind;

import static org.junit.Assert.*;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.fxmisc.easybind.monadic.MonadicBinding;
import org.junit.Test;

public class MonadicTest {

    private static class A {
        public final Property<B> b = new SimpleObjectProperty<>();
    }

    private static class B {
        public final Property<String> s = new SimpleStringProperty();
    }

    @Test
    public void flatMapTest() {
        Property<A> base = new SimpleObjectProperty<>();
        MonadicBinding<String> select = EasyBind.monadic(base).flatMap(a -> a.b).flatMap(b -> b.s);

        Counter invalidationCounter = new Counter();
        select.addListener(obs -> invalidationCounter.inc());

        assertNull(select.getValue());

        A a = new A();
        B b = new B();
        b.s.setValue("s1");
        a.b.setValue(b);
        base.setValue(a);
        assertEquals(1, invalidationCounter.getAndReset());
        assertEquals("s1", select.getValue());

        a.b.setValue(new B());
        assertEquals(1, invalidationCounter.getAndReset());
        assertNull(select.getValue());

        b.s.setValue("s2");
        assertEquals(0, invalidationCounter.getAndReset());
        assertNull(select.getValue());

        a.b.getValue().s.setValue("x");
        assertEquals(1, invalidationCounter.getAndReset());
        assertEquals("x", select.getValue());

        a.b.setValue(null);
        assertEquals(1, invalidationCounter.getAndReset());
        assertNull(select.getValue());

        a.b.setValue(b);
        assertEquals(1, invalidationCounter.getAndReset());
        assertEquals("s2", select.getValue());
    }

    @Test
    public void orElseTest() {
        StringProperty s1 = new SimpleStringProperty("a");
        StringProperty s2 = new SimpleStringProperty("b");
        StringProperty s3 = new SimpleStringProperty("c");

        MonadicBinding<String> firstNonNull = EasyBind.monadic(s1).orElse(s2).orElse(s3);
        assertEquals("a", firstNonNull.getValue());

        s2.set(null);
        assertEquals("a", firstNonNull.getValue());

        s1.set(null);
        assertEquals("c", firstNonNull.getValue());

        s2.set("b");
        assertEquals("b", firstNonNull.getValue());

        s2.set(null);
        s3.set(null);
        assertNull(firstNonNull.getValue());
    }

}

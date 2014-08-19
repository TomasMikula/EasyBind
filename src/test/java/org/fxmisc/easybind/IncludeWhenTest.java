package org.fxmisc.easybind;

import static org.junit.Assert.*;

import java.util.Arrays;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

public class IncludeWhenTest {

    @Test
    public void test() {
        ObservableList<String> list = FXCollections.observableArrayList("a", "b");

        BooleanProperty condC = new SimpleBooleanProperty(false);
        BooleanProperty condD = new SimpleBooleanProperty(true);

        Subscription subC = EasyBind.includeWhen(list, "c", condC);
        Subscription subD = EasyBind.includeWhen(list, "d", condD);

        assertEquals(Arrays.asList("a", "b", "d"), list);

        list.add("c");
        assertEquals(Arrays.asList("a", "b", "d", "c"), list);

        condC.set(true);
        assertEquals(Arrays.asList("a", "b", "d", "c", "c"), list);

        condC.set(false);
        assertEquals(Arrays.asList("a", "b", "d", "c"), list);

        list.remove("c");
        assertEquals(Arrays.asList("a", "b", "d"), list);

        condC.set(true);
        assertEquals(Arrays.asList("a", "b", "d", "c"), list);

        list.add("c");
        assertEquals(Arrays.asList("a", "b", "d", "c", "c"), list);

        condC.set(false);
        assertEquals(Arrays.asList("a", "b", "d", "c"), list);

        subC.unsubscribe();
        list.remove("c");
        condC.set(true);
        assertEquals(Arrays.asList("a", "b", "d"), list);

        subD.unsubscribe();
        condD.set(false);
        assertEquals(Arrays.asList("a", "b", "d"), list);
    }

}

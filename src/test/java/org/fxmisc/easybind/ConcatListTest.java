package org.fxmisc.easybind;

import static org.junit.Assert.*;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Test;

public class ConcatListTest {
	@Test
	public void test() {
		ObservableList<String> a = FXCollections.observableArrayList("zero", "one", "two");
		ObservableList<String> b = FXCollections.observableArrayList("three", "four", "five");
		ObservableList<String> c = EasyBind.concat(a, b);
		ObservableList<String> d = EasyBind.concat(a, a);

		StringBinding bindOne = Bindings.stringValueAt(c, 1);
		StringBinding bindFour = Bindings.stringValueAt(c, 4);

		assertEquals(6, c.size());
		assertEquals("one", bindOne.get());
		assertEquals("five", c.get(5));

		a.remove(1);
		assertEquals(5, c.size());
		assertEquals("three", c.get(2));
		assertEquals("two", bindOne.get());
		assertEquals("five", bindFour.get());

		b.add(1, "x");
		assertEquals(6, c.size());
		assertEquals("x", c.get(3));
		assertEquals("four", bindFour.get());

		a.set(0, "null");
		assertEquals("null", c.get(0));

		assertEquals(4, d.size());
		assertEquals("two", d.get(1));
		assertEquals("null", d.get(2));
	}
}

package org.fxmisc.easybind;

import static org.junit.Assert.*;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ConcatListTest {

	abstract class CountedChangeListener<E> implements ListChangeListener<E> {

		private int callCount = 0;

		@Override
		public void onChanged(Change<? extends E> c) {
			callCount++;
			actualChange(c);
		}

		public int getCallCount() {
			return callCount;
		}

		public abstract void actualChange(Change<? extends E> c);
	}

	private ObservableList<String> a;
	private ObservableList<String> b;
	private ObservableList<String> c;
	private ObservableList<String> d;

	private StringBinding bindCOne;
	private StringBinding bindCFour;

	@Before
	public void setup() {
		a = FXCollections.observableArrayList("zero", "one", "two");
		b = FXCollections.observableArrayList("three", "four", "five");
		c = EasyBind.concat(a, b);
		d = EasyBind.concat(a, a);

		bindCOne = Bindings.stringValueAt(c, 1);
		bindCFour = Bindings.stringValueAt(c, 4);
	}

	@Test
	public void basicQuery() {
		assertEquals(6, c.size());
		assertEquals("one", bindCOne.get());
		assertEquals("four", bindCFour.get());
		assertEquals("five", c.get(5));
	}

	@Test
	public void removeValueSub() {

		ListChangeListener<String> checkChange = c -> {
			assertTrue(c.wasRemoved());
			assertEquals(1, c.getRemovedSize());

			assertEquals("one", c.getRemoved().get(0));
		};

		CountedChangeListener<String> c_index1Removed =
				new VerifyCountedChangeListener<>(checkChange, 1);

		CountedChangeListener<String> d_index1Removed =
				new VerifyCountedChangeListener<>(checkChange, 2);

		c.addListener(c_index1Removed);
		d.addListener(d_index1Removed);

		a.remove(1);
		assertEquals(5, c.size());
		assertEquals("three", c.get(2));
		assertEquals("two", bindCOne.get());
		assertEquals("five", bindCFour.get());
		assertEquals(1, c_index1Removed.getCallCount());

		assertEquals(4, d.size());
		assertEquals("two", d.get(1));
		assertEquals("two", d.get(3));
		assertEquals(1, d_index1Removed.getCallCount());

	}

	@Test
	public void addElement() {

		CountedChangeListener<String> c_index1Added =
				new VerifyCountedChangeListener<>(c -> {
					assertTrue(c.wasAdded());
					assertEquals(1, c.getAddedSize());

					assertEquals("x", c.getAddedSubList().get(0));
				}, 1);

		CountedChangeListener<String> d_index1Added = new CountedChangeListener<String>() {
					@Override
					public void actualChange(Change c) {
						fail("Should not be called, d is not changed.");
					}
				};

		c.addListener(c_index1Added);
		d.addListener(d_index1Added);

		b.add(1, "x"); // "three", "x", "four", "five"

		List<String> expectedC = Arrays.asList("zero", "one", "two", "three", "x", "four", "five");
		assertEquals(expectedC, c);
		assertEquals("x", c.get(a.size() + 1));
		assertEquals("x", bindCFour.get());
		assertEquals(1, c_index1Added.getCallCount());
	}

	@Test
	public void setItem() {
		ListChangeListener<String> checkChange = c -> {
			assertTrue(c.wasAdded());
			assertEquals(1, c.getAddedSize());

			assertEquals("null", c.getAddedSubList().get(0));
		};

		CountedChangeListener<String> c_index0Update =
				new VerifyCountedChangeListener<>(checkChange, 1);

		CountedChangeListener<String> d_index0Update =
				new VerifyCountedChangeListener<>(checkChange, 2);

		c.addListener(c_index0Update);
		d.addListener(d_index0Update);

		// Trigger an "Added" event for the list.. because odd.. not Updated
		a.set(0, "null");

		List<String> expectedC = Arrays.asList("null", "one", "two", "three", "four", "five");
		List<String> expectedD = Arrays.asList("null", "one", "two", "null", "one", "two");

		assertEquals(expectedC, c);
		assertEquals(expectedD, d);
		assertEquals(1, c_index0Update.getCallCount());
		assertEquals(1, d_index0Update.getCallCount());
	}

	private class VerifyCountedChangeListener<E> extends CountedChangeListener<E> {
		private final int iterationCount;
		private final ListChangeListener<E> checkChange;

		VerifyCountedChangeListener(ListChangeListener<E> checkChange, int iterationCount) {
			this.checkChange = checkChange;
			this.iterationCount = iterationCount;
		}

		@Override
        public void actualChange(Change<? extends E> c) {
            int iterationCount = 0;
            while (c.next()) {
                checkChange.onChanged(c);
                iterationCount++;
            }

            assertEquals(this.iterationCount, iterationCount);
        }
	}
}

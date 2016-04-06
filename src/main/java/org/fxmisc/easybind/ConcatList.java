package org.fxmisc.easybind;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

class ConcatList<E> extends ObservableListBase<E> {
	private final List<ObservableList<? extends E>> sourceLists;

	public ObservableConcatList(List<ObservableList<? extends E>> sourceLists) {
		assert sourceLists != null;
		this.sourceLists = sourceLists;
		for (ObservableList<? extends E> source : sourceLists)
			source.addListener(this::onSourceListChanged);
	}

	private void onSourceListChanged(ListChangeListener.Change<? extends E> change) {
		ObservableList<? extends E> source = change.getList();
		int indexOffset = 0;
		for (int i = 0; sourceLists.get(i) != source; ++i)
			indexOffset += sourceLists.get(i).size();

		beginChange();
		while (change.next()) {
			if (change.wasPermutated()) {
				int rangeSize = change.getTo() - change.getFrom();
				int[] permutation = new int[rangeSize];
				for (int i = 0; i < rangeSize; ++i)
					permutation[i] = change.getPermutation(i + change.getFrom()) + indexOffset;
				nextPermutation(change.getFrom() + indexOffset, change.getTo() + indexOffset, permutation);
			} else if (change.wasUpdated()) {
				for (int i = change.getFrom(); i < change.getTo(); ++i)
					nextUpdate(i+indexOffset);
			} else if (change.wasAdded()) {
				nextAdd(change.getFrom()+indexOffset, change.getTo()+indexOffset);
			} else
				nextRemove(change.getFrom()+indexOffset, change.getRemoved());
		}
		endChange();
	}

	@Override
	public E get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException("List index must be >= 0. Was " + index);

		for (ObservableList<? extends E> source : sourceLists) {
			if (index < source.size())
				return source.get(index);
			index -= source.size();
		}
		throw new IndexOutOfBoundsException("Index too large.");
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			Iterator<ObservableList<? extends E>> sourceIterator = sourceLists.iterator();
			Iterator<? extends E> currentIterator = null;

			@Override
			public boolean hasNext() {
				while (currentIterator == null || !currentIterator.hasNext())
					if (sourceIterator.hasNext())
						currentIterator = sourceIterator.next().iterator();
					else
						return false;
				return true;
			}

			@Override
			public E next() {
				while (currentIterator == null || !currentIterator.hasNext())
					if (sourceIterator.hasNext())
						currentIterator = sourceIterator.next().iterator();
					else
						throw new NoSuchElementException();
				return currentIterator.next();
			}
		};
	}

	@Override
	public int size() {
		return sourceLists.stream().mapToInt(ObservableList<? extends E>::size).sum();
	}
}

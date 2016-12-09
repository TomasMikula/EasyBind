package org.fxmisc.easybind;

import java.util.*;
import java.util.function.Consumer;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

class FlattenedList<E> extends ObservableListBase<E> {
	private final ObservableList<ObservableList<? extends E>> sourceLists;

	FlattenedList(ObservableList<ObservableList<? extends E>> sourceLists) {
		if (sourceLists == null) {
			throw new NullPointerException("sourceLists = null");
		}

		this.sourceLists = sourceLists;

		// We make a Unique set of source lists, otherwise the event gets called multiple
		// times if there are duplicate lists.
		Set<ObservableList<? extends E>> sourcesSet = new HashSet<>(sourceLists);
		sourcesSet.forEach(source -> source.addListener(this::onSourceChanged));

		sourceLists.addListener(this::onSourcesListChanged);
	}

	private void onSourcesListChanged(ListChangeListener.Change<? extends ObservableList<? extends E>> change) {

		beginChange();

		while (change.next()) {
			int fromIdx = 0; // Flattened start idx
			for (int i = 0; i < change.getFrom(); ++i) {
				fromIdx += sourceLists.get(i).size();
			}

			int toIdx = fromIdx; // Flattened end idx
			for (int i = change.getFrom(); i < change.getTo(); ++i) {
				toIdx += sourceLists.get(i).size();
			}

			final int rangeSize = toIdx - fromIdx;

			if (change.wasPermutated()) {

				// build up a set of permutations based on the offsets AND the actual permutation
				int[] permutation = new int[rangeSize];
				int fIdx = fromIdx;
				for (int parentIdx = change.getFrom(); parentIdx < change.getTo(); ++parentIdx) {
					for (int i = 0; i < sourceLists.get(i).size(); ++i, fIdx++) {
						permutation[fIdx] = change.getPermutation(parentIdx) + i;
					}
				}

				nextPermutation(fromIdx, toIdx, permutation);
			} else if (change.wasUpdated()) {
				// Just iterate over the fromIdx..toIdx
				for (int i = fromIdx; i < toIdx; ++i) {
					nextUpdate(i);
				}
			} else if (change.wasAdded()) {
				nextAdd(fromIdx, toIdx);
			} else {
				// Each remove is indexed
				List<E> itemsToRemove = new ArrayList<>(rangeSize);

				change.getRemoved().forEach(itemsToRemove::addAll);

				nextRemove(fromIdx, itemsToRemove);
			}
		}

		endChange();
	}

	private void onSourceChanged(ListChangeListener.Change<? extends E> change) {
		ObservableList<? extends E> source = change.getList();

		List<Integer> offsets = new ArrayList<>();
		int calcOffset = 0;
		for (ObservableList<? extends E> currList : sourceLists) {
			if (currList == source) {
				offsets.add(calcOffset);
			}

			calcOffset += currList.size();
		}

		// Because a List could be duplicated, we have to do the change for EVERY offset.
		// Annoying, but it's needed.

		beginChange();
		while (change.next()) {
			if (change.wasPermutated()) {
				int rangeSize = change.getTo() - change.getFrom();

				// build up a set of permutations based on the offsets AND the actual permutation
				int[] permutation = new int[rangeSize * offsets.size()];
				for (int offsetIdx = 0; offsetIdx < offsets.size(); ++offsetIdx) {
					int indexOffset = offsets.get(offsetIdx);
					for (int i = 0; i < rangeSize; ++i) {
						permutation[i + offsetIdx * rangeSize] =
								change.getPermutation(i + change.getFrom()) + indexOffset;
					}
				}

				for (int indexOffset: offsets) {
					nextPermutation(change.getFrom() + indexOffset, change.getTo() + indexOffset, permutation);
				}
			} else if (change.wasUpdated()) {

				// For each update, it's just the index from getFrom()..getTo() + indexOffset
				for (int indexOffset: offsets) {
					for (int i = change.getFrom(); i < change.getTo(); ++i) {
						nextUpdate(i + indexOffset);
					}
				}
			} else if (change.wasAdded()) {

				// Each Add is just from() + the offset
				for (int indexOffset: offsets) {
					nextAdd(change.getFrom() + indexOffset, change.getTo() + indexOffset);
				}

			} else {
				// Each remove is indexed
				for (int indexOffset: offsets) {
					nextRemove(change.getFrom() + indexOffset, change.getRemoved());
				}
			}
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
		return sourceLists.stream().mapToInt(ObservableList::size).sum();
	}
}

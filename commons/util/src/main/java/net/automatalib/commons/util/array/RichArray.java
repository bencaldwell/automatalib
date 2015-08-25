/* Copyright (C) 2015 TU Dortmund
 * This file is part of AutomataLib, http://www.automatalib.net/.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.automatalib.commons.util.array;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkPositionIndex;
import static com.google.common.base.Preconditions.checkPositionIndexes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;


/**
 * A "modern", object-oriented wrapper for plain Java arrays.
 * <p>
 * Instances of this class are backed by a plain Java array. The reference to this array never changes.
 * It is impossible to obtain an outside reference to the backing array; however, if an array
 * is supplied to a constructor of this class, all methods of this class manipulate the original array.
 * <p>
 * This class was designed for efficiency. The many methods it offers are intended to allow direct
 * manipulation of the backing array as often as possible, eliminating the indirection via
 * {@link #get(int)} and {@link #update(int, Object)} methods.
 * <p>
 * This class is designed to "feel" like an array as much as possible. This includes:
 * <ul>
 * <li>its length (or size) is fixed and cannot be changed once created</li>
 * <li>the length is accessible via a {@code public final} field {@link #length}</li>
 * <li>it is (shallow) cloneable</li>
 * </ul>
 * On the other hand, it implements the {@link List} interface. Note, however, that none of the
 * optional methods are implemented.
 * 
 * @author Malte Isberner
 *
 * @param <T> element type
 */
public class RichArray<T> implements List<T>, IntFunction<T>, RandomAccess, Serializable, Cloneable {
	
	private static final long serialVersionUID = 1L;
	
	private final T[] contents;
	private final int start;
	public final int length;

	@SuppressWarnings("unchecked")
	public RichArray(int length) {
		this((T[]) new Object[length]);
	}
	
	public RichArray(T[] contents, int start, int end) {
		checkPositionIndexes(start, end, contents.length);
		this.contents = contents;
		this.start = start;
		this.length = end - start;
	}
	
	public RichArray(T[] contents) {
		this(contents, 0, contents.length);
	}
	
	@SuppressWarnings("unchecked")
	public RichArray(Collection<? extends T> coll) {
		this((T[]) coll.toArray());
	}
	
	
	public RichArray(int length, T value) {
		this(length);
		for (int i = 0; i < length; i++) {
			contents[i] = value;
		}
	}
	
	public RichArray(int length, Supplier<? extends T> valueSupp) {
		this(length);
		setAll(valueSupp);
	}
	
	public RichArray(int length, IntFunction<? extends T> initializer) {
		this(length);
		setAll(initializer);
	}
	
	@Override
	public int hashCode() {
		// hashcode calculation as specified in List javadoc
		int hashCode = 1;
		int end = end();
		for (int i = start; i < end; i++) {
			hashCode = 31*hashCode + Objects.hashCode(contents[i]);
		}
		return hashCode;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (!(o instanceof List)) {
			return false;
		}
		return equals((List<?>) o);
	}
	
	public boolean equals(@Nonnull List<?> other) {
		if (other.getClass() == RichArray.class) {
			return equals((RichArray<?>) other);
		}
		if (length != other.size()) {
			return false;
		}
		Iterator<?> otherIt = other.iterator();
		
		int end = end();
		for (int i = start; i < end; i++) {
			assert otherIt.hasNext();
			if (!Objects.equals(contents[i], otherIt.next())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(@Nonnull RichArray<?> other) {
		if (length != other.length) {
			return false;
		}
		int end = end();
		for (int i = start, j = other.start; i < end; i++, j++) {
			if (!Objects.equals(contents[i], other.contents[j])) {
				return false;
			}
		}
		return true;
	}
	
	public boolean contentEquals(Object[] array) {
		return contentEquals(array, 0, array.length);
	}
	
	public boolean contentEquals(Object[] array, int start, int end) {
		checkPositionIndexes(start, end, array.length);
		int alength = end - start;
		if (alength != length) {
			return false;
		}
		for (int i = this.start, j = start; j < end; i++, j++) {
			if (!Objects.equals(contents[i], array[j])) {
				return false;
			}
		}
		return true;
	}
	
	public boolean contentDeepEquals(Object[] array) {
		return contentDeepEquals(array, 0, array.length);
	}
	
	public boolean contentDeepEquals(Object[] array, int start, int end) {
		checkPositionIndexes(start, end, array.length);
		int alength = end - start;
		if (alength != length) {
			return false;
		}
		for (int i = this.start, j = start; j < end; i++, j++) {
			if (!Objects.deepEquals(contents[i], array[j])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public RichArray<T> clone() {
		return new RichArray<>((T[]) toArray());
	}

	@Override
	public Iterator<T> iterator() {
		return listIterator();
	}

	@Override
	public int size() {
		return length;
	}

	@Override
	public boolean isEmpty() {
		return length == 0;
	}

	@Override
	public boolean contains(Object o) {
		int end = end();
		for (int i = start; i < end; i++) {
			if (Objects.equals(o, contents[i])) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object[] toArray() {
		Object[] result = new Object[length];
		System.arraycopy(contents, start, result, 0, length);
		return result;
	}

	@Override
	public <U> U[] toArray(U[] a) {
		if (a.length < length) {
			a = Arrays.copyOf(a, length);
		}
		System.arraycopy(contents, start, a, 0, length);
		return a;
	}

	@Override
	@Deprecated
	public boolean add(T e) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		int end = end();
outer:	for (Object o : c) {
			for (int i = start; i < end; i++) {
				if (Objects.equals(o, contents[i])) {
					continue outer;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(int index) {
		checkElementIndex(index, length);
		return contents[start + index];
	}

	@Override
	public T set(int index, T element) {
		checkElementIndex(index, length);
		int effIndex = start + index;
		T old = contents[effIndex];
		contents[effIndex] = element;
		return old;
	}
	
	public void update(int index, T element) {
		checkElementIndex(index, length);
		contents[start+index] = element;
	}

	@Override
	@Deprecated
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Deprecated
	public T remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int indexOf(Object o) {
		int end = end();
		for (int i = start; i < end; i++) {
			if (Objects.equals(o, contents[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		for (int i = end() - 1; i >= start; i--) {
			if (Objects.equals(o, contents[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ArrayIterator<>(contents, start, end());
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		checkPositionIndex(index, length);
		return new ArrayIterator<>(contents, start + index, end());
	}

	@Override
	@Deprecated
	public RichArray<T> subList(int fromIndex, int toIndex) {
		return slice(fromIndex, toIndex);
	}
	
	public RichArray<T> slice(int fromIndex, int toIndex) {
		checkPositionIndexes(fromIndex, toIndex, length);
		return new RichArray<>(contents, start + fromIndex, start + toIndex);
	}
	
	public RichArray<T> copyFrom(T[] source, int sourceOfs, int destOfs, int len) {
		// source being checked by System.arraycopy
		checkPositionIndexes(destOfs, destOfs + len, length);
		System.arraycopy(source, sourceOfs, contents, start + destOfs, len);
		return this;
	}
	
	public RichArray<T> copyInto(int sourceOfs, Object[] dest, int destOfs, int len) {
		checkPositionIndexes(sourceOfs, sourceOfs + len, length);
		// dest being checked by System.arraycopy
		System.arraycopy(contents, start + sourceOfs, dest, destOfs, len);
		return this;
	}
	
	@Override
	public void sort(Comparator<? super T> cmp) {
		Arrays.sort(contents, start, end(), cmp);
	}
	
	public void sort() {
		Arrays.sort(contents, start, end());
	}
	
	public void parallelSort(Comparator<? super T> cmp) {
		Arrays.parallelSort(contents, start, end(), cmp);
	}
	
	@SuppressWarnings("unchecked")
	public void parallelSort() {
		Arrays.parallelSort(contents, start, end(), (x, y) -> ((Comparable<T>)x).compareTo(y));
	}
	
	public void sort(int startInclusive, int endExclusive, Comparator<? super T> cmp) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Arrays.sort(contents, start + startInclusive, start + endExclusive, cmp);
	}
	
	public void sort(int startInclusive, int endExclusive) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Arrays.sort(contents, start + startInclusive, start + endExclusive);
	}
	
	public void parallelSort(int startInclusive, int endExclusive, Comparator<? super T> cmp) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Arrays.parallelSort(contents, start + startInclusive, start + endExclusive, cmp);
	}
	
	@SuppressWarnings("unchecked")
	public void parallelSort(int startInclusive, int endExclusive) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Arrays.parallelSort(contents, start + startInclusive, start + endExclusive,
				(x, y) -> ((Comparable<T>)x).compareTo(y));
	}
	
	public void parallelPrefix(BinaryOperator<T> op) {
		Arrays.parallelPrefix(contents, start, end(), op);
	}
	
	public void parallelPrefix(int startInclusive, int endExclusive, BinaryOperator<T> op) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Arrays.parallelPrefix(contents, start + startInclusive, start + endExclusive, op);
	}
	
	public int binarySearch(T key, Comparator<? super T> cmp) {
		int result = Arrays.binarySearch(contents, start, end(), key, cmp);
		if (result != -1) {
			result -= start;
		}
		return result;
	}
	
	public int binarySearch(int startInclusive, int endExclusive, T key, Comparator<? super T> cmp) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		int result = Arrays.binarySearch(contents, start + startInclusive, start + endExclusive, key, cmp);
		if (result != -1) {
			result -= start;
		}
		return result;
	}
	
	public void parallelSetAll(IntFunction<? extends T> generator) {
		Objects.requireNonNull(generator);
		IntStream.range(start, end()).parallel().forEach(i -> { contents[i] = generator.apply(i - start); });
	}
	
	public void parallelSetAll(int startInclusive, int endExclusive, IntFunction<? extends T> generator) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(generator);
		IntStream.range(start + startInclusive, start + endExclusive).parallel().forEach(i -> { contents[i] = generator.apply(i - start); });
	}
	
	public void setAll(IntFunction<? extends T> generator) {
		Objects.requireNonNull(generator);
		for (int i = start, j = 0; j < length; i++, j++) {
			contents[i] = generator.apply(j);
		}
	}
	
	public void setAll(int startInclusive, int endExclusive, IntFunction<? extends T> generator) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(generator);
		for (int i = start + startInclusive, j = startInclusive; j < endExclusive; i++, j++) {
			contents[i] = generator.apply(j);
		}
	}
	
	
	public void parallelSetAll(T value) {
		IntStream.range(start, end()).parallel().forEach(i -> { contents[i] = value; });
	}
	
	public void parallelSetAll(int startInclusive, int endExclusive, T value) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		IntStream.range(start + startInclusive, start + endExclusive).parallel().forEach(i -> { contents[i] = value; });
	}
	
	public void setAll(T value) {
		int end = end();
		for (int i = start; i < end; i++) {
			contents[i] = value;
		}
	}
	
	public void setAll(int startInclusive, int endExclusive, T value) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		int end = start + endExclusive;
		for (int i = start + startInclusive; i < end; i++) {
			contents[i] = value;
		}
	}
	
	
	public void parallelSetAll(Supplier<? extends T> supplier) {
		Objects.requireNonNull(supplier);
		IntStream.range(start, end()).parallel().forEach(i -> { contents[i] = supplier.get(); });
	}
	
	public void parallelSetAll(int startInclusive, int endExclusive, Supplier<? extends T> supplier) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(supplier);
		IntStream.range(start + startInclusive, start + endExclusive).parallel().forEach(i -> { contents[i] = supplier.get(); });
	}
	
	public void setAll(Supplier<? extends T> supplier) {
		Objects.requireNonNull(supplier);
		int end = end();
		for (int i = start; i < end; i++) {
			contents[i] = supplier.get();
		}
	}
	
	public void setAll(int startInclusive, int endExclusive, Supplier<? extends T> supplier) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(supplier);
		int end = start + endExclusive;
		for (int i = start + startInclusive; i < end; i++) {
			contents[i] = supplier.get();
		}
	}
	
	public void swap(int i, int j) {
		checkElementIndex(i, length);
		checkElementIndex(j, length);
		doSwap(contents, start + i, start + j);
	}
	
	public void shuffle(Random r) {
		for (int i = end(); i > start; i++) {
			doSwap(contents, i - 1, start + r.nextInt(i - start));
		}
	}
	
	public void shuffle() {
		shuffle(new Random());
	}
	
	public void reverse() {
		for (int i = start, j = end() - 1; i < j; i++, j--) {
			doSwap(contents, i, j);
		}
	}

	@Override
	public Spliterator<T> spliterator() {
		return Arrays.spliterator(contents, start, end());
	}
	
	public Spliterator<T> spliterator(int startInclusive, int endExclusive) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		return Arrays.spliterator(contents, start + startInclusive, start + endExclusive);
	}

	@SuppressWarnings("unchecked")
	public int minIndex() {
		return minIndex((a, b) -> ((Comparable<T>)a).compareTo(b));
	}
	
	public int minIndex(Comparator<? super T> cmp) {
		Objects.requireNonNull(cmp);
		
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		T best = contents[start];
		int bestIdx = start;
		int end = end();
		for (int i = start + 1; i < end; i++) {
			T curr = contents[i];
			if (cmp.compare(curr, best) < 0) {
				best = curr;
				bestIdx = i;
			}
		}
		
		return bestIdx - start;
	}
	
	public T min() {
		int minIdx = minIndex();
		return contents[start + minIdx];
	}
	
	public T min(Comparator<? super T> cmp) {
		int minIdx = minIndex(cmp);
		return contents[start + minIdx];
	}
	
	@SuppressWarnings("unchecked")
	public int maxIndex() {
		return maxIndex((a, b) -> ((Comparable<T>) a).compareTo(b));
	}
	
	public int maxIndex(Comparator<? super T> cmp) {
		Objects.requireNonNull(cmp);
		if (isEmpty()) {
			throw new NoSuchElementException();
		}
		T best = contents[start];
		int bestIdx = start;
		int end = end();
		for (int i = start + 1; i < end; i++) {
			T curr = contents[i];
			if (cmp.compare(curr, best) > 0) {
				best = curr;
				bestIdx = i;
			}
		}
		
		return bestIdx - start;
	}
	
	public T max() {
		int maxIdx = maxIndex();
		return contents[start + maxIdx];
	}
	
	public T max(Comparator<? super T> cmp) {
		int maxIdx = maxIndex(cmp);
		return contents[start + maxIdx];
	}
	
	
	
	public void transformSingle(int index, Function<? super T,? extends T> transformer) {
		checkElementIndex(index, length);
		Objects.requireNonNull(transformer);
		int effIdx = start + index;
		T elem = contents[effIdx];
		contents[effIdx] = transformer.apply(elem);
	}
	
	public void transform(Function<? super T,? extends T> transformer) {
		Objects.requireNonNull(transformer);
		int end = end();
		for (int i = start; i < end; i++) {
			contents[i] = transformer.apply(contents[i]);
		}
	}
	
	public void transform(int startInclusive, int endExclusive, Function<? super T,? extends T> transformer) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(transformer);
		
		int end = start + endExclusive;
		for (int i = start + startInclusive; i < end; i++) {
			contents[i] = transformer.apply(contents[i]);
		}
	}
	
	public RichArray<T> parallelTransform(Function<? super T,? extends T> transformer) {
		Objects.requireNonNull(transformer);
		IntStream.range(start, end()).parallel().forEach(i -> { contents[i] = transformer.apply(contents[i]); });
		return this;
	}
	
	public RichArray<T> parallelTransform(int startInclusive, int endExclusive, Function<? super T,? extends T> transformer) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(transformer);
		IntStream.range(start + startInclusive, start + endExclusive).parallel().forEach(i -> { contents[i] = transformer.apply(contents[i]); });
		return this;
	}
	
	public RichArray<T> transformWithIndex(WithIndexTransformer<? super T, ? extends T> transformer) {
		Objects.requireNonNull(transformer);
		IntStream.range(start, end()).forEach(i -> { contents[i] = transformer.apply(i - start, contents[i]); });
		return this;
	}
	
	public RichArray<T> transformWithIndex(int startInclusive, int endExclusive,
			WithIndexTransformer<? super T,? extends T> transformer) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(transformer);
		IntStream.range(start + startInclusive, start + endExclusive).forEach(i -> { contents[i] = transformer.apply(i - start, contents[i]); });
		return this;
	}
	
	public RichArray<T> parallelTransformWithIndex(WithIndexTransformer<? super T, ? extends T> transformer) {
		Objects.requireNonNull(transformer);
		IntStream.range(start, end()).parallel().forEach(i -> { contents[i] = transformer.apply(i - start, contents[i]); });
		return this;
	}
	
	public RichArray<T> parallelTransformWithIndex(int startInclusive, int endExclusive,
			WithIndexTransformer<? super T,? extends T> transformer) {
		checkPositionIndexes(startInclusive, endExclusive, length);
		Objects.requireNonNull(transformer);
		IntStream.range(start + startInclusive, start + endExclusive).parallel().forEach(i -> { contents[i] = transformer.apply(i - start, contents[i]); });
		return this;
	}

	@Override
	public T apply(int value) {
		return get(value);
	}
	
	private int end() {
		return start + length;
	}
	
	
	private static final <T> void doSwap(T[] array, int i, int j) {
		T tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;
	}
}

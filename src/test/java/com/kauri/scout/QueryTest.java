/*
 * This file is part of the scout package.
 *
 * Copyright (C) 2013, Eric Fritz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without 
 * restriction, including without limitation the rights to use, copy, modify, merge, publish, 
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
 */

package com.kauri.scout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Fritz
 */
abstract public class QueryTest
{
	public <T> List<T> getVisited(SpatialIndex<T> index, Query query)
	{
		return getVisited(index, query, Integer.MAX_VALUE);
	}

	public <T> List<T> getVisited(SpatialIndex<T> index, Query query, final int limit)
	{
		final List<T> visited = new ArrayList<>();

		index.query(query, new QueryResultVisitor<T>() {
			@Override
			public boolean visit(T o)
			{
				return visited.add(o) && visited.size() < limit;
			}
		});

		return visited;
	}

	public <T> List<Pair<T>> getVisited(SpatialIndex<T> index1, SpatialIndex<T> index2, JoinQuery query)
	{
		return getVisited(index1, index2, query, Integer.MAX_VALUE);
	}

	public <T> List<Pair<T>> getVisited(SpatialIndex<T> index1, SpatialIndex<T> index2, JoinQuery query, final int limit)
	{
		final List<Pair<T>> visited = new ArrayList<>();

		index1.query(index2, query, new JoinQueryResultVisitor<T, T>() {
			@Override
			public boolean visit(T o1, T o2)
			{
				return visited.add(new Pair<T>(o1, o2)) && visited.size() < limit;
			}
		});

		return visited;
	}

	public <T> void ensureSame(List<T> actual, List<T> expected)
	{
		assertEquals(expected.size(), actual.size());

		for (T item : expected) {
			assertTrue(actual.contains(item));
		}
	}

	public <T> void ensureSameSymmetric(List<Pair<T>> actual, List<Pair<T>> expected)
	{
		assertEquals(expected.size(), actual.size());

		for (Pair<T> item : expected) {
			assertTrue(actual.contains(item) || actual.contains(new Pair<>(item.o2, item.o1)));
		}
	}

	public class Pair<T>
	{
		T o1;
		T o2;

		public Pair(T o1, T o2)
		{
			this.o1 = o1;
			this.o2 = o2;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) {
				return true;
			}

			if (o == null || !(o instanceof Pair<?>)) {
				return false;
			}

			return ((Pair<?>) o).o1.equals(o1) && ((Pair<?>) o).o2.equals(o2);
		}
	}

}

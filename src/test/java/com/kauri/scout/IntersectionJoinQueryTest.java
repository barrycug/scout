/*
 * This file is part of the scout package.
 *
 * Copyright (c) 2014 Eric Fritz
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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class IntersectionJoinQueryTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void testOneIndex()
	{
		SpatialIndex<Object> index = new SpatialIndex<>();

		Object o1, o2, o3, o4;
		index.insert(o1 = new Object(), new AABB2(0, 1, 2, 2)); // Intersects o2
		index.insert(o2 = new Object(), new AABB2(1, 1, 2, 3)); // Intersects o1, o3
		index.insert(o3 = new Object(), new AABB2(3, 1, 2, 2)); // Intersects o2, o4
		index.insert(o4 = new Object(), new AABB2(5, 3, 1, 1)); // Intersects o3

		List<Pair<Object>> expected = new ArrayList<>();
		expected.add(new Pair<>(o1, o2));
		expected.add(new Pair<>(o2, o3));
		expected.add(new Pair<>(o3, o4));

		ensureSameSymmetric(getVisited(index, index, new IntersectionJoinQuery()), expected);
	}

	@Test
	public void testOneIndexBulk()
	{
		SpatialIndex<Object> index = new SpatialIndex<>();
		List<Pair<Object>> expected = new ArrayList<>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			index.insert(o1 = new Object(), new AABB2(2 * i, 0, 1, 2));
			index.insert(o2 = new Object(), new AABB2(2 * i, 1, 1, 2));

			expected.add(new Pair<>(o1, o2));
		}

		ensureSameSymmetric(getVisited(index, index, new IntersectionJoinQuery()), expected);
	}

	@Test
	public void testTwoIndices()
	{
		SpatialIndex<Object> index1 = new SpatialIndex<>();
		SpatialIndex<Object> index2 = new SpatialIndex<>();

		Object o1, o2, o3;
		index1.insert(o1 = new Object(), new AABB2(3, 0, 2, 2)); // Intersects o4
		index1.insert(o2 = new Object(), new AABB2(0, 1, 4, 4)); // Intersects o4, o5, o6
		index1.insert(o3 = new Object(), new AABB2(3, 4, 1, 1)); // Intersects o6

		Object o4, o5, o6;
		index2.insert(o4 = new Object(), new AABB2(3, 0, 2, 2)); // Intersects o1, o2
		index2.insert(o5 = new Object(), new AABB2(0, 1, 2, 2)); // Intersects o2
		index2.insert(o6 = new Object(), new AABB2(2, 5, 2, 2)); // Intersects o2, o3

		List<Pair<Object>> expected1 = new ArrayList<>();
		expected1.add(new Pair<>(o1, o4));
		expected1.add(new Pair<>(o2, o4));
		expected1.add(new Pair<>(o2, o5));
		expected1.add(new Pair<>(o2, o6));
		expected1.add(new Pair<>(o3, o6));

		List<Pair<Object>> expected2 = new ArrayList<>();
		expected2.add(new Pair<>(o4, o1));
		expected2.add(new Pair<>(o4, o2));
		expected2.add(new Pair<>(o5, o2));
		expected2.add(new Pair<>(o6, o2));
		expected2.add(new Pair<>(o6, o3));

		ensureSame(getVisited(index1, index2, new IntersectionJoinQuery()), expected1);
		ensureSame(getVisited(index2, index1, new IntersectionJoinQuery()), expected2);
	}

	@Test
	public void testTwoIndicesBulk()
	{
		SpatialIndex<Object> index1 = new SpatialIndex<>();
		SpatialIndex<Object> index2 = new SpatialIndex<>();

		List<Pair<Object>> expected1 = new ArrayList<>();
		List<Pair<Object>> expected2 = new ArrayList<>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			index1.insert(o1 = new Object(), new AABB2(2 * i, 0, 1, 2));
			index2.insert(o2 = new Object(), new AABB2(2 * i, 1, 1, 2));

			expected1.add(new Pair<>(o1, o2));
			expected2.add(new Pair<>(o2, o1));
		}

		ensureSame(getVisited(index1, index2, new IntersectionJoinQuery()), expected1);
		ensureSame(getVisited(index2, index1, new IntersectionJoinQuery()), expected2);
	}
}

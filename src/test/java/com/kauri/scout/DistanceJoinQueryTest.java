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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class DistanceJoinQueryTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void testOneIndex()
	{
		SpatialIndex<Object> index = new SpatialIndex<Object>();

		Object o1, o2, o3, o4;
		index.insert(o1 = new Object(), new AABB2(0, 0, 1, 1)); // In range of o2
		index.insert(o2 = new Object(), new AABB2(4, 5, 5, 6)); // In range of o1, o3, o4
		index.insert(o3 = new Object(), new AABB2(8, 0, 9, 1)); // In range of o2, o4
		index.insert(o4 = new Object(), new AABB2(8, 4, 9, 5)); // In range of o2, o3

		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();
		expected.add(new Pair<Object>(o1, o2));
		expected.add(new Pair<Object>(o2, o3));
		expected.add(new Pair<Object>(o2, o4));
		expected.add(new Pair<Object>(o3, o4));

		ensureSameSymmetric(getVisited(index, index, new DistanceJoinQuery(5)), expected);
	}

	@Test
	public void testOneIndexBulk()
	{
		SpatialIndex<Object> index = new SpatialIndex<Object>();
		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();

		Object o1, o2 = null;
		for (int i = 0; i < ENTITIES; i++) {
			index.insert(o1 = new Object(), new AABB2(5 * i, 0, 5, 5));

			if (o2 != null) {
				expected.add(new Pair<Object>(o2, o1));
			}

			o2 = o1;
		}

		ensureSameSymmetric(getVisited(index, index, new DistanceJoinQuery(1)), expected);
	}

	@Test
	public void testTwoIndices()
	{
		SpatialIndex<Object> index1 = new SpatialIndex<Object>();
		SpatialIndex<Object> index2 = new SpatialIndex<Object>();

		Object o1, o2, o3;
		index1.insert(o1 = new Integer(1), new AABB2(2, 0, 1, 3)); // In range of o4, o5
		index1.insert(o2 = new Integer(2), new AABB2(0, 5, 1, 1)); // In range of o6
		index1.insert(o3 = new Integer(3), new AABB2(0, 7, 1, 1)); // In range of o6

		Object o4, o5, o6;
		index2.insert(o4 = new Integer(4), new AABB2(0, 0, 1, 1)); // In range of o1
		index2.insert(o5 = new Integer(5), new AABB2(0, 2, 1, 1)); // In range of o1
		index2.insert(o6 = new Integer(6), new AABB2(2, 5, 1, 3)); // In range of o2, o3

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		expected1.add(new Pair<Object>(o1, o4));
		expected1.add(new Pair<Object>(o1, o5));
		expected1.add(new Pair<Object>(o2, o6));
		expected1.add(new Pair<Object>(o3, o6));

		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();
		expected2.add(new Pair<Object>(o4, o1));
		expected2.add(new Pair<Object>(o5, o1));
		expected2.add(new Pair<Object>(o6, o2));
		expected2.add(new Pair<Object>(o6, o3));

		ensureSame(getVisited(index1, index2, new DistanceJoinQuery(1)), expected1);
		ensureSame(getVisited(index2, index1, new DistanceJoinQuery(1)), expected2);
	}

	@Test
	public void testTwoIndicesBulk()
	{
		SpatialIndex<Object> index1 = new SpatialIndex<Object>();
		SpatialIndex<Object> index2 = new SpatialIndex<Object>();

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			index1.insert(o1 = new Object(), new AABB2(5 * i, 0, 1, 1));
			index2.insert(o2 = new Object(), new AABB2(5 * i, 2, 1, 1));

			expected1.add(new Pair<Object>(o1, o2));
			expected2.add(new Pair<Object>(o2, o1));
		}

		ensureSame(getVisited(index1, index2, new DistanceJoinQuery(1)), expected1);
		ensureSame(getVisited(index2, index1, new DistanceJoinQuery(2)), expected2);
	}
}

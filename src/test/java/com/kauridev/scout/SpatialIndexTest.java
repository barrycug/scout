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

package com.kauridev.scout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Eric Fritz
 */
public class SpatialIndexTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void testQueryEarlyExit() {
		SpatialIndex<Object> index = new SpatialIndex<>();

		for (int i = 0; i < ENTITIES; i++) {
			index.insert(new Object(), new AABB2(i, 0, 1, 1));
		}

		assertEquals(10, getVisited(index, new AllQuery(), 10).size());
	}

	@Test
	public void testJoinQueryEarlyExit() {
		SpatialIndex<Object> index1 = new SpatialIndex<>();
		SpatialIndex<Object> index2 = new SpatialIndex<>();

		for (int i = 0; i < ENTITIES; i++) {
			index1.insert(new Object(), new AABB2(i, 0, 1, 1));
			index2.insert(new Object(), new AABB2(i, 0, 1, 1));
		}

		assertEquals(10, getVisited(index1, index2, new AllJoinQuery(), 10).size());
	}

	@Test
	public void testUpdate() {
		SpatialIndex<Object> index = new SpatialIndex<>();

		List<Object> set1 = new ArrayList<>();
		List<Object> set2 = new ArrayList<>();
		List<Object> set3 = new ArrayList<>();

		Object o1, o2, o3;
		for (int i = 0; i < ENTITIES; i++) {
			set1.add(o1 = new Object());
			set2.add(o2 = new Object());
			set3.add(o3 = new Object());

			index.insert(o1, new AABB2(3 * i + 0, 0, 1, 1));
			index.insert(o2, new AABB2(3 * i + 1, 0, 1, 1));
			index.insert(o3, new AABB2(3 * i + 2, 0, 1, 1));
		}

		for (int i = 0; i < ENTITIES; i++) {
			index.update(set1.get(i), new AABB2(3 * i + 0, 0, 1, 1)); // Moves none
			index.update(set2.get(i), new AABB2(3 * i + 2, 0, 1, 1)); // Moves within leaf (likely)
			index.update(set3.get(i), new AABB2(3 * i + 2, 1, 1, 1)); // Moves out of leaf
		}

		for (int i = 0; i < ENTITIES; i++) {
			ensureSame(getVisited(index, new ContainsQuery(new AABB2(3 * i + 0, 0, 1, 1))), Arrays.asList(set1.get(i)));
			ensureSame(getVisited(index, new ContainsQuery(new AABB2(3 * i + 2, 0, 1, 1))), Arrays.asList(set2.get(i)));
			ensureSame(getVisited(index, new ContainsQuery(new AABB2(3 * i + 2, 1, 1, 1))), Arrays.asList(set3.get(i)));
		}
	}

	@Test
	public void testRemove() {
		SpatialIndex<Object> index = new SpatialIndex<>();

		List<Object> set1 = new ArrayList<>();
		List<Object> set2 = new ArrayList<>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			set1.add(o1 = new Object());
			set2.add(o2 = new Object());

			index.insert(o1, new AABB2(i, 0, 1, 1));
			index.insert(o2, new AABB2(i, 0, 1, 1));
		}

		for (int i = 0; i < ENTITIES; i++) {
			index.remove(set2.get(i));
		}

		for (int i = 0; i < ENTITIES; i++) {
			ensureSame(getVisited(index, new ContainsQuery(new AABB2(i, 0, 1, 1))), Arrays.asList(set1.get(i)));
		}
	}
}

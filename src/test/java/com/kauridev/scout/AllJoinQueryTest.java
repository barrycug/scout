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
import java.util.List;
import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class AllJoinQueryTest extends QueryTest
{
	private static final int ENTITIES = 50;

	@Test
	public void testOneIndexBulk() {
		SpatialIndex<Object> index = new SpatialIndex<>();

		List<Object> objects = new ArrayList<>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			index.insert(o1 = new Object(), new AABB2(i, 0, 1, 1));
			index.insert(o2 = new Object(), new AABB2(i, 1, 1, 1));

			objects.add(o1);
			objects.add(o2);
		}

		List<Pair<Object>> expected = new ArrayList<>();

		for (int i = 0; i < objects.size(); i++) {
			for (int j = i + 1; j < objects.size(); j++) {
				expected.add(new Pair<>(objects.get(i), objects.get(j)));
			}
		}

		ensureSameSymmetric(getVisited(index, index, new AllJoinQuery()), expected);
	}

	@Test
	public void testTwoIndicesBulk() {
		SpatialIndex<Object> index1 = new SpatialIndex<>();
		SpatialIndex<Object> index2 = new SpatialIndex<>();

		List<Object> objects1 = new ArrayList<>();
		List<Object> objects2 = new ArrayList<>();

		List<Pair<Object>> expected1 = new ArrayList<>();
		List<Pair<Object>> expected2 = new ArrayList<>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			index1.insert(o1 = new Object(), new AABB2(i, 0, 1, 1));
			index2.insert(o2 = new Object(), new AABB2(i, 1, 1, 1));

			objects1.add(o1);
			objects2.add(o2);
		}

		for (int i = 0; i < ENTITIES; i++) {
			for (int j = 0; j < ENTITIES; j++) {
				expected1.add(new Pair<>(objects1.get(i), objects2.get(j)));
				expected2.add(new Pair<>(objects2.get(j), objects1.get(i)));
			}
		}

		ensureSame(getVisited(index1, index2, new AllJoinQuery()), expected1);
		ensureSame(getVisited(index2, index1, new AllJoinQuery()), expected2);
	}
}

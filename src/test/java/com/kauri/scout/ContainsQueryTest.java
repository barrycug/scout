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
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class ContainsQueryTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void test()
	{
		SpatialIndex<Object> index = new SpatialIndex<>();

		Object o1, o2, o3;
		index.insert(o1 = new Object(), new AABB2(1, 1, 3, 3)); // Within bounds
		index.insert(o2 = new Object(), new AABB2(2, 2, 3, 3)); // Within bounds
		index.insert(o3 = new Object(), new AABB2(0, 0, 5, 5)); // Copies bounds

		index.insert(new Object(), new AABB2(4, 0, 2, 2)); // Intersects bounds
		index.insert(new Object(), new AABB2(6, 0, 1, 1)); // Outside of bounds

		ensureSame(getVisited(index, new ContainsQuery(new AABB2(0, 0, 5, 5))), Arrays.asList(o1, o2, o3));
	}

	@Test
	public void testBulk()
	{
		SpatialIndex<Object> index = new SpatialIndex<>();
		List<Object> expected = new ArrayList<>();

		Object o;
		for (int i = 0; i < ENTITIES; i++) {
			index.insert(o = new Object(), new AABB2(5 * i, 0, 5, 5));

			if (i < ENTITIES / 4) {
				expected.add(o);
			}
		}

		ensureSame(getVisited(index, new ContainsQuery(new AABB2(0, 0, ENTITIES / 4 * 5, 5))), expected);
	}
}

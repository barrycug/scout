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

import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class ContainedQueryTest
{
	@Test
	public void test()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3;
		tree.insert(o1 = new Object(), new AABB2(0, 0, 5, 5)); // Covers bounds
		tree.insert(o2 = new Object(), new AABB2(1, 1, 3, 3)); // Covers bounds
		tree.insert(o3 = new Object(), new AABB2(2, 2, 1, 1)); // Copies bounds

		tree.insert(new Object(), new AABB2(3, 3, 1, 1)); // Intersects bounds
		tree.insert(new Object(), new AABB2(4, 4, 1, 1)); // Outside of bounds

		final List<Object> visited = new ArrayList<Object>();

		tree.query(new ContainedQuery(new AABB2(2, 2, 1, 1)), new QueryResultVisitor<Object>() {
			@Override
			public void visit(Object o)
			{
				visited.add(o);
			}
		});

		assertEquals(3, visited.size());
		assertTrue(visited.contains(o1));
		assertTrue(visited.contains(o2));
		assertTrue(visited.contains(o3));
	}
}

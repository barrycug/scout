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
public class RTreeTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void testUpdate()
	{
		// TODO
	}

	@Test
	public void testRemove()
	{
		RTree<Object> tree = new RTree<Object>();

		List<Object> set1 = new ArrayList<Object>();
		List<Object> set2 = new ArrayList<Object>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			set1.add(o1 = new Object());
			set2.add(o2 = new Object());

			tree.insert(o1, new AABB2(i, 0, 1, 1));
			tree.insert(o2, new AABB2(i, 1, 1, 1));
		}

		for (Object o : set2) {
			tree.remove(o);
		}

		ensureSame(getVisited(tree, new AllQuery()), set1);
	}
}

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
public class AllJoinQueryTest extends QueryTest
{
	private static final int ENTITIES = 50;

	@Test
	public void testOneTreeBulk()
	{
		SpatialIndex<Object> tree = new SpatialIndex<Object>();

		List<Object> objects = new ArrayList<Object>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			tree.insert(o1 = new Object(), new AABB2(i, 0, 1, 1));
			tree.insert(o2 = new Object(), new AABB2(i, 1, 1, 1));

			objects.add(o1);
			objects.add(o2);
		}

		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();

		for (int i = 0; i < objects.size(); i++) {
			for (int j = i + 1; j < objects.size(); j++) {
				expected.add(new Pair<Object>(objects.get(i), objects.get(j)));
			}
		}

		ensureSameSymmetric(getVisited(tree, tree, new AllJoinQuery()), expected);
	}

	@Test
	public void testTwoTreesBulk()
	{
		SpatialIndex<Object> tree1 = new SpatialIndex<Object>();
		SpatialIndex<Object> tree2 = new SpatialIndex<Object>();

		List<Object> objects1 = new ArrayList<Object>();
		List<Object> objects2 = new ArrayList<Object>();

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			tree1.insert(o1 = new Object(), new AABB2(i, 0, 1, 1));
			tree2.insert(o2 = new Object(), new AABB2(i, 1, 1, 1));

			objects1.add(o1);
			objects2.add(o2);
		}

		for (int i = 0; i < ENTITIES; i++) {
			for (int j = 0; j < ENTITIES; j++) {
				expected1.add(new Pair<Object>(objects1.get(i), objects2.get(j)));
				expected2.add(new Pair<Object>(objects2.get(j), objects1.get(i)));
			}
		}

		ensureSame(getVisited(tree1, tree2, new AllJoinQuery()), expected1);
		ensureSame(getVisited(tree2, tree1, new AllJoinQuery()), expected2);
	}
}

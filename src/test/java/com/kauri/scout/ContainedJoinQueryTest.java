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
public class ContainedJoinQueryTest extends QueryTest
{
	private static final int NUM_BOUNDS = 1000;

	@Test
	public void testOneTree()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3, o4;
		tree.insert(o1 = new Object(), new AABB2(0, 0, 7, 7)); // Contained by nothing
		tree.insert(o2 = new Object(), new AABB2(1, 1, 5, 5)); // Contained by o1
		tree.insert(o3 = new Object(), new AABB2(2, 2, 3, 3)); // Contained by o1, o2
		tree.insert(o4 = new Object(), new AABB2(3, 3, 1, 1)); // Contained by o1, o2, o3

		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();
		expected.add(new Pair<Object>(o2, o1));
		expected.add(new Pair<Object>(o3, o1));
		expected.add(new Pair<Object>(o3, o2));
		expected.add(new Pair<Object>(o4, o1));
		expected.add(new Pair<Object>(o4, o2));
		expected.add(new Pair<Object>(o4, o3));

		ensureSame(getVisited(tree, tree, new ContainedJoinQuery()), expected);
	}

	@Test
	public void testOneTreeBulk()
	{
		RTree<Object> tree = new RTree<Object>();
		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();

		Object o1, o2;
		for (int i = 0; i < NUM_BOUNDS; i++) {
			tree.insert(o1 = new Object(), new AABB2(5 * i + 0, 0, 5, 5));
			tree.insert(o2 = new Object(), new AABB2(5 * i + 1, 1, 3, 3));

			expected.add(new Pair<Object>(o2, o1));
		}

		ensureSame(getVisited(tree, tree, new ContainedJoinQuery()), expected);
	}

	@Test
	public void testTwoTrees()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		Object o1, o2, o3;
		tree1.insert(o1 = new Object(), new AABB2(0, 0, 8, 8)); // Contained by nothing
		tree1.insert(o2 = new Object(), new AABB2(3, 3, 2, 2)); // Contained by o4, o5
		tree1.insert(o3 = new Object(), new AABB2(4, 4, 1, 1)); // Contained by o4, o5

		Object o4, o5, o6;
		tree2.insert(o4 = new Object(), new AABB2(1, 1, 6, 6)); // Contained by o1
		tree2.insert(o5 = new Object(), new AABB2(2, 2, 4, 4)); // Contained by o1
		tree2.insert(o6 = new Object(), new AABB2(3, 3, 1, 1)); // Contained by o1, o2

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		expected1.add(new Pair<Object>(o2, o4));
		expected1.add(new Pair<Object>(o2, o5));
		expected1.add(new Pair<Object>(o3, o4));
		expected1.add(new Pair<Object>(o3, o5));

		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();
		expected2.add(new Pair<Object>(o4, o1));
		expected2.add(new Pair<Object>(o5, o1));
		expected2.add(new Pair<Object>(o6, o1));
		expected2.add(new Pair<Object>(o6, o2));

		ensureSame(getVisited(tree1, tree2, new ContainedJoinQuery()), expected1);
		ensureSame(getVisited(tree2, tree1, new ContainedJoinQuery()), expected2);
	}

	@Test
	public void testTwoTreesBulk()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();

		Object o1, o2, o3;
		for (int i = 0; i < NUM_BOUNDS; i++) {
			tree1.insert(o1 = new Object(), new AABB2(5 * i + 0, 0, 5, 5));
			tree2.insert(o2 = new Object(), new AABB2(5 * i + 1, 1, 3, 3));
			tree1.insert(o3 = new Object(), new AABB2(5 * i + 2, 2, 1, 1));

			expected1.add(new Pair<Object>(o3, o2));
			expected2.add(new Pair<Object>(o2, o1));
		}

		ensureSame(getVisited(tree1, tree2, new ContainedJoinQuery()), expected1);
		ensureSame(getVisited(tree2, tree1, new ContainedJoinQuery()), expected2);
	}
}

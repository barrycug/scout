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
public class IntersectionJoinQueryTest extends QueryTest
{
	private static final int ENTITIES = 1000;

	@Test
	public void testOneTree()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3, o4;
		tree.insert(o1 = new Object(), new AABB2(0, 1, 2, 2)); // Intersects o2
		tree.insert(o2 = new Object(), new AABB2(1, 1, 2, 3)); // Intersects o1, o3
		tree.insert(o3 = new Object(), new AABB2(3, 1, 2, 2)); // Intersects o2, o4
		tree.insert(o4 = new Object(), new AABB2(5, 3, 1, 1)); // Intersects o3

		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();
		expected.add(new Pair<Object>(o1, o2));
		expected.add(new Pair<Object>(o2, o3));
		expected.add(new Pair<Object>(o3, o4));

		ensureSameSymmetric(getVisited(tree, tree, new IntersectionJoinQuery()), expected);
	}

	@Test
	public void testOneTreeBulk()
	{
		RTree<Object> tree = new RTree<Object>();
		List<Pair<Object>> expected = new ArrayList<Pair<Object>>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			tree.insert(o1 = new Object(), new AABB2(2 * i, 0, 1, 2));
			tree.insert(o2 = new Object(), new AABB2(2 * i, 1, 1, 2));

			expected.add(new Pair<Object>(o1, o2));
		}

		ensureSameSymmetric(getVisited(tree, tree, new IntersectionJoinQuery()), expected);
	}

	@Test
	public void testTwoTrees()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		Object o1, o2, o3;
		tree1.insert(o1 = new Integer(1), new AABB2(3, 0, 2, 2)); // Intersects o4
		tree1.insert(o2 = new Integer(2), new AABB2(0, 1, 4, 4)); // Intersects o4, o5, o6
		tree1.insert(o3 = new Integer(3), new AABB2(3, 4, 1, 1)); // Intersects o6

		Object o4, o5, o6;
		tree2.insert(o4 = new Integer(4), new AABB2(3, 0, 2, 2)); // Intersects o1, o2
		tree2.insert(o5 = new Integer(5), new AABB2(0, 1, 2, 2)); // Intersects o2
		tree2.insert(o6 = new Integer(6), new AABB2(2, 5, 2, 2)); // Intersects o2, o3

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		expected1.add(new Pair<Object>(o1, o4));
		expected1.add(new Pair<Object>(o2, o4));
		expected1.add(new Pair<Object>(o2, o5));
		expected1.add(new Pair<Object>(o2, o6));
		expected1.add(new Pair<Object>(o3, o6));

		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();
		expected2.add(new Pair<Object>(o4, o1));
		expected2.add(new Pair<Object>(o4, o2));
		expected2.add(new Pair<Object>(o5, o2));
		expected2.add(new Pair<Object>(o6, o2));
		expected2.add(new Pair<Object>(o6, o3));

		ensureSame(getVisited(tree1, tree2, new IntersectionJoinQuery()), expected1);
		ensureSame(getVisited(tree2, tree1, new IntersectionJoinQuery()), expected2);
	}

	@Test
	public void testTwoTreesBulk()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		List<Pair<Object>> expected1 = new ArrayList<Pair<Object>>();
		List<Pair<Object>> expected2 = new ArrayList<Pair<Object>>();

		Object o1, o2;
		for (int i = 0; i < ENTITIES; i++) {
			tree1.insert(o1 = new Object(), new AABB2(2 * i, 0, 1, 2));
			tree2.insert(o2 = new Object(), new AABB2(2 * i, 1, 1, 2));

			expected1.add(new Pair<Object>(o1, o2));
			expected2.add(new Pair<Object>(o2, o1));
		}

		ensureSame(getVisited(tree1, tree2, new IntersectionJoinQuery()), expected1);
		ensureSame(getVisited(tree2, tree1, new IntersectionJoinQuery()), expected2);
	}
}

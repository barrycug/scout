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
public class IntersectionJoinQueryTest extends QueryTest
{
	private static final int NUM_BOUNDS = 1000;

	@Test
	public void testOneTree()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3, o4;
		tree.insert(o1 = new Object(), new AABB2(0, 1, 2, 2)); // Intersects o2
		tree.insert(o2 = new Object(), new AABB2(1, 1, 2, 3)); // Intersects o1, o3
		tree.insert(o3 = new Object(), new AABB2(3, 1, 2, 2)); // Intersects o2, o4
		tree.insert(o4 = new Object(), new AABB2(5, 3, 1, 1)); // Intersects o3

		List<Pair> visited = this.getVisited(tree, tree, new IntersectionJoinQuery());

		assertEquals(3, visited.size());
		assertTrue(visited.contains(new Pair(o1, o2)) || visited.contains(new Pair(o2, o1)));
		assertTrue(visited.contains(new Pair(o2, o3)) || visited.contains(new Pair(o3, o2)));
		assertTrue(visited.contains(new Pair(o3, o4)) || visited.contains(new Pair(o4, o3)));
	}

	@Test
	public void testOneTreeBulk()
	{
		RTree<Object> tree = new RTree<Object>();

		final List<Object> row1 = new ArrayList<Object>();
		final List<Object> row2 = new ArrayList<Object>();

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = new Object();
			Object o2 = new Object();

			tree.insert(o1, new AABB2(2 * i, 0, 1, 2));
			tree.insert(o2, new AABB2(2 * i, 1, 1, 2));

			row1.add(o1);
			row2.add(o2);
		}

		List<Pair> visited = this.getVisited(tree, tree, new IntersectionJoinQuery());

		assertEquals(row1.size(), visited.size());

		for (int i = 0; i < 200; i++) {
			Object o1 = row1.get(i);
			Object o2 = row2.get(i);

			assertTrue(visited.contains(new Pair(o1, o2)) || visited.contains(new Pair(o2, o1)));
		}
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

		List<Pair> visited1 = this.getVisited(tree1, tree2, new IntersectionJoinQuery());
		List<Pair> visited2 = this.getVisited(tree2, tree1, new IntersectionJoinQuery());

		assertEquals(5, visited1.size());
		assertTrue(visited1.contains(new Pair(o1, o4)));
		assertTrue(visited1.contains(new Pair(o2, o4)));
		assertTrue(visited1.contains(new Pair(o2, o5)));
		assertTrue(visited1.contains(new Pair(o2, o6)));
		assertTrue(visited1.contains(new Pair(o3, o6)));

		assertEquals(5, visited2.size());
		assertTrue(visited2.contains(new Pair(o4, o1)));
		assertTrue(visited2.contains(new Pair(o4, o2)));
		assertTrue(visited2.contains(new Pair(o5, o2)));
		assertTrue(visited2.contains(new Pair(o6, o2)));
		assertTrue(visited2.contains(new Pair(o6, o3)));
	}

	@Test
	public void testTwoTreesBulk()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		final List<Object> row1 = new ArrayList<Object>();
		final List<Object> row2 = new ArrayList<Object>();

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = new Object();
			Object o2 = new Object();

			tree1.insert(o1, new AABB2(2 * i, 0, 1, 2));
			tree2.insert(o2, new AABB2(2 * i, 1, 1, 2));

			row1.add(o1);
			row2.add(o2);
		}

		List<Pair> visited1 = this.getVisited(tree1, tree2, new IntersectionJoinQuery());
		List<Pair> visited2 = this.getVisited(tree2, tree1, new IntersectionJoinQuery());

		assertEquals(row1.size(), visited1.size());
		assertEquals(row2.size(), visited2.size());

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = row1.get(i);
			Object o2 = row2.get(i);

			assertTrue(visited1.contains(new Pair(o1, o2)));
			assertTrue(visited2.contains(new Pair(o2, o1)));
		}
	}
}

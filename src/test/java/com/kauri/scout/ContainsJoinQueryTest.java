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
public class ContainsJoinQueryTest extends QueryTest
{
	private static final int NUM_BOUNDS = 1000;

	@Test
	public void testOneTree()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3, o4;
		tree.insert(o1 = new Object(), new AABB2(0, 0, 7, 7)); // Contains o2, o3, o4
		tree.insert(o2 = new Object(), new AABB2(1, 1, 5, 5)); // Contains o3, o4
		tree.insert(o3 = new Object(), new AABB2(2, 2, 3, 3)); // Contains o4
		tree.insert(o4 = new Object(), new AABB2(3, 3, 1, 1)); // Contains nothing

		List<Pair<Object>> visited = this.getVisited(tree, tree, new ContainsJoinQuery());

		assertEquals(6, visited.size());
		assertTrue(visited.contains(new Pair<Object>(o1, o2)));
		assertTrue(visited.contains(new Pair<Object>(o1, o3)));
		assertTrue(visited.contains(new Pair<Object>(o1, o4)));
		assertTrue(visited.contains(new Pair<Object>(o2, o3)));
		assertTrue(visited.contains(new Pair<Object>(o2, o4)));
		assertTrue(visited.contains(new Pair<Object>(o3, o4)));
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

			tree.insert(o1, new AABB2(5 * i + 0, 0, 5, 5));
			tree.insert(o2, new AABB2(5 * i + 1, 1, 3, 3));

			row1.add(o1);
			row2.add(o2);
		}

		List<Pair<Object>> visited = this.getVisited(tree, tree, new ContainsJoinQuery());

		assertEquals(row1.size(), visited.size());

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = row1.get(i);
			Object o2 = row2.get(i);

			assertTrue(visited.contains(new Pair<Object>(o1, o2)));
		}
	}

	@Test
	public void testTwoTrees()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		Object o1, o2, o3;
		tree1.insert(o1 = new Object(), new AABB2(0, 0, 8, 8)); // Contains o4, o5, o6
		tree1.insert(o2 = new Object(), new AABB2(3, 3, 2, 2)); // Contains o6
		tree1.insert(o3 = new Object(), new AABB2(4, 4, 1, 1)); // Contains nothing

		Object o4, o5, o6;
		tree2.insert(o4 = new Object(), new AABB2(1, 1, 6, 6)); // Contains o2, o3
		tree2.insert(o5 = new Object(), new AABB2(2, 2, 4, 4)); // Contains o2, o3
		tree2.insert(o6 = new Object(), new AABB2(3, 3, 1, 1)); // Contains nothing

		List<Pair<Object>> visited1 = this.getVisited(tree1, tree2, new ContainsJoinQuery());
		List<Pair<Object>> visited2 = this.getVisited(tree2, tree1, new ContainsJoinQuery());

		assertEquals(4, visited1.size());
		assertTrue(visited1.contains(new Pair<Object>(o1, o4)));
		assertTrue(visited1.contains(new Pair<Object>(o1, o5)));
		assertTrue(visited1.contains(new Pair<Object>(o1, o6)));
		assertTrue(visited1.contains(new Pair<Object>(o2, o6)));

		assertEquals(4, visited2.size());
		assertTrue(visited2.contains(new Pair<Object>(o4, o2)));
		assertTrue(visited2.contains(new Pair<Object>(o4, o3)));
		assertTrue(visited2.contains(new Pair<Object>(o5, o2)));
		assertTrue(visited2.contains(new Pair<Object>(o5, o3)));
	}

	@Test
	public void testTwoTreesBulk()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		final List<Object> row1 = new ArrayList<Object>();
		final List<Object> row2 = new ArrayList<Object>();
		final List<Object> row3 = new ArrayList<Object>();

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = new Object();
			Object o2 = new Object();
			Object o3 = new Object();

			tree1.insert(o1, new AABB2(5 * i + 0, 0, 5, 5));
			tree2.insert(o2, new AABB2(5 * i + 1, 1, 3, 3));
			tree1.insert(o3, new AABB2(5 * i + 2, 2, 1, 1));

			row1.add(o1);
			row2.add(o2);
			row3.add(o3);
		}

		List<Pair<Object>> visited1 = this.getVisited(tree1, tree2, new ContainsJoinQuery());
		List<Pair<Object>> visited2 = this.getVisited(tree2, tree1, new ContainsJoinQuery());

		assertEquals(row1.size(), visited1.size());
		assertEquals(row2.size(), visited2.size());

		for (int i = 0; i < NUM_BOUNDS; i++) {
			Object o1 = row1.get(i);
			Object o2 = row2.get(i);
			Object o3 = row3.get(i);

			assertTrue(visited1.contains(new Pair<Object>(o1, o2)));
			assertTrue(visited2.contains(new Pair<Object>(o2, o3)));
		}
	}
}

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
public class DistanceJoinQueryTest
{
	@Test
	public void testOneTree()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3, o4;
		tree.insert(o1 = new Object(), new AABB2(0, 0, 1, 1)); // In range of o2
		tree.insert(o2 = new Object(), new AABB2(4, 5, 5, 6)); // In range of o1, o3, o4
		tree.insert(o3 = new Object(), new AABB2(8, 0, 9, 1)); // In range of o2, o4
		tree.insert(o4 = new Object(), new AABB2(8, 4, 9, 5)); // In range of o2, o3

		final List<Pair> visited = new ArrayList<Pair>();

		tree.queryJoin(new DistanceJoinQuery(5), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited.add(new Pair(o1, o2));
			}
		});

		assertEquals(4, visited.size());
		assertTrue(visited.contains(new Pair(o1, o2)) || visited.contains(new Pair(o2, o1)));
		assertTrue(visited.contains(new Pair(o2, o3)) || visited.contains(new Pair(o3, o2)));
		assertTrue(visited.contains(new Pair(o2, o4)) || visited.contains(new Pair(o4, o2)));
		assertTrue(visited.contains(new Pair(o3, o4)) || visited.contains(new Pair(o4, o3)));
	}

	@Test
	public void testOneTreeBulk()
	{
		RTree<Object> tree = new RTree<Object>();

		final List<Object> row1 = new ArrayList<Object>();

		for (int i = 0; i < 200; i++) {
			Object o1 = new Object();

			tree.insert(o1, new AABB2(5 * i, 0, 5, 5));

			row1.add(o1);
		}

		final List<Pair> visited = new ArrayList<Pair>();

		tree.queryJoin(new DistanceJoinQuery(1), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited.add(new Pair(o1, o2));
			}
		});

		assertEquals(row1.size() - 1, visited.size());

		for (int i = 1; i < 200; i++) {
			Object o1 = row1.get(i - 1);
			Object o2 = row1.get(i);

			assertTrue(visited.contains(new Pair(o1, o2)) || visited.contains(new Pair(o2, o1)));
		}
	}

	@Test
	public void testTwoTrees()
	{
		RTree<Object> tree1 = new RTree<Object>();
		RTree<Object> tree2 = new RTree<Object>();

		Object o1, o2, o3;
		tree1.insert(o1 = new Integer(1), new AABB2(2, 0, 1, 3)); // In range of o4, o5
		tree1.insert(o2 = new Integer(2), new AABB2(0, 5, 1, 1)); // In range of o6
		tree1.insert(o3 = new Integer(3), new AABB2(0, 7, 1, 1)); // In range of o6

		Object o4, o5, o6;
		tree2.insert(o4 = new Integer(4), new AABB2(0, 0, 1, 1)); // In range of o1
		tree2.insert(o5 = new Integer(5), new AABB2(0, 2, 1, 1)); // In range of o1
		tree2.insert(o6 = new Integer(6), new AABB2(2, 5, 1, 3)); // In range of o2, o3

		final List<Pair> visited1 = new ArrayList<Pair>();
		final List<Pair> visited2 = new ArrayList<Pair>();

		tree1.queryJoin(tree2, new DistanceJoinQuery(1), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited1.add(new Pair(o1, o2));
			}
		});

		tree2.queryJoin(tree1, new DistanceJoinQuery(1), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited2.add(new Pair(o1, o2));
			}
		});

		assertEquals(4, visited1.size());
		assertTrue(visited1.contains(new Pair(o1, o4)));
		assertTrue(visited1.contains(new Pair(o1, o5)));
		assertTrue(visited1.contains(new Pair(o2, o6)));
		assertTrue(visited1.contains(new Pair(o3, o6)));

		assertEquals(4, visited2.size());
		assertTrue(visited2.contains(new Pair(o4, o1)));
		assertTrue(visited2.contains(new Pair(o5, o1)));
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

		for (int i = 0; i < 200; i++) {
			Object o1 = new Object();
			Object o2 = new Object();

			tree1.insert(o1, new AABB2(5 * i, 0, 1, 1));
			tree2.insert(o2, new AABB2(5 * i, 2, 1, 1));

			row1.add(o1);
			row2.add(o2);
		}

		final List<Pair> visited1 = new ArrayList<Pair>();
		final List<Pair> visited2 = new ArrayList<Pair>();

		tree1.queryJoin(tree2, new DistanceJoinQuery(1), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited1.add(new Pair(o1, o2));
			}
		});

		tree2.queryJoin(tree1, new DistanceJoinQuery(1), new QueryJoinResultVisitor<Object, Object>() {
			@Override
			public void visit(Object o1, Object o2)
			{
				visited2.add(new Pair(o1, o2));
			}
		});

		assertEquals(row1.size(), visited1.size());
		assertEquals(row2.size(), visited2.size());

		for (int i = 0; i < 200; i++) {
			Object o1 = row1.get(i);
			Object o2 = row2.get(i);

			assertTrue(visited1.contains(new Pair(o1, o2)));
			assertTrue(visited2.contains(new Pair(o2, o1)));
		}
	}
}

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

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Eric Fritz
 */
public class RTreeTest extends QueryTest
{
	@Test
	public void testUpdate()
	{
		// TODO
	}

	@Test
	public void testRemove()
	{
		RTree<Object> tree = new RTree<Object>();

		Object o1, o2, o3;
		tree.insert(o1 = new Object(), new AABB2(0, 0, 1, 1));
		tree.insert(o2 = new Object(), new AABB2(1, 0, 1, 1));
		tree.insert(o3 = new Object(), new AABB2(2, 0, 1, 1));

		ensureSame(getVisited(tree, new AllQuery()), Arrays.asList(o1, o2, o3));

		tree.remove(o3);

		ensureSame(getVisited(tree, new AllQuery()), Arrays.asList(o1, o2));
	}
}

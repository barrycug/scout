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

/**
 * @author Eric Fritz
 */
abstract public class QueryTest
{
	public <T> List<T> getVisited(RTree<T> tree, Query query)
	{
		final List<T> visited = new ArrayList<T>();

		tree.query(query, new QueryResultVisitor<T>() {
			@Override
			public void visit(T o)
			{
				visited.add(o);
			}
		});

		return visited;
	}

	public <T> List<Pair> getVisited(RTree<T> tree1, RTree<T> tree2, JoinQuery query)
	{
		final List<Pair> visited = new ArrayList<Pair>();

		tree1.queryJoin(tree2, query, new QueryJoinResultVisitor<T, T>() {
			@Override
			public void visit(T o1, T o2)
			{
				visited.add(new Pair(o1, o2));
			}
		});

		return visited;
	}
}

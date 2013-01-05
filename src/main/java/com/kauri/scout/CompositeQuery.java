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

/**
 * @author Eric Fritz
 */
public class CompositeQuery implements Query
{
	private Query query1;
	private Query query2;

	public CompositeQuery(Query query1, Query query2)
	{
		this.query1 = query1;
		this.query2 = query2;
	}

	@Override
	public QueryResult queryInternalNode(AABB aabb)
	{
		QueryResult result1 = query1.queryInternalNode(aabb);
		QueryResult result2 = query1.queryInternalNode(aabb);

		if (result1 == QueryResult.ALL && result2 == QueryResult.ALL) {
			return QueryResult.ALL;
		}

		if (result1 == QueryResult.NONE || result2 == QueryResult.NONE) {
			return QueryResult.NONE;
		}

		return QueryResult.SOME;
	}

	@Override
	public boolean queryElement(AABB aabb)
	{
		return query1.queryElement(aabb) && query2.queryElement(aabb);
	}
}

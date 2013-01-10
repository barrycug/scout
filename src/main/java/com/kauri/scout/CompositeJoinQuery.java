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
public class CompositeJoinQuery implements JoinQuery
{
	private JoinQuery query1;
	private JoinQuery query2;

	public CompositeJoinQuery(JoinQuery query1, JoinQuery query2)
	{
		this.query1 = query1;
		this.query2 = query2;
	}

	@Override
	public QueryResult query(AABB aabb1, AABB aabb2, boolean queryPartial)
	{
		QueryResult result1 = query1.query(aabb1, aabb2, queryPartial);
		QueryResult result2 = query2.query(aabb1, aabb2, queryPartial);

		if (result1 == QueryResult.ALL && result2 == QueryResult.ALL) {
			return QueryResult.ALL;
		}

		if (queryPartial && result1 != QueryResult.NONE && result2 != QueryResult.NONE) {
			return QueryResult.SOME;
		}

		return QueryResult.NONE;
	}

	@Override
	public boolean isSymmetricRelation()
	{
		return query1.isSymmetricRelation() && query2.isSymmetricRelation();
	}
}

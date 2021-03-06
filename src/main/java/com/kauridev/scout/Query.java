/*
 * This file is part of the scout package.
 *
 * Copyright (c) 2014 Eric Fritz
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

package com.kauridev.scout;

/**
 * Defines the query criteria used by spatial queries.
 *
 * @author Eric Fritz
 * @see SpatialIndex#query(Query, QueryResultHandler)
 */
public interface Query
{
	public enum QueryResult
	{
		PASS,
		PARTIAL,
		FAIL
	}

	/**
	 * Determines if the specified <tt>volume</tt> matches the query criteria.
	 * <p>
	 * If <tt>partial</tt> queries are allowed, then <tt>volume</tt> represents the minimum bounding
	 * volume of a group of objects. {@link QueryResult#PARTIAL} should be returned in the case that
	 * some of the sub-volumes may match the query criteria during a subsequent query.
	 * <p>
	 * Otherwise, it should return {@link QueryResult#PASS} if the query criteria was matched
	 * completely, or {@link QueryResult#FAIL} if the query criteria was not matched completely.
	 *
	 * @param volume  The volume.
	 * @param partial Whether to allow partial matches.
	 *
	 * @return A {@link QueryResult} specifying whether the query criteria was matched.
	 */
	public QueryResult query(AABB volume, boolean partial);
}

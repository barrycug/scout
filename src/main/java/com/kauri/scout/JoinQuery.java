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

package com.kauri.scout;

/**
 * Defines the query criteria used by spatial joins.
 *
 * @author Eric Fritz
 * @see SpatialIndex#query(JoinQuery, JoinQueryResultHandler)
 * @see SpatialIndex#query(SpatialIndex, JoinQuery, JoinQueryResultHandler)
 */
public interface JoinQuery
{
	/**
	 * Determines if the specified volumes match the query criteria.
	 * <p>
	 * If <tt>partial</tt> queries are allowed, then <tt>volume1</tt> and <tt>volume2</tt> represent
	 * the minimum bounding volume of a group of objects. In this case, the query criteria should be
	 * relaxed, and the return value should reflect the possibility that subsequent queries on pairs
	 * of sub-volumes (one from <tt>volume1</tt> and one from <tt>volume2</tt>) may match.
	 *
	 * @param volume1 The first volume.
	 * @param volume2 The second volume.
	 * @param partial Whether to allow partial matches.
	 *
	 * @return <tt>true</tt> if the volumes match the query criteria, <tt>false</tt> otherwise.
	 */
	public boolean query(AABB volume1, AABB volume2, boolean partial);

	/**
	 * Returns <tt>true</tt> if this query criteria is symmetric, <tt>false</tt> otherwise.
	 * <p>
	 * A query criteria is symmetric if the query does not depend on the order of the volume
	 * parameters. That is, the query result should be the same after swapping the values of
	 * <tt>volume1</tt> and <tt>volume2</tt> (all else being identical).
	 *
	 * @return <tt>true</tt> if this query criteria is symmetric, <tt>false</tt> otherwise.
	 */
	public boolean isSymmetric();
}

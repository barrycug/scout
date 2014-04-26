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
 * A query that matches volumes at most <tt>distance</tt> away from the specified <tt>volume</tt>.
 *
 * @author Eric Fritz
 */
public class DistanceQuery implements Query
{
	/**
	 * The reference volume.
	 */
	private AABB volume;

	/**
	 * The maximum allowed distance.
	 */
	private float distanceSquared;

	/**
	 * Creates a new DistanceQuery.
	 *
	 * @param volume
	 *            The reference volume.
	 * @param distance
	 *            The maximum distance matched volumes must be from <tt>volume</tt>.
	 */
	public DistanceQuery(AABB volume, float distance)
	{
		this.volume = volume;
		this.distanceSquared = distance * distance;
	}

	@Override
	public QueryResult query(AABB volume, boolean partial)
	{
		if (AABBUtil.distanceSquared(this.volume, volume) <= distanceSquared) {
			return partial ? QueryResult.PARTIAL : QueryResult.PASS;
		}

		return QueryResult.FAIL;
	}
}

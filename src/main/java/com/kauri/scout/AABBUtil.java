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
 * Common methods relating to arbitrary bounding volumes.
 * 
 * @author Eric Fritz
 */
public class AABBUtil
{
	/**
	 * Returns the center point of the bounding box with respect to the specified dimension.
	 * 
	 * @param aabb
	 *            The box.
	 * @param dimension
	 *            The dimension.
	 * @return The center point of the bounding box with respect to the specified dimension.
	 */
	public static float getCenter(AABB aabb, int dimension)
	{
		if (dimension > aabb.getDimensions()) {
			throw new IllegalArgumentException();
		}

		return aabb.getMinimum(dimension) + aabb.getExtent(dimension) / 2;
	}

	/**
	 * Returns the minimum distance between two bounding boxes.
	 * 
	 * @param aabb1
	 *            The first box.
	 * @param aabb2
	 *            The second box.
	 * @return The minimum distance between two bounding boxes.
	 */
	public static float distanceSquared(AABB aabb1, AABB aabb2)
	{
		if (aabb1.getDimensions() != aabb2.getDimensions()) {
			throw new IllegalArgumentException();
		}

		float dist = 0;
		for (int i = 0; i < aabb1.getDimensions(); i++) {
			float min = aabb1.getMinimum(i) - aabb2.getExtent(i);
			float max = aabb1.getMaximum(i);
			float pnt = aabb2.getMinimum(i);

			if (pnt < min) {
				dist += (min - pnt) * (min - pnt);
			} else if (pnt > max) {
				dist += (pnt - max) * (pnt - max);
			}
		}

		return dist;
	}

	/**
	 * Minimally expands <tt>volume1</tt> so that it complete contains <tt>volume2</tt>.
	 * 
	 * @param volume1
	 *            The volume to expand.
	 * @param volume2
	 *            The reference volume.
	 */
	public static void expand(AABB volume1, AABB volume2)
	{
		if (volume1.getDimensions() != volume2.getDimensions()) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < volume1.getDimensions(); i++) {
			float min1 = volume1.getMinimum(i);
			float max1 = volume1.getMaximum(i);
			float min2 = volume2.getMinimum(i);
			float max2 = volume2.getMaximum(i);

			volume1.setBounds(i, Math.min(min1, min2), Math.max(max1, max2));
		}
	}
}

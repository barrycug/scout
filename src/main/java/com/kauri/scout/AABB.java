/*
 * This file is part of the scout package.
 *
 * Copyright (C) 2012, Eric Fritz
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
 * An axis-aligned bounding box.
 * 
 * @author Eric Fritz
 */
public class AABB
{
	private float[] min;
	private float[] max;
	private int dimensions;

	/**
	 * Creates a new AABB.
	 * 
	 * @param min
	 *            The minimum points in each dimension.
	 * @param max
	 *            The maximum points in each dimension.
	 */
	public AABB(float[] min, float[] max)
	{
		if (min.length != max.length) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < min.length; i++) {
			if (min[i] > max[i]) {
				throw new IllegalArgumentException();
			}
		}

		this.min = min;
		this.max = max;
		this.dimensions = min.length;
	}

	/**
	 * Returns the number of edges composing this box.
	 * 
	 * @return The number of edges composing this box.
	 */
	public int getDimensions()
	{
		return dimensions;
	}

	/**
	 * Returns the minimum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The maximum value in the given dimension.
	 */
	public float getMinimum(int dimension)
	{
		if (dimension < 0 || dimension >= min.length) {
			throw new IllegalArgumentException();
		}

		return min[dimension];
	}

	/**
	 * Returns the maximum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The maximum value in the given dimension.
	 */
	public float getMaximum(int dimension)
	{
		if (dimension < 0 || dimension >= min.length) {
			throw new IllegalArgumentException();
		}

		return max[dimension];
	}

	/**
	 * Returns the sum of the edges composing the box.
	 * 
	 * @return The sum of the edges composing the box.
	 */
	public float getMargin()
	{
		float margin = 0;
		for (int i = 0; i < dimensions; i++) {
			margin += max[i] - min[i];
		}

		return margin;
	}

	/**
	 * Returns the product of the edges composing the box.
	 * 
	 * @return The product of the edges composing the box.
	 */
	public float getVolume()
	{
		float volume = 1;
		for (int i = 0; i < dimensions; i++) {
			volume *= max[i] - min[i];
		}

		return volume;
	}

	/**
	 * Determines whether this box contains the specified box.
	 * 
	 * @param aabb
	 *            The other box.
	 * @return <tt>true</tt> if this box contains the specified box.
	 */
	public boolean contains(AABB aabb)
	{
		if (aabb.dimensions != dimensions) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < dimensions; i++) {
			if (min[i] > aabb.min[i] || max[i] < aabb.max[i]) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Determines whether this box intersects with the specified box.
	 * 
	 * @param aabb
	 *            The other box.
	 * @return <tt>true</tt> if this box intersects with the specified box.
	 */
	public boolean intersects(AABB aabb)
	{
		if (aabb.dimensions != dimensions) {
			throw new IllegalArgumentException();
		}

		for (int i = 0; i < dimensions; i++) {
			if (min[i] > aabb.max[i] || max[i] < aabb.min[i]) {
				return false;
			}
		}

		return true;
	}
}
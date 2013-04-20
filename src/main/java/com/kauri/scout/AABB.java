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
public interface AABB
{
	/**
	 * Creates and returns a copy of this object.
	 * 
	 * @return A copy of this instance.
	 */
	public AABB copy();

	/**
	 * Returns the number of edges composing this box.
	 * 
	 * @return The number of edges composing this box.
	 */
	public int getDimensions();

	/**
	 * Returns the minimum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The maximum value in the given dimension.
	 */
	public float getMinimum(int dimension);

	/**
	 * Updates the minimum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @param minimum
	 *            The minimum value in the given dimension.
	 */
	public void setMinimum(int dimension, float minimum);

	/**
	 * Returns the maximum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The maximum value in the given dimension.
	 */
	public float getMaximum(int dimension);

	/**
	 * Updates the maximum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @param minimum
	 *            The maximum value in the given dimension.
	 */
	public void setMaximum(int dimension, float maximum);

	/**
	 * Returns the product of the edges composing the box.
	 * 
	 * @return The product of the edges composing the box.
	 */
	public float getVolume();

	/**
	 * Determines whether this box contains the specified box.
	 * 
	 * @param aabb
	 *            The other box.
	 * @return <tt>true</tt> if this box contains the specified box.
	 */
	public boolean contains(AABB aabb);

	/**
	 * Determines whether this box intersects with the specified box.
	 * 
	 * @param aabb
	 *            The other box.
	 * @return <tt>true</tt> if this box intersects with the specified box.
	 */
	public boolean intersects(AABB aabb);
}

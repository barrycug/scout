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
 * An axis-aligned bounding volume.
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
	 * Returns the number of dimensions this volume occupies.
	 * 
	 * @return The number of dimensions this volume occupies.
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
	 * Returns the maximum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The maximum value in the given dimension.
	 */
	public float getMaximum(int dimension);

	/**
	 * Updates the minimum and maximum value in the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @param minimum
	 *            The minimum value in the given dimension.
	 * @param maximum
	 *            The maximum value in the given dimension.
	 */
	public void setBounds(int dimension, float minimum, float maximum);

	/**
	 * Returns the extent of the given dimension.
	 * 
	 * @param dimension
	 *            The dimension.
	 * @return The extent of the given dimension.
	 */
	public float getExtent(int dimension);

	/**
	 * Returns the amount of space occupied by this volume.
	 * 
	 * @return The amount of space occupied by this volume.
	 */
	public float getVolume();

	/**
	 * Determines whether this volume contains the specified volume.
	 * 
	 * @param aabb
	 *            The other volume.
	 * @return <tt>true</tt> if this volume contains the specified volume.
	 */
	public boolean contains(AABB aabb);

	/**
	 * Determines whether this volume intersects with the specified volume.
	 * 
	 * @param aabb
	 *            The other volume.
	 * @return <tt>true</tt> if this volume intersects with the specified volume.
	 */
	public boolean intersects(AABB aabb);
}

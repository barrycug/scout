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
public class AABB2 implements AABB
{
	private float x1;
	private float y1;
	private float x2;
	private float y2;

	/**
	 * Creates a new AABB2.
	 * 
	 * @param x
	 *            The x-coordinate of the upper left corner.
	 * @param y
	 *            The y-coordinate of the upper left corner.
	 * @param w
	 *            The box's width.
	 * @param h
	 *            The box's height.
	 */
	public AABB2(float x, float y, float w, float h)
	{
		if (w < 0) {
			throw new IllegalArgumentException();
		}

		if (h < 0) {
			throw new IllegalArgumentException();
		}

		this.x1 = x;
		this.y1 = y;
		this.x2 = x + w;
		this.y2 = y + h;
	}

	@Override
	public boolean equals(Object object)
	{
		if (object == this) {
			return true;
		}

		if (object == null || !(object instanceof AABB2)) {
			return false;
		}

		AABB2 aabb = (AABB2) object;

		return aabb.x1 == x1 && aabb.y1 == y1 && aabb.x2 == x2 && aabb.y2 == y2;
	}

	@Override
	public int hashCode()
	{
		int hash = 1;
		hash = 31 * hash + Float.floatToIntBits(x1);
		hash = 31 * hash + Float.floatToIntBits(y1);
		hash = 31 * hash + Float.floatToIntBits(x2);
		hash = 31 * hash + Float.floatToIntBits(y2);

		return hash;
	}

	@Override
	public AABB2 copy()
	{
		return new AABB2(x1, y1, x2 - x1, y2 - y1);
	}

	@Override
	public int getDimensions()
	{
		return 2;
	}

	@Override
	public float getMinimum(int dimension)
	{
		if (dimension == 0) {
			return x1;
		}

		if (dimension == 1) {
			return y1;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public float getMaximum(int dimension)
	{
		if (dimension == 0) {
			return x2;
		}

		if (dimension == 1) {
			return y2;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public void setBounds(int dimension, float minimum, float maximum)
	{
		if (maximum < minimum) {
			throw new IllegalArgumentException();
		}

		if (dimension == 0) {
			x1 = minimum;
			x2 = maximum;
		} else if (dimension == 1) {
			y1 = minimum;
			y2 = maximum;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public float getExtent(int dimension)
	{
		if (dimension == 0) {
			return x2 - x1;
		}

		if (dimension == 1) {
			return y2 - y1;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public float getVolume()
	{
		return (x2 - x1) * (y2 - y1);
	}

	@Override
	public boolean contains(AABB aabb)
	{
		if (aabb.getDimensions() != 2) {
			throw new IllegalArgumentException();
		}

		if (x1 > aabb.getMinimum(0) || x2 < aabb.getMaximum(0)) {
			return false;
		}

		if (y1 > aabb.getMinimum(1) || y2 < aabb.getMaximum(1)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean intersects(AABB aabb)
	{
		if (aabb.getDimensions() != 2) {
			throw new IllegalArgumentException();
		}

		if (x1 > aabb.getMaximum(0) || x2 < aabb.getMinimum(0)) {
			return false;
		}

		if (y1 > aabb.getMaximum(1) || y2 < aabb.getMinimum(1)) {
			return false;
		}

		return true;
	}
}

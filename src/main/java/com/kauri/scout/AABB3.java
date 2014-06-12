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
 * A three-dimensional axis-aligned bounding volume.
 *
 * @author Eric Fritz
 */
public class AABB3 implements AABB
{
	private float x1;
	private float y1;
	private float z1;
	private float x2;
	private float y2;
	private float z2;

	/**
	 * Creates a new AABB2.
	 *
	 * @param x The x-coordinate of the upper left corner.
	 * @param y The y-coordinate of the upper left corner.
	 * @param z The z-coordinate of the upper left corner.
	 * @param w The box's width.
	 * @param h The box's height.
	 * @param d The box's depth.
	 */
	public AABB3(float x, float y, float z, float w, float h, float d) {
		setBounds(0, x, x + w);
		setBounds(1, y, y + h);
		setBounds(2, z, z + d);
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}

		if (object == null || !(object instanceof AABB3)) {
			return false;
		}

		AABB3 aabb = (AABB3) object;

		return aabb.x1 == x1 && aabb.y1 == y1 && aabb.z1 == z1 && aabb.x2 == x2 && aabb.y2 == y2 && aabb.z2 == z2;
	}

	@Override
	public int hashCode() {
		int hash = 1;
		hash = 31 * hash + Float.floatToIntBits(x1);
		hash = 31 * hash + Float.floatToIntBits(y1);
		hash = 31 * hash + Float.floatToIntBits(z1);
		hash = 31 * hash + Float.floatToIntBits(x2);
		hash = 31 * hash + Float.floatToIntBits(y2);
		hash = 31 * hash + Float.floatToIntBits(z2);

		return hash;
	}

	@Override
	public AABB3 copy() {
		return new AABB3(x1, y1, z1, x2 - x1, y2 - y1, z2 - z1);
	}

	@Override
	public int getDimensions() {
		return 3;
	}

	@Override
	public float getMinimum(int dimension) {
		if (dimension == 0) {
			return x1;
		}

		if (dimension == 1) {
			return y1;
		}

		if (dimension == 2) {
			return z1;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public float getMaximum(int dimension) {
		if (dimension == 0) {
			return x2;
		}

		if (dimension == 1) {
			return y2;
		}

		if (dimension == 2) {
			return z2;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public void setBounds(int dimension, float minimum, float maximum) {
		if (maximum < minimum) {
			throw new IllegalArgumentException();
		}

		if (dimension == 0) {
			x1 = minimum;
			x2 = maximum;
		} else if (dimension == 1) {
			y1 = minimum;
			y2 = maximum;
		} else if (dimension == 2) {
			z1 = minimum;
			z2 = maximum;
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public float getExtent(int dimension) {
		if (dimension == 0) {
			return x2 - x1;
		}

		if (dimension == 1) {
			return y2 - y1;
		}

		if (dimension == 2) {
			return z2 - z1;
		}

		throw new IllegalArgumentException();
	}

	@Override
	public float getVolume() {
		return (x2 - x1) * (y2 - y1) * (z2 - z1);
	}

	@Override
	public boolean contains(AABB aabb) {
		if (aabb.getDimensions() != 3) {
			throw new IllegalArgumentException();
		}

		if (x1 > aabb.getMinimum(0) || x2 < aabb.getMaximum(0)) {
			return false;
		}

		if (y1 > aabb.getMinimum(1) || y2 < aabb.getMaximum(1)) {
			return false;
		}

		if (z1 > aabb.getMinimum(2) || z2 < aabb.getMaximum(2)) {
			return false;
		}

		return true;
	}

	@Override
	public boolean intersects(AABB aabb) {
		if (aabb.getDimensions() != 3) {
			throw new IllegalArgumentException();
		}

		if (x1 > aabb.getMaximum(0) || x2 < aabb.getMinimum(0)) {
			return false;
		}

		if (y1 > aabb.getMaximum(1) || y2 < aabb.getMinimum(1)) {
			return false;
		}

		if (z1 > aabb.getMaximum(2) || z2 < aabb.getMinimum(2)) {
			return false;
		}

		return true;
	}
}

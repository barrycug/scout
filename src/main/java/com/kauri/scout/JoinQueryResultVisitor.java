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
 * A visitor for objects during a spatial join.
 * 
 * @author Eric Fritz
 */
public interface JoinQueryResultVisitor<E, F>
{
	/**
	 * Called during a spatial join for each pair of objects whose volumes match the query criteria.
	 * 
	 * <p>
	 * <tt>object1</tt> is guaranteed to be from the first spatial index, and <tt>object2</tt> is
	 * guaranteed to be from the second spatial index.
	 * 
	 * @param object1
	 *            An object whose volume matched the query criteria.
	 * @param object2
	 *            An object whose volume matched the query criteria.
	 * @return <tt>false</tt> to cancel the query, <tt>true</tt> otherwise.
	 */
	public boolean visit(E object1, F object2);
}

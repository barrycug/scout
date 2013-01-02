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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Fritz
 */
public class RTree
{
	private final int MIN_OBJECTS_PER_NODE = 2;
	private final int MAX_OBJECTS_PER_NODE = 3;

	private Node root;

	private Map<Object, Node> leafMap = new HashMap<Object, Node>();

	public RTree()
	{
		root = new Node(true);
	}

	public List<Object> search(AABB aabb)
	{
		return searchRange(new ArrayList<Object>(), aabb, root);
	}

	public void insert(Object object, AABB aabb)
	{
		Node node1 = chooseLeaf(aabb);
		Node node2 = null;

		if (node1.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
			addEntry(node1, aabb, object);
		} else {
			node2 = splitNode(node1, aabb, object, true);
		}

		while (node1 != root) {
			Node parent = node1.parent;
			Node psplit = null;

			updateBounds(parent, node1);

			if (node2 != null) {
				if (parent.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
					addEntry(parent, getBoundsForNode(node2), node2);
					node2.parent = parent;
				} else {
					psplit = splitNode(parent, getBoundsForNode(node2), node2, false);
				}
			}

			node1 = parent;
			node2 = psplit;
		}

		if (node2 != null) {
			root = new Node(false);
			addEntry(root, getBoundsForNode(node1), node1);
			addEntry(root, getBoundsForNode(node2), node2);

			node1.parent = root;
			node2.parent = root;
		}
	}

	public void remove(Object object)
	{
		Node node = leafMap.get(object);

		if (node == null) {
			return;
		}

		removeEntry(node, object);

		List<Node> queue = new ArrayList<Node>();

		while (node != root) {
			Node parent = node.parent;

			if (node.numEntries < MIN_OBJECTS_PER_NODE) {
				removeEntry(parent, node);
				queue.add(node);
			} else {
				updateBounds(parent, node);
			}

			node = parent;
		}

		for (Node n : queue) {
			reinsert(n);
		}

		if (!root.isLeaf && root.numEntries == 1) {
			root = (Node) root.entries[0];
			root.parent = null;
		}
	}

	public void update(Object object, AABB aabb)
	{
		remove(object);
		insert(object, aabb);
	}

	//
	// RECURSIVE

	private List<Object> searchRange(List<Object> objects, AABB aabb, Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.bounds[i].intersects(aabb)) {
				if (node.isLeaf) {
					objects.add(node.entries[i]);
				} else {
					if (aabb.contains(node.bounds[i])) {
						getAllObjects(objects, (Node) node.entries[i]);
					} else {
						searchRange(objects, aabb, (Node) node.entries[i]);
					}
				}
			}
		}

		return objects;
	}

	//
	// RECURSIVE

	private List<Object> getAllObjects(List<Object> objects, Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				objects.add(node.entries[i]);
			} else {
				getAllObjects(objects, (Node) node.entries[i]);
			}
		}

		return objects;
	}

	private Node chooseLeaf(AABB aabb)
	{
		Node node = root;
		while (!node.isLeaf) {
			int index = 0;
			float best = Float.POSITIVE_INFINITY;

			for (int i = 0; i < node.numEntries; i++) {
				float increase = AABB.getUnionVolume(node.bounds[i], aabb);

				if (increase < best || (increase == best && node.bounds[i].getVolume() < node.bounds[index].getVolume())) {
					index = i;
					best = increase;
				}
			}

			node = (Node) node.entries[index];
		}

		return node;
	}

	private Node splitNode(Node node, AABB aabb, Object object, boolean createLeaves)
	{
		Node node1 = node;
		Node node2 = new Node(createLeaves);

		List<AABB> aabbs = new ArrayList<AABB>();
		List<Object> objects = new ArrayList<Object>();

		for (int i = 0; i < node1.numEntries; i++) {
			aabbs.add(node1.bounds[i]);
			objects.add(node1.entries[i]);
		}

		clearEntries(node1);

		aabbs.add(aabb);
		objects.add(object);

		for (int i = 0; i < aabbs.size(); i++) {
			Node target = i % 2 == 0 ? node1 : node2;

			addEntry(target, aabbs.get(i), objects.get(i));

			if (!createLeaves) {
				((Node) objects.get(i)).parent = target;
			}
		}

		return node2;
	}

	//
	// RECURSIVE

	private void reinsert(Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				insert(node.entries[i], node.bounds[i]);
			} else {
				reinsert((Node) node.entries[i]);
			}
		}
	}

	private boolean updateBounds(Node parent, Node node)
	{
		for (int i = 0; i < parent.numEntries; i++) {
			if (parent.entries[i] == node) {
				parent.bounds[i] = getBoundsForNode(node);
				return true;
			}
		}

		return false;
	}

	private AABB getBoundsForNode(Node node)
	{
		return AABB.getUnion(Arrays.copyOf(node.bounds, node.numEntries));
	}

	public void addEntry(Node node, AABB aabb, Object object)
	{
		node.bounds[node.numEntries] = aabb;
		node.entries[node.numEntries] = object;
		node.numEntries++;

		if (node.isLeaf) {
			leafMap.put(object, node);
		}
	}

	public boolean removeEntry(Node node, Object object)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.entries[i] == object) {
				if (node.isLeaf) {
					leafMap.remove(object);
				}

				node.bounds[i] = node.bounds[node.numEntries - 1];
				node.entries[i] = node.entries[node.numEntries - 1];

				node.bounds[node.numEntries - 1] = null;
				node.entries[node.numEntries - 1] = null;

				node.numEntries--;
				return true;
			}
		}

		return false;
	}

	public void clearEntries(Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				leafMap.remove(node.entries[i]);
			}

			node.bounds[i] = null;
			node.entries[i] = null;
		}

		node.numEntries = 0;
	}

	class Node
	{
		private Node parent;
		private boolean isLeaf;
		private int numEntries;
		private AABB[] bounds = new AABB[MAX_OBJECTS_PER_NODE];
		private Object[] entries = new Object[MAX_OBJECTS_PER_NODE];

		public Node(boolean isLeaf)
		{
			this.isLeaf = isLeaf;
		}
	}
}

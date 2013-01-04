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

	public void query(Query query, QueryResultVisitor visitor)
	{
		query(query, visitor, root);
	}

	public void queryJoin(JoinQuery query, QueryJoinResultVisitor visitor)
	{
		query(query, visitor, root, root, true);
	}

	public void queryJoin(RTree tree, JoinQuery query, QueryJoinResultVisitor visitor)
	{
		query(query, visitor, root, tree.root, tree == this);
	}

	private void query(Query query, QueryResultVisitor visitor, Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				if (query.queryElement(node.bounds[i], node.entries[i])) {
					visitor.visit(node.entries[i]);
				}
			} else {
				QueryResult result = query.queryInternalNode(node.bounds[i], (Node) node.entries[i]);

				if (result == QueryResult.ALL) {
					visitAllObjects(visitor, (Node) node.entries[i]);
				} else if (result == QueryResult.SOME) {
					query(query, visitor, (Node) node.entries[i]);
				}
			}
		}
	}

	private void query(JoinQuery query, QueryJoinResultVisitor visitor, Node node1, Node node2, boolean sameTree)
	{
		if (node1.isLeaf && node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = sameTree && node1 == node2 ? i + 1 : 0; j < node2.numEntries; j++) {
					if (query.queryJoin(node1.bounds[i], node2.bounds[j])) {
						visitor.visit(node1.entries[i], node2.entries[j]);
					}
				}
			}
		} else if (node1.isLeaf) {
			for (int i = 0; i < node2.numEntries; i++) {
				query(query, visitor, node1, (Node) node2.entries[i], sameTree);
			}
		} else if (node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				query(query, visitor, (Node) node1.entries[i], node2, sameTree);
			}
		} else {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = sameTree && node1 == node2 ? i : 0; j < node2.numEntries; j++) {
					if (query.queryJoin(node1.bounds[i], node2.bounds[j])) {
						query(query, visitor, (Node) node1.entries[i], (Node) node2.entries[j], sameTree);
					}
				}
			}
		}
	}

	private void visitAllObjects(QueryResultVisitor visitor, Node node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				visitor.visit(node.entries[i]);
			} else {
				visitAllObjects(visitor, (Node) node.entries[i]);
			}
		}
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

	private Node chooseLeaf(AABB aabb)
	{
		Node node = root;
		while (!node.isLeaf) {
			int index = 0;
			float best = Float.POSITIVE_INFINITY;

			for (int i = 0; i < node.numEntries; i++) {
				float increase = 1;

				for (int j = 0; j < aabb.getDimensions(); j++) {
					float min = Math.min(node.bounds[i].getMinimum(j), aabb.getMinimum(j));
					float max = Math.max(node.bounds[i].getMaximum(j), aabb.getMaximum(j));

					increase *= max - min;
				}

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
		int dimensions = node.bounds[0].getDimensions();

		float[] min = new float[dimensions];
		float[] max = new float[dimensions];

		Arrays.fill(min, Float.POSITIVE_INFINITY);
		Arrays.fill(max, Float.NEGATIVE_INFINITY);

		for (int j = 0; j < node.numEntries; j++) {
			AABB aabb = node.bounds[j];

			for (int i = 0; i < dimensions; i++) {
				min[i] = Math.min(min[i], aabb.getMinimum(i));
				max[i] = Math.max(max[i], aabb.getMaximum(i));
			}
		}

		return new AABB(min, max);
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

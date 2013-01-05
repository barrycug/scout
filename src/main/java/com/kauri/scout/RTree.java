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
public class RTree<E>
{
	private final static int MIN_OBJECTS_PER_NODE = 2;
	private final static int MAX_OBJECTS_PER_NODE = 3;

	private Node<E> root;
	private Map<E, Node<E>> leafMap = new HashMap<E, Node<E>>();

	public RTree()
	{
		root = new Node<E>(true);
	}

	public void query(Query query, QueryResultVisitor<E> visitor)
	{
		query(query, visitor, root);
	}

	public void queryJoin(JoinQuery query, QueryJoinResultVisitor<E, E> visitor)
	{
		query(query, visitor, root, root, true);
	}

	public <E2> void queryJoin(RTree<E2> tree, JoinQuery query, QueryJoinResultVisitor<E, E2> visitor)
	{
		query(query, visitor, root, tree.root, tree == this);
	}

	@SuppressWarnings("unchecked")
	private void query(Query query, QueryResultVisitor<E> visitor, Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				if (query.queryElement(node.bounds[i])) {
					visitor.visit((E) node.entries[i]);
				}
			} else {
				QueryResult result = query.queryInternalNode(node.bounds[i]);

				if (result == QueryResult.ALL) {
					visitAllObjects(visitor, (Node<E>) node.entries[i]);
				} else if (result == QueryResult.SOME) {
					query(query, visitor, (Node<E>) node.entries[i]);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <E2> void query(JoinQuery query, QueryJoinResultVisitor<E, E2> visitor, Node<E> node1, Node<E2> node2, boolean sameTree)
	{
		boolean queryBoth = sameTree && !query.isSymmetricRelation();

		if (node1.isLeaf && node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = node1 == node2 ? i + 1 : 0; j < node2.numEntries; j++) {
					if (query.queryLeafNode(node1.bounds[i], node2.bounds[j]) || (queryBoth && query.queryLeafNode(node2.bounds[j], node1.bounds[i]))) {
						visitor.visit((E) node1.entries[i], (E2) node2.entries[j]);
					}
				}
			}
		} else if (node1.isLeaf) {
			for (int i = 0; i < node2.numEntries; i++) {
				query(query, visitor, node1, (Node<E2>) node2.entries[i], sameTree);
			}
		} else if (node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				query(query, visitor, (Node<E>) node1.entries[i], node2, sameTree);
			}
		} else {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = node1 == node2 ? i : 0; j < node2.numEntries; j++) {
					if (query.queryInternalNode(node1.bounds[i], node2.bounds[j]) != QueryResult.NONE || (queryBoth && query.queryInternalNode(node2.bounds[j], node1.bounds[i]) != QueryResult.NONE)) {
						query(query, visitor, (Node<E>) node1.entries[i], (Node<E2>) node2.entries[j], sameTree);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void visitAllObjects(QueryResultVisitor<E> visitor, Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				visitor.visit((E) node.entries[i]);
			} else {
				visitAllObjects(visitor, (Node<E>) node.entries[i]);
			}
		}
	}

	public void insert(E object, AABB aabb)
	{
		Node<E> node1 = chooseLeaf(aabb);
		Node<E> node2 = null;

		if (node1.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
			addEntry(node1, aabb, object);
		} else {
			node2 = splitNode(node1, aabb, object, true);
		}

		while (node1 != root) {
			Node<E> parent = node1.parent;
			Node<E> psplit = null;

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
			root = new Node<E>(false);
			addEntry(root, getBoundsForNode(node1), node1);
			addEntry(root, getBoundsForNode(node2), node2);

			node1.parent = root;
			node2.parent = root;
		}
	}

	@SuppressWarnings("unchecked")
	public void remove(E object)
	{
		Node<E> node = leafMap.get(object);

		if (node == null) {
			return;
		}

		removeEntry(node, object);

		List<Node<E>> queue = new ArrayList<Node<E>>();

		while (node != root) {
			Node<E> parent = node.parent;

			if (node.numEntries < MIN_OBJECTS_PER_NODE) {
				removeEntry(parent, node);
				queue.add(node);
			} else {
				updateBounds(parent, node);
			}

			node = parent;
		}

		for (Node<E> n : queue) {
			reinsert(n);
		}

		if (!root.isLeaf && root.numEntries == 1) {
			root = (Node<E>) root.entries[0];
			root.parent = null;
		}
	}

	public void update(E object, AABB aabb)
	{
		remove(object);
		insert(object, aabb);
	}

	@SuppressWarnings("unchecked")
	private Node<E> chooseLeaf(AABB aabb)
	{
		Node<E> node = root;
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

			node = (Node<E>) node.entries[index];
		}

		return node;
	}

	@SuppressWarnings("unchecked")
	private Node<E> splitNode(Node<E> node, AABB aabb, Object object, boolean createLeaves)
	{
		Node<E> node1 = node;
		Node<E> node2 = new Node<E>(createLeaves);

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
			Node<E> target = i % 2 == 0 ? node1 : node2;

			addEntry(target, aabbs.get(i), objects.get(i));

			if (!createLeaves) {
				((Node<E>) objects.get(i)).parent = target;
			}
		}

		return node2;
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private void reinsert(Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				insert((E) node.entries[i], node.bounds[i]);
			} else {
				reinsert((Node<E>) node.entries[i]);
			}
		}
	}

	private boolean updateBounds(Node<E> parent, Node<E> node)
	{
		for (int i = 0; i < parent.numEntries; i++) {
			if (parent.entries[i] == node) {
				parent.bounds[i] = getBoundsForNode(node);
				return true;
			}
		}

		return false;
	}

	private AABB getBoundsForNode(Node<E> node)
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

	@SuppressWarnings("unchecked")
	public void addEntry(Node<E> node, AABB aabb, Object object)
	{
		node.bounds[node.numEntries] = aabb;
		node.entries[node.numEntries] = object;
		node.numEntries++;

		if (node.isLeaf) {
			leafMap.put((E) object, node);
		}
	}

	public boolean removeEntry(Node<E> node, Object object)
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

	public void clearEntries(Node<E> node)
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

	static class Node<E>
	{
		private Node<E> parent;
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

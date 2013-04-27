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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * @author Eric Fritz
 */
public class RTree<E>
{
	private final static int MIN_OBJECTS_PER_NODE = 3;
	private final static int MAX_OBJECTS_PER_NODE = 8;
	private final static int MAX_SPLIT_ITERATIONS = 8;

	private Node root;
	private Map<E, Node> leafMap = new HashMap<E, Node>();

	public RTree()
	{
		root = new Node(true);
	}

	public void query(Query query, QueryResultVisitor<E> visitor)
	{
		query(query, visitor, root);
	}

	public void queryJoin(JoinQuery query, QueryJoinResultVisitor<E, E> visitor)
	{
		query(query, visitor, root, root, true);
	}

	public <F> void queryJoin(RTree<F> tree, JoinQuery query, QueryJoinResultVisitor<E, F> visitor)
	{
		query(query, visitor, root, tree.root, tree == this);
	}

	public void insert(E object, AABB volume)
	{
		Node node1 = chooseLeaf(root, volume);
		Node node2 = null;

		if (node1.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
			node1.add(volume, object);
		} else {
			node2 = splitNode(node1, volume, object);
		}

		while (node1 != root) {
			Node parent = node1.parent;
			Node psplit = null;

			updateVolumes(parent, node1);

			if (node2 != null) {
				if (parent.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
					parent.add(getVolumeForNode(node2), node2);
				} else {
					psplit = splitNode(parent, getVolumeForNode(node2), node2);
				}
			}

			node1 = parent;
			node2 = psplit;
		}

		if (node2 != null) {
			root = new Node(false);
			root.add(getVolumeForNode(node1), node1);
			root.add(getVolumeForNode(node2), node2);
		}
	}

	public void update(E object, AABB volume)
	{
		Node node = leafMap.get(object);

		if (node == null) {
			return;
		}

		if (node != root) {
			Node parent = node.parent;

			outer: for (int i = 0; i < parent.numEntries; i++) {
				if (parent.entries[i] == node) {
					if (!parent.volumes[i].contains(volume)) {
						break outer;
					}

					for (int j = 0; j < node.numEntries; j++) {
						if (node.entries[j] == object) {
							node.volumes[j] = volume;
							return;
						}
					}
				}
			}
		}

		remove(object);
		insert(object, volume);
	}

	@SuppressWarnings("unchecked")
	public void remove(E object)
	{
		Node node = leafMap.get(object);

		if (node == null) {
			return;
		}

		node.remove(object);

		Queue<Node> queue = new LinkedList<Node>();

		while (node != root) {
			Node parent = node.parent;

			if (node.numEntries < MIN_OBJECTS_PER_NODE) {
				parent.remove(node);
				queue.add(node);
			} else {
				updateVolumes(parent, node);
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

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private boolean query(Query query, QueryResultVisitor<E> visitor, Node node)
	{
		if (node.isLeaf) {
			for (int i = 0; i < node.numEntries; i++) {
				if (query.query(node.volumes[i], false) == QueryResult.ALL) {
					if (!visitor.visit((E) node.entries[i])) {
						return false;
					}
				}
			}
		} else {
			for (int i = 0; i < node.numEntries; i++) {
				QueryResult result = query.query(node.volumes[i], true);

				if (result == QueryResult.ALL) {
					if (!visitAllObjects(visitor, (Node) node.entries[i])) {
						return false;
					}
				} else if (result == QueryResult.SOME) {
					if (!query(query, visitor, (Node) node.entries[i])) {
						return false;
					}
				}
			}
		}

		return true;
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private boolean visitAllObjects(QueryResultVisitor<E> visitor, Node node)
	{
		if (node.isLeaf) {
			for (int i = 0; i < node.numEntries; i++) {
				if (!visitor.visit((E) node.entries[i])) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < node.numEntries; i++) {
				if (!visitAllObjects(visitor, (Node) node.entries[i])) {
					return false;
				}
			}
		}

		return true;
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private <F> boolean query(JoinQuery query, QueryJoinResultVisitor<E, F> visitor, RTree<E>.Node node1, RTree<F>.Node node2, boolean sameTree)
	{
		boolean queryBoth = sameTree && !query.isSymmetricRelation();

		if (node1.isLeaf && node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = node1 == node2 ? i + 1 : 0; j < node2.numEntries; j++) {
					if (query.query(node1.volumes[i], node2.volumes[j], false) == QueryResult.ALL) {
						if (!visitor.visit((E) node1.entries[i], (F) node2.entries[j])) {
							return false;
						}
					} else if (queryBoth && query.query(node2.volumes[j], node1.volumes[i], false) == QueryResult.ALL) {
						if (!visitor.visit((E) node2.entries[j], (F) node1.entries[i])) {
							return false;
						}
					}
				}
			}
		} else if (node1.isLeaf) {
			for (int i = 0; i < node2.numEntries; i++) {
				if (!query(query, visitor, node1, (RTree<F>.Node) node2.entries[i], sameTree)) {
					return false;
				}
			}
		} else if (node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				if (!query(query, visitor, (RTree<E>.Node) node1.entries[i], node2, sameTree)) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < node1.numEntries; i++) {
				int k = sameTree && node1 == node2 ? i : 0;

				for (int j = k; j < node2.numEntries; j++) {
					if (query.query(node1.volumes[i], node2.volumes[j], true) != QueryResult.NONE) {
						if (!query(query, visitor, (RTree<E>.Node) node1.entries[i], (RTree<F>.Node) node2.entries[j], sameTree)) {
							return false;
						}
					} else if (queryBoth && query.query(node2.volumes[j], node1.volumes[i], true) != QueryResult.NONE) {
						if (!query(query, visitor, (RTree<E>.Node) node1.entries[i], (RTree<F>.Node) node2.entries[j], sameTree)) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private Node chooseLeaf(Node node, AABB volume)
	{
		while (!node.isLeaf) {
			int index = 0;
			float best = Float.POSITIVE_INFINITY;

			for (int i = 0; i < node.numEntries; i++) {
				float increase = 1;

				for (int j = 0; j < volume.getDimensions(); j++) {
					float min = Math.min(node.volumes[i].getMinimum(j), volume.getMinimum(j));
					float max = Math.max(node.volumes[i].getMaximum(j), volume.getMaximum(j));

					increase *= max - min;
				}

				increase -= node.volumes[i].getVolume();

				if (increase < best || (increase == best && node.volumes[i].getVolume() < node.volumes[index].getVolume())) {
					index = i;
					best = increase;
				}
			}

			node = (Node) node.entries[index];
		}

		return node;
	}

	private Node splitNode(Node oldNode, AABB volume, Object object)
	{
		AABB[] volumes1 = new AABB[MAX_OBJECTS_PER_NODE];
		AABB[] volumes2 = new AABB[MAX_OBJECTS_PER_NODE];

		Object[] entries1 = new Object[MAX_OBJECTS_PER_NODE];
		Object[] entries2 = new Object[MAX_OBJECTS_PER_NODE];

		int seed1 = (int) (Math.random() * MAX_OBJECTS_PER_NODE);
		int seed2 = 0;

		do {
			seed2 = (int) (Math.random() * MAX_OBJECTS_PER_NODE);
		} while (seed1 == seed2);

		volumes1[0] = oldNode.volumes[seed1];
		entries1[0] = oldNode.entries[seed1];
		volumes2[0] = oldNode.volumes[seed2];
		entries2[0] = oldNode.entries[seed2];

		int size1 = 1;
		int size2 = 1;

		{
			double dist1 = AABBUtil.distanceSquared(volume, volumes1[0]);
			double dist2 = AABBUtil.distanceSquared(volume, volumes2[0]);

			if (dist1 < dist2) {
				volumes1[size1] = volume;
				entries1[size1] = object;
				size1++;
			} else {
				volumes2[size2] = volume;
				entries2[size2] = object;
				size2++;
			}
		}

		for (int i = 0; i < oldNode.numEntries; i++) {
			if (i == seed1 || i == seed2) {
				continue;
			}

			double dist1 = AABBUtil.distanceSquared(oldNode.volumes[i], volumes1[0]);
			double dist2 = AABBUtil.distanceSquared(oldNode.volumes[i], volumes2[0]);

			if (dist1 < dist2) {
				volumes1[size1] = oldNode.volumes[i];
				entries1[size1] = oldNode.entries[i];
				size1++;
			} else {
				volumes2[size2] = oldNode.volumes[i];
				entries2[size2] = oldNode.entries[i];
				size2++;
			}
		}

		oldNode.clear();

		Node newNode = new Node(oldNode.isLeaf);

		partitionEntries(oldNode, newNode, volumes1, volumes2, entries1, entries2, size1, size2);

		return newNode;
	}

	private void partitionEntries(Node oldNode, Node newNode, AABB[] volumes1, AABB[] volumes2, Object[] entries1, Object[] entries2, int size1, int size2)
	{
		AABB median1 = volumes1[0].copy();
		AABB median2 = volumes2[0].copy();

		int iterations = 0;
		while (iterations < MAX_SPLIT_ITERATIONS) {
			adjustMedian(median1, volumes1, size1);
			adjustMedian(median2, volumes2, size2);

			int n = moveToGroup(volumes1, entries1, size1, volumes2, entries2, size2, median1, median2);

			size1 -= n;
			size2 += n;

			int m = moveToGroup(volumes2, entries2, size2, volumes1, entries1, size1, median2, median1);

			size1 += m;
			size2 -= m;

			if (n == 0 && m == 0) {
				break;
			}

			iterations++;
		}

		bulkAddEntry(oldNode, volumes1, entries1, size1);
		bulkAddEntry(newNode, volumes2, entries2, size2);
	}

	private void adjustMedian(AABB median, AABB[] volumes, int size)
	{
		float totalmass = 0;
		float[] centers = new float[median.getDimensions()];

		for (int i = 0; i < size; i++) {
			float mass = volumes[i].getVolume();

			for (int j = 0; j < median.getDimensions(); j++) {
				centers[j] += AABBUtil.getCenter(volumes[i], j) * mass;
			}

			totalmass += mass;
		}

		for (int j = 0; j < median.getDimensions(); j++) {
			median.setBounds(j, centers[j] / totalmass, centers[j] / totalmass);
		}
	}

	private int moveToGroup(AABB[] volumes1, Object[] entries1, int size1, AABB[] volumes2, Object[] entries2, int size2, AABB median1, AABB median2)
	{
		int i = 0;
		int transfers = 0;

		while (i < size1 - transfers && size1 - transfers > 1) {
			float dist1 = AABBUtil.distanceSquared(volumes1[i], median1);
			float dist2 = AABBUtil.distanceSquared(volumes1[i], median2);

			if (dist2 < dist1) {
				volumes2[size2 + transfers] = volumes1[i];
				entries2[size2 + transfers] = entries1[i];

				transfers++;

				volumes1[i] = volumes1[size1 - transfers];
				entries1[i] = entries1[size1 - transfers];
			} else {
				i++;
			}
		}

		return transfers;
	}

	private void bulkAddEntry(Node target, AABB[] volumes, Object[] entries, int size)
	{
		for (int i = 0; i < size; i++) {
			target.add(volumes[i], entries[i]);
		}
	}

	private boolean updateVolumes(Node parent, Node node)
	{
		for (int i = 0; i < parent.numEntries; i++) {
			if (parent.entries[i] == node) {
				parent.volumes[i] = getVolumeForNode(node);
				return true;
			}
		}

		return false;
	}

	private AABB getVolumeForNode(Node node)
	{
		AABB volume = node.volumes[0].copy();

		for (int i = 1; i < node.numEntries; i++) {
			for (int j = 0; j < volume.getDimensions(); j++) {
				float min1 = volume.getMinimum(j);
				float max1 = volume.getMaximum(j);
				float min2 = node.volumes[i].getMinimum(j);
				float max2 = node.volumes[i].getMaximum(j);

				volume.setBounds(j, Math.min(min1, min2), Math.max(max1, max2));
			}
		}

		return volume;
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private void reinsert(Node node)
	{
		if (node.isLeaf) {
			for (int i = 0; i < node.numEntries; i++) {
				insert((E) node.entries[i], node.volumes[i]);
			}
		} else {
			for (int i = 0; i < node.numEntries; i++) {
				reinsert((Node) node.entries[i]);
			}
		}
	}

	private class Node
	{
		private Node parent;
		private boolean isLeaf;
		private int numEntries;
		private AABB[] volumes = new AABB[MAX_OBJECTS_PER_NODE];
		private Object[] entries = new Object[MAX_OBJECTS_PER_NODE];

		public Node(boolean isLeaf)
		{
			this.isLeaf = isLeaf;
		}

		@SuppressWarnings("unchecked")
		public void add(AABB volume, Object object)
		{
			volumes[numEntries] = volume;
			entries[numEntries] = object;
			numEntries++;

			if (isLeaf) {
				leafMap.put((E) object, this);
			} else {
				((Node) object).parent = this;
			}
		}

		public void remove(Object object)
		{
			for (int i = 0; i < numEntries; i++) {
				if (entries[i] == object) {
					if (isLeaf) {
						leafMap.remove(object);
					}

					numEntries--;
					volumes[i] = volumes[numEntries];
					entries[i] = entries[numEntries];

					volumes[numEntries] = null;
					entries[numEntries] = null;
				}
			}
		}

		public void clear()
		{
			for (int i = 0; i < numEntries; i++) {
				if (isLeaf) {
					leafMap.remove(entries[i]);
				}

				volumes[i] = null;
				entries[i] = null;
			}

			numEntries = 0;
		}
	}
}

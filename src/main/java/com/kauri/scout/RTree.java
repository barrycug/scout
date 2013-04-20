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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eric Fritz
 */
public class RTree<E>
{
	private final static int MIN_OBJECTS_PER_NODE = 24;
	private final static int MAX_OBJECTS_PER_NODE = 32;
	private final static int MAX_SPLIT_ITERATIONS = 8;

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

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private void query(Query query, QueryResultVisitor<E> visitor, Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				if (query.query(node.volumes[i], false) == QueryResult.ALL) {
					visitor.visit((E) node.entries[i]);
				}
			} else {
				QueryResult result = query.query(node.volumes[i], true);

				if (result == QueryResult.ALL) {
					visitAllObjects(visitor, (Node<E>) node.entries[i]);
				} else if (result == QueryResult.SOME) {
					query(query, visitor, (Node<E>) node.entries[i]);
				}
			}
		}
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private <E2> void query(JoinQuery query, QueryJoinResultVisitor<E, E2> visitor, Node<E> node1, Node<E2> node2, boolean sameTree)
	{
		boolean queryBoth = sameTree && !query.isSymmetricRelation();

		if (node1.isLeaf && node2.isLeaf) {
			for (int i = 0; i < node1.numEntries; i++) {
				for (int j = node1 == node2 ? i + 1 : 0; j < node2.numEntries; j++) {
					if (query.query(node1.volumes[i], node2.volumes[j], false) == QueryResult.ALL) {
						visitor.visit((E) node1.entries[i], (E2) node2.entries[j]);
					} else if (queryBoth && query.query(node2.volumes[j], node1.volumes[i], false) == QueryResult.ALL) {
						visitor.visit((E) node2.entries[j], (E2) node1.entries[i]);
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
					if (query.query(node1.volumes[i], node2.volumes[j], true) != QueryResult.NONE || (queryBoth && query.query(node2.volumes[j], node1.volumes[i], true) != QueryResult.NONE)) {
						query(query, visitor, (Node<E>) node1.entries[i], (Node<E2>) node2.entries[j], sameTree);
					}
				}
			}
		}
	}

	//
	// RECURSIVE

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

	public void insert(E object, AABB volume)
	{
		Node<E> node1 = chooseLeaf(volume);
		Node<E> node2 = null;

		if (node1.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
			addEntry(node1, volume, object);
		} else {
			node2 = splitNode(node1, volume, object, true);
		}

		while (node1 != root) {
			Node<E> parent = node1.parent;
			Node<E> psplit = null;

			updateVolumes(parent, node1);

			if (node2 != null) {
				if (parent.numEntries + 1 <= MAX_OBJECTS_PER_NODE) {
					addEntry(parent, getVolumeForNode(node2), node2);
					node2.parent = parent;
				} else {
					psplit = splitNode(parent, getVolumeForNode(node2), node2, false);
				}
			}

			node1 = parent;
			node2 = psplit;
		}

		if (node2 != null) {
			root = new Node<E>(false);
			addEntry(root, getVolumeForNode(node1), node1);
			addEntry(root, getVolumeForNode(node2), node2);

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
				updateVolumes(parent, node);
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

	public void update(E object, AABB volume)
	{
		remove(object);
		insert(object, volume);
	}

	@SuppressWarnings("unchecked")
	private Node<E> chooseLeaf(AABB volume)
	{
		Node<E> node = root;

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

				if (increase < best || (increase == best && node.volumes[i].getVolume() < node.volumes[index].getVolume())) {
					index = i;
					best = increase;
				}
			}

			node = (Node<E>) node.entries[index];
		}

		return node;
	}

	private Node<E> splitNode(Node<E> oldNode, AABB volume, Object object, boolean createLeaves)
	{
		AABB[] volumes1 = new AABB[MAX_OBJECTS_PER_NODE + 1];
		AABB[] volumes2 = new AABB[MAX_OBJECTS_PER_NODE + 1];

		Object[] objects1 = new Object[MAX_OBJECTS_PER_NODE + 1];
		Object[] objects2 = new Object[MAX_OBJECTS_PER_NODE + 1];

		int seed = chooseSecondSeed(oldNode, volume);

		volumes1[0] = volume;
		objects1[0] = object;

		volumes2[0] = oldNode.volumes[seed];
		objects2[0] = oldNode.entries[seed];

		int size1 = 1;
		int size2 = 1;

		AABB median1 = volumes1[0].copy();
		AABB median2 = volumes2[0].copy();

		for (int i = 0; i < oldNode.numEntries; i++) {
			if (i == seed) {
				continue;
			}

			double dist1 = AABBUtil.distanceSquared(oldNode.volumes[i], median1);
			double dist2 = AABBUtil.distanceSquared(oldNode.volumes[i], median2);

			if (dist1 < dist2) {
				volumes1[size1] = oldNode.volumes[i];
				objects1[size1] = oldNode.entries[i];
				size1++;
			} else {
				volumes2[size2] = oldNode.volumes[i];
				objects2[size2] = oldNode.entries[i];
				size2++;
			}
		}

		int iterations = 0;
		while (iterations < MAX_SPLIT_ITERATIONS) {
			adjustMedian(median1, volumes1, size1);
			adjustMedian(median2, volumes2, size2);

			int n = moveToGroup(volumes1, objects1, size1, volumes2, objects2, size2, median1, median2);

			size1 -= n;
			size2 += n;

			int m = moveToGroup(volumes2, objects2, size2, volumes1, objects1, size1, median2, median1);

			size1 += m;
			size2 -= m;

			if (n == 0 && m == 0) {
				break;
			}

			iterations++;
		}

		clearEntries(oldNode);

		Node<E> newNode = new Node<E>(createLeaves);
		bulkAddEntry(oldNode, volumes1, objects1, size1, createLeaves);
		bulkAddEntry(newNode, volumes2, objects2, size2, createLeaves);

		return newNode;
	}

	private int chooseSecondSeed(Node<E> node, AABB firstSeed)
	{
		int seed = 0;
		float best = Float.NEGATIVE_INFINITY;

		for (int i = 0; i < MAX_OBJECTS_PER_NODE; i++) {
			float dist = AABBUtil.distanceSquared(firstSeed, node.volumes[i]);

			if (dist > best) {
				best = dist;
				seed = i;
			}
		}

		return seed;
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
			median.setMinimum(j, centers[j] / totalmass);
			median.setMaximum(j, centers[j] / totalmass);
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

	@SuppressWarnings("unchecked")
	private void bulkAddEntry(Node<E> target, AABB[] volumes, Object[] entries, int size, boolean createLeaves)
	{
		for (int i = 0; i < size; i++) {
			addEntry(target, volumes[i], entries[i]);

			if (!createLeaves) {
				((Node<E>) entries[i]).parent = target;
			}
		}
	}

	//
	// RECURSIVE

	@SuppressWarnings("unchecked")
	private void reinsert(Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				insert((E) node.entries[i], node.volumes[i]);
			} else {
				reinsert((Node<E>) node.entries[i]);
			}
		}
	}

	private boolean updateVolumes(Node<E> parent, Node<E> node)
	{
		for (int i = 0; i < parent.numEntries; i++) {
			if (parent.entries[i] == node) {
				parent.volumes[i] = getVolumeForNode(node);
				return true;
			}
		}

		return false;
	}

	private AABB getVolumeForNode(Node<E> node)
	{
		AABB volume = node.volumes[0].copy();

		for (int i = 1; i < node.numEntries; i++) {
			for (int j = 0; j < volume.getDimensions(); j++) {
				float min1 = volume.getMinimum(j);
				float max1 = volume.getMaximum(j);
				float min2 = node.volumes[i].getMinimum(j);
				float max2 = node.volumes[i].getMaximum(j);

				if (min2 < min1) {
					volume.setMinimum(j, min2);
				}

				if (max2 > max1) {
					volume.setMaximum(j, max2);
				}
			}
		}

		return volume;
	}

	@SuppressWarnings("unchecked")
	private void addEntry(Node<E> node, AABB volume, Object object)
	{
		node.volumes[node.numEntries] = volume;
		node.entries[node.numEntries] = object;
		node.numEntries++;

		if (node.isLeaf) {
			leafMap.put((E) object, node);
		}
	}

	private boolean removeEntry(Node<E> node, Object object)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.entries[i] == object) {
				if (node.isLeaf) {
					leafMap.remove(object);
				}

				node.volumes[i] = node.volumes[node.numEntries - 1];
				node.entries[i] = node.entries[node.numEntries - 1];

				node.volumes[node.numEntries - 1] = null;
				node.entries[node.numEntries - 1] = null;

				node.numEntries--;
				return true;
			}
		}

		return false;
	}

	private void clearEntries(Node<E> node)
	{
		for (int i = 0; i < node.numEntries; i++) {
			if (node.isLeaf) {
				leafMap.remove(node.entries[i]);
			}

			node.volumes[i] = null;
			node.entries[i] = null;
		}

		node.numEntries = 0;
	}

	static class Node<E>
	{
		private Node<E> parent;
		private boolean isLeaf;
		private int numEntries;
		private AABB[] volumes = new AABB[MAX_OBJECTS_PER_NODE];
		private Object[] entries = new Object[MAX_OBJECTS_PER_NODE];

		public Node(boolean isLeaf)
		{
			this.isLeaf = isLeaf;
		}
	}
}

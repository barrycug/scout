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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.Timer;

/**
 * @author Eric Fritz
 */
public class Test
{
	private static Canvas c1 = new Canvas();
	private static Canvas c2 = new Canvas();

	private static RTree<Integer> tree1 = new RTree<Integer>();
	private static RTree<Integer> tree2 = new RTree<Integer>();

	private static List<AABB> leaves1 = new ArrayList<AABB>();
	private static List<AABB> leaves2 = new ArrayList<AABB>();

	private static List<Color> colors = new ArrayList<Color>();

	static {
		colors.add(Color.red);
		colors.add(Color.blue);
		colors.add(Color.green);
		colors.add(Color.orange);
	}

	public static void main(String[] args)
	{
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 600);

		c1.setSize(300, 600);
		frame.add(c1);

		c1.addMouseListener(new MouseListener() {
			private int startX;
			private int startY;

			@Override
			public void mouseClicked(MouseEvent e)
			{

			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				startX = e.getX();
				startY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int minX = Math.min(startX, e.getX());
				int minY = Math.min(startY, e.getY());
				int maxX = Math.max(startX, e.getX());
				int maxY = Math.max(startY, e.getY());

				clicked(c1, tree1, leaves1, new AABB(new float[] { minX, minY }, new float[] { maxX, maxY }));
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}
		});

		c2.setSize(300, 600);
		frame.add(c2);

		c2.addMouseListener(new MouseListener() {
			private int startX;
			private int startY;

			@Override
			public void mouseClicked(MouseEvent e)
			{

			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				startX = e.getX();
				startY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				int minX = Math.min(startX, e.getX()) - c1.getWidth();
				int minY = Math.min(startY, e.getY());
				int maxX = Math.max(startX, e.getX()) - c1.getWidth();
				int maxY = Math.max(startY, e.getY());

				clicked(c2, tree2, leaves2, new AABB(new float[] { minX, minY }, new float[] { maxX, maxY }));
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
			}
		});

		frame.setVisible(true);

		Timer t = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e)
			{
				redraw(c1, tree1, leaves1);
				redraw(c2, tree2, leaves2);
			}
		});

		t.setRepeats(false);
		t.start();
	}

	private static int object = 1;

	private static void clicked(Canvas c, RTree<Integer> tree, List<AABB> leaves, AABB bounds)
	{
		leaves.add(bounds);
		tree.insert(object++, bounds);

		System.out.println("Tree1 result: ");

		tree1.queryJoin(new ContainsJoinQuery(), new QueryJoinResultVisitor<Integer, Integer>() {
			@Override
			public void visit(Integer o1, Integer o2)
			{
				System.out.println("\t" + o1 + ", " + o2);
			}
		});

		System.out.println("Tree2 result: ");

		tree2.queryJoin(new ContainsJoinQuery(), new QueryJoinResultVisitor<Integer, Integer>() {
			@Override
			public void visit(Integer o1, Integer o2)
			{
				System.out.println("\t" + o1 + ", " + o2);
			}
		});

		System.out.println("Tree1 + Tree2 result: ");

		tree1.queryJoin(tree2, new ContainsJoinQuery(), new QueryJoinResultVisitor<Integer, Integer>() {
			@Override
			public void visit(Integer o1, Integer o2)
			{
				System.out.println("\t" + o1 + ", " + o2);
			}
		});

		System.out.println("Tree2 + Tree1 result: ");

		tree2.queryJoin(tree1, new ContainsJoinQuery(), new QueryJoinResultVisitor<Integer, Integer>() {
			@Override
			public void visit(Integer o1, Integer o2)
			{
				System.out.println("\t" + o1 + ", " + o2);
			}
		});

		System.out.println();
		redraw(c, tree, leaves);
	}

	static class WrappedAABB
	{
		AABB aabb;
		int level;

		public WrappedAABB(AABB aabb, int level)
		{
			this.aabb = aabb;
			this.level = level;
		}
	}

	@SuppressWarnings("unchecked")
	private static List<WrappedAABB> getBoundingBoxes(RTree<Integer> tree)
	{
		for (Field f : tree.getClass().getDeclaredFields()) {
			if (f.getName().equals("root")) {
				f.setAccessible(true);

				try {
					return getBoundingBoxes(new ArrayList<WrappedAABB>(), (RTree.Node<Integer>) f.get(tree), 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private static List<WrappedAABB> getBoundingBoxes(List<WrappedAABB> boxes, RTree.Node<Integer> node, int level)
	{
		int numEntries = 0;
		boolean isLeaf = false;

		AABB[] bounds = null;
		Object[] entries = null;

		for (Field f : node.getClass().getDeclaredFields()) {
			f.setAccessible(true);

			try {
				if (f.getName().equals("numEntries")) {
					numEntries = (Integer) f.get(node);
				} else if (f.getName().equals("isLeaf")) {
					isLeaf = (Boolean) f.get(node);
				} else if (f.getName().equals("entries")) {
					entries = (Object[]) f.get(node);
				} else if (f.getName().equals("bounds")) {
					bounds = (AABB[]) f.get(node);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!isLeaf) {
			for (int i = 0; i < numEntries; i++) {
				boxes.add(new WrappedAABB(bounds[i], level));
				getBoundingBoxes(boxes, (RTree.Node<Integer>) entries[i], level + 1);
			}
		}

		return boxes;
	}

	private static void redraw(Canvas c, RTree<Integer> tree, List<AABB> leaves)
	{
		BufferStrategy bs = c.getBufferStrategy();
		while (bs == null) {
			c.createBufferStrategy(3);
			bs = c.getBufferStrategy();
		}

		Graphics g = bs.getDrawGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, c.getWidth(), c.getHeight());

		g.setColor(Color.white);
		g.fillRect(1, 1, c.getWidth() - 1, c.getHeight() - 1);

		int xAdjust = c == c2 ? c1.getWidth() : 0;

		for (AABB bounds : leaves) {
			g.setColor(Color.black);
			g.drawRect((int) bounds.getMinimum(0) + xAdjust, (int) bounds.getMinimum(1), (int) (bounds.getMaximum(0) - bounds.getMinimum(0)), (int) (bounds.getMaximum(1) - bounds.getMinimum(1)));
		}

		int maxLevel = 0;
		List<WrappedAABB> internal = getBoundingBoxes(tree);

		for (WrappedAABB bounds : internal) {
			maxLevel = Math.max(maxLevel, bounds.level);
		}

		for (WrappedAABB bounds : internal) {
			int margin = 2 * (maxLevel - bounds.level + 1);
			int x = (int) (bounds.aabb.getMinimum(0)) - margin;
			int y = (int) (bounds.aabb.getMinimum(1)) - margin;
			int w = (int) (bounds.aabb.getMaximum(0) - x) + margin;
			int h = (int) (bounds.aabb.getMaximum(1) - y) + margin;

			g.setColor(colors.get(bounds.level % colors.size()));
			g.drawRect(x + xAdjust, y, w, h);
		}

		g.dispose();
		bs.show();
	}
}

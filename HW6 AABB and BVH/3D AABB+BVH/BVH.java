package mchhui.booststructure;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Consumer;

import org.joml.Vector4f;

import mchhui.booststructure.AABB.Axis;

public class BVH {
	public static class Node {
		public AABB aabb;
		public boolean isLeaf = false;
		public ArrayList<AABB> primitiveAABB = new ArrayList<AABB>();
		public Node left;
		public Node right;

		public Node() {
			// TODO Auto-generated constructor stub
		}

		public Node(AABB aabb) {
			this.aabb = aabb;
		}
	}

	public Node root = new Node();

	public int minNode = Integer.MAX_VALUE;
	public int maxNode = 0;
	public int maxDepth = 0;

	public BVH(ArrayList<AABB> source) {
		float minX = Float.MAX_VALUE;
		float minY = Float.MAX_VALUE;
		float minZ = Float.MAX_VALUE;
		float maxX = Float.MIN_VALUE;
		float maxY = Float.MIN_VALUE;
		float maxZ = Float.MIN_VALUE;
		for (AABB aabb : source) {
			minX = Math.min(minX, aabb.minX);
			minY = Math.min(minY, aabb.minY);
			minZ = Math.min(minZ, aabb.minZ);
			maxX = Math.max(maxX, aabb.maxX);
			maxY = Math.max(maxY, aabb.maxY);
			maxZ = Math.max(maxZ, aabb.maxZ);
		}
		root.aabb = new AABB(minX, minY, minZ, maxX, maxY, maxZ);
		split(root, source, 0);
		System.out.println("minNode:" + minNode + ", maxNode:" + maxNode + ", maxDepth:" + maxDepth);
	}

	public static int MAX_SPLIT_DEPTH = 16;
	public static int MIN_SPLIT_PRIMITIVES = 1;
	public static int COLOR_INDEX = 0;

	public void split(Node node, ArrayList<AABB> primitives, int depth) {
		Axis splitAxis = node.aabb.getLongestAxis();
		boolean flag=false;
		int minRemeaning=Integer.MAX_VALUE;
		for (Axis axis : Axis.values()) {
			float mid = node.aabb.getMid(axis);
			boolean flagLeft = false;
			boolean flagRight = false;
			int remeaning = primitives.size();
			int seed = new Random().nextInt(32);
			for (int i = 0; i < primitives.size(); i++) {
				AABB p = primitives.get(i);
				if (primitives.size() > MIN_SPLIT_PRIMITIVES && depth < MAX_SPLIT_DEPTH) {
					if (p.getMin(axis) > mid) {
						flagRight = true;
						remeaning--;
					} else if (p.getMax(axis) < mid) {
						flagLeft = true;
						remeaning--;
					}
				}
				p.color = ColorSet.getColor(seed);
			}
			boolean flag1 = flagLeft && flagRight;
			if(flag1) {
				if(remeaning<minRemeaning) {
					minRemeaning=remeaning;
					flag=true;
					splitAxis=axis;
				}
			}
		}
		COLOR_INDEX++;
		float mid = node.aabb.getMid(splitAxis);
		if (!flag) {
			node.isLeaf = true;
			node.primitiveAABB = primitives;
		} else {
			ArrayList<AABB> leftList = new ArrayList<AABB>();
			ArrayList<AABB> rightList = new ArrayList<AABB>();
			for (int i = 0; i < primitives.size(); i++) {
				AABB p = primitives.get(i);
				if (p.getMin(splitAxis) > mid) {
					rightList.add(p);
					primitives.remove(i);
					i--;
					flag = true;
					p.color = ColorSet.getColor(COLOR_INDEX << 1);
				} else if (p.getMax(splitAxis) < mid) {
					leftList.add(p);
					primitives.remove(i);
					i--;
					flag = true;
					p.color = ColorSet.getColor((COLOR_INDEX << 1) + 1);
				}
			}
			node.primitiveAABB = primitives;
			AABB[] aabbs = node.aabb.split(splitAxis);
			node.left = new Node(aabbs[0]);
			node.right = new Node(aabbs[1]);
			split(node.left, leftList, depth + 1);
			split(node.right, rightList, depth + 1);
		}
		if (node.primitiveAABB.size() > 0) {
			if (node.primitiveAABB.size() > maxNode) {
				maxNode = node.primitiveAABB.size();
			}
			if (node.primitiveAABB.size() < minNode) {
				minNode = node.primitiveAABB.size();
			}
			if (depth > maxDepth) {
				maxDepth = depth;
			}
		}
	}

	public void intersect(Vector4f start, Vector4f direction, ArrayList<AABB> result) {
		intersect(root, start, direction, result);
	}

	public void intersect(Node node, Vector4f start, Vector4f direction, ArrayList<AABB> result) {
		if (node.aabb.intersect(start, direction) == Float.MAX_VALUE) {
			return;
		}
		node.primitiveAABB.forEach(result::add);
		if (!node.isLeaf) {
			intersect(node.left, start, direction, result);
			intersect(node.right, start, direction, result);
		}
	}
}

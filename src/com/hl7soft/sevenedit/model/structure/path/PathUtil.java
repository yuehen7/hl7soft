package com.hl7soft.sevenedit.model.structure.path;

import java.util.ArrayList;
import java.util.List;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class PathUtil {
	public static IndexPath convertToIndexPath(TreePath path) {
		if ((path == null) || (path.getPathCount() == 0)) {
			return null;
		}

		IndexPath indexPath = new IndexPath();
		TreeNode currentNode = (TreeNode) path.getPathComponent(0);
		int i = 1;
		for (int n = path.getPathCount(); i < n; i++) {
			TreeNode tmpNode = (TreeNode) path.getPathComponent(i);
			indexPath.addValue(currentNode.getIndex(tmpNode));
			currentNode = tmpNode;
		}

		return indexPath;
	}

	public static List<IndexPath> convertToIndexPaths(List<TreePath> paths) {
		if (paths == null) {
			return null;
		}

		List res = new ArrayList(paths.size());
		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			res.add(convertToIndexPath((TreePath) paths.get(i)));
		}
		return res;
	}

	public static TreePath convertToTreePath(TreeNode root, IndexPath path) {
		if ((path == null) || (path.getSize() == 0)) {
			return null;
		}

		ArrayList treePath = new ArrayList(path.getSize() + 1);
		treePath.add(root);

		TreeNode currentNode = root;
		int i = 0;
		for (int n = path.getSize(); i < n; i++) {
			int idx = path.getValue(i);
			if ((idx < 0) || (idx >= currentNode.getChildCount())) {
				break;
			}
			currentNode = currentNode.getChildAt(idx);
			treePath.add(currentNode);
		}

		TreeNode[] ary = (TreeNode[]) treePath.toArray(new TreeNode[treePath.size()]);

		return new TreePath(ary);
	}

	public static List<TreePath> convertToTreePaths(TreeNode root, List<IndexPath> paths) {
		if (paths == null) {
			return null;
		}

		List res = new ArrayList(paths.size());
		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			res.add(convertToTreePath(root, (IndexPath) paths.get(i)));
		}
		return res;
	}

	public static List<IndexPath> getDeeperPaths(List<IndexPath> paths, IndexPath pathTemplate) {
		List res = new ArrayList();

		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			IndexPath path = (IndexPath) paths.get(i);
			if (matchDeeper(path, pathTemplate)) {
				res.add(path);
			}
		}
		return res;
	}

	public static IndexPath getExactPath(List<IndexPath> paths, IndexPath pathTemplate) {
		int i = 0;
		for (int n = paths.size(); i < n; i++) {
			IndexPath path = (IndexPath) paths.get(i);
			if (matchExact(path, pathTemplate)) {
				return path;
			}
		}
		return null;
	}

	public static boolean matchDeeper(IndexPath p1, IndexPath p2) {
		if (p1.getSize() <= p2.getSize()) {
			return false;
		}

		int i = 0;
		for (int n = p2.getSize(); i < n; i++) {
			if (p1.getValue(i) != p2.getValue(i)) {
				return false;
			}
		}
		return true;
	}

	public static boolean matchExact(IndexPath p1, IndexPath p2) {
		if (p1.getSize() != p2.getSize()) {
			return false;
		}

		int i = 0;
		for (int n = p2.getSize(); i < n; i++) {
			if (p1.getValue(i) != p2.getValue(i)) {
				return false;
			}
		}
		return true;
	}
}
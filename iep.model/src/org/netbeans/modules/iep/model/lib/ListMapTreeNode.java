/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.iep.model.lib;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * Provides a TreeModel view of the tree whose root is the ListMap, and whose
 * children are values of the ListMap, so on recursively.
 *
 * @author Bing Lu
 *
 * @since May 8, 2002
 */
public class ListMapTreeNode
    implements MutableTreeNode {

    private DefaultMutableTreeNode mDelegate;
    private ListMap mListMap;
    private Object mUserKey;
    private Object mUserObject;

    /**
     * DOCUMENT ME!
     *
     * @param listMap The list map
     * @param userKey The user key
     * @param userObject The user object
     * @param allowsChildren Whether children are allowed
     *
     * @todo Document this constructor
     */
    public ListMapTreeNode(ListMap listMap, Object userKey, Object userObject,
                           boolean allowsChildren) {

        mDelegate = new DefaultMutableTreeNode(this, allowsChildren);
        mListMap = listMap;
        mUserKey = userKey;
        mUserObject = userObject;
    }

    /**
     * Determines whether or not this node is allowed to have children. If
     * <code>allows</code> is false, all of this node's children are removed.
     *
     * <p>
     * Note: By default, a node allows children.
     * </p>
     *
     * @param allows true if this node is allowed to have children
     */
    public void setAllowsChildren(boolean allows) {
        mDelegate.setAllowsChildren(allows);
    }

    /**
     * Returns true if this node is allowed to have children.
     *
     * @return true if this node allows children, else false
     */
    public boolean getAllowsChildren() {
        return mDelegate.getAllowsChildren();
    }

    /**
     * Returns the child in this node's child array that immediately follows
     * <code>aChild</code>, which must be a child of this node. If
     * <code>aChild</code> is the last child, returns null. This method
     * performs a linear search of this node's children for
     * <code>aChild</code> and is O(n) where n is the number of children; to
     * traverse the entire array of children, use an enumeration instead.
     *
     * @param aChild A child node
     *
     * @return the child of this node that immediately follows
     *         <code>aChild</code>
     *
     *
     * @see #children
     */
    public TreeNode getChildAfter(TreeNode aChild) {

        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(aChild instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode c = (ListMapTreeNode) aChild;
        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getChildAfter(c.mDelegate);

        return (n == null)
               ? null
               : (TreeNode) n.getUserObject();
    }

    // *******************TreeNode***************************/

    /**
     * Returns the child at the specified index in this node's child array.
     *
     * @param index an index into this node's child array
     *
     * @return the TreeNode in this node's child array at the specified index
     */
    public TreeNode getChildAt(int index) {

        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getChildAt(index);

        return (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the child in this node's child array that immediately precedes
     * <code>aChild</code>, which must be a child of this node. If
     * <code>aChild</code> is the first child, returns null. This method
     * performs a linear search of this node's children for
     * <code>aChild</code> and is O(n) where n is the number of children.
     *
     * @param aChild A child node
     *
     * @return the child of this node that immediately precedes
     *         <code>aChild</code>
     *
     */
    public TreeNode getChildBefore(TreeNode aChild) {

        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(aChild instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode c = (ListMapTreeNode) aChild;
        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getChildBefore(c.mDelegate);

        return (n == null)
               ? null
               : (TreeNode) n.getUserObject();
    }

    /**
     * Returns the number of children of this node.
     *
     * @return an int giving the number of children of this node
     */
    public int getChildCount() {
        return mDelegate.getChildCount();
    }

    /**
     * Returns the depth of the tree rooted at this node -- the longest
     * distance from this node to a leaf. If this node has no children,
     * returns 0. This operation is much more expensive than
     * <code>getLevel()</code> because it must effectively traverse the entire
     * tree rooted at this node.
     *
     * @return the depth of the tree whose root is this node
     *
     * @see #getLevel
     */
    public int getDepth() {
        return mDelegate.getDepth();
    }

    /**
     * Returns this node's first child. If this node has no children, throws
     * NoSuchElementException.
     *
     * @return the first child of this node
     *
     * @throws NoSuchElementException If there is no such element
     */
    public TreeNode getFirstChild()
        throws NoSuchElementException {

        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getFirstChild();

        return (TreeNode) n.getUserObject();
    }

    // 
    // Leaf Queries
    // 

    /**
     * Finds and returns the first leaf that is a descendant of this node --
     * either this node or its first child's first leaf. Returns this node if
     * it is a leaf.
     *
     * @return the first leaf in the subtree rooted at this node
     *
     * @see #isLeaf
     * @see #isNodeDescendant
     */
    public ListMapTreeNode getFirstLeaf() {

        DefaultMutableTreeNode n = mDelegate.getFirstLeaf();

        return (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the index of the specified child in this node's child array. If
     * the specified node is not a child of this node, returns
     * <code>-1</code>. This method performs a linear search and is O(n) where
     * n is the number of children.
     *
     * @param aChild the TreeNode to search for among this node's children
     *
     * @return an int giving the index of the node in this node's child array,
     *         or <code>-1</code> if the specified node is a not a child of
     *         this node
     *
     */
    public int getIndex(TreeNode aChild) {

        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(aChild instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode c = (ListMapTreeNode) aChild;

        return mDelegate.getIndex(c.mDelegate);
    }

    /**
     * Returns this node's last child. If this node has no children, throws
     * NoSuchElementException.
     *
     * @return the last child of this node
     *
     * @throws NoSuchElementException If there is no such element
     */
    public TreeNode getLastChild()
        throws NoSuchElementException {

        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getLastChild();

        return (TreeNode) n.getUserObject();
    }

    /**
     * Finds and returns the last leaf that is a descendant of this node --
     * either this node or its last child's last leaf. Returns this node if it
     * is a leaf.
     *
     * @return the last leaf in the subtree rooted at this node
     *
     * @see #isLeaf
     * @see #isNodeDescendant
     */
    public ListMapTreeNode getLastLeaf() {

        DefaultMutableTreeNode n = mDelegate.getLastLeaf();

        return (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns true if this node has no children. To distinguish between nodes
     * that have no children and nodes that <i>cannot</i> have children (e.g.
     * to distinguish files from empty directories), use this method in
     * conjunction with <code>getAllowsChildren</code>
     *
     * @return true if this node has no children
     *
     * @see #getAllowsChildren
     */
    public boolean isLeaf() {
        return mDelegate.isLeaf();
    }

    /**
     * Returns the total number of leaves that are descendants of this node. If
     * this node is a leaf, returns <code>1</code>. This method is O(n) where
     * n is the number of descendants of this node.
     *
     * @return the number of leaves beneath this node
     *
     * @see #isNodeAncestor
     */
    public int getLeafCount() {
        return mDelegate.getLeafCount();
    }

    /**
     * Returns the number of levels above this node -- the distance from the
     * root to this node. If this node is the root, returns 0.
     *
     * @return the number of levels above this node
     *
     * @see #getDepth
     */
    public int getLevel() {
        return mDelegate.getLevel();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @todo Document: Getter for ListMap attribute of the ListMapTreeNode
     *       object
     */
    public ListMap getListMap() {
        return mListMap;
    }

    /**
     * Returns the leaf after this node or null if this node is the last leaf
     * in the tree.
     *
     * <p>
     * In this implementation of the <code>MutableNode</code> interface, this
     * operation is very inefficient. In order to determine the next node,
     * this method first performs a linear search in the parent's child-list
     * in order to find the current node.
     * </p>
     *
     * <p>
     * That implementation makes the operation suitable for short traversals
     * from a known position. But to traverse all of the leaves in the tree,
     * you should use <code>depthFirstEnumeration</code> to enumerate the
     * nodes in the tree and use <code>isLeaf</code> on each node to determine
     * which are leaves.
     * </p>
     *
     * @return returns the next leaf past this node
     *
     * @see #depthFirstEnumeration
     * @see #isLeaf
     */
    public ListMapTreeNode getNextLeaf() {

        DefaultMutableTreeNode n = mDelegate.getNextLeaf();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the node that follows this node in a preorder traversal of this
     * node's tree. Returns null if this node is the last node of the
     * traversal. This is an inefficient way to traverse the entire tree; use
     * an enumeration, instead.
     *
     * @return the node that follows this node in a preorder traversal, or null
     *         if this node is last
     *
     * @see #preorderEnumeration
     */
    public ListMapTreeNode getNextNode() {

        DefaultMutableTreeNode n = mDelegate.getNextNode();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the next sibling of this node in the parent's children array.
     * Returns null if this node has no parent or is the parent's last child.
     * This method performs a linear search that is O(n) where n is the number
     * of children; to traverse the entire array, use the parent's child
     * enumeration instead.
     *
     * @return the sibling of this node that immediately follows this node
     *
     * @see #children
     */
    public ListMapTreeNode getNextSibling() {

        DefaultMutableTreeNode n = mDelegate.getNextSibling();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    // 
    // Tree Queries
    // 

    /**
     * Returns true if <code>anotherNode</code> is an ancestor of this node --
     * if it is this node, this node's parent, or an ancestor of this node's
     * parent. (Note that a node is considered an ancestor of itself.) If
     * <code>anotherNode</code> is null, this method returns false. This
     * operation is at worst O(h) where h is the distance from the root to
     * this node.
     *
     * @param anotherNode node to test as an ancestor of this node
     *
     * @return true if this node is a descendant of <code>anotherNode</code>
     *
     *
     * @see #isNodeDescendant
     * @see #getSharedAncestor
     */
    public boolean isNodeAncestor(TreeNode anotherNode) {

        if (anotherNode == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(anotherNode instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode n = (ListMapTreeNode) anotherNode;

        return mDelegate.isNodeAncestor(n.mDelegate);
    }

    // 
    // Child Queries
    // 

    /**
     * Returns true if <code>aNode</code> is a child of this node. If
     * <code>aNode</code> is null, this method returns false.
     *
     * @param aNode The node to test.
     *
     * @return true if <code>aNode</code> is a child of this node; false if
     *         <code>aNode</code> is null
     */
    public boolean isNodeChild(TreeNode aNode) {

        if (aNode == null) {
            return false;
        }

        if (!(aNode instanceof ListMapTreeNode)) {
            return false;
        }

        ListMapTreeNode n = (ListMapTreeNode) aNode;

        return mDelegate.isNodeChild(n.mDelegate);
    }

    /**
     * Returns true if <code>anotherNode</code> is a descendant of this node --
     * if it is this node, one of this node's children, or a descendant of one
     * of this node's children. Note that a node is considered a descendant of
     * itself. If <code>anotherNode</code> is null, returns false. This
     * operation is at worst O(h) where h is the distance from the root to
     * <code>anotherNode</code>.
     *
     * @param anotherNode node to test as descendant of this node
     *
     * @return true if this node is an ancestor of <code>anotherNode</code>
     *
     * @see #isNodeAncestor
     * @see #getSharedAncestor
     */
    public boolean isNodeDescendant(ListMapTreeNode anotherNode) {

        if (anotherNode == null) {
            return false;
        }

        return mDelegate.isNodeDescendant(anotherNode.mDelegate);
    }

    /**
     * Returns true if and only if <code>aNode</code> is in the same tree as
     * this node. Returns false if <code>aNode</code> is null.
     *
     * @param aNode The node to test.
     *
     * @return true if <code>aNode</code> is in the same tree as this node;
     *         false if <code>aNode</code> is null
     *
     * @see #getSharedAncestor
     * @see #getRoot
     */
    public boolean isNodeRelated(ListMapTreeNode aNode) {

        if (aNode == null) {
            return false;
        }

        return mDelegate.isNodeRelated(aNode.mDelegate);
    }

    // 
    // Sibling Queries
    // 

    /**
     * Returns true if <code>anotherNode</code> is a sibling of (has the same
     * parent as) this node. A node is its own sibling. If
     * <code>anotherNode</code> is null, returns false.
     *
     * @param anotherNode node to test as sibling of this node
     *
     * @return true if <code>anotherNode</code> is a sibling of this node
     */
    public boolean isNodeSibling(TreeNode anotherNode) {

        if (anotherNode == null) {
            return false;
        }

        if (!(anotherNode instanceof ListMapTreeNode)) {
            return false;
        }

        ListMapTreeNode c = (ListMapTreeNode) anotherNode;

        return mDelegate.isNodeSibling(c.mDelegate);
    }

    /**
     * Sets this node's parent to <code>newParent</code> but does not change
     * the parent's child array. This method is called from
     * <code>insert()</code> and <code>remove()</code> to reassign a child's
     * parent, it should not be messaged from anywhere else.
     *
     * @param newParent this node's new parent
     *
     */
    public void setParent(MutableTreeNode newParent) {

        if (!(newParent instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not a ListMapTreeNode");
        }

        ListMapTreeNode p = (ListMapTreeNode) newParent;

        mDelegate.setParent(p.mDelegate);
    }

    /**
     * Returns this node's parent or null if this node has no parent.
     *
     * @return this node's parent TreeNode, or null if this node has no parent
     */
    public TreeNode getParent() {

        DefaultMutableTreeNode n =
            (DefaultMutableTreeNode) mDelegate.getParent();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the path from the root, to get to this node. The last element in
     * the path is this node.
     *
     * @return an array of TreeNode objects giving the path, where the first
     *         element in the path is the root and the last element is this
     *         node.
     */
    public TreeNode[] getPath() {

        TreeNode[] p = mDelegate.getPath();
        TreeNode[] path = new TreeNode[p.length];

        for (int i = 0, size = p.length; i < size; i++) {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) p[i];

            path[i] = (ListMapTreeNode) n.getUserObject();
        }

        return path;
    }

    /**
     * Returns the leaf before this node or null if this node is the first leaf
     * in the tree.
     *
     * <p>
     * In this implementation of the <code>MutableNode</code> interface, this
     * operation is very inefficient. In order to determine the previous node,
     * this method first performs a linear search in the parent's child-list
     * in order to find the current node.
     * </p>
     *
     * <p>
     * That implementation makes the operation suitable for short traversals
     * from a known position. But to traverse all of the leaves in the tree,
     * you should use <code>depthFirstEnumeration</code> to enumerate the
     * nodes in the tree and use <code>isLeaf</code> on each node to determine
     * which are leaves.
     * </p>
     *
     * @return returns the leaf before this node
     *
     * @see #depthFirstEnumeration
     * @see #isLeaf
     */
    public ListMapTreeNode getPreviousLeaf() {

        DefaultMutableTreeNode n = mDelegate.getPreviousLeaf();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the node that precedes this node in a preorder traversal of this
     * node's tree. Returns <code>null</code> if this node is the first node
     * of the traversal -- the root of the tree. This is an inefficient way to
     * traverse the entire tree; use an enumeration, instead.
     *
     * @return the node that precedes this node in a preorder traversal, or
     *         null if this node is the first
     *
     * @see #preorderEnumeration
     */
    public ListMapTreeNode getPreviousNode() {

        DefaultMutableTreeNode n = mDelegate.getPreviousNode();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the previous sibling of this node in the parent's children
     * array. Returns null if this node has no parent or is the parent's first
     * child. This method performs a linear search that is O(n) where n is the
     * number of children.
     *
     * @return the sibling of this node that immediately precedes this node
     */
    public ListMapTreeNode getPreviousSibling() {

        DefaultMutableTreeNode n = mDelegate.getPreviousSibling();

        return (n == null)
               ? null
               : (ListMapTreeNode) n.getUserObject();
    }

    /**
     * Returns the root of the tree that contains this node. The root is the
     * ancestor with a null parent.
     *
     * @return the root of the tree that contains this node
     *
     * @see #isNodeAncestor
     */
    public TreeNode getRoot() {

        DefaultMutableTreeNode r = (DefaultMutableTreeNode) mDelegate.getRoot();

        return (TreeNode) r.getUserObject();
    }

    /**
     * Returns true if this node is the root of the tree. The root is the only
     * node in the tree with a null parent; every tree has exactly one root.
     *
     * @return true if this node is the root of its tree
     */
    public boolean isRoot() {

        return mDelegate.isRoot();

        // getParent() == null;
    }

    /**
     * Returns the nearest common ancestor to this node and <code>aNode</code>.
     * Returns null, if no such ancestor exists -- if this node and
     * <code>aNode</code> are in different trees or if <code>aNode</code> is
     * null. A node is considered an ancestor of itself.
     *
     * @param aNode node to find common ancestor with
     *
     * @return nearest ancestor common to this node and <code>aNode</code>, or
     *         null if none
     *
     * @see #isNodeAncestor
     * @see #isNodeDescendant
     */
    public TreeNode getSharedAncestor(ListMapTreeNode aNode) {

        if (aNode == null) {
            return null;
        }

        return mDelegate.getSharedAncestor(aNode.mDelegate);
    }

    /**
     * Returns the number of siblings of this node. A node is its own sibling
     * (if it has no parent or no siblings, this method returns
     * <code>1</code>).
     *
     * @return the number of siblings of this node
     */
    public int getSiblingCount() {
        return mDelegate.getSiblingCount();
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @todo Document: Getter for UserKey attribute of the ListMapTreeNode
     *       object
     */
    public Object getUserKey() {
        return mUserKey;
    }

    /**
     * Does nothing. User object cannot be changed
     *
     * @param object user object
     */
    public void setUserObject(Object object) {
    }

    /**
     * Returns the user object of the receiver.
     *
     * @return DOCUMENT ME!
     */
    public Object getUserObject() {
        return mUserObject;
    }

    /**
     * Returns the user object path, from the root, to get to this node. If
     * some of the TreeNodes in the path have null user objects, the returned
     * path will contain nulls.
     *
     * @return DOCUMENT ME!
     */
    public Object[] getUserObjectPath() {

        TreeNode[] realPath = getPath();
        Object[] retPath = new Object[realPath.length];

        for (int i = 0, size = realPath.length; i < size; i++) {
            retPath[i] = ((ListMapTreeNode) realPath[i]).mUserObject;
        }

        return retPath;
    }

    // ****************************Similar to DefaultMutableTreeNode********************

    /**
     * Removes <code>newChild</code> from its parent and makes it a child of
     * this node by adding it to the end of this node's child array.
     *
     * @param newChild node to add as a child of this node
     *
     *
     * @see #insert
     */
    public void add(MutableTreeNode newChild) {

        if (newChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(newChild instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not a ListMapTreeNode");
        }

        ListMapTreeNode c = (ListMapTreeNode) newChild;

        if (isNodeAncestor(c)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }

        if (mListMap.containsKey(c.getUserKey())) {
            int index = mListMap.getKeyList().indexOf(c.getUserKey());

            mDelegate.remove(index);
            mListMap.remove(index);
        }

        ListMapTreeNode cp = (ListMapTreeNode) c.getParent();

        if (cp != null) {
            cp.mListMap.remove(c.getUserKey());
        }

        mDelegate.add(c.mDelegate);
        mListMap.put(c.getUserKey(), c.getUserObject());
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in breadth-first order. The first node returned by the
     * enumeration's <code>nextElement()</code> method is this node.
     *
     * <P>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * </p>
     *
     * @return an enumeration for traversing the tree in breadth-first order
     *
     * @see #depthFirstEnumeration
     */
    public Enumeration breadthFirstEnumeration() {

        Vector v = new Vector();

        for (Enumeration e = mDelegate.breadthFirstEnumeration();
                e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    /**
     * Creates and returns a forward-order enumeration of this node's children.
     * Modifying this node's child array invalidates any child enumerations
     * created before the modification.
     *
     * @return an Enumeration of this node's children
     */
    public Enumeration children() {

        Vector v = new Vector();

        for (Enumeration e = mDelegate.children(); e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in depth-first order. The first node returned by the
     * enumeration's <code>nextElement()</code> method is the leftmost leaf.
     * This is the same as a postorder traversal.
     *
     * <P>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * </p>
     *
     * @return an enumeration for traversing the tree in depth-first order
     *
     * @see #breadthFirstEnumeration
     * @see #postorderEnumeration
     */
    public Enumeration depthFirstEnumeration() {

        Vector v = new Vector();

        for (Enumeration e = mDelegate.depthFirstEnumeration();
                e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    // *******************MutableTreeNode***************************/

    /**
     * Removes <code>newChild</code> from its present parent (if it has a
     * parent), sets the child's parent to this node, and then adds the child
     * to this node's child array at index <code>childIndex</code>.
     * <code>newChild</code> must not be null and must not be an ancestor of
     * this node.
     *
     * @param child
     * @param index
     *
     *
     * @see #isNodeDescendant
     */
    public void insert(MutableTreeNode child, int index) {

        if (child == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(child instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not a ListMapTreeNode");
        }

        if (index > mListMap.size()) {
            throw new ArrayIndexOutOfBoundsException(index);
        }

        ListMapTreeNode c = (ListMapTreeNode) child;

        if (isNodeAncestor(c)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }

        boolean removed = false;
        boolean append = (index == mListMap.size());

        if (mListMap.containsKey(c.getUserKey())) {
            int i = mListMap.getKeyList().indexOf(c.getUserKey());

            mDelegate.remove(i);
            mListMap.remove(i);

            removed = true;
        }

        ListMapTreeNode cp = (ListMapTreeNode) c.getParent();

        if (cp != null) {
            cp.mListMap.remove(c.getUserKey());
        }

        if (removed && append) {
            mDelegate.add(c.mDelegate);
            mListMap.put(c.getUserKey(), c.getUserObject());
        } else {
            mDelegate.insert(c.mDelegate, index);
            mListMap.put(index, c.getUserKey(), c.getUserObject());
        }
    }

    /**
     * Creates and returns an enumeration that follows the path from
     * <code>ancestor</code> to this node. The enumeration's
     * <code>nextElement()</code> method first returns <code>ancestor</code>,
     * then the child of <code>ancestor</code> that is an ancestor of this
     * node, and so on, and finally returns this node. Creation of the
     * enumeration is O(m) where m is the number of nodes between this node
     * and <code>ancestor</code>, inclusive. Each <code>nextElement()</code>
     * message is O(1).
     *
     * <P>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * </p>
     *
     * @param ancestor Ancestor of our node
     *
     * @return an enumeration for following the path from an ancestor of this
     *         node to this one
     *
     *
     * @see #isNodeAncestor
     * @see #isNodeDescendant
     */
    public Enumeration pathFromAncestorEnumeration(TreeNode ancestor) {

        if (ancestor == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(ancestor instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode n = (ListMapTreeNode) ancestor;
        Vector v = new Vector();

        for (Enumeration e = mDelegate.pathFromAncestorEnumeration(n.mDelegate);
                e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in postorder. The first node returned by the enumeration's
     * <code>nextElement()</code> method is the leftmost leaf. This is the
     * same as a depth-first traversal.
     *
     * <P>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * </p>
     *
     * @return an enumeration for traversing the tree in postorder
     *
     * @see #depthFirstEnumeration
     * @see #preorderEnumeration
     */
    public Enumeration postorderEnumeration() {

        Vector v = new Vector();

        for (Enumeration e = mDelegate.postorderEnumeration();
                e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in preorder. The first node returned by the enumeration's
     * <code>nextElement()</code> method is this node.
     *
     * <P>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     * </p>
     *
     * @return an enumeration for traversing the tree in preorder
     *
     * @see #postorderEnumeration
     */
    public Enumeration preorderEnumeration() {

        Vector v = new Vector();

        for (Enumeration e = mDelegate.preorderEnumeration();
                e.hasMoreElements();) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) e.nextElement();

            v.add(c.getUserObject());
        }

        return v.elements();
    }

    /**
     * Removes the child at the specified index from this node's children and
     * sets that node's parent to null. The child node to remove must be a
     * <code>MutableTreeNode</code>.
     *
     * @param index The index
     */
    public void remove(int index) {
        mDelegate.remove(index);
        mListMap.remove(index);
    }

    /**
     * Removes <code>aChild</code> from this node's child array, giving it a
     * null parent.
     *
     * @param node The node to remove.
     *
     */
    public void remove(MutableTreeNode node) {

        if (node == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!(node instanceof ListMapTreeNode)) {
            throw new IllegalArgumentException(
                "argument is not ListMapTreeNode");
        }

        ListMapTreeNode c = (ListMapTreeNode) node;

        if (mListMap.containsKey(c.getUserKey())) {
            int index = mListMap.getKeyList().indexOf(c.getUserKey());

            mDelegate.remove(index);
            mListMap.remove(index);
        } else {
            throw new IllegalArgumentException("argument is not a child");
        }
    }

    /**
     * Removes all of this node's children, setting their parents to null. If
     * this node has no children, this method does nothing.
     */
    public void removeAllChildren() {
        mDelegate.removeAllChildren();
        mListMap.clear();
    }

    /**
     * Removes the subtree rooted at this node from the tree, giving this node
     * a null parent. Does nothing if this node is the root of its tree.
     */
    public void removeFromParent() {

        ListMapTreeNode parent = (ListMapTreeNode) getParent();

        mDelegate.removeFromParent();

        if (parent.mListMap.containsKey(getUserKey())) {
            parent.mListMap.remove(getUserKey());
        }
    }

    // 
    // Overrides
    // 

    /**
     * Returns the result of sending <code>toString()</code> to this node's
     * user object, or null if this node has no user object.
     *
     * @see #getUserObject
     *
     * @return
     */
    public String toString() {

        if (mUserObject == null) {
            return null;
        } else {
            return mUserObject.toString();

            // + ":" + mListMap;
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

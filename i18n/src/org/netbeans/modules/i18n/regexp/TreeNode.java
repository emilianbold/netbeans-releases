/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.i18n.regexp;

/**
 * Node of a regular expression syntax tree.
 * This node and its subnodes represent a subexpression of a regular expression.
 *
 * @author  Marian Petras
 */
class TreeNode {

    static final int REGEXP             =  0;
    static final int MULTI_REGEXP       =  1;
    static final int SIMPLE_REGEXP      =  2;
    static final int Q_REGEXP           =  3;
    static final int QUANTIFIER         =  4;
    static final int NUMBER             =  5;
    static final int METACHAR           =  6;
    static final int UNICODE_CHAR       =  7;
    static final int CHAR               =  8;
    static final int SUBEXPR            =  9;
    static final int POSIX_SET          = 10;
    static final int SET                = 11;
    static final int RANGE              = 12;
    static final int TOKEN              = 13;

    /**
     * index of the first character of a subexpression represented by this node
     */
    int start;
    /**
     * index of the last character of a subexpression represented by this node
     */
    int end;
    /** type of a subexpression this node represents */
    private int tokenType;
    /** attributes of this node */
    private Object attribs;
    /** this node's parent node */
    private TreeNode parent;
    /** direct subnodes of this node */
    private java.util.List children;

    /**
     * Creates a new node representing a given part of a regular expression.
     *
     * @param  tokenType  type of a subexpression this node represents
     * @param  start  index of the first character of a subexpression
     *                represented by this node
     * @param  end  index of the last character of a subexpression
     *              represented by this node
     */
    TreeNode(int tokenType, int start, int end) {
        this.tokenType = tokenType;
        this.start = start;
        this.end = end;
    }

    /**
     * Creates a new node representing a given part of a regular expression.
     *
     * @param  tokenType  type of a subexpression this node represents
     * @param  start  index of the first character of a subexpression
     *                represented by this node
     * @param  end  index of the last character of a subexpression
     *              represented by this node
     * @param  attribs  attributes of this node
     */
    TreeNode(int tokenType, int start, int end, Object attribs) {
        this(tokenType, start, end);
        this.attribs = attribs;
    }

    /**
     * Adds a subnode to this node.
     *
     * @param  child  subnode to be added
     */
    void add(TreeNode child) {
        if (child == null) {
            throw new IllegalArgumentException("null");                 //NOI18N
        }

        child.parent = this;

        if (children == null) {
            children = new java.util.ArrayList(4);
        }
        children.add(child);
    }

    /**
     * Returns a regular expression represented by the root node of the whole
     * tree.
     *
     * @return  regular expression represented by the whole tree this node
     *          is part of
     */
    String getRegexp() {

        TreeNode candidate = this;
        TreeNode candidParent;

        /* Find the root: */
        while ((candidParent = candidate.parent) != null) {
            candidate = candidParent;
        }
        assert candidate instanceof TreeNodeRoot;

        return candidate.getRegexp();
    }

    /**
     * Returns the type of regular expression represented by this node.
     *
     * @return  type of regular expression represented by this node's subtree
     */
    int getTokenType() {
        return tokenType;
    }

    /**
     * Returns this node's attributes.
     *
     * @return  attributes of this node
     * @see  #TreeNode(int, int, int, Object)
     */
    Object getAttribs() {
        return attribs;
    }

    /**
     * Returns this node's children.
     *
     * @return  list of this node's direct subnodes;
     *          or <code>null</code> if this node has no subnodes
     */
    java.util.List getChildren() {
        return children != null ? new java.util.ArrayList(children)
                                : null;
    }

}

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
 * Root node of a regular expression syntax tree.
 *
 * @author  Marian Petras
 */
class TreeNodeRoot extends TreeNode {

    /** regular expression represented by the tree */
    private String regexp;

    /**
     * Creates a new tree node representing a given regular expression.
     *
     * @param  regexp  regular expression to be represented by this node
     */
    TreeNodeRoot(String regexp) {
        super(TreeNode.REGEXP, 0, regexp.length());
        this.regexp = regexp;
    }

    /**
     * Creates a new tree node representing a given regular expression.
     *
     * @param  regexp  regular expression to be represented by this node
     * @param  attribs  attributes of this node
     */
    TreeNodeRoot(String regexp, Object attribs) {
        super(TreeNode.REGEXP, 0, regexp.length(), attribs);
        this.regexp = regexp;
    }

    /**
     * Returns a regular expression represented by the tree
     *
     * @return  regular expression represented by this node and its subnodes
     */
    final String getRegexp() {
        return regexp;
    }

}

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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

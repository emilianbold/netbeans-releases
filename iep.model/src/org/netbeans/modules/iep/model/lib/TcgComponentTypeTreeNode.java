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

import javax.swing.tree.MutableTreeNode;


/**
 * This class provides a tree node for PdsComponenTypes and PdsComponenType.
 *
 * @author Bing Lu
 *
 * @since May 31, 2002
 */
class TcgComponentTypeTreeNode
    extends ListMapTreeNode {

    /**
     * Constructor
     *
     * @param listMap
     * @param userKey
     * @param userObject
     * @param allowsChildren
     *
     * @see org.netbeans.modules.iep.editor.tcg.util.ListMapTreeNode
     */
    public TcgComponentTypeTreeNode(ListMap listMap, Object userKey,
                                 Object userObject, boolean allowsChildren) {
        super(listMap, userKey, userObject, allowsChildren);
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being set
     */

    /**
     * DOCUMENT ME!
     *
     * @param newParent parent
     *
     * @todo Document: Setter for Parent attribute of the TcgComponentTypeTreeNode
     *       object
     */
    public void setParent(MutableTreeNode newParent) {

        if (newParent instanceof TcgComponentTypeTreeNode) {
            super.setParent(newParent);
        }
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
     */

    /**
     * DOCUMENT ME!
     *
     * @param child
     *
     * @todo Document this method
     */
    public void add(MutableTreeNode child) {

        if (child instanceof TcgComponentTypeTreeNode) {
            super.add((TcgComponentTypeTreeNode) child);
        }
    }

    /*
     *  Override ListMapTreeNode's method to prevent objects of wrong type from being added
     */

    /**
     * DOCUMENT ME!
     *
     * @param child
     * @param index
     *
     * @todo Document this method
     */
    public void insert(MutableTreeNode child, int index) {

        if (child instanceof TcgComponentTypeTreeNode) {
            super.insert(child, index);
        }
    }
}


/*--- Formatted in SeeBeyond Java Convention Style on Thu, Dec 5, '02 ---*/


/*------ Formatted by Jindent 3.24 Gold 1.02 --- http://www.jindent.de ------*/

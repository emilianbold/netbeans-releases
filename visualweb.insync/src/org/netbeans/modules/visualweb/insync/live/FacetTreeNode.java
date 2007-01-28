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

package org.netbeans.modules.visualweb.insync.live;

import com.sun.rave.designtime.DesignBean;
import java.util.Enumeration;
import javax.swing.tree.TreeNode;

import com.sun.rave.designtime.faces.FacetDescriptor;

/**
 * FacetTreeNode - represents facets in a Tree such as the app outline
 *
 * @author Tor Norbye
 * @version 1.0
 */
public class FacetTreeNode implements TreeNode {

    private FacetDescriptor fd;
    private SourceDesignBean parent;
    private DesignBean bean;
    private int index;

    FacetTreeNode(SourceDesignBean parent, FacetDescriptor fd, int index) {
        this.parent = parent;
        this.fd = fd;
        this.index = index;
        if (parent instanceof FacesDesignBean) {
            FacesDesignBean fdb = (FacesDesignBean)parent;
            bean = fdb.getFacet(fd.getName());
        }
    }

    /** Return the name of this facet */
    public String getName() {
        return fd.getName();
    }

    int getIndex() {
        return index;
    }

    /** Return the DesignBean associated with this FacetDescriptor, if any.
     * Returns null if there is no current DesignBean for the facet.
     */
    public DesignBean getDesignBean() {
        return bean;
    }

    public TreeNode getChildAt(int childIndex) {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).getChildAt(childIndex);
        }
        return null;
    }

    public int getChildCount() {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).getChildCount();
        }
        return 0;
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).getIndex(node);
        }
        return -1;
    }

    public boolean getAllowsChildren() {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).getAllowsChildren();
        }
        return false;
    }

    public boolean isLeaf() {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).isLeaf();
        }
        return true;
    }

    // Note: using old Enumeration only for old NetBeans code
    public Enumeration children() {
        if (bean != null && bean instanceof TreeNode) {
            return ((TreeNode)bean).children();
        }
        return null;
    }

    public String toString() {
        if (bean != null) {
            return fd.getName() + ": " + bean.getInstanceName();
        }
        return fd.getName();
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

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
package org.netbeans.modules.visualweb.propertyeditors.binding;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.propertyeditors.binding.nodes.PropertyTargetNode;
import org.netbeans.modules.visualweb.propertyeditors.util.Bundle;

public abstract class BindingTargetNode implements TreeNode {

    private static final Bundle bundle = Bundle.getBundle(BindingTargetNode.class);

    //------------------------------------------------------------------------------- STATIC METHODS

    private static List factoryList = new ArrayList();
    public static void _registerBindingTargetNodeFactory(BindingTargetNodeFactory btnf) {
        factoryList.add(0, btnf);
    }

    public static BindingTargetNode _createTargetNode(
        BindingTargetNode parent, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {

        if (bean == null) return null;
        Class targetClass = propInstance != null ? propInstance.getClass() : null;
        if (targetClass == null && propPath != null && propPath.length > 0) {
            Object o = PropertyBindingHelper.getPropInstance(bean, propPath);
            if (o != null) {
                propInstance = o;
                targetClass = o.getClass();
            }
            else {
                targetClass = propPath[propPath.length - 1].getPropertyType();
            }
        }
        if (targetClass == null) {
            Object o = bean.getInstance();
            if (o != null) {
                propInstance = o;
                targetClass = o.getClass();
            }
            else {
                targetClass = bean.getBeanInfo().getBeanDescriptor().getBeanClass();
            }
        }
        for (int i = 0; i < factoryList.size(); i++) {
            BindingTargetNodeFactory tnf = (BindingTargetNodeFactory)factoryList.get(i);
            if (tnf.supportsTargetClass(targetClass)) {
                BindingTargetNode btn = tnf.createTargetNode(parent, bean, propPath, propInstance);
                if (btn != null) {
                    return btn;
                }
            }
        }
        return new PropertyTargetNode(parent, bean, propPath, propInstance);
    }

    //----------------------------------------------------------------------------- abstract methods

    public abstract boolean lazyLoad();
    public abstract boolean isValidBindingTarget();
    public abstract String getBindingExpressionPart();
    public abstract Class getTargetTypeClass();
    public abstract String getDisplayText(boolean enabled);

    public BindingTargetNode(BindingTargetNode parent) {
        this.parent = parent;
    }

    protected HashMap userDataHash = new HashMap();
    public Map getUserDataMap() {
        return userDataHash;
    }

    public boolean hasDisplayIcon() {
        return false;
    }
    public Icon getDisplayIcon(boolean enabled) {
        // subclasses can put their custom icon here - only called if hasCustomDisplayIcon() returns true
        return null;
    }
    public JComponent getCustomDisplayPanel(ActionListener updateCallback) {
        return null;
    }
    public String getTargetTypeDisplayName() {
        Class c = getTargetTypeClass();
        if (c != null) {
            return PropertyBindingHelper.getPrettyTypeName(c.getName());
        }
        return "";  //NOI18N
    }

    protected BindingTargetNode parent;
    protected Vector children = new Vector();
    protected boolean loaded = false;

    //------------------------------------------------------------------------ Tree Building Methods

    public DefaultTreeModel getTreeModel() {
        TreeNode p = getParent();
        while (p != null) {
            if (p instanceof BindingTargetNode.Root) {
                return ((BindingTargetNode.Root)p).getTreeModel();
            }
            p = p.getParent();
        }
        return null;
    }

    public void add(BindingTargetNode child) {
        if (child instanceof BindingTargetNode) {
            children.add(child);
            child.setParent(this);
        }
    }

    public void add(int index, BindingTargetNode child) {
        if (child instanceof BindingTargetNode) {
            children.add(index, child);
            child.setParent(this);
        }
    }

    public void remove(BindingTargetNode child) {
        children.remove(child);
    }

    public void removeAll() {
        children.clear();
        loaded = false;
    }

    public void setParent(BindingTargetNode parent) {
        if (parent instanceof BindingTargetNode) {
            this.parent = parent;
        }
    }

    //------------------------------------------------------------------------------------- TreeNode

    public TreeNode getChildAt(int childIndex) {
        if (!loaded) {
            loaded = lazyLoad();
        }
        if (children.size() > childIndex) {
            return (TreeNode)children.get(childIndex);
        }
        return null;
    }

    public int getChildCount() {
        if (!loaded) {
            loaded = lazyLoad();
        }
        return children.size();
    }

    public TreeNode getParent() {
        return parent;
    }

    public int getIndex(TreeNode node) {
        if (!loaded) {
            loaded = lazyLoad();
        }
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        if (!loaded) {
            loaded = lazyLoad();
        }
        return children.size() == 0;
    }

    public Enumeration children() {
        if (!loaded) {
            loaded = lazyLoad();
        }
        return children.elements();
    }

    //----------------------------------------------------------------------- BindingTargetNode.Root

    public static class Root extends BindingTargetNode {
        public Root() {
            super(null);
        }
        protected DefaultTreeModel treeModel;
        protected void setTreeModel(DefaultTreeModel treeModel) {
            this.treeModel = treeModel;
        }
        public DefaultTreeModel getTreeModel() {
            return treeModel;
        }
        public boolean lazyLoad() { return true; }
        public String getDisplayText(boolean enableNode) { return null; }
        public boolean isValidBindingTarget() { return false; }
        public String getBindingExpressionPart() { return null; }
        public Class getTargetTypeClass() { return null; }
    }

    //----------------------------------------------------------------------- BindingTargetNode.Null

    public static class Null extends BindingTargetNode {
        public Null(BindingTargetNode parent) {
            super(parent);
            this.displayText = "<html><b>" + bundle.getMessage("propertyNotBound") + "</b></html>";  //NOI18N
        }
        public boolean lazyLoad() {
            return true;
        }
        protected String displayText;
        public String getDisplayText(boolean enableNode) {
            return displayText;
        }
        public boolean isValidBindingTarget() {
            return true;
        }
        public String getBindingExpressionPart() {
            return null;
        }
        public Class getTargetTypeClass() {
            return null;
        }
    }
}

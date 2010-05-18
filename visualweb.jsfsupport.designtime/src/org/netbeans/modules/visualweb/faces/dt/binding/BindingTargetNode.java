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
package org.netbeans.modules.visualweb.faces.dt.binding;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.List;
import javax.faces.context.*;
import javax.faces.el.*;
import javax.swing.*;
import javax.swing.tree.*;
import com.sun.rave.designtime.*;
import com.sun.rave.designtime.faces.*;
import com.sun.rave.designtime.markup.*;
import org.netbeans.modules.visualweb.faces.dt.util.ComponentBundle;

public abstract class BindingTargetNode implements TreeNode {

    private static final ComponentBundle bundle = ComponentBundle.getBundle(BindingTargetNode.class);

    //------------------------------------------------------------------------------- STATIC METHODS

    private static List factoryList = new ArrayList();
    public static void _registerTargetNodeFactory(TargetNodeFactory tnf) {
        factoryList.add(0, tnf);
    }

    public static BindingTargetNode _createTargetNode(
        DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {

        if (bean == null) return null;
        Class targetClass = propInstance != null ? propInstance.getClass() : null;
        if (targetClass == null && propPath != null && propPath.length > 0) {
            Object o = getPropInstance(bean, propPath);
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
            TargetNodeFactory tnf = (TargetNodeFactory)factoryList.get(i);
            if (tnf.supportsTargetClass(targetClass)) {
                BindingTargetNode btn = tnf.createTargetNode(treeModel, bean, propPath, propInstance);
                if (btn != null) {
                    return btn;
                }
            }
        }
        return new PropertyTargetNode(treeModel, bean, propPath, propInstance);
    }

    //---------------------------------------------------------------------------- BindingTargetNode

    //----------------------------------------------------------------------------- abstract methods

    public abstract boolean lazyLoad();
    public abstract boolean isValidBindingTarget();
    public abstract String getBindingExpressionPart();
    public abstract Class getTargetTypeClass();
    public abstract String getDisplayText(boolean enabled);

    public BindingTargetNode(DefaultTreeModel treeModel) {
        this.treeModel = treeModel;
    }

    protected DefaultTreeModel treeModel;
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
            return decodeTypeName(c.getName());
        }
        return "";  //NOI18N
    }

    protected TreeNode parent;
    protected Vector children = new Vector();
    protected boolean loaded = false;

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

    public void setParent(TreeNode parent) {
        this.parent = parent;
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

    //------------------------------------------------------------------- BindingTargetNode.RootNode

    public static class RootTargetNode extends BindingTargetNode {
        public RootTargetNode() { super(null); }
        public boolean lazyLoad() { return true; }
        public String getDisplayText(boolean enableNode) { return null; }
        public boolean isValidBindingTarget() { return false; }
        public String getBindingExpressionPart() { return null; }
        public Class getTargetTypeClass() { return null; }
    }

    //------------------------------------------------------------- BindingTargetNode.NullTargetNode

    public static class NullTargetNode extends BindingTargetNode {
        public NullTargetNode(DefaultTreeModel treeModel) {
            super(treeModel);
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

    //-------------------------------------------------------------- BindingTargetNode.UIDataVarNode

    public static class UIDataVarNode extends BindingTargetNode {
        public UIDataVarNode(DefaultTreeModel treeModel, DesignBean uiDataBean) {
            super(treeModel);
            this.uiDataBean = uiDataBean;
            this.displayText = "<html>" + uiDataBean.getInstanceName() + " var property: <b>" + uiDataBean.getProperty("var").getValue() + "</b></html>";
        }
        protected DesignBean uiDataBean;
        public DesignBean getUIDataBean() {
            return uiDataBean;
        }
        public boolean lazyLoad() {
//            UIData udb = (UIData)uiDataBean.getInstance();
//            Object value = udb.getValue();
//            if (value instanceof DataModel) {
//                DataModel dm = (DataModel)value;
//                try {
//                    Object o = null;
//                    try {
//                        int rc = dm.getRowCount();
//                        if (rc > 0) {
//                            int idx = dm.getRowIndex();
//                            if (idx < 0) {
//                                dm.setRowIndex(0);
//                            }
//                        }
//                        o = dm.getRowData();
//                    }
//                    catch (Exception x1) {
//                        x1.printStackTrace();
//                    }
//                    if (o != null) {
//                        BeanInfo bi = Introspector.getBeanInfo(o.getClass());
//                        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
//                        PropertyDescriptor[] pdArray = new PropertyDescriptor[0];
//                        DesignBean b = uiDataBean;
//                        for (int i = 0; pds != null && i < pds.length; i++) {
//                            if (pds[i].getReadMethod() != null) {
//                                ArrayList pdList = new ArrayList(Arrays.asList(pdArray));
//                                pdList.add(pds[i]);
//                                PropertyDescriptor[] pdPath = (PropertyDescriptor[])pdList.toArray(
//                                    new PropertyDescriptor[pdList.size()]);
//                                BindingTargetNode btn = _createTargetNode(
//                                    treeModel, b, pdPath);
//                                super.add(btn);
//                            }
//                        }
//                    }
//                }
//                catch (Exception x) {
//                    x.printStackTrace();
//                }
//            }
            return true;
        }
        protected String displayText;
        public String getDisplayText(boolean enableNode) {
            return displayText;
        }
        public boolean hasDisplayIcon() {
            return getChildCount() < 1;
        }
        Icon displayIcon = UIManager.getIcon("Tree.closedIcon"); // NOI18N
        public Icon getDisplayIcon(boolean enableNode) {
            return displayIcon;
        }
        public boolean isValidBindingTarget() {
            return true;
        }
        public String getBindingExpressionPart() {
            return "" + uiDataBean.getProperty("var").getValue();
        }
        public Class getTargetTypeClass() {
            return null;
        }
    }

    //---------------------------------------------------------- BindingTargetNode.ContextTargetNode

    public static class ContextTargetNode extends BindingTargetNode {
        public ContextTargetNode(DefaultTreeModel treeModel, DesignContext context) {
            super(treeModel);
            this.context = context;
            this.displayText = "<html><b>" + context.getDisplayName() + "</b></html>";  //NOI18N
        }
        protected DesignContext context;
        public DesignContext getDesignContext() {
            return context;
        }
        public boolean lazyLoad() {
            DesignBean[] kids = getDesignContext().getRootContainer().getChildBeans();
            for (int i = 0; kids != null && i < kids.length; i++) {
                super.add(_createTargetNode(treeModel, kids[i], null, kids[i].getInstance()));
            }
            return true;
        }
        protected String displayText;
        public String getDisplayText(boolean enableNode) {
            return displayText;
        }
        public boolean hasDisplayIcon() {
            return getChildCount() < 1;
        }
        Icon displayIcon = UIManager.getIcon("Tree.closedIcon"); // NOI18N
        public Icon getDisplayIcon(boolean enableNode) {
            return displayIcon;
        }
        public boolean isValidBindingTarget() {
            return true;
        }
        public String getBindingExpressionPart() {
            if (context instanceof FacesDesignContext) {
                return ((FacesDesignContext)context).getReferenceName();
            }
            return context.getDisplayName();
        }
        public Class getTargetTypeClass() {
            return null;
        }
    }

    //--------------------------------------------------------- BindingTargetNode.PropertyTargetNode

    public static class PropertyTargetNode extends BindingTargetNode {
        public PropertyTargetNode(DefaultTreeModel treeModel, DesignBean bean, PropertyDescriptor[] propPath, Object propInstance) {
            super(treeModel);
            this.bean = bean;
            this.propPath = propPath;
            this.propInstance = propInstance;
            this.displayTextEnabled = getDisplayText(true);
            this.displayTextDisabled = getDisplayText(false);
        }

        protected DesignBean bean;
        public DesignBean getBean() {
            return bean;
        }
        protected PropertyDescriptor[] propPath;
        public PropertyDescriptor[] getPropPath() {
            return propPath;
        }
        protected Object propInstance;
        public Object getPropInstance() {
            return propInstance;
        }

        public boolean lazyLoad() {
            lazyLoadCustomTargetNodes();
            if (propPath == null) {
                lazyLoadBeanTargetNodes();
            }
            if (isValidBindingTarget()) {
                lazyLoadPropertyTargetNodes();
            }
            return true;
        }
        public void lazyLoadCustomTargetNodes() {
            // subclasses can put their stuff here
        }
        public void lazyLoadBeanTargetNodes() {
            if (bean.isContainer()) {
                DesignBean[] kids = bean.getChildBeans();
                for (int i = 0; kids != null && i < kids.length; i++) {
                    super.add(_createTargetNode(treeModel, kids[i], null, kids[i].getInstance()));
                }
            }
        }
        public void lazyLoadPropertyTargetNodes() {
            try {
                BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
                PropertyDescriptor[] pds = bi.getPropertyDescriptors();
                for (int i = 0; pds != null && i < pds.length; i++) {
                    if (pds[i].getReadMethod() != null) {
                        ArrayList pdList = new ArrayList();
                        for (int j = 0; propPath != null && j < propPath.length; j++) {
                            pdList.add(propPath[j]);
                        }
                        pdList.add(pds[i]);
                        PropertyDescriptor[] pda = (PropertyDescriptor[])pdList.toArray(new PropertyDescriptor[pdList.size()]);
                        BindingTargetNode btn = _createTargetNode(treeModel, bean, pda, null);
                        super.add(btn);
                    }
                }
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        protected String displayTextEnabled;
        protected String displayTextDisabled;
        public String getDisplayText(boolean enableNode) {
            if (enableNode && displayTextEnabled != null) {
                return displayTextEnabled;
            }
            else if (!enableNode && displayTextDisabled != null) {
                return displayTextDisabled;
            }
            PropertyDescriptor pd = (propPath != null && propPath.length > 0) ?
                propPath[propPath.length - 1] : null;
            StringBuffer sb = new StringBuffer();
            sb.append("<html>"); //NOI18N
            if (!enableNode) {
                sb.append("<font color=\"gray\">"); //NOI18N
            }
            if (pd != null) {
                sb.append(bundle.getMessage("property")); //NOI18N
                sb.append(" ");  //NOI18N
            }
            if (enableNode) {
                sb.append("<b>");  //NOI18N
            }
            if (pd != null) {
                sb.append(pd.getName());
            }
            else {
                sb.append(bean.getInstanceName());
            }
            if (enableNode) {
                sb.append("</b>");  //NOI18N
            }
            sb.append(" &nbsp; <font size=\"-1\"><i>");  //NOI18N
            sb.append(getTargetTypeDisplayName());
            sb.append("</i></font>");  //NOI18N
            if (!enableNode) {
                sb.append("</font>");  //NOI18N
            }
            sb.append("</html>");  //NOI18N
            return sb.toString();
        }
        public boolean isValidBindingTarget() {
            if (propPath == null && bean.getDesignContext() instanceof FacesDesignContext) {
                return ((FacesDesignContext)bean.getDesignContext()).isValidBindingTarget(bean);
            }
            return true;
        }
        public String getBindingExpressionPart() {
            if (propPath != null && propPath.length > 0) {
                return propPath[propPath.length - 1].getName();
            }
            return bean.getInstanceName();
        }
        public Class getTargetTypeClass() {
            if (propInstance == null) {
                propInstance = getPropInstance(bean, propPath);
            }
            if (propInstance != null) {
                if (!propInstance.getClass().isPrimitive()) {
                    return propInstance.getClass();
                }
            }
            return propPath != null && propPath.length > 0
                ? propPath[propPath.length - 1].getPropertyType()
                : bean.getInstance() != null
                    ? bean.getInstance().getClass()
                    : bean.getBeanInfo() != null
                        ? bean.getBeanInfo().getBeanDescriptor().getBeanClass()
                        : null;
        }
        boolean iconChecked = false;
        public boolean hasDisplayIcon() {
            if (!iconChecked) {
                displayIcon = getDisplayIcon(true);
                iconChecked = true;
            }
            return displayIcon != null;
        }
        Icon displayIcon = null;
        public Icon getDisplayIcon(boolean enableNode) {
            if (displayIcon == null) {
                if (propInstance == null) {
                    propInstance = getPropInstance(bean, propPath);
                }
                if (propInstance != null) {
                    try {
                        BeanInfo bi = Introspector.getBeanInfo(propInstance.getClass());
                        Image img = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
                        if (img != null) {
                            displayIcon = new ImageIcon(img);
                        }
                    } catch (Exception x) {}
                    if (displayIcon == null && (propPath == null || propPath.length == 0)) {
                        if (bean instanceof MarkupDesignBean && ((MarkupDesignBean)bean).getElement() != null) {
                            displayIcon = TargetPanel.TAG_ICON;
                        }
                    }
                }
            }
            if (displayIcon == null/* && propPath == null*/) {
                displayIcon = TargetPanel.BEAN_ICON;
            }
            return displayIcon;
        }
    }

    //------------------------------------------------------------------------------- Helper methods

    public static Object getPropInstance(DesignBean bean, PropertyDescriptor[] propPath) {
        if (propPath != null && propPath.length > 0) {
            try {
                ArrayList propList = new ArrayList();
                for (int i = 0; i < propPath.length; i++) {
                    propList.add(propPath[i]);
                }
                Object o = bean.getInstance();
                while (o != null && propList.size() > 0) {
                    PropertyDescriptor pdnext = (PropertyDescriptor)propList.get(0);
                    BeanInfo bi = Introspector.getBeanInfo(o.getClass());
                    PropertyDescriptor[] pdanext = bi.getPropertyDescriptors();
                    for (int i = 0; i < pdanext.length; i++) {
                        if (pdanext[i].getName().equals(pdnext.getName())) {
                            //System.out.println("found: " + pdnext.getName() + " : " + propList.size() + " left");
                            Method read = pdanext[i].getReadMethod();
                            if (read != null) {
                                try {
                                    o = read.invoke(o, new Object[] {});
                                    if (o instanceof ValueBinding) {
                                        o = ((ValueBinding)o).getValue(FacesContext.
                                            getCurrentInstance());
                                    }
                                }
                                catch (Exception x) {
                                    return null;
                                }
                                if (o != null) {
                                    propList.remove(0);
                                    continue;
                                }
                            }
                            else {
                                return null;
                            }
                        }
                    }
                }
                return o;
            }
            catch (Exception x) {
                x.printStackTrace();
            }
        }
        else {
            return bean.getInstance();
        }
        return null;
    }

    static HashMap arrayTypeKeyHash = new HashMap();
    static {
        arrayTypeKeyHash.put("B", "byte");   //NOI18N
        arrayTypeKeyHash.put("C", "char");   //NOI18N
        arrayTypeKeyHash.put("D", "double");   //NOI18N
        arrayTypeKeyHash.put("F", "float");   //NOI18N
        arrayTypeKeyHash.put("I", "int");   //NOI18N
        arrayTypeKeyHash.put("J", "long");   //NOI18N
        arrayTypeKeyHash.put("S", "short");   //NOI18N
        arrayTypeKeyHash.put("Z", "boolean");   //NOI18N
        arrayTypeKeyHash.put("V", "void");   //NOI18N
    }

    public static String decodeTypeName(String tn) {
        if (tn.startsWith("[")) {   //NOI18N
            int depth = 0;
            while (tn.startsWith("[")) {   //NOI18N
                tn = tn.substring(1);
                depth++;
            }
            if (tn.startsWith("L")) {   //NOI18N
                tn = tn.substring(1);
                tn = tn.substring(0, tn.length() - 1);
            }
            else {
                char typeKey = tn.charAt(0);
                tn = (String)arrayTypeKeyHash.get("" + typeKey);   //NOI18N
            }
            for (int i = 0; i < depth; i++) {
                tn += "[]";   //NOI18N
            }
        }
        if (tn.indexOf(".") > -1) {   //NOI18N
            tn = tn.substring(tn.lastIndexOf(".") + 1);
        }
        return tn;
    }
}
//    //------------------------------------------------------------- BindingTargetNode.BeanTargetNode
//
//    public static final String FACET_KEY = "facet"; // NOI18N
//
//    public static class BeanTargetNode extends BindingTargetNode {
//        public BeanTargetNode(DefaultTreeModel treeModel, DesignBean bean) {
//            super(treeModel);
//            this.bean = bean;
//            this.displayTextEnabled = getCustomDisplayText(true);
//            this.displayTextDisabled = getCustomDisplayText(false);
//        }
//        protected DesignBean bean;
//        public DesignBean getBean() {
//            return bean;
//        }
//        public boolean lazyLoad() {
//            lazyLoadCustomTargetNodes();
//            lazyLoadBeanTargetNodes();
//            if (isValidBindingTarget()) {
//                lazyLoadPropertyTargetNodes();
//            }
//            return true;
//        }
//        public void lazyLoadCustomTargetNodes() {
//            // subclasses can put their stuff here
//        }
//        public void lazyLoadBeanTargetNodes() {
//            if (bean.isContainer()) {
//                DesignBean[] kids = bean.getChildBeans();
//                for (int i = 0; kids != null && i < kids.length; i++) {
//                    super.add(_createTargetNode(treeModel, kids[i], null));
//                }
//            }
//        }
//        public void lazyLoadPropertyTargetNodes() {
//            try {
//                BeanInfo bi = Introspector.getBeanInfo(getTargetTypeClass());
//                PropertyDescriptor[] pds = bi.getPropertyDescriptors();
//                for (int i = 0; pds != null && i < pds.length; i++) {
//                    if (pds[i].getReadMethod() != null) {
//                        BindingTargetNode btn = _createTargetNode(
//                            treeModel, bean, new PropertyDescriptor[] { pds[i] });
//                        super.add(btn);
//                    }
//                }
//            }
//            catch (Exception x) {
//                x.printStackTrace();
//            }
//        }
//        protected String displayTextEnabled;
//        protected String displayTextDisabled;
//        public String getCustomDisplayText(boolean enableNode) {
//            if (enableNode && displayTextEnabled != null) {
//                return displayTextEnabled;
//            }
//            else if (!enableNode && displayTextDisabled != null) {
//                return displayTextDisabled;
//            }
//            String facet = (String)getUserDataMap().get(FACET_KEY);
//            StringBuffer sb = new StringBuffer();
//            sb.append("<html>");    //NOI18N
//            if (facet != null) {
//                sb.append(bundle.getMessage("facet")); //NOI18N
//                sb.append(" "); //NOI18N
//                if (enableNode) {
//                    sb.append("<b>");
//                }
//                sb.append(facet);
//                if (enableNode) {
//                    sb.append("</b>");
//                }
//                sb = new StringBuffer(bundle.getMessage("trailingColon", sb.toString())); //NOI18N
//                sb.append(" ");  //NOI18N
//            }
//            if (enableNode) {
//                sb.append("<b>");    //NOI18N
//            }
//            else {
//                sb.append("<font color=\"gray\">");  //NOI18N
//            }
//            sb.append(bean.getInstanceName());
//            if (enableNode) {
//                sb.append("</b>");   //NOI18N
//            }
//            sb.append(" &nbsp; <font size=\"-1\"><i>");  //NOI18N
//            sb.append(getTargetTypeDisplayName());
//            sb.append("</i></font>");    //NOI18N
//            if (!enableNode) {
//                sb.append("</font>");    //NOI18N
//            }
//            sb.append("</html>");    //NOI18N
//            return sb.toString();
//        }
//        public boolean hasCustomDisplayIcon() {
//            return true;
//        }
//        Icon displayIcon = null;
//        public Icon getCustomDisplayIcon(boolean enableNode) {
//            if (displayIcon == null) {
//                BeanInfo bi = bean.getBeanInfo();
//                Image img = bi.getIcon(BeanInfo.ICON_COLOR_16x16);
//                if (img != null) {
//                    displayIcon = new ImageIcon(img);
//                }
//                else {
//                    displayIcon = TargetPanel.BEAN_ICON;
//                }
//            }
//            return displayIcon;
//        }
//        public boolean isValidBindingTarget() {
//            if (bean.getDesignContext() instanceof FacesDesignContext) {
//                return ((FacesDesignContext)bean.getDesignContext()).isValidBindingTarget(bean);
//            }
//            return false;
//        }
//        public String getBindingExpressionPart() {
//            return bean.getInstanceName();
//        }
//        public Class getTargetTypeClass() {
//            Object o = bean.getInstance();
//            if (o != null) {
//                if (!o.getClass().isPrimitive()) {
//                    return o.getClass();
//                }
//            }
//            return bean.getBeanInfo().getBeanDescriptor().getBeanClass();
//        }
//    }

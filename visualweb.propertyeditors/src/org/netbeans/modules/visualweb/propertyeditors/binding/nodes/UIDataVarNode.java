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
package org.netbeans.modules.visualweb.propertyeditors.binding.nodes;

import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import com.sun.rave.designtime.DesignBean;
import org.netbeans.modules.visualweb.propertyeditors.binding.BindingTargetNode;

public class UIDataVarNode extends BindingTargetNode {
    public UIDataVarNode(BindingTargetNode parent, DesignBean uiDataBean) {
        super(parent);
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

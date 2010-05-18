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

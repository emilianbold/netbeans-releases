/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.tree.model;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.util.NbBundle;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.util.ImageUtilities;

/**
 * The implementation of the TreeItemInfoProvider for the variables' tree.
 * 
 * @author nk160297
 * @author AlexanderPermyakov
 */
public class VariableTreeInfoProvider 
        implements TreeItemInfoProvider, TreeItemActionsProvider {

    private static Image VAR_IMG = ImageUtilities.loadImage(
            "org/netbeans/modules/worklist/editor/mapper/tree/model/VARIABLE.png"); // NOI18N
    private static Image MESSAGE_PART_IMG = ImageUtilities.loadImage(
            "org/netbeans/modules/worklist/editor/mapper/tree/model/MESSAGE_PART.png"); // NOI18N

    private static VariableTreeInfoProvider singleton = new VariableTreeInfoProvider();
    
    public static VariableTreeInfoProvider getInstance() {
        return singleton;
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getDisplayByDataObj(dataObj);
    }

    private String getDisplayByDataObj(Object dataObj) {
        if (dataObj instanceof SchemaComponent) {
            return SchemaTreeInfoProvider.getInstance().getDisplayByDataObj(dataObj);
        }
        if (dataObj instanceof TTask) {
            return "Variables";
        } 
        if (dataObj instanceof Named) {
            return ((Named)dataObj).getName();
        }
        if (dataObj instanceof VariableDeclaration) {
            return ((VariableDeclaration)dataObj).getVariableName();
        }
        if (dataObj == SoaTreeModel.TREE_ROOT) {
            return dataObj.toString();
        }
        //
        return null;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return getIconByDataObj(dataObj);
    }    
        
    private Icon getIconByDataObj(Object dataObj) {
        //
        if (dataObj instanceof VariableDeclaration) {
            return new ImageIcon(VAR_IMG);
        }
        //
        if (dataObj instanceof SchemaComponent) {
            return SchemaTreeInfoProvider.getInstance().getIconByDataObj(dataObj);
        } 
        //
        if (dataObj instanceof Part) {
            return new ImageIcon(MESSAGE_PART_IMG);
        }
        //
        return null;
    }

    public List<Action> getMenuActions(TreeItem treeItem, 
            Object context, TreePath treePath) {
        return Collections.EMPTY_LIST;
    }
    
    public String getToolTipText(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        String name = getDisplayByDataObj(dataObj);
        return getToolTipTextByDataObj(dataObj, name);
    }

    private String getToolTipTextByDataObj(Object dataObj, String name) {
        String nameSpace = null;
        if (dataObj instanceof SchemaComponent) {
            String result = SchemaTreeInfoProvider.getInstance().
                    getToolTipTextByDataObj(dataObj, name);
            if (result != null) {
                return result;
            }
        }
        
        if (dataObj instanceof Part) {
            String partLbl = NbBundle.getMessage(
                    VariableTreeInfoProvider.class, "MESSAGE_PART"); // NOI18N
            
            if (((Part) dataObj).getType() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(
                        partLbl, name, ((Part) dataObj).getType().
                        getRefString(), nameSpace);
            }
            if (((Part) dataObj).getElement() != null) {
                return SchemaTreeInfoProvider.getColorTooltip(
                        partLbl, name, ((Part) dataObj).
                        getElement().getRefString(), nameSpace);
            }
        }

        if (dataObj instanceof VariableDeclaration) {
            VariableDeclaration var = (VariableDeclaration)dataObj;
            NamedComponentReference typeRef = var.getTypeRef();
            Class varTypeClass = var.getTypeClass();
            if (Message.class.isAssignableFrom(varTypeClass)) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class,
                        "MESSAGE_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, typeRef.getRefString(), nameSpace);
            } else if (GlobalType.class.isAssignableFrom(varTypeClass)) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class,
                        "TYPE_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, typeRef.getRefString(), nameSpace);
            } else if (GlobalElement.class.isAssignableFrom(varTypeClass)) {
                String varLbl = NbBundle.getMessage(
                        VariableTreeInfoProvider.class,
                        "ELEMENT_VARIABLE"); // NOI18N
                return SchemaTreeInfoProvider.getColorTooltip(
                        varLbl, name, typeRef.getRefString(), nameSpace);
            }
        }
        //
        return null;
    }

}

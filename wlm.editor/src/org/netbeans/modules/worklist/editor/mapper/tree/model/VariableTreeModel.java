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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.utl.Util;
import org.netbeans.modules.worklist.editor.mapper.WlmDesignContext;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xam.NamedReferenceable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.filesystems.FileObject;

/**
 * The implementation of the MapperTreeModel for the variables' tree.
 *
 * @author nk160297
 */
public class VariableTreeModel implements SoaTreeModel,
        TreeStructureProvider, MapperConnectabilityProvider {

    private FindAllChildrenSchemaVisitor sSchemaSearcher = 
            new FindAllChildrenSchemaVisitor(true, true, true);
    
    private WlmDesignContext mDesignContext;
    private VariableTreeInfoProvider mTreeInfoProvider;
    private boolean leftTreeFlag = true;
    
    public VariableTreeModel(WlmDesignContext context, boolean leftTree) {
        mDesignContext = context;
        mTreeInfoProvider = VariableTreeInfoProvider.getInstance();
        leftTreeFlag = leftTree;
    }
    
    public List<Object> getChildren(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj == SoaTreeModel.TREE_ROOT) {
            TTask task = mDesignContext.getWlmModel().getTask();
            return Collections.singletonList((Object)task);
        } else if (dataObj instanceof TTask) {
            List<VariableDeclaration> allVariables = 
                    Util.getAllVariables(mDesignContext.getWlmModel());
            return (List)allVariables;
        } else if (dataObj instanceof VariableDeclaration) {
            List<Object> result = null;
            VariableDeclaration varDecl = (VariableDeclaration)dataObj;
            Class varType = varDecl.getTypeClass();
            NamedComponentReference ref = varDecl.getTypeRef();
            if (varType != null && ref != null) {
                Referenceable obj = ref.get();
                if (obj != null) {
                }
                if (obj instanceof Message) {
                    Collection<Part> parts = ((Message)obj).getParts();
                    if (parts != null && !parts.isEmpty()) {
                        result = (parts != null)
                                ? new ArrayList<Object>(parts)
                                : Collections.EMPTY_LIST;
                    }
                } else if (obj instanceof SchemaComponent) {
                    result = loadSchemaComponents(treeItem, (SchemaComponent)obj);
                }
            }
            return result;
        } else if (dataObj instanceof Part) {
            Part part = (Part)dataObj;
            NamedComponentReference<GlobalType> gTypeRef = part.getType();
            if (gTypeRef != null) {
                GlobalType gType = gTypeRef.get();
                if (gType != null) {
                    return loadSchemaComponents(treeItem, gType);
                }
            } else {
                NamedComponentReference<GlobalElement> gElemRef = part.getElement();
                if (gElemRef != null) {
                    GlobalElement gElem = gElemRef.get();
                    if (gElem != null) {
                        return loadSchemaComponents(treeItem, gElem);
                    }
                }
            }
        } else if (dataObj instanceof SchemaComponent) {
            return loadSchemaComponents(treeItem, (SchemaComponent)dataObj);
        }
        //
        return null;
    }

    private List loadSchemaComponents(TreeItem treeItem, SchemaComponent parent) {
        sSchemaSearcher.lookForSubcomponents(parent);
        List<SchemaComponent> childrenComp = sSchemaSearcher.getFound();
        return childrenComp;
    }
    
    public Boolean isLeaf(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        return null;
    }

    public Boolean isConnectable(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof Element || 
                dataObj instanceof Attribute ||
                dataObj instanceof Part) {
            return Boolean.TRUE;
        }
        
        if (dataObj instanceof FileObject &&
                !((FileObject) dataObj).isFolder())
        {
            return Boolean.TRUE;
        }
        
        //
        if (dataObj instanceof VariableDeclaration) {
            return Boolean.TRUE;
        }
        //
        return null;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return mTreeInfoProvider;
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }
    
    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return mTreeInfoProvider;
    }

    public Object getRoot() {
        return TREE_ROOT;
    }

    public List<SoaTreeExtensionModel> getExtensionModelList() {
        return Collections.EMPTY_LIST;
    }
}

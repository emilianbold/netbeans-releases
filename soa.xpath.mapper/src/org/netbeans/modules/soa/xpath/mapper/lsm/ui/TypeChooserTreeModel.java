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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.xpath.mapper.lsm.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.soa.ui.schema.ReferencedSchemaFolder;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;

/**
 * The SOA tree model for choosing a global type for a pseudo element or attribute.
 * @author nk160297
 */
public class TypeChooserTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemInfoProvider {

    // Indicates if element or attribute is going to be the owner of chosen type.
    // The tree is different depends on this property.
    private boolean mIsAttribute; 

    public TypeChooserTreeModel(boolean isAttribute) {
        mIsAttribute = isAttribute;
    }
    
    public Object getRoot() {
        return SoaTreeModel.TREE_ROOT;
    }

    public List<SoaTreeExtensionModel> getExtensionModelList() {
        return null;
    }

    public TreeStructureProvider getTreeStructureProvider() {
        return this;
    }

    public TreeItemInfoProvider getTreeItemInfoProvider() {
        return this;
    }

    public TreeItemActionsProvider getTreeItemActionsProvider() {
        return null;
    }

    //--------------------------------------------------------------------------
    
    public List<Object> getChildren(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        List<Object> result = getChildrenImpl(dataObj);
        //
        return result;
    }

    protected List<Object> getChildrenImpl(Object dataObj) {
        List<Object> result = new ArrayList<Object>();
        if (dataObj == null) {
            return result;
        } else if (dataObj == getRoot()) {
            //
            // Add a branch with primitive types here.
            result.add(SchemaTreeInfoProvider.ToolTipTitles.PRIMITIVE_TYPES.getName());
            //
        } else if (dataObj.equals(SchemaTreeInfoProvider.ToolTipTitles.PRIMITIVE_TYPES.getName())) {
            Collection<GlobalSimpleType> primitiveTypes = 
                    SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                    getSchema().getSimpleTypes();
            result.addAll(primitiveTypes);
        } else if (dataObj instanceof SchemaModel) {
            SchemaModel schemaModel = (SchemaModel)dataObj;
            result.addAll(getChildrenImpl(schemaModel.getSchema()));
        } else if (dataObj instanceof Schema) {
            Schema schema = (Schema)dataObj;
            if (mIsAttribute) {
                // Only simple types can be chosen for attributes
                List<GlobalSimpleType> gSimpleType = 
                        schema.getChildren(GlobalSimpleType.class);
                result.addAll(gSimpleType);
            } else {
                List<GlobalType> gType = 
                        schema.getChildren(GlobalType.class);
                result.addAll(gType);
            }
            //
            Collection<Import> imports = schema.getImports();
            Collection<Include> includes = schema.getIncludes();
            //
            if (!imports.isEmpty() || !includes.isEmpty()) {
                result.add(new ReferencedSchemaFolder(schema));
            }
            //
        } else if (dataObj instanceof ReferencedSchemaFolder) { 
            ReferencedSchemaFolder rsf = (ReferencedSchemaFolder)dataObj;
            Schema schema = rsf.getOwner();
            //
            Collection<Import> imports = schema.getImports();
            result.addAll(imports);
            //
            Collection<Include> includes = schema.getIncludes();
            result.addAll(includes);
            //
        } else if (dataObj instanceof Import) {
            try {
                SchemaModel sModel = ((Import)dataObj).resolveReferencedModel();
                if (sModel != null) {
                    result.addAll(getChildrenImpl(sModel));
                }
            } catch (CatalogModelException ex) {
                // the import cannot be resolved 
            }
        } else if (dataObj instanceof Include) {
            try {
                SchemaModel sModel = ((Include)dataObj).resolveReferencedModel();
                if (sModel != null) {
                    result.addAll(getChildrenImpl(sModel));
                }
            } catch (CatalogModelException ex) {
                // the import cannot be resolved 
            }
        } else if (dataObj instanceof SchemaComponent) {
            SchemaComponent schemaComp = (SchemaComponent)dataObj;
            FindAllChildrenSchemaVisitor visitor = 
                    new FindAllChildrenSchemaVisitor(true, true, true);
            visitor.lookForSubcomponents(schemaComp);
            List<SchemaComponent> sCompList = visitor.getFound();
            result.addAll(sCompList);
        } else {
            //
        }
        //
        return result;
    }

    public Boolean isLeaf(TreeItem treeItem) {
        List childrent = getChildren(treeItem);
        return childrent == null || childrent.isEmpty();
    }

    /** Can return null */
    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        String result = null;
        if (dataObj instanceof SchemaComponent) {
            result = SchemaTreeInfoProvider.getInstance().getDisplayName(treeItem);
        } else if (dataObj instanceof SchemaModel) {
            SchemaModel schemaModel = (SchemaModel)dataObj;
            result = SchemaTreeInfoProvider.getDisplayName(schemaModel);
        } else if (dataObj instanceof ReferencedSchemaFolder) {
            result = ReferencedSchemaFolder.REFERENCED_SCHEMA_FOLDER;
        }
        //
        return result;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof SchemaComponent || 
                dataObject instanceof SchemaModel) {
            Icon result = SchemaTreeInfoProvider.getInstance().getIcon(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        return null;
    }

    public String getToolTipText(TreeItem treeItem) {
        String result = SchemaTreeInfoProvider.getInstance().getToolTipText(treeItem);
        return result;
    }

}

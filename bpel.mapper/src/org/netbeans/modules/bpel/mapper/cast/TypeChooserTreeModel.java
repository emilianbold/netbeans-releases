/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bpel.mapper.cast;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.schema.ReferencedSchemaFolder;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.xml.catalogsupport.util.ProjectUtilities;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xpath.ext.schema.FindAllChildrenSchemaVisitor;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 * The SOA tree model for choosing a global type for a pseudo element or attribute.
 * @author nk160297
 */
public class TypeChooserTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemInfoProvider {

    private BpelModel mBpelModel;
    
    // Indicates if element or attribute is going to be the owner of chosen type.
    // The tree is different depends on this property.
    private boolean mIsAttribute; 

    public TypeChooserTreeModel(BpelModel bpelModel, boolean isAttribute) {
        mBpelModel = bpelModel;
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

    private List<Object> getChildrenImpl(Object dataObj) {
        List<Object> result = new ArrayList<Object>();
        if (dataObj == getRoot()) {
            Project project = Utils.safeGetProject(mBpelModel);
            if (project != null) {
                Collection<SchemaModel> projSchemas = getAllSchemasInProject(project);
                for (SchemaModel schemaModel : projSchemas) {
                    List<Object> children = getChildrenImpl(schemaModel);
                    if (children != null && !children.isEmpty()) {
                        result.add(schemaModel);
                    }
                }
                //
                Collection<WSDLModel> projWsdls = getAllWsdlInProject(project);
                result.addAll(projWsdls);
                //
                Set<Project> refProj = ProjectUtilities.getReferencedProjects(project);
                result.addAll(refProj);
            }
            //
            // Add a branch with primitive types here.
            result.add(SchemaTreeInfoProvider.PRIMITIVE_TYPES);
            //
        } else if (dataObj.equals(SchemaTreeInfoProvider.PRIMITIVE_TYPES)) {
            Collection<GlobalSimpleType> primitiveTypes = 
                    SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                    getSchema().getSimpleTypes();
            result.addAll(primitiveTypes);
        } else if (dataObj instanceof Project) {
            Project project = (Project)dataObj;
            Collection<SchemaModel> projSchemas = getAllSchemasInProject(project);
            for (SchemaModel schemaModel : projSchemas) {
                List<Object> children = getChildrenImpl(schemaModel);
                if (children != null && !children.isEmpty()) {
                    result.add(schemaModel);
                }
            }
            Collection<WSDLModel> projWsdls = getAllWsdlInProject(project);
            for (WSDLModel wsdlModel : projWsdls) {
                List<Object> children = getChildrenImpl(wsdlModel);
                if (children != null && !children.isEmpty()) {
                    result.add(wsdlModel);
                }
            }
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
        } else if (dataObj instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel)dataObj;
            Definitions def = wsdlModel.getDefinitions();
            if (def != null) {
                Types types = def.getTypes();
                if (types != null) {
                    Collection<Schema> schemas = types.getSchemas();
                    result.addAll(schemas);
                }
            }
            // result.addAll(gType);
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

    private Collection<SchemaModel> getAllSchemasInProject(Project project) {
        List<FileObject> schemaFoList = ProjectUtilities.
                getXSDFilesRecursively(project, true);
        Collection<SchemaModel> resultList = new ArrayList<SchemaModel>();
        for (FileObject fo : schemaFoList) {
            SchemaModel sModel = Utils.getSchemaModel(fo);
            if (sModel != null) {
                resultList.add(sModel);
            }
        }
        //
        return resultList;
    }
    
    private Collection<WSDLModel> getAllWsdlInProject(Project project) {
        List<FileObject> wsdlFoList = ProjectUtilities.
                getWSDLFilesRecursively(project, true);
        Collection<WSDLModel> resultList = new ArrayList<WSDLModel>();
        for (FileObject fo : wsdlFoList) {
            WSDLModel sModel = Utils.getWsdlModel(fo);
            if (sModel != null) {
                resultList.add(sModel);
            }
        }
        //
        return resultList;
    }
    
    public Boolean isLeaf(TreeItem treeItem) {
        List childrent = getChildren(treeItem);
        return childrent == null || childrent.isEmpty();
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        String result = null;
        if (dataObj instanceof SchemaComponent) {
            result = SchemaTreeInfoProvider.getInstance().getDisplayName(treeItem);
        } else if (dataObj instanceof SchemaModel) {
            SchemaModel schemaModel = (SchemaModel)dataObj;
            result = SchemaTreeInfoProvider.getDisplayName(schemaModel);
        } else if (dataObj instanceof WSDLModel) {
            WSDLModel wsdlModel = (WSDLModel)dataObj;
            Project ownerProject = Utils.safeGetProject(wsdlModel);
            if (ownerProject == null) { 
                result = wsdlModel.getDefinitions().getTargetNamespace();
                // TODO: it can be null
            } else {
                FileObject projectDir = ownerProject.getProjectDirectory();
                FileObject schemaFo = SoaUtil.getFileObjectByModel(wsdlModel);
                result = FileUtil.getRelativePath(projectDir, schemaFo);
            }
        } else if (dataObj instanceof Project) {
            Project project = (Project)dataObj;
            LogicalViewProvider viewProvider = project.getLookup().
                    lookup(LogicalViewProvider.class);
            if (viewProvider != null) {
                Node node = viewProvider.createLogicalView();
                if (node != null) {
                    result = node.getDisplayName();
                }
            }
        } else if (dataObj instanceof ReferencedSchemaFolder) {
            result = ReferencedSchemaFolder.REFERENCED_SCHEMA_FOLDER;
        }
        //
        if (result == null) {
            result = dataObj.toString();
        }
        //
        return result;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof SchemaComponent || 
                dataObject instanceof SchemaModel || 
                dataObject instanceof WSDLModel) {
            Icon result = SchemaTreeInfoProvider.getInstance().getIcon(treeItem);
            if (result != null) {
                return result;
            }
        }
        //
        if (dataObject instanceof Project) {
            Project project = (Project)dataObject;
            LogicalViewProvider viewProvider = project.getLookup().
                    lookup(LogicalViewProvider.class);
            if (viewProvider != null) {
                Node node = viewProvider.createLogicalView();
                if (node != null) {
                    Image img = node.getIcon(BeanInfo.ICON_COLOR_16x16);
                    if (img != null) {
                        Icon result = new ImageIcon(img);
                        return result;
                    }
                }
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

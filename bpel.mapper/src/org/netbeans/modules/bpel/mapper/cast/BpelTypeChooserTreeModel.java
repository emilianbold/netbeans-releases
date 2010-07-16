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

package org.netbeans.modules.bpel.mapper.cast;

import java.awt.Image;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.mapper.model.BpelMapperUtils;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.soa.ui.schema.SchemaTreeInfoProvider;
import org.netbeans.modules.soa.xpath.mapper.lsm.ui.TypeChooserTreeModel;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;

/**
 * The SOA tree model for choosing a global type for a pseudo element or attribute.
 * @author nk160297
 */
public class BpelTypeChooserTreeModel extends TypeChooserTreeModel {

    private BpelModel mBpelModel;
    
    public BpelTypeChooserTreeModel(BpelModel bpelModel, boolean isAttribute) {
        super(isAttribute);
        mBpelModel = bpelModel;
    }
    
    @Override
    protected List<Object> getChildrenImpl(Object dataObj) {
        List<Object> result = super.getChildrenImpl(dataObj);
        if (dataObj == getRoot()) {
            result.add(NodeType.BPEL_GLOBAL_CATALOG);
            //
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
                Set<Project> refProj = getReferencedProjects(project);
                result.addAll(refProj);
            }
        } else if (dataObj.equals(SchemaTreeInfoProvider.ToolTipTitles.PRIMITIVE_TYPES.getName())) {
            Collection<GlobalSimpleType> primitiveTypes = 
                    SchemaModelFactory.getDefault().getPrimitiveTypesModel().
                    getSchema().getSimpleTypes();
            result.addAll(primitiveTypes);
        } else if (dataObj.equals(NodeType.BPEL_GLOBAL_CATALOG)) {
            result.addAll(BpelMapperUtils.getGlobalCatalogModels(mBpelModel));
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
        }
        //
        return result;
    }

    public static Set<Project> getReferencedProjects(Project baseProj) {
      HashSet<Project> projectSet = new HashSet<Project>();
      populateReferencedProjects(baseProj, projectSet);
      return projectSet;
    }

    private static void populateReferencedProjects(Project baseProj, Set<Project> projectSet) {
      DefaultProjectCatalogSupport instance = DefaultProjectCatalogSupport.getInstance(baseProj.getProjectDirectory());

      if (instance != null) {
        Set references = instance.getProjectReferences();

        for (Object proj : references) {
          assert proj instanceof Project;
          projectSet.add((Project) proj);
          populateReferencedProjects((Project) proj, projectSet);
        }
      }
    }

    private Collection<SchemaModel> getAllSchemasInProject(Project project) {
        List<FileObject> schemaFoList = ReferenceUtil.getXSDFilesRecursively(project, true);
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
        List<FileObject> wsdlFoList = ReferenceUtil.getWSDLFilesRecursively(project, true);
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
    
    @Override
    public String getDisplayName(TreeItem treeItem) {
        String result = super.getDisplayName(treeItem);
        if (result != null) {
            return result;
        }
        //
        Object dataObj = treeItem.getDataObject();
        if (dataObj instanceof WSDLModel) {
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
        } else if (dataObj.equals(NodeType.BPEL_GLOBAL_CATALOG)) {
            result = NodeType.BPEL_GLOBAL_CATALOG.getDisplayName();
        }
        //
        if (result == null) {
            result = dataObj.toString();
        }
        //
        return result;
    }

    @Override
    public Icon getIcon(TreeItem treeItem) {
        Icon result = super.getIcon(treeItem);
        if (result != null) {
            return result;
        }
        //
        Object dataObject = treeItem.getDataObject();
        //
        if (dataObject instanceof WSDLModel) {
            result = SchemaTreeInfoProvider.getInstance().getIcon(treeItem);
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
                        result = new ImageIcon(img);
                        return result;
                    }
                }
            }
        }
        //
        if (dataObject.equals(NodeType.BPEL_GLOBAL_CATALOG)) {
            return null;
        }
        //
        return result;
    }

}

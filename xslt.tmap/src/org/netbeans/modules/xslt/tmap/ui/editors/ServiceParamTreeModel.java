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

package org.netbeans.modules.xslt.tmap.ui.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.xml.reference.ReferenceFile;
import org.netbeans.modules.xml.reference.ReferenceUtil;
import org.netbeans.modules.soa.ui.tree.SoaTreeExtensionModel;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.ui.tree.TreeItemActionsProvider;
import org.netbeans.modules.soa.ui.tree.TreeItemInfoProvider;
import org.netbeans.modules.soa.ui.tree.TreeStructureProvider;
import org.netbeans.modules.soa.ui.wsdl.WSDLTreeInfoProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xslt.tmap.model.api.Import;
import org.netbeans.modules.xslt.tmap.model.api.TMapModel;
import org.netbeans.modules.xslt.tmap.model.api.TransformMap;
import org.netbeans.modules.xslt.tmap.nodes.DecoratedTransformMap;
import org.netbeans.modules.xslt.tmap.nodes.NodeType;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * 
 * @author nk160297
 */
public class ServiceParamTreeModel implements SoaTreeModel, 
        TreeStructureProvider, TreeItemInfoProvider {

    private static final Logger LOGGER = Logger.getLogger(ServiceParamTreeModel.class.getName());
    private TMapModel myTMapModel;
    private Class<? extends WSDLComponent> myLeafType;
    private PortType myPtTreeFilter;

    public ServiceParamTreeModel(TMapModel tMapModel, 
            Class<? extends WSDLComponent> leafType, PortType ptTreeFilter) {
        myTMapModel = tMapModel;
        myLeafType = leafType;
        myPtTreeFilter = ptTreeFilter;
    }

    public ServiceParamTreeModel(TMapModel tMapModel, Class<? extends WSDLComponent> leafType) {
        this(tMapModel, leafType, null);
    }
    
    public Object getRoot() {
        return myTMapModel;
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
        List<Object> result = new ArrayList<Object>();
        //
        Object dataObj = treeItem.getDataObject();
        if (isLeafDataObject(dataObj)) {
            return result;
        }
        
        if (dataObj instanceof TMapModel) { // tMapModel is the root
            result.addAll(getImportedModels(myTMapModel, myPtTreeFilter));
            result.add(NodeType.NON_IMPORTED_ARTIFACTS);
        } else if (dataObj == NodeType.NON_IMPORTED_ARTIFACTS) {
            result.addAll(getNonImportedModels(myTMapModel, myPtTreeFilter));
        } else if (dataObj instanceof WSDLModel) {
            Definitions defs = ((WSDLModel)dataObj).getDefinitions();
            Collection<PortType> pts = defs != null ? defs.getPortTypes() : null;
            if (pts != null) {
                for (PortType pt : pts) {
                    result.add(pt);
                }
            }
        } else if (dataObj instanceof PortType) {
            Collection<Operation> ops = ((PortType)dataObj).getOperations();
            if (ops != null) {
                for (Operation op : ops) {
                    result.add(op);
                }
            }
        }
        //
        return result;
    }

    private boolean isLeafDataObject(Object dataObj) {
        if (dataObj instanceof PortType && myLeafType == PortType.class) {
            return true;
        } 
        return false;
    }
    
    /**
     * 
     * @param model - wsdlModel
     * @param pt - portType
     * @return true in case model contains pt or pt is null
     */
    private static boolean hasPortType(WSDLModel model, PortType portType) {
        assert model != null;
        if (portType == null) {
            return true;
        }
        
        Definitions defs = model.getDefinitions();
        if (defs == null) {
            return false;
        }
        
        Collection<PortType> pts = defs.getPortTypes();
        if (pts == null || pts.size() <= 0) {
            return false;
        }
        
        for (PortType pt : pts) {
            if (portType.equals(pt)) {
                return true;
            }
        }
        
        return false;
    }
    
    public static  List<WSDLModel> getImportedModels(TMapModel tMapModel) {
        return getImportedModels(tMapModel, null);
    }
    
    public static  List<WSDLModel> getImportedModels(TMapModel tMapModel, PortType ptTreeFilter) {
        assert tMapModel != null;
        List<WSDLModel> result = new ArrayList<WSDLModel>();
        TransformMap transformMap = tMapModel.getTransformMap();
        List<Import> imports = null;
        if (transformMap != null) {
            imports = transformMap.getImports();
        }
        if (imports != null) {
            WSDLModel tmpWsdlModel = null;
            for (Import imprt : imports) {
                try {
                    tmpWsdlModel = imprt.getImportModel();
                    if (tmpWsdlModel != null 
                            && hasPortType(tmpWsdlModel, ptTreeFilter)) 
                    {
                        result.add(tmpWsdlModel);
                        tmpWsdlModel = null;
                    }
                } catch (CatalogModelException ex) {
                    LOGGER.log(Level.WARNING, null, ex);
                }
            }
        }
        return result;
    }
    
    public static List<WSDLModel> getNonImportedModels(TMapModel tMapModel) {
        return getNonImportedModels(tMapModel, null);
    }
    
    public static List<WSDLModel> getNonImportedModels(TMapModel tMapModel, PortType ptTreeFilter) {
        assert tMapModel != null;
        List<WSDLModel> result = new ArrayList<WSDLModel>();
        Project project = SoaUtil.getProject(SoaUtil.getFileObjectByModel(tMapModel));
        if (project == null) {
            LOGGER.log(Level.WARNING, NbBundle.getMessage(ServiceParamTreeModel.class, "MSG_EMPTY_PROJECT")); // NOI18N
            return result;
        }
        List<FileObject> wsdlFiles = new ArrayList<FileObject>();
        List<ReferenceFile> projectWsdlFiles = ReferenceUtil.getWSDLResources(project);
        if (projectWsdlFiles == null) {
            return result;
        }
        for (ReferenceFile projectWSDL : projectWsdlFiles) {
            wsdlFiles.add(projectWSDL.getFile());
        }
        
        
        for (FileObject wsdlFo : wsdlFiles) {
            if (wsdlFo == null) {
                continue;
            }
            ModelSource ms = Utilities.getModelSource(wsdlFo, false);
            if (ms == null) {
                continue;
            }
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(ms);
            if (wsdlModel != null && hasPortType(wsdlModel, ptTreeFilter)) {
                result.add(wsdlModel);
            }
        }

        result.removeAll(getImportedModels(tMapModel, ptTreeFilter));
        
        return result;
    }
    
    public Boolean isLeaf(TreeItem treeItem) {
        List childrent = getChildren(treeItem);
        return childrent == null || childrent.isEmpty();
    }

    public String getDisplayName(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        String result = null;
        if (dataObj instanceof TMapModel) {
            TransformMap transformMap = ((TMapModel)dataObj).getTransformMap();
            if (transformMap != null) {
                result = new DecoratedTransformMap(transformMap).getDisplayName();
            }
        } else if (dataObj == NodeType.NON_IMPORTED_ARTIFACTS) {
            result = NodeType.NON_IMPORTED_ARTIFACTS.getDisplayName();
        } else if (dataObj instanceof WSDLModel || dataObj instanceof WSDLComponent) {
            result = WSDLTreeInfoProvider.getInstance().getDisplayName(treeItem);
        }
        //
        if (result == null) {
            result = dataObj.toString();
        }
        //
        return result;
    }

    public Icon getIcon(TreeItem treeItem) {
        Object dataObj = treeItem.getDataObject();
        //
        Icon result = null;
        if (dataObj instanceof TMapModel) {
            result = NodeType.TRANSFORMMAP.getIcon();
        } else if (dataObj == NodeType.NON_IMPORTED_ARTIFACTS) {
            result =NodeType.TRANSFORMMAP.getIcon();//[FIXME_MOVE_TO6_1] ImageUtilities.image2Icon(FolderIcon.getOpenedIcon());
        } else if (dataObj instanceof WSDLModel || dataObj instanceof WSDLComponent) {
            result = WSDLTreeInfoProvider.getInstance().getIcon(treeItem);
        } 
        //
        return result;
    }

    public String getToolTipText(TreeItem treeItem) {
        String result = WSDLTreeInfoProvider.getInstance().getToolTipText(treeItem);
        if (result != null) {
            return result;
        }
        //
        return null;
    }

}

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

package org.netbeans.modules.worklist.editor.view;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.node.NodesFactory;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;

import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.customizer.FolderNode;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode.Children;
import org.openide.util.NbBundle;

public class MessagePartChooserHelper extends ChooserHelper<WSDLComponent>{

    private Node projectsFolderNode;
    private DataObject dObj;
    
    public MessagePartChooserHelper(DataObject dObj) {
        this.dObj = dObj;
    }
    
    @Override
    public void populateNodes(Node parentNode) {
        List<Class<? extends WSDLComponent>> filters = new ArrayList<Class<? extends WSDLComponent>>();
        filters.add(PortType.class);
        
        FileObject wsdlFile = dObj.getPrimaryFile();

        projectsFolderNode = new FolderNode(new Children.Array());

        projectsFolderNode.setDisplayName(NbBundle.getMessage(MessagePartChooserHelper.class, "LBL_MessageParts_DisplayName"));
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);

            if (project != null) {
                LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);

                ArrayList<Node> nodes = new ArrayList<Node>();
                nodes.add(new WSDLProjectFolderNode(viewProvider.createLogicalView(), project, filters));

                DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFile);
                
                // TODO
                // was: Set refProjects = catalogSupport.getProjectReferences();
                Set refProjects = (catalogSupport == null) ? null : catalogSupport.getProjectReferences();
                if (refProjects != null && refProjects.size() > 0) {
                    for (Object o : refProjects) {
                        Project refPrj = (Project) o;
                        viewProvider = refPrj.getLookup().lookup(LogicalViewProvider.class);
                        nodes.add(new WSDLProjectFolderNode(viewProvider.createLogicalView(), refPrj, filters));
                    }
                }
                projectsFolderNode.getChildren().add(nodes.toArray(new Node[nodes.size()]));
            }
        }
        
        if (projectsFolderNode != null) {
            parentNode.getChildren().add(new Node[] {projectsFolderNode});
        }
    }
    

    @Override
    public Node selectNode(WSDLComponent comp) {
        if (comp == null) return null;
        return selectNode(projectsFolderNode, comp);
    }
    
    private Node selectNode(Node parentNode, WSDLComponent element) {
        org.openide.nodes.Children children = parentNode.getChildren();
        for (Node node : children.getNodes()) {
            WSDLComponent sc = null;
            if (sc == null) {
                sc = node.getLookup().lookup(WSDLComponent.class);
            }
            
            if (sc == element) {
                return node;
            }
            
            Node node1 = selectNode(node, element);
            if (node1 != null) {
                return node1;
            }
        }
        return null;
    }
    
    class WSDLProjectFolderNode extends FilterNode {
        public WSDLProjectFolderNode(Node original, Project project, List<Class<? extends WSDLComponent>> filters) {
            super(original, new WSDLProjectFolderChildren(original, project, filters));
        }
    }
    
    class WSDLProjectFolderChildren extends FilterNode.Children { // Children.Keys<FileObject> {

        private final FileObject projectDir;
        private final Project project;
        private final List<Class<? extends WSDLComponent>> filters;
        private Set<FileObject> emptySet = Collections.emptySet();

        public WSDLProjectFolderChildren(Node original, Project project,
                List<Class<? extends WSDLComponent>> filters)
        {
            super(original);
            this.project = project;
            this.filters = filters;
            this.projectDir = project.getProjectDirectory();
        }

//        public WSDLProjectFolderChildren(Project project, List<Class<? extends WSDLComponent>> filters) {
//            this.project = project;
//            this.filters = filters;
//            this.projectDir = project.getProjectDirectory();
//        }
//
//        @Override
//        public Node[] createNodes(FileObject fo) {
//            ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(fo, false);
//            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
//            NodesFactory factory = NodesFactory.getInstance();
//            return new Node[] {new FileNode(
//                    factory.createFilteredDefinitionNode(wsdlModel.getDefinitions(), filters),
//                    FileUtil.getRelativePath(projectDir, fo), 2)};
//
//        }
//
//        @Override
//        protected void addNotify() {
//            resetKeys();
//        }
//
//        @Override
//        protected void removeNotify() {
//            this.setKeys(emptySet);
//
//        }
//
//        private void resetKeys() {
//            ArrayList<FileObject> keys = new ArrayList<FileObject>();
//            LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);
//            Node node = viewProvider.createLogicalView();
//            org.openide.nodes.Children children = node.getChildren();
//            children.findChild(null);
//            Node[] childNodes = children.getNodes(true);
//            if (childNodes == null || childNodes.length == 0) {
//                childNodes = children.getNodes(true);
//            }
//            for (Node child : childNodes) {
//                DataObject dobj = child.getCookie(DataObject.class);
//                if (dobj != null) {
//                    File[] files = recursiveListFiles(FileUtil.toFile(dobj.getPrimaryFile()), new WSDLFileFilter());
//                    for (File file : files) {
//                        FileObject fo = FileUtil.toFileObject(file);
//                        keys.add(fo);
//                    }
//                }
//            }
//            this.setKeys(keys);
//        }

        @Override
        protected Node[] createNodes(Node originalNode) {
            DataObject dataObject = originalNode.getCookie(DataObject.class);

            if (dataObject == null) {
                return new Node[0];
            }

            File[] files = recursiveListFiles(FileUtil.toFile(dataObject
                    .getPrimaryFile()), new WSDLFileFilter());

            if (files == null || files.length == 0) {
                return new Node[0];
            }

            List<Node> result = new ArrayList<Node>();

            for (File file : files) {
                FileObject fileObject = FileUtil.toFileObject(file);
                if (fileObject != null) {
                    ModelSource modelSource = org.netbeans.modules.xml
                            .retriever.catalog.Utilities
                            .getModelSource(fileObject, false);
                    WSDLModel wsdlModel = WSDLModelFactory.getDefault()
                            .getModel(modelSource);
                    NodesFactory factory = NodesFactory.getInstance();
                    result.add(new FileNode(factory
                            .createFilteredDefinitionNode(wsdlModel
                            .getDefinitions(), filters), FileUtil
                            .getRelativePath(projectDir, fileObject), 2));
                }
            }

            return result.toArray(new Node[result.size()]);
        }
    }
    
    
    public static final String WSDL_FILE_EXTENSION = "wsdl";
    
    static class WSDLFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            boolean result = false;
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }

            if(fileExtension != null 
                    && (fileExtension.equalsIgnoreCase(WSDL_FILE_EXTENSION))) {
                result = true;
            }

            return result;
        }
    }

}

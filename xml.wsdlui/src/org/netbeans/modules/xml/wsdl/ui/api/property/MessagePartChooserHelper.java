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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModelFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.NodesFactory;
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
    private WSDLModel model;
    
    public MessagePartChooserHelper(WSDLModel model) {
        this.model = model;
    }
    
    @Override
    public void populateNodes(Node parentNode) {
        List<Class<? extends WSDLComponent>> filters = new ArrayList<Class<? extends WSDLComponent>>();
        filters.add(Message.class);
        
        FileObject wsdlFile = model.getModelSource().getLookup().lookup(FileObject.class);
        projectsFolderNode = new FolderNode(new Children.Array()); 
        projectsFolderNode.setDisplayName(NbBundle.getMessage(MessagePartChooserHelper.class, "LBL_MessageParts_DisplayName"));
        if(wsdlFile != null) {
            Project project = FileOwnerQuery.getOwner(wsdlFile);
            if (project != null) {
                LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);
                ArrayList<Node> nodes = new ArrayList<Node>();
                nodes.add(new WSDLProjectFolderNode(viewProvider.createLogicalView(), project, filters));

                DefaultProjectCatalogSupport catalogSupport = DefaultProjectCatalogSupport.getInstance(wsdlFile);
                Set refProjects = catalogSupport.getProjectReferences();
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
            super(original, new WSDLProjectFolderChildren(project, filters));
        }
    }
    
    class WSDLProjectFolderChildren extends Children.Keys<FileObject> {

        private final FileObject projectDir;
        private final Project project;
        private final List<Class<? extends WSDLComponent>> filters;
        private Set<FileObject> emptySet = Collections.emptySet();

        public WSDLProjectFolderChildren (Project project, List<Class<? extends WSDLComponent>> filters) {
            this.project = project;
            this.filters = filters;
            this.projectDir = project.getProjectDirectory();
        }

        @Override
        public Node[] createNodes(FileObject fo) {
            ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(fo, false); 
            WSDLModel wsdlModel = WSDLModelFactory.getDefault().getModel(modelSource);
            NodesFactory factory = NodesFactory.getInstance();
            return new Node[] {new FileNode(
                    factory.createFilteredDefinitionNode(wsdlModel.getDefinitions(), filters), 
                    FileUtil.getRelativePath(projectDir, fo), 2)};

        }

        @Override
        protected void addNotify() {
            resetKeys();
        }

        @Override
        protected void removeNotify() {
            this.setKeys(emptySet);

        }

        private void resetKeys() {
            ArrayList<FileObject> keys = new ArrayList<FileObject>();
            LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);
            Node node = viewProvider.createLogicalView();
            org.openide.nodes.Children children = node.getChildren();
            for (Node child : children.getNodes()) {
                DataObject dobj = child.getCookie(DataObject.class);
                if (dobj != null) {
                    File[] files = recursiveListFiles(FileUtil.toFile(dobj.getPrimaryFile()), new WSDLFileFilter());
                    for (File file : files) {
                        FileObject fo = FileUtil.toFileObject(file);
                        keys.add(fo);
                    }
                }
            }
            this.setKeys(keys);
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

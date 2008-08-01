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
package org.netbeans.modules.iep.editor.ps;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.xsd.nodes.FileNode;
import org.netbeans.modules.iep.editor.wizard.ChooserHelper;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPModelFactory;
import org.netbeans.modules.iep.model.InputOperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.PlanComponent;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.ui.customizer.FolderNode;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode.Children;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

public class StreamInputChooserHelper extends ChooserHelper<SchemaComponent>{

    private Logger mLogger = Logger.getLogger(StreamInputChooserHelper.class.getName());
    
    private Node inlineSchemaFolderNode;
    private Node builtinSchemaFolderNode;
    private Node projectsFolderNode;
    private List<Class<? extends SchemaComponent>> filters;
    private Project project;
    
    //Called from wizard. the model may not be in the project, if created in temporary location
    public StreamInputChooserHelper(Project project) {
        this.project = project;
    }
    
    @Override
    public void populateNodes(Node parentNode) {
        ArrayList<Node> chooserFolders = new ArrayList<Node>();
        filters = new ArrayList<Class<? extends SchemaComponent>>();
//        filters.add(GlobalSimpleType.class);
        filters.add(GlobalComplexType.class);
        filters.add(GlobalElement.class);
     
//        if (project == null) {
//            FileObject wsdlFile = model.getModelSource().getLookup().lookup(FileObject.class);
//            if(wsdlFile != null) {
//                project = FileOwnerQuery.getOwner(wsdlFile);
//            }
//        }
        if (project != null) {
            projectsFolderNode = new FolderNode(new Children.Array()); 
            projectsFolderNode.setDisplayName(NbBundle.getMessage(StreamInputChooserHelper.class, "LBL_ByFile_DisplayName"));
            LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);


            ArrayList<Node> nodes = new ArrayList<Node>();
            nodes.add(new EnabledNode(new IEPProjectFolderNode(viewProvider.createLogicalView(), project, filters)));

            DefaultProjectCatalogSupport catalogSupport = new DefaultProjectCatalogSupport(project);
            Set refProjects = catalogSupport.getProjectReferences();
            if (refProjects != null && refProjects.size() > 0) {
                for (Object o : refProjects) {
                    Project refPrj = (Project) o;
                    viewProvider = refPrj.getLookup().lookup(LogicalViewProvider.class);
                    nodes.add(new EnabledNode(new IEPProjectFolderNode(viewProvider.createLogicalView(), refPrj, filters)));
                }
            }
            
            projectsFolderNode.getChildren().add(nodes.toArray(new Node[nodes.size()]));
        }
        
//        if (model != null) {
//            Definitions def = model.getDefinitions();
//            if (def.getTypes() != null) {
//                Collection<Schema> schemas = def.getTypes().getSchemas();
//                if (schemas != null && !schemas.isEmpty()) {
//                    List<Schema> filteredSchemas = new ArrayList<Schema>();
//                    for (Schema schema : schemas) {
//                        Collection<SchemaComponent> children = schema.getChildren();
//                        for (SchemaComponent comp : children) {
//                            boolean isInstance = false;
//                            for (Class clazz : filters) {
//                                if (clazz.isInstance(comp)) {
//                                    isInstance = true;
//                                    break;
//                                }
//                            }
//                            if (isInstance) {
//                                filteredSchemas.add(schema);
//                                break;
//                            }
//                        }
//                    }
//                    if (filteredSchemas.size() > 0) {
//                        inlineSchemaFolderNode = new InlineTypesFolderNode(NodesFactory.getInstance().create(def.getTypes()), filteredSchemas, filters);
//                    }
//                }
//            }
//        }
        
//        builtinSchemaFolderNode = new BuiltInTypeFolderNode();
        
        
        if (projectsFolderNode != null) {
            chooserFolders.add(projectsFolderNode);
        }
//        if (inlineSchemaFolderNode != null) {
//            chooserFolders.add(inlineSchemaFolderNode);
//        }
        
//        chooserFolders.add(builtinSchemaFolderNode);
        
        parentNode.getChildren().add(chooserFolders.toArray(new Node[chooserFolders.size()]));
    }
    

    @Override
    public Node selectNode(SchemaComponent comp) {
        if (comp == null) return null;
        Node selected = null;
        if (comp != null) {
            String tns = comp.getModel().getSchema().getTargetNamespace();
            if (tns != null) {
                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(tns)) {
                    selected = selectNode(builtinSchemaFolderNode, comp);
                } else {
                    if (inlineSchemaFolderNode == null || (selected = selectNode(inlineSchemaFolderNode, comp)) == null) {
                        selected = projectsFolderNode != null ? selectNode(projectsFolderNode , comp) : null;
                    }
                }
            } else {
                // must be inline.
                if (inlineSchemaFolderNode != null) {
                    selected = selectNode(inlineSchemaFolderNode, comp);
                }
            }
        }
        return selected;
    }
    
    private Node selectNode(Node parentNode, SchemaComponent element) {
        org.openide.nodes.Children children = parentNode.getChildren();
        for (Node node : children.getNodes()) {
            SchemaComponent sc = null;
            SchemaComponentReference reference = node.getLookup().lookup(SchemaComponentReference.class);
            if (reference != null) {
                sc = reference.get();
            }
            if (sc == null) {
                sc = node.getLookup().lookup(SchemaComponent.class);
            }
            
            if (sc == element) {
                return node;
            }
            
            Node node1 = null;
            if ((node1 = selectNode(node, element)) != null) {
                return node1;
            }
        }
        return null;
    }
    
    class IEPProjectFolderNode extends FilterNode {
        public IEPProjectFolderNode(Node original, Project project, List<Class<? extends SchemaComponent>> filters) {
            super(original, new IEPProjectFolderChildren(project, filters));
        }
    }
    
    class IEPProjectFolderChildren extends Children.Keys<FileObject> {

        private final FileObject projectDir;
        private final Project wsdlProject;
        private final List<Class<? extends SchemaComponent>> schemaComponentFilters;
        private Set<FileObject> emptySet = Collections.emptySet();

        public IEPProjectFolderChildren (Project project, List<Class<? extends SchemaComponent>> filters) {
            this.wsdlProject = project;
            this.schemaComponentFilters = filters;
            this.projectDir = project.getProjectDirectory();
        }

        @Override
        public Node[] createNodes(FileObject fo) {
            ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(fo, false); 
            IEPModel iepModel = IEPModelFactory.getDefault().getModel(modelSource);
            try {
            DataObject dObj = DataObject.find(fo);
                if(dObj != null) {
                    Node delegate = dObj.getNodeDelegate();
                    return new Node[] {new IEPFileNode(delegate,
                                                        iepModel,
                                                        FileUtil.getRelativePath(projectDir, fo))};
                }
            } catch(Exception ex) {
                mLogger.log(Level.SEVERE, "Failed to get data object for "+ fo, ex);
            }
            
            

            return null;
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
            LogicalViewProvider viewProvider = wsdlProject.getLookup().lookup(LogicalViewProvider.class);
            Node node = viewProvider.createLogicalView();
            org.openide.nodes.Children children = node.getChildren();
            for (Node child : children.getNodes()) {
                DataObject dobj = child.getCookie(DataObject.class);
                if (dobj != null) {
                    File[] files = recursiveListFiles(FileUtil.toFile(dobj.getPrimaryFile()), new IEPFileFilter());
                    for (File file : files) {
                        FileObject fo = FileUtil.toFileObject(file);
                        ModelSource modelSource = org.netbeans.modules.xml.retriever.catalog.Utilities.getModelSource(fo, false); 
                        IEPModel iepModel = IEPModelFactory.getDefault().getModel(modelSource);
                        if (iepModel != null) {
                            keys.add(fo);
                        }
                    }
                }
            }
            this.setKeys(keys);
        }

    }
    
    public static final String IEP_FILE_EXTENSION = "iep";
    
    static class IEPFileFilter implements FileFilter {

        public boolean accept(File pathname) {
            boolean result = false;
            String fileName = pathname.getName();
            String fileExtension = null;
            int dotIndex = fileName.lastIndexOf('.');
            if(dotIndex != -1) {
                fileExtension = fileName.substring(dotIndex +1);
            }

            if(fileExtension != null 
                    && (fileExtension.equalsIgnoreCase(IEP_FILE_EXTENSION))) {
                result = true;
            }

            return result;
        }
    }
    
    class IEPFileNode extends FilterNode {

        String displayName;

        private IEPModel mModel;
        
        public IEPFileNode(Node original, IEPModel model, String path, int level) {
            super(original, new IEPFileNodeChildren(original, model, level));
            displayName = path;
            mModel = model;
        }
        
        public IEPFileNode(Node original,IEPModel model, String path) {
            this(original, model, path, 1);
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }



    }

    class IEPFileNodeChildren extends Children.Keys<InputOperatorComponent> {

        int level = 1;
        private IEPModel mModel;
        private Set emptySet = Collections.emptySet();
        
        public IEPFileNodeChildren(Node node, IEPModel model) {
            this(node, model, 1);
        }
        
        public IEPFileNodeChildren(Node node, IEPModel model, int level) {
            this.mModel = model;
            this.level = level;
            
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
            ArrayList<InputOperatorComponent> keys = new ArrayList<InputOperatorComponent>();
            
            PlanComponent planComp = mModel.getPlanComponent();
            if(planComp != null) {
                OperatorComponentContainer opContainer = planComp.getOperatorComponentContainer();
                if(opContainer != null) {
                    List<OperatorComponent> operators = opContainer.getAllOperatorComponent();
                    Iterator<OperatorComponent> it = operators.iterator();
                    while(it.hasNext()) {
                        OperatorComponent op = it.next();
                        if(op instanceof InputOperatorComponent) {
                            keys.add((InputOperatorComponent) op);
                        }
                    }
                }
                
                setKeys(keys);
            }
        }
        
        @Override
        protected Node[] createNodes(InputOperatorComponent key) {
            Node[] nodes =new Node[] {new IEPStreamInputOperatorNode(key)};
            return nodes;
        }

    }
    
    class IEPStreamInputOperatorNode extends AbstractNode {
        private InputOperatorComponent mOperator;
        
        public IEPStreamInputOperatorNode(InputOperatorComponent operator) {
            super(Children.LEAF, new ProxyLookup(Lookups.fixed(new Object[] {operator})) );
            this.mOperator = operator;
            this.setDisplayName(this.mOperator.getDisplayName());
            setIconBaseWithExtension("images/icons16x16/streamInputx16.png");
            
        }
        
       
        
    }
}

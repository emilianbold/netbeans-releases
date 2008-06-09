/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.wizard.ElementOrTypeChooserHelper;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 *
 * @author radval
 */
public class SchemaArtifactTreeModel extends DefaultTreeModel {

    private DefaultMutableTreeNode mRootNode;
            
    private Project mProject;
     
    private List<AXIComponent> mExistingArtificatNames = new ArrayList<AXIComponent>();
    
    private JTree mTree;
    
    private List<AbstractSchemaArtifactNode> mNodesToBeExpanded = new ArrayList<AbstractSchemaArtifactNode>();
    
    SchemaArtifactTreeModel(FolderNode node, JTree tree) {
        super(node, true);
        this.mTree = tree;
    }
    
    public SchemaArtifactTreeModel(FolderNode node, 
                                   Project  project, 
                                   List<AXIComponent> existingArtificatNames,
                                   JTree tree) {
        this(node, tree);
        this.mRootNode = node;
        this.mProject = project;
        this.mExistingArtificatNames = existingArtificatNames;
        populateTree();
        
        Runnable r = new Runnable() {
            public void run() {
                Iterator<AbstractSchemaArtifactNode> it = mNodesToBeExpanded.iterator();
                
                while(it.hasNext()) {
                    AbstractSchemaArtifactNode node = it.next();
                    TreePath path = new TreePath(node.getPath());
                    mTree.expandPath(path);
                }
                
            }
        };
        
        
        SwingUtilities.invokeLater(r);
    }
    
    private void populateTree() {
        ElementOrTypeChooserHelper schemaHelper = new ElementOrTypeChooserHelper(mProject);
        
        if (this.mProject != null) {
            addProjectNodes(mProject);
            
            DefaultProjectCatalogSupport catalogSupport = new DefaultProjectCatalogSupport(mProject);
            Set refProjects = catalogSupport.getProjectReferences();
            if (refProjects != null && refProjects.size() > 0) {
                for (Object o : refProjects) {
                    Project refPrj = (Project) o;
                    addProjectNodes(refPrj);
//                    nodes.add(new EnabledNode(new SchemaProjectFolderNode(viewProvider.createLogicalView(), refPrj, filters)));
                }
            }
        }
            
//            projectsFolderNode.getChildren().add(nodes.toArray(new Node[nodes.size()]));
    }
        
    private void addProjectNodes(Project project) {
        LogicalViewProvider viewProvider = project.getLookup().lookup(LogicalViewProvider.class);
        if(viewProvider != null) {
           Node projectNode = viewProvider.createLogicalView(); 
           ProjectNode pNode = new ProjectNode(projectNode, mExistingArtificatNames, mTree);
           this.mRootNode.add(pNode);
           
           //expand nodes
           List<AbstractSchemaArtifactNode> nodesToBeExpanded = pNode.getNodesToBeExpanded();
           mNodesToBeExpanded.addAll(nodesToBeExpanded);
           
        }
    }
    
    void processProjectNodes() {
        int childCount = this.mRootNode.getChildCount();
        
        for(int i =0; i < childCount; i++){
            ProjectNode pNode = (ProjectNode) this.mRootNode.getChildAt(i);
            processProjectNode(pNode);
        }
    }
    
    void processProjectNode(ProjectNode pNode) {
        int childCount = pNode.getChildCount();
        for(int i =0; i < childCount; i++){
            FileNode fNode = (FileNode) pNode.getChildAt(i);
            processFileNode(fNode);
        }
    }
    
    void processFileNode(FileNode fNode) {
        int childCount = fNode.getChildCount();
        for(int i =0; i < childCount; i++){
            
        }
    }
}

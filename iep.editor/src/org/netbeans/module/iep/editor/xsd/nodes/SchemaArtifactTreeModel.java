/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.module.iep.editor.xsd.nodes;

import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.wizard.ElementOrTypeChooserHelper;
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
     
    
    SchemaArtifactTreeModel(FolderNode node) {
        super(node, true);
    }
    
    public SchemaArtifactTreeModel(FolderNode node, Project  project) {
        this(node);
        this.mRootNode = node;
        this.mProject = project;
        populateTree();
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
           ProjectNode pNode = new ProjectNode(projectNode);
           this.mRootNode.add(pNode);
        }
    }    
}

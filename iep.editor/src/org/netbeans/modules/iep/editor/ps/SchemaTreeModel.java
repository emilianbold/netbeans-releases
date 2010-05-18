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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.iep.editor.ps;

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
import org.netbeans.modules.iep.editor.xsd.nodes.AbstractSchemaArtifactNode;
import org.netbeans.modules.iep.editor.xsd.nodes.FolderNode;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.catalogsupport.DefaultProjectCatalogSupport;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.Node;

/**
 *
 * @author radval
 */
public class SchemaTreeModel extends DefaultTreeModel {

    private DefaultMutableTreeNode mRootNode;
            
    private Project mProject;
     
    private List<String> mExistingArtificatNames = new ArrayList<String>();
    
    private JTree mTree;
    
    private List<AbstractSchemaArtifactNode> mNodesToBeExpanded = new ArrayList<AbstractSchemaArtifactNode>();
    
    SchemaTreeModel(FolderNode node, JTree tree) {
        super(node, true);
        this.mTree = tree;
    }
    
    public SchemaTreeModel(FolderNode node, 
                                   Project  project, 
                                   List<String> existingArtificatNames,
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
           SchemaProjectNode pNode = new SchemaProjectNode(projectNode, mExistingArtificatNames, mTree);
           this.mRootNode.add(pNode);
           
           //expand nodes
           List<AbstractSchemaArtifactNode> nodesToBeExpanded = pNode.getNodesToBeExpanded();
           mNodesToBeExpanded.addAll(nodesToBeExpanded);
           
        }
    }
    
    void processProjectNodes() {
        int childCount = this.mRootNode.getChildCount();
        
        for(int i =0; i < childCount; i++){
            SchemaProjectNode pNode = (SchemaProjectNode) this.mRootNode.getChildAt(i);
            processProjectNode(pNode);
        }
    }
    
    void processProjectNode(SchemaProjectNode pNode) {
        int childCount = pNode.getChildCount();
        for(int i =0; i < childCount; i++){
            SchemaFileNode fNode = (SchemaFileNode) pNode.getChildAt(i);
            processFileNode(fNode);
        }
    }
    
    void processFileNode(SchemaFileNode fNode) {
        int childCount = fNode.getChildCount();
        for(int i =0; i < childCount; i++){
            
        }
    }
}

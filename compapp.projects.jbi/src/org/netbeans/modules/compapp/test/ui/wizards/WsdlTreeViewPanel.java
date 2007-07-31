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


package org.netbeans.modules.compapp.test.ui.wizards;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Set;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;

/**
 * WsdlTreeView.java
 *
 * Created on February 6, 2006, 3:57 PM
 *
 * @author Bing Lu
 */
public class WsdlTreeViewPanel extends JPanel implements ExplorerManager.Provider {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.ui.wizards.WsdlTreeViewPanel"); // NOI18N

    private static final String SOURCES_TYPE_ICANPRO = "BIZPRO"; // NOI18N
    
    private Project mProject;
    
    private ExplorerManager mManager;
    
    private BeanTreeView mBtv;
    
    private FileObject mSelectedWsdlFile;
    
    /** Creates a new instance of WsdlTreeView */
    public WsdlTreeViewPanel(Project project) {
        mProject = project;
        init(); 
    }
    
    private void init() {
        this.setLayout(new BorderLayout());
        
        mManager = new ExplorerManager();
        mManager.addPropertyChangeListener(new ExplorerPropertyChangeListener());
        if(mProject != null) {
            
            SourceGroup[] ownerSourceGroup;
            
            List<SourceGroup> sourceGroupsList = new ArrayList<SourceGroup>();

            //wsdl in JbiProject 
            ownerSourceGroup = getSourceGroups(mProject);            
            
            //find depedent projects
            Set dependentProjects = ProjectUtil.getClasspathProjects(mProject);
            Iterator it = dependentProjects.iterator();
            while(it.hasNext()) {
                Project dProject = (Project) it.next();
                SourceGroup[] groups = getSourceGroups(dProject);
                
                if(groups != null && groups.length > 0) {
                    for(int i = 0; i < groups.length; i++) {
                        sourceGroupsList.add(groups[i]);
                    }
                }
            }
            
            AbstractNode rootNode = new AbstractNode(
                new WsdlViewNodes.SourceGroups(mProject,
                            ownerSourceGroup,
                            sourceGroupsList.toArray(new SourceGroup[]{})));
            mManager.setRootContext( rootNode );
        }        
        
        // Create the templates view
        mBtv = new BeanTreeView();
        mBtv.getAccessibleContext().setAccessibleName(
                NbBundle.getMessage(WsdlTreeViewPanel.class, "ACS_WsdlTreeView_A11YName"));  // NOI18N
        mBtv.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(WsdlTreeViewPanel.class, "ACS_WsdlTreeView_A11YDesc"));  // NOI18N
        mBtv.setRootVisible( false );
        mBtv.setSelectionMode( javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION );
        mBtv.setPopupAllowed( false );        
//        mBtv.expandAll();
        this.add(mBtv, BorderLayout.CENTER);   
    }
    
    private SourceGroup[] getSourceGroups(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        if(sources != null) {
            SourceGroup[] groups = sources.getSourceGroups(SOURCES_TYPE_ICANPRO);
            if (groups != null && groups.length > 0) {
                return groups;
            }
            groups = sources.getSourceGroups(JbiProject.SOURCES_TYPE_JBI);
            if (groups != null && groups.length > 0) {
                return groups;
            }
            if ((groups == null) || (groups.length < 1)) {
                groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            return groups;
        }
        
        return null;
    }
    
    public ExplorerManager getExplorerManager() {
        return mManager;
    }
    
    public BeanTreeView getTreeView() {
        return this.mBtv;
    }
    
    public FileObject getSelectedWsdlFile() {
        return mSelectedWsdlFile;
    }
    
    class ExplorerPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if(!evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                return;
            }
            Node[] nodes = (Node[]) evt.getNewValue();
            if(nodes.length == 0) {
                return;
            }
            Node node = nodes[0];
            WsdlViewNodes.FileObjectCookie cookie = 
                node.getCookie(WsdlViewNodes.FileObjectCookie.class);
            if(cookie != null) {
                mSelectedWsdlFile = cookie.getFileObject();
            } else {
                mSelectedWsdlFile = null;
            }
        }
    }
}

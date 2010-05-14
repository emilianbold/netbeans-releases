/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.netbeans.modules.compapp.test.ui.wizards;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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

        if (mProject != null) {
            
            SourceGroup[] ownerProjectSourceGroup;
            
            List<SourceGroup> dependentProjectSourceGroups = new ArrayList<SourceGroup>();

            // wsdls in JbiProject
            ownerProjectSourceGroup = getSourceGroups(mProject);
            
            // find depedent projects (JBI module projects and their referened projects)
            // #149180
            Set<Project> dependentProjects = 
                    ProjectUtil.getClasspathProjects(mProject, true);

            for (Project dProject : dependentProjects) {
                SourceGroup[] groups = getSourceGroups(dProject);
                
                if (groups != null) {
                    for (SourceGroup group : groups) {
                        dependentProjectSourceGroups.add(group);
                    }
                }
            }
            
            AbstractNode rootNode = new AbstractNode(
                new WsdlViewNodes.SourceGroups(mProject,
                            ownerProjectSourceGroup,
                            dependentProjectSourceGroups.toArray(new SourceGroup[]{})));
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

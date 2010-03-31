/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

public class RecentProjects extends AbstractAction implements Presenter.Menu, Presenter.Popup, PropertyChangeListener {
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_URL_KEY = "org.netbeans.modules.project.ui.RecentProjectItem.Project_URL"; // NOI18N
    private final ProjectDirListener prjDirListener = new ProjectDirListener(); 

    private UpdatingMenu subMenu;
    
    private boolean recreate;
    
    public RecentProjects() {
        super( NbBundle.getMessage(RecentProjects.class, "LBL_RecentProjectsAction_Name")); // NOI18N
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        recreate = true;
    }
    
        
    public boolean isEnabled() {
        return !OpenProjectList.getDefault().isRecentProjectsEmpty();
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        createMainSubMenu();
        return subMenu;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenu menu = createSubMenu();
        fillSubMenu(menu);
        return menu;
    }
    
    private UpdatingMenu createSubMenu() {
        UpdatingMenu menu = new UpdatingMenu(this);
        //ok to have mnenomics here, not shown on mac anyway
        menu.setMnemonic(NbBundle.getMessage(RecentProjects.class, "MNE_RecentProjectsAction_Name").charAt(0));
        return menu;
    }
    
    private void createMainSubMenu() {
        if ( subMenu == null ) {
            subMenu = createSubMenu();
            // model listening is the only lazy menu procedure that works on macosx
            subMenu.getModel().addChangeListener(subMenu);

        }
    }
        
    private void fillSubMenu(final JMenu menu) {
        menu.removeAll();

        List<UnloadedProjectInformation> projects = OpenProjectList.getDefault().getRecentProjectsInformation();
        if ( projects.isEmpty() ) {
            menu.setEnabled( false );
            return;
        }

        menu.setEnabled( true );
        ActionListener jmiActionListener = new MenuItemActionListener();
                        
        // Fill menu with items

        for (UnloadedProjectInformation p : projects) {
                URL prjDirURL = p.getURL();
                FileObject prjDir = URLMapper.findFileObject(prjDirURL);
                if ( prjDirURL == null || prjDir == null || !prjDir.isValid()) {
                    continue;
                }
                prjDir.removeFileChangeListener(prjDirListener);            
                prjDir.addFileChangeListener(prjDirListener);
                JMenuItem jmi = new JMenuItem(p.getDisplayName(), p.getIcon());
                menu.add(jmi);
                jmi.putClientProperty( PROJECT_URL_KEY, prjDirURL );
                jmi.addActionListener( jmiActionListener );
        }

    }

    // Implementation of change listener ---------------------------------------
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( OpenProjectList.PROPERTY_RECENT_PROJECTS.equals( e.getPropertyName() ) ) {
            createMainSubMenu();
            subMenu.setEnabled( !OpenProjectList.getDefault().isRecentProjectsEmpty() );
            recreate = true;
        }
        
    }
    
    
    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        public void actionPerformed( ActionEvent e ) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                
                URL url = (URL)jmi.getClientProperty( PROJECT_URL_KEY );                
                Project project = null;

                FileObject dir = URLMapper.findFileObject( url );
                if ( dir != null && dir.isFolder() ) {
                    try {
                        project = ProjectManager.getDefault().findProject( dir );
                    }       
                    catch ( IOException ioEx ) {
                        // Ignore invalid folders
                    }
                }
                
                if ( project != null ) {
                    OpenProjectList.getDefault().open( new Project[] {project}, false, true );
                    ProjectTab ptLogical = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                    Node root = ptLogical.getExplorerManager ().getRootContext ();
                    String name = ProjectUtils.getInformation(project).getName();
                    Node projNode = root.getChildren().findChild(name);
                    if (projNode != null) {
                        try {
                            ptLogical.getExplorerManager().setSelectedNodes(new Node[] {projNode});
                        } catch (PropertyVetoException ignore) {
                            // may ignore it
                        }
                    } else {
                        Logger.getLogger(RecentProjects.class.getName()).log(Level.WARNING, "Could not find {0} among {1}",
                                new Object[] {name, Arrays.asList(root.getChildren().getNodes())});
                    }
                    ProjectUtilities.makeProjectTabVisible();
                } else {
                    String msg = NbBundle.getMessage(RecentProjects.class, "ERR_InvalidProject"); //NOI18N
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg);
                    DialogDisplayer.getDefault().notify(nd);
                }
                
            }
            
        }
        
    }
    
    private class ProjectDirListener extends FileChangeAdapter {
        public void fileDeleted(FileEvent fe) {
            recreate = true;
        }
    }
    
    private class UpdatingMenu extends JMenu implements /*DynamicMenuContent,*/ ChangeListener {
        
        public UpdatingMenu(Action action) {
            super(action);
        }
        
//        public JComponent[] synchMenuPresenters(JComponent[] items) {
//            return getMenuPresenters();
//        }
//        
//        public JComponent[] getMenuPresenters() {
//            return new JComponent[] { this };
//        }
        
        public void stateChanged(ChangeEvent e) {
            if (recreate && getModel().isSelected()) {
                fillSubMenu(this);
                recreate = false;
            }
        }
        
    }
    
}

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;

public class RecentProjects extends AbstractAction implements Presenter.Menu, PropertyChangeListener {
    
    private static final String ICON = "org/netbeans/modules/project/ui/resources/empty.gif"; //NOI18N    
    
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.project.ui.RecentProjectItem"; // NOI18N
    private final ProjectDirListener prjDirListener = new ProjectDirListener(); 
    
    private JMenu subMenu;
    
    public RecentProjects() {
        super( NbBundle.getMessage(RecentProjects.class, "LBL_RecentProjectsAction_Name"), // NOI18N
              new ImageIcon(Utilities.loadImage(ICON)));
        OpenProjectList.getDefault().addPropertyChangeListener( this );
    }
    
        
    public boolean isEnabled() {
        return !OpenProjectList.getDefault().getRecentProjects().isEmpty();
    }
    
    /** Perform the action. Tries the performer and then scans the ActionMap
     * of selected topcomponent.
     */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        // no operation
    }
    
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    
    private void createSubMenu() {
        if ( subMenu == null ) {
            subMenu = new JMenu(this);
            subMenu.setMnemonic (NbBundle.getMessage(RecentProjects.class, "MNE_RecentProjectsAction_Name").charAt (0)); // NOI18N
        }
        
        subMenu.removeAll();
        
        List projects = OpenProjectList.getDefault().getRecentProjects();
        if ( projects.isEmpty() ) {
            subMenu.setEnabled( false );
            return;
        }
        
        subMenu.setEnabled( true );
        ActionListener jmiActionListener = new MenuItemActionListener(); 
                        
        // Fill menu with items
        
        for ( Iterator it = projects.iterator(); it.hasNext(); ) {
            Project p = (Project)it.next();
            FileObject prjDir = p.getProjectDirectory();
            if (prjDir == null || !prjDir.isValid()) {
                continue;
            }
            prjDir.removeFileChangeListener(prjDirListener);            
            prjDir.addFileChangeListener(prjDirListener);
            ProjectInformation pi = ProjectUtils.getInformation(p);
            JMenuItem jmi = new JMenuItem(pi.getDisplayName(), pi.getIcon());
            subMenu.add( jmi );
            jmi.putClientProperty( PROJECT_KEY, p );
            jmi.addActionListener( jmiActionListener );
        }
    }

    // Implementation of change listener ---------------------------------------
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( OpenProjectList.PROPERTY_RECENT_PROJECTS.equals( e.getPropertyName() ) ) {
            createSubMenu();
        }
        
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        public void actionPerformed( ActionEvent e ) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                Project project = (Project)jmi.getClientProperty( PROJECT_KEY );
                if ( project != null ) {
                    OpenProjectList.getDefault().open( project );
                    ProjectTab ptLogial  = ProjectTab.findDefault (ProjectTab.ID_LOGICAL);
                    Node root = ptLogial.getExplorerManager ().getRootContext ();
                    Node projNode = root.getChildren ().findChild ( project.getProjectDirectory().getName ());
                    try {
                        ptLogial.getExplorerManager ().setSelectedNodes (new Node[] {projNode});
                        ptLogial.open ();
                        ptLogial.requestActive ();
                    } catch (Exception ignore) {
                        // may ignore it
                    }
                }
                
            }
            
        }
        
    }
    
    private class ProjectDirListener extends FileChangeAdapter {
        public void fileDeleted(FileEvent fe) {
            createSubMenu();
        }
    }
    
}

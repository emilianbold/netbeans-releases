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

package org.netbeans.modules.welcome.content;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.project.ui.api.RecentProjects;
import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * Panel showing all recent projects as clickable buttons.
 * 
 * @author S. Aubrecht
 */
public class RecentProjectsPanel extends JPanel implements Constants {
    
    private static final int MAX_PROJECTS = 10;
    private PropertyChangeListener changeListener;
    
    /** Creates a new instance of RecentProjectsPanel */
    public RecentProjectsPanel() {
        super( new BorderLayout() );
        setOpaque( false );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        removeAll();
        add( rebuildContent(), BorderLayout.CENTER );
        RecentProjects.getDefault().addPropertyChangeListener( getPropertyChangeListener() );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        RecentProjects.getDefault().removePropertyChangeListener( getPropertyChangeListener() );
    }
    
    private PropertyChangeListener getPropertyChangeListener() {
        if( null == changeListener ) {
            changeListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if( RecentProjects.PROP_RECENT_PROJECT_INFO.equals( e.getPropertyName() ) ) {
                        removeAll();
                        add( rebuildContent(), BorderLayout.CENTER );
                        invalidate();
                        revalidate();
                        repaint();
                    }
                }
            };
        }
        return changeListener;
    }
    
    private JPanel rebuildContent() {
        JPanel panel = new JPanel( new GridBagLayout() );
        panel.setOpaque( false );
        int row = 0;
        List<UnloadedProjectInformation> projects = RecentProjects.getDefault().getRecentProjectInformation();
        for( UnloadedProjectInformation p : projects ) {
            addProject( panel, row++, p );
            if( row >= MAX_PROJECTS )
                break;
        }
        if( 0 == row ) {
            panel.add( new JLabel(BundleSupport.getLabel( "NoRecentProject" )), //NOI18N
                    new GridBagConstraints( 0,row,1,1,1.0,1.0,
                        GridBagConstraints.CENTER, GridBagConstraints.NONE,
                        new Insets(10,10,10,10), 0, 0 ) );
        } else {
            panel.add( new JLabel(), new GridBagConstraints( 0,row,1,1,0.0,1.0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                new Insets(0,0,0,0), 0, 0 ) );
        }
        return panel;
    }
    
    private void addProject( JPanel panel, int row, final UnloadedProjectInformation project ) {
        OpenProjectAction action = new OpenProjectAction( project );
        ActionButton b = new ActionButton( action, true, project.getURL().toString() );
        b.setFont( BUTTON_FONT );
        b.getAccessibleContext().setAccessibleName( b.getText() );
        b.getAccessibleContext().setAccessibleDescription( 
                BundleSupport.getAccessibilityDescription( "RecentProject", b.getText() ) ); //NOI18N
        panel.add( b, new GridBagConstraints( 0,row,1,1,1.0,0.0,
            GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2,2,2,2), 0, 0 ) );
    }
    
    private static class OpenProjectAction extends AbstractAction {
        private UnloadedProjectInformation project;
        public OpenProjectAction( UnloadedProjectInformation project ) {
            super( project.getDisplayName(), project.getIcon() );
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            URL url = project.getURL();
            Project prj = null;

            FileObject dir = URLMapper.findFileObject( url );
            if ( dir != null && dir.isFolder() ) {
                try {
                    prj = ProjectManager.getDefault().findProject( dir );
                }       
                catch ( IOException ioEx ) {
                    // Ignore invalid folders
                }
            }

            if ( prj != null ) {
                OpenProjects.getDefault().open( new Project[] { prj }, false );
            }
        }
    }
}

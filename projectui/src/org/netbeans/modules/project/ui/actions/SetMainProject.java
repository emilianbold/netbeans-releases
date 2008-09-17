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
import java.util.Arrays;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.api.project.ProjectInformation;
import org.openide.awt.Mnemonics;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;

public class SetMainProject extends ProjectAction implements Presenter.Menu, PropertyChangeListener {
    
    private static final String namePattern = NbBundle.getMessage( SetMainProject.class, "LBL_SetAsMainProjectAction_Name" ); // NOI18N
        
    /** Key for remembering project in JMenuItem
     */
    private static final String PROJECT_KEY = "org.netbeans.modules.project.ui.MainProjectItem"; // NOI18N
    
    protected JMenu subMenu;
    
    // private PropertyChangeListener wpcl;
    
    public SetMainProject() {
        this( null );
    }
    
    public SetMainProject( Lookup context ) {
        super( SetMainProject.class.getName() /*this is a fake command to make ActionUtils.SHORCUTS_MANAGER work*/, namePattern, null, context );
        // wpcl = WeakListeners.propertyChange( this, OpenProjectList.getDefault() );
        // OpenProjectList.getDefault().addPropertyChangeListener( wpcl );
        if ( context == null ) { 
            OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        }
        refresh( getLookup() );
    }
    
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );        
        
        if ( projects != null && projects.length > 0 ) 
        OpenProjectList.getDefault().setMainProject( projects[0] );
        
    }
    
    public void refresh( Lookup context ) {
        
        super.refresh( context );
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length != 1 /* Some projects have to be open !OpenProjectList.getDefault().isOpen( projects[0] ) */ ) {
            setEnabled( false );
        }
        else {
            setEnabled( true );
        }        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new SetMainProject( actionContext );
    }
    
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }
        
    private void createSubMenu() {
        
        Project projects[] = OpenProjectList.getDefault().getOpenProjects();
        
        Arrays.sort( projects, OpenProjectList.PROJECT_BY_DISPLAYNAME );
        
        // Enable disable the action according to number of open projects
        if ( projects == null || projects.length == 0 ) {
            setEnabled( false );
        }
        else {
            setEnabled( true );
        }
        
        if ( subMenu == null ) {
            String label = NbBundle.getMessage( SetMainProject.class, "LBL_SetMainProjectAction_Name" ); // NOI18N
            subMenu = new JMenu( label );
            //ok to have mnenomics here, not shown on mac anyway
            subMenu.setMnemonic (NbBundle.getMessage (SetMainProject.class, "MNE_SetMainProjectAction_Name").charAt (0)); // NOI18N
            //#70835: the menu bar holds only subMenu not this action. As this action listens only weakly on OPL, noone holds this action.
            //The action is the garbage collected and the sub menu does not react on changes of opened projects.
            //The action instance has to exists as long as the subMenu:
            subMenu.putClientProperty(SetMainProject.class, this);
        }
        
        subMenu.removeAll();
        ActionListener jmiActionListener = new MenuItemActionListener(); 
        
        JRadioButtonMenuItem jmiNone = new JRadioButtonMenuItem((javax.swing.Icon) null, false);
        Mnemonics.setLocalizedText(jmiNone, NbBundle.getMessage(SetMainProject.class, "LBL_NoneMainProject_Name"));
        jmiNone.addActionListener(jmiActionListener);
        subMenu.add(jmiNone);
        subMenu.add(new Separator());
        
        // Fill menu with items
        for ( int i = 0; i < projects.length; i++ ) {
            ProjectInformation pi = ProjectUtils.getInformation(projects[i]);
            JRadioButtonMenuItem jmi = new JRadioButtonMenuItem(pi.getDisplayName(), pi.getIcon(), false);
            subMenu.add( jmi );
            jmi.putClientProperty( PROJECT_KEY, projects[i] );
            jmi.addActionListener( jmiActionListener );
        }

        // Set main project
        selectMainProject();
        
        subMenu.setEnabled( projects.length > 0 );
        
    }
    
    private void selectMainProject() {

        boolean prjSelected = false;
        JRadioButtonMenuItem noneItem = null;
        for( int i = 0; i < subMenu.getItemCount(); i++ ) {
            JMenuItem jmi = subMenu.getItem( i );
            if (jmi != null) {
                Project project = (Project)jmi.getClientProperty( PROJECT_KEY );
                if (project == null) {
                    noneItem = (JRadioButtonMenuItem) jmi;
                }
                if ( jmi instanceof JRadioButtonMenuItem ) {
                    if ( OpenProjectList.getDefault().isMainProject( project ) ) {
                        ((JRadioButtonMenuItem)jmi).setSelected( true );
                        prjSelected = true;
                    }
                    else {
                        ((JRadioButtonMenuItem)jmi).setSelected( false );
                    }
                }
            }
        }
        if (!prjSelected && noneItem != null) {
            noneItem.setSelected(true);
        }
        
    }
    
    // Implementation of change listener ---------------------------------------
    
    
    public void propertyChange( PropertyChangeEvent e ) {
        
        if ( OpenProjectList.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
            createSubMenu();
        }
        else if ( OpenProjectList.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) && subMenu != null ) {
            selectMainProject();
        }
        
        
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class MenuItemActionListener implements ActionListener {
        
        public void actionPerformed( ActionEvent e ) {
            
            if ( e.getSource() instanceof JMenuItem ) {
                JMenuItem jmi = (JMenuItem)e.getSource();
                Project project = (Project)jmi.getClientProperty( PROJECT_KEY );
                OpenProjectList.getDefault().setMainProject(project);
            }
            
        }
        
    }
    
    /**
     * Variant which behaves just like the menu presenter without a context, but
     * can be displayed in a context menu.
     */
    public static final class PopupWithoutContext extends SetMainProject implements Presenter.Popup {
        
        public PopupWithoutContext() {}
        
        private PopupWithoutContext(Lookup actionContext) {
            super(actionContext);
        }
        
        public JMenuItem getPopupPresenter() {
            // Hack!
            subMenu = null;
            return getMenuPresenter();
        }
        
        public Action createContextAwareInstance(Lookup actionContext) {
            return new PopupWithoutContext(actionContext);
        }

    }
    
}

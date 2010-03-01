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
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.JPopupMenu.Separator;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.NewFileWizard;
import org.netbeans.modules.project.ui.NoProjectNew;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.modules.project.ui.ProjectUtilities;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter.Popup;

/** Action for invoking the project sensitive NewFile Wizard
 */
public class NewFile extends ProjectAction implements PropertyChangeListener, Popup {

    private static final Icon ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/project/ui/resources/newFile.png", false); //NOI18N
    private static final String _NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_Name" );
    private static final String _SHORT_DESCRIPTION = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_Tooltip" );
    private static final String POPUP_NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_PopupName" );
    private static final String FILE_POPUP_NAME = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_File_PopupName" );
    private static final String TEMPLATE_NAME_FORMAT = NbBundle.getMessage( NewFile.class, "LBL_NewFileAction_Template_PopupName" );

    private JMenu subMenu;

    public NewFile() {
        this( null );
    }

    public NewFile( Lookup context ) {
        super( (String)null, _NAME, ICON, context ); //NOI18N
        putValue("iconBase","org/netbeans/modules/project/ui/resources/newFile.png"); //NOI18N
        putValue(SHORT_DESCRIPTION, _SHORT_DESCRIPTION);
        OpenProjectList.getDefault().addPropertyChangeListener( WeakListeners.propertyChange( this, OpenProjectList.getDefault() ) );
        refresh( getLookup() );
    }

    @Override
    protected void refresh( Lookup context ) {
        // #59615: update synch if possible; only replan if not already in EQ.
        Mutex.EVENT.readAccess(new Runnable() {
            public void run() {
                setEnabled(OpenProjectList.getDefault().getOpenProjects().length > 0);
                setDisplayName(_NAME);
            }
        });
    }

    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new JMenuItem(this);
        menu.setIcon(null);
        Mnemonics.setLocalizedText(menu, (String) getValue(Action.NAME));
        // XXX accelerator not displayed here for some reason...why???
        return menu;
    }

    //private NewFileWizard wizardIterator;

    @Override
    protected void actionPerformed( Lookup context ) {
        doPerform( context, null, true );
    }

    private void doPerform( Lookup context, DataObject template, boolean inProject ) {

        if ( context == null ) {
            context = getLookup();
        }

        if ( !inProject ) {
            // Context outside of projects
            NoProjectNew.showDialog( template, preselectedFolder( context ) );
            return;
        }
        
        if (OpenProjectList.getDefault().getOpenProjects().length == 0) {
            // Can sometimes happen when pressing Ctrl-N, it seems.
            return;
        }

        NewFileWizard wd = new NewFileWizard( preselectedProject( context ) /* , null */ );
        
        DataFolder preselectedFolder = preselectedFolder( context );
        if ( preselectedFolder != null ) {
            wd.setTargetFolder( preselectedFolder );
        }

        try {
            Set resultSet = template == null ? wd.instantiate () : wd.instantiate( template );

            if (resultSet == null || resultSet.isEmpty ()) {
                // no new object, no work
                return ;
            }

            Iterator it = resultSet.iterator ();

            while (it.hasNext ()) {
                Object obj = it.next ();
                DataObject newDO = null;
                if (obj instanceof DataObject) {
                    newDO = (DataObject) obj;
                } else if (obj instanceof FileObject) {
                    try {
                        newDO = DataObject.find ((FileObject) obj);
                    } catch (DataObjectNotFoundException x) {
                        // XXX
                        assert false : obj;
                    }
                } else {
                    assert false : obj;
                }
                if (newDO != null) {
                    ProjectUtilities.openAndSelectNewObject (newDO);
                }
            }
        }
        catch ( IOException e ) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

        // Update the Templates LRU for given project
        //Project project = Templates.getProject( wd );
        FileObject foTemplate = Templates.getTemplate( wd );
        OpenProjectList.getDefault().updateTemplatesLRU( foTemplate );

    }

    // Context Aware action implementation -------------------------------------

    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new NewFile( actionContext );
    }

    // Presenter.Popup implementation ------------------------------------------

    public JMenuItem getSubmenuPopupPresenter() {
        if (subMenu == null) {
            subMenu = new JMenu(POPUP_NAME);
        }
        return subMenu;
    }

    protected void fillSubMenu() {
        Project projects[] = ActionsUtil.getProjectsFromLookup( getLookup(), null );
        if ( projects != null && projects.length > 0 ) {
            fillSubMenu(subMenu, projects[0]);
        }
        else {
            // When no project is seleceted only file and folder can be created
            fillNonProjectSubMenu(subMenu);
        }
    }

    // Private methods ---------------------------------------------------------

    private Project preselectedProject( Lookup context ) {
        Project preselectedProject = null;

        // if ( activatedNodes != null && activatedNodes.length != 0 ) {

        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        if ( projects.length > 0 ) {
            preselectedProject = projects[0];
        }


        if ( preselectedProject == null ) {
            // No project context => use main project
            preselectedProject = OpenProjectList.getDefault().getMainProject();
            if (preselectedProject == null && OpenProjectList.getDefault().getOpenProjects().length > 0) {
                // No main project => use the first one
                preselectedProject = OpenProjectList.getDefault().getOpenProjects()[0];
            }
        }

        if ( preselectedProject == null ) {
            assert false : "Action should be disabled"; // NOI18N
        }

        return preselectedProject;
    }

    private DataFolder preselectedFolder( Lookup context ) {

        DataFolder preselectedFolder = null;

        // Try to find selected folder
        preselectedFolder = context.lookup(DataFolder.class);
        if ( preselectedFolder == null ) {
            // No folder selectd try with DataObject
            DataObject dobj = context.lookup(DataObject.class);
            if ( dobj != null) {
                // DataObject found => we'll use the parent folder
                preselectedFolder = dobj.getFolder();
            }
        }

        return preselectedFolder;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        refresh( Lookup.EMPTY );
    }

    public static String TEMPLATE_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.Template"; // NOI18N
    public static String IN_PROJECT_PROPERTY = "org.netbeans.modules.project.ui.actions.NewFile.InProject"; // NOI18N


     private void fillSubMenu(JMenu menuItem, Project project) {
         menuItem.removeAll();

         ActionListener menuListener = new PopupListener();
         MessageFormat mf = new MessageFormat(TEMPLATE_NAME_FORMAT);
         boolean itemAdded = OpenProjectList.prepareTemplates(menuItem, menuListener, mf, TEMPLATE_PROPERTY, project, getLookup());
         if (itemAdded) {
             menuItem.add( new Separator() );
         }
         JMenuItem fileItem = new JMenuItem( FILE_POPUP_NAME, (Icon)getValue( Action.SMALL_ICON ) );
         fileItem.addActionListener( menuListener );
         fileItem.putClientProperty( TEMPLATE_PROPERTY, null );
         menuItem.add( fileItem );
    }

    private void fillNonProjectSubMenu(JMenu menuItem) {
        menuItem.removeAll();

        ActionListener menuListener = new PopupListener();

        DataFolder preselectedFolder = preselectedFolder( getLookup() );

        boolean canWrite;
        if ( preselectedFolder == null ) {
            canWrite = false;
        }
        else {
            FileObject pf = preselectedFolder.getPrimaryFile();
            canWrite = pf != null && pf.canWrite();
        }

        DataObject templates[] = NoProjectNew.getTemplates();
        for( int i = 0; i < templates.length; i++ ) {
            Node n = templates[i].getNodeDelegate();
            JMenuItem item = new JMenuItem(
                MessageFormat.format( TEMPLATE_NAME_FORMAT, new Object[] { n.getDisplayName() } ),
                                      new ImageIcon( n.getIcon( BeanInfo.ICON_COLOR_16x16 ) ) );
            item.addActionListener( menuListener );
            item.putClientProperty( TEMPLATE_PROPERTY, templates[i] );
            item.putClientProperty( IN_PROJECT_PROPERTY, Boolean.FALSE );
            item.setEnabled( canWrite );
            menuItem.add( item );
        }
    }

    private class PopupListener implements ActionListener {

        public void actionPerformed( ActionEvent e ) {
            JMenuItem source = (JMenuItem)e.getSource();

            Boolean inProject = (Boolean)source.getClientProperty( IN_PROJECT_PROPERTY );
            DataObject template = (DataObject)source.getClientProperty( TEMPLATE_PROPERTY );

            if ( inProject != null && inProject == Boolean.FALSE ) {
                doPerform( null, template, false );
            }
            else {
                doPerform( null, template, true );
            }
        }

    }

    /**
     * Variant for folder context menus that makes a submenu.
     */
    public static final class WithSubMenu extends NewFile {

        public WithSubMenu() {}

        private WithSubMenu(Lookup actionContext) {
            super(actionContext);
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return new DynaMenu(POPUP_NAME);
        }

        @Override
        public Action createContextAwareInstance(Lookup actionContext) {
            return new WithSubMenu(actionContext);
        }

        private final class DynaMenu extends JMenu implements DynamicMenuContent {

            public DynaMenu(String a) {
                super(a);
            }

            public JComponent[] getMenuPresenters() {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }

            public JComponent[] synchMenuPresenters(JComponent[] items) {
                JComponent jc = getSubmenuPopupPresenter();
                fillSubMenu();
                return new JComponent[]{ jc };
            }
        }
    }


}

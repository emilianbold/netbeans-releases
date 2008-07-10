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

package org.netbeans.modules.project.ui.actions;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.ProjectTab;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class SelectNodeAction extends LookupSensitiveAction implements Presenter.Menu, Presenter.Popup {
    
    // XXX Better icons
    private static final Icon SELECT_IN_PROJECTS_ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/projectTab.png" ) ); //NOI18N
    private static final Icon SELECT_IN_FILES_ICON = new ImageIcon( Utilities.loadImage( "org/netbeans/modules/project/ui/resources/filesTab.png" ) ); //NOI18N
    
    private static final String SELECT_IN_PROJECTS_NAME = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_Name" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_Name" ); // NOI18N
    
    private static final String SELECT_IN_PROJECTS_NAME_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_MenuName" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_MenuName" ); // NOI18N
    private static final String SELECT_IN_PROJECTS_NAME_MAIN_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInProjectsAction_MainMenuName" ); // NOI18N
    private static final String SELECT_IN_FILES_NAME_MAIN_MENU = NbBundle.getMessage( CloseProject.class, "LBL_SelectInFilesAction_MainMenuName" ); // NOI18N
    
    private String command;
    private ProjectActionPerformer performer;
    private String namePattern;
    
    private String findIn;
    
    public static Action inProjects() {
        SelectNodeAction a = new SelectNodeAction( SELECT_IN_PROJECTS_ICON, SELECT_IN_PROJECTS_NAME );
        a.findIn = ProjectTab.ID_LOGICAL;
        return a;
    }
    /** for test */
    static Action inProjects(Lookup lookup) {
        SelectNodeAction a = new SelectNodeAction(SELECT_IN_PROJECTS_ICON, SELECT_IN_PROJECTS_NAME, lookup);
        a.findIn = ProjectTab.ID_LOGICAL;
        return a;
    }
    
    public static Action inFiles() {
        SelectNodeAction a = new SelectNodeAction( SELECT_IN_FILES_ICON, SELECT_IN_FILES_NAME );
        a.findIn = ProjectTab.ID_PHYSICAL;
        return a;
    }
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public SelectNodeAction( Icon icon, String name ) {
        this(icon, name, null);
    }
    private SelectNodeAction(Icon icon, String name, Lookup lookup) {
        super( icon, lookup, new Class[] { DataObject.class, FileObject.class } );
        this.setDisplayName( name );
    }
    
    private SelectNodeAction(String command, ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        super( icon, lookup, new Class[] { Project.class, DataObject.class, FileObject.class } );
        this.command = command;
        this.performer = performer;
        this.namePattern = namePattern;
        refresh( getLookup() );
    }
       
    protected void actionPerformed( Lookup context ) {
        
        FileObject fo = getFileFromLookup( context );
        if ( fo != null ) {
            ProjectTab pt  = ProjectTab.findDefault( findIn );      
            pt.selectNodeAsync( fo );
        }
    }
    
    protected void refresh( Lookup context ) {        
        FileObject fo = getFileFromLookup( context );
        setEnabled( fo != null );        
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    // Presenter.Menu implementation ------------------------------------------
    
    public JMenuItem getMenuPresenter () {
        if (ProjectTab.ID_LOGICAL.equals (this.findIn)) {
            return buildPresenter(SELECT_IN_PROJECTS_NAME_MAIN_MENU);
        } else {
            return buildPresenter(SELECT_IN_FILES_NAME_MAIN_MENU);
        }
    }

    // Presenter.Popup implementation ------------------------------------------
    
    public JMenuItem getPopupPresenter() {
        if (ProjectTab.ID_LOGICAL.equals (this.findIn)) {
            return buildPresenter(SELECT_IN_PROJECTS_NAME_MENU);
        } else {
            return buildPresenter(SELECT_IN_FILES_NAME_MENU);
        }
    }

    
   // Private methods ---------------------------------------------------------
    
    private FileObject getFileFromLookup( Lookup context ) {
   
        FileObject fo = context.lookup(FileObject.class);     
        if (fo != null) {
            return fo;
        }

        DataObject dobj = context.lookup(DataObject.class);
        
        return dobj == null ? null : dobj.getPrimaryFile();
    }
    
    private JMenuItem buildPresenter (String title) {
        JMenuItem menuPresenter = new JMenuItem (this);
        Mnemonics.setLocalizedText(menuPresenter, title);
        menuPresenter.setIcon(null);
        
        return menuPresenter;
    }

    
}

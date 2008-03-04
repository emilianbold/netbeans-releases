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

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Actions;
import org.openide.awt.Mnemonics;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class ProjectAction extends LookupSensitiveAction implements Presenter.Menu, ContextAwareAction {
    
    private String command;
    private ProjectActionPerformer performer;
    private String namePattern;
    private String presenterName;
    private JMenuItem menuPresenter;
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public ProjectAction(String command, String namePattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, icon, lookup );
    }
    
    public ProjectAction( ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, icon, lookup );
    }
    
    private ProjectAction( String command, ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup ) {
        super( icon, lookup, new Class[] { Project.class, DataObject.class } );
        this.command = command;
        if ( command != null ) {
            ActionsUtil.SHORCUTS_MANAGER.registerAction( command, this );
        }
        this.performer = performer;
        this.namePattern = namePattern;
        presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    public void putValue( String key, Object value ) {
        super.putValue( key, value );
        
        if ( key == Action.ACCELERATOR_KEY ) {
            ActionsUtil.SHORCUTS_MANAGER.registerShortcut( command, value );
            
            //#68616: make sure the accelarator is propagated into the menu:
            if (menuPresenter != null) {
                menuPresenter.setAccelerator((KeyStroke) value);
            }
        }
        
    }
       
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( projects.length == 1 ) {
            if ( command != null ) {
                ActionProvider ap = projects[0].getLookup().lookup(ActionProvider.class);
                ap.invokeAction( command, Lookup.EMPTY );        
            }
            else if ( performer != null ) {
                performer.perform( projects[0] );
            }
        }
        
    }
    
    protected void refresh( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( command != null ) {
            setEnabled( projects.length == 1 );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        else if ( performer != null && projects.length == 1 ) {
            setEnabled( performer.enable( projects[0] ) );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        else {
            setEnabled( false );
            presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        }
        
        setLocalizedTextToMenuPresented(presenterName);
                        
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    protected final void setLocalizedTextToMenuPresented(String presenterName) {
        if ( menuPresenter != null ) {
            Mnemonics.setLocalizedText( menuPresenter, presenterName );
        }
    }
    
    
    // Implementation of Presenter.Menu ----------------------------------------
    
    public JMenuItem getMenuPresenter () {
        if ( menuPresenter == null ) {
            menuPresenter = new JMenuItem();
            Actions.connect(menuPresenter, this, false);
/*
            Icon icon = null;
            // ignore icon if noIconInMenu flag is set
            if (!Boolean.TRUE.equals( getValue( "noIconInMenu" ) ) ) { 
                icon = (Icon)getValue( Action.SMALL_ICON );
            }
            menuPresenter.setIcon( icon );
            Mnemonics.setLocalizedText( menuPresenter, presenterName );
 */
        }
        
        return menuPresenter;        
    }
    
    
    // Implementation of ContextAwareAction ------------------------------------
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new ProjectAction( command, performer, namePattern, (Icon)getValue( SMALL_ICON ), actionContext );
    }

    
}
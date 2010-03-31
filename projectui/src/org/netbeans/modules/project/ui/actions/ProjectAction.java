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

import java.awt.EventQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.Actions;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/** Action sensitive to current project
 * 
 * @author Pet Hrebejk 
 */
public class ProjectAction extends LookupSensitiveAction implements ContextAwareAction {
    
    private String command;
    private ProjectActionPerformer performer;
    private final String namePattern;
    private final String popupPattern;
    
    /** 
     * Constructor for global actions. E.g. actions in main menu which 
     * listen to the global context.
     *
     */
    public ProjectAction(String command, String namePattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, null, icon, lookup );
    }

    public ProjectAction(String command, String namePattern, String popupPattern, Icon icon, Lookup lookup) {
        this( command, null, namePattern, popupPattern, icon, lookup );
    }
    
    public ProjectAction( ProjectActionPerformer performer, String namePattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, null, icon, lookup );
    }

    private ProjectAction( ProjectActionPerformer performer, String namePattern, String popupPattern, Icon icon, Lookup lookup) {
        this( null, performer, namePattern, popupPattern, icon, lookup );
    }
    
    private ProjectAction( String command, ProjectActionPerformer performer, String namePattern, String popupPattern, Icon icon, Lookup lookup ) {
        super( icon, lookup, new Class[] { Project.class, DataObject.class } );
        this.command = command;
        if ( command != null ) {
            ActionsUtil.SHORCUTS_MANAGER.registerAction( command, this );
        }
        this.performer = performer;
        this.namePattern = namePattern;
        this.popupPattern = popupPattern;
        String presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    @Override
    public void putValue( String key, Object value ) {
        super.putValue( key, value );
        
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionsUtil.SHORCUTS_MANAGER.registerShortcut( command, value );
        }
        
    }
       
    @Override
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( projects.length == 1 ) {
            if ( command != null ) {
                ActionProvider ap = projects[0].getLookup().lookup(ActionProvider.class);
                LogRecord r = new LogRecord(Level.FINE, "PROJECT_ACTION"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(ProjectAction.class));
                r.setParameters(new Object[] {
                    getClass().getName(),
                    projects[0].getClass().getName(),
                    getValue(NAME)
                });
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
                ap.invokeAction( command, Lookup.EMPTY );        
            }
            else if ( performer != null ) {
                performer.perform( projects[0] );
            }
        }
        
    }
    
    @Override
    protected void refresh( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, command );
        
        if ( command != null ) {
            enable( projects.length == 1 );
        } else if ( performer != null && projects.length == 1 ) {
            enable( performer.enable( projects[0] ) );
        } else {
            enable( false );
        }
        
        String presenterName = ActionsUtil.formatProjectSensitiveName( namePattern, projects );
        putValue("menuText", presenterName); // NOI18N
        if (popupPattern != null) {
            String popupName = ActionsUtil.formatProjectSensitiveName(popupPattern, projects);
            putValue("popupText", popupName); // NOI18N
        }
                        
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    // #131674
    private void enable(final boolean enable) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setEnabled(enable);
                }
            });
        } else {
            setEnabled(enable);
        }
    }
    
    protected final String getCommand() {
        return command;
    }
    
    protected final String getNamePattern() {
        return namePattern;
    }
    
    // Implementation of ContextAwareAction ------------------------------------
    
    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new ProjectAction( command, performer, namePattern, popupPattern, (Icon)getValue( SMALL_ICON ), actionContext );
    }
}

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

import java.util.Arrays;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;

/** An action sensitive to selected node. Used for 1-off actions
 */
public final class FileCommandAction extends ProjectAction {

    private String presenterName;
        
    public FileCommandAction( String command, String namePattern, String iconResource, Lookup lookup ) {
        this( command, namePattern, ImageUtilities.loadImageIcon(iconResource, false), lookup );
    }
    
    public FileCommandAction( String command, String namePattern, Icon icon, Lookup lookup ) {
        super( command, namePattern, icon, lookup );
        assert namePattern != null : "Name patern must not be null";
        presenterName = ActionsUtil.formatName( getNamePattern(), 0, "" );
        setDisplayName( presenterName );
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    @Override
    protected void refresh( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, getCommand() );

        if ( projects.length != 1 ) {
            if (projects.length == 0 && globalProvider(context) != null) {
                setEnabled(true);
                Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
                presenterName = ActionsUtil.formatName(getNamePattern(), files.size(),
                        files.isEmpty() ? "" : files.iterator().next().getPrimaryFile().getNameExt()); // NOI18N
            } else {
                setEnabled(false); // Zero or more than one projects found or command not supported
                presenterName = ActionsUtil.formatName(getNamePattern(), 0, "");
            }
        }
        else {
            FileObject[] files = ActionsUtil.getFilesFromLookup( context, projects[0] );
            setEnabled( true );
            presenterName = ActionsUtil.formatName( getNamePattern(), files.length, files.length > 0 ? files[0].getNameExt() : "" ); // NOI18N
        }
        
        putValue("menuText", presenterName);
        putValue(SHORT_DESCRIPTION, Actions.cutAmpersand(presenterName));
    }
    
    @Override
    protected void actionPerformed( Lookup context ) {
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, getCommand() );

        if ( projects.length == 1 ) {            
            ActionProvider ap = projects[0].getLookup().lookup(ActionProvider.class);
            ap.invokeAction( getCommand(), context );
            return;
        }
        
        ActionProvider provider = globalProvider(context);
        if (provider != null) {
            provider.invokeAction(getCommand(), context);
        }
    }

    @Override
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new FileCommandAction( getCommand(), getNamePattern(), (Icon)getValue( SMALL_ICON ), actionContext );
    }

    private ActionProvider globalProvider(Lookup context) {
        for (ActionProvider ap : Lookup.getDefault().lookupAll(ActionProvider.class)) {
            if (Arrays.asList(ap.getSupportedActions()).contains(getCommand()) && ap.isActionEnabled(getCommand(), context)) {
                return ap;
            }
        }
        return null;
    }

}
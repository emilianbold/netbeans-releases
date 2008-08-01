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

package org.netbeans.modules.tasklist.projectint;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Task scanning scope for the main project and all opened projects that depend on it.
 * 
 * @author S. Aubrecht
 */
public class MainProjectScanningScope extends TaskScanningScope 
        implements PropertyChangeListener, Runnable {
    
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project currentProject;
    private Collection<FileObject> editedFiles;
    
    private MainProjectScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
    }
    
    /**
     * @return New instance of MainProjectScanningScope
     */
    public static MainProjectScanningScope create() {
        return new MainProjectScanningScope(
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "LBL_MainProjectScope" ), //NOI18N
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "HINT_MainProjectScope" ), //NOI18N
                Utilities.loadImage( "org/netbeans/modules/tasklist/projectint/main_project_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        return new MainProjectIterator( editedFiles );
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource || null == currentProject )
            return false;
        
        Project owner = FileOwnerQuery.getOwner( resource );
        if( null == owner )
            return false;
        
        if( owner.equals( currentProject ) )
            return true;
        
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for( int i=0; i<projects.length; i++ ) {
            if( projects[i].equals( currentProject ) )
                continue;

            SubprojectProvider subProjectProvider = projects[i].getLookup().lookup( SubprojectProvider.class );
            if( null != subProjectProvider 
                    && subProjectProvider.getSubprojects().contains( currentProject )
                    && projects[i].equals( owner ) ) {
                return true;
            }
        }
        
        return false;
    }

    public Lookup getLookup() {
        if( null == lookup ) {
            lookup = new AbstractLookup( lookupContent );
        }
        return lookup;
    }
    
    public void attach( Callback newCallback ) {
        if( null != newCallback && null == callback ) {
            OpenProjects.getDefault().addPropertyChangeListener( this );
            TopComponent.getRegistry().addPropertyChangeListener( this );
            setLookupContent( OpenProjects.getDefault().getMainProject() );
            if( SwingUtilities.isEventDispatchThread() ) {
                run();
            } else {
                SwingUtilities.invokeLater( this );
            }
        } else if( null == newCallback && null != callback ) {
            OpenProjects.getDefault().removePropertyChangeListener( this );
            TopComponent.getRegistry().removePropertyChangeListener( this );
            editedFiles = null;
            setLookupContent( null );
        }
        this.callback = newCallback;
    }
    
    public void propertyChange( PropertyChangeEvent e ) {
        if( OpenProjects.PROPERTY_MAIN_PROJECT.equals( e.getPropertyName() ) ) {
            if( null != callback ) {
                setLookupContent( OpenProjects.getDefault().getMainProject() );
                callback.refresh();
            }
        } else if( TopComponent.Registry.PROP_OPENED.equals( e.getPropertyName() ) ) {
            //remember which files are opened so that they can be scanned first
            run();
        }
    }
    
    public void run() {
        editedFiles = Utils.collectEditedFiles();
    }
    
    private void setLookupContent( Project newProject ) {
        if( null != currentProject ) {
            lookupContent.remove( currentProject );
        }
        if( null != newProject ) {
            lookupContent.add( newProject );
        }
        currentProject = newProject;
    }
}

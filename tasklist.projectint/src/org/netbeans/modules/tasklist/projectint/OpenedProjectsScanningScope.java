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

package org.netbeans.modules.tasklist.projectint;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/**
 * Task scanning scope for all opened projects.
 * 
 * @author S. Aubrecht
 */
public class OpenedProjectsScanningScope extends TaskScanningScope 
        implements PropertyChangeListener, Runnable {
    
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project[] currentProjects;
    private Collection<FileObject> editedFiles;
    
    /** Creates a new instance of OpenedProjectsScanningScope 
     * @param displayName 
     * @param description
     * @param icon 
     */
    private OpenedProjectsScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon );
    }
        
    /**
     * @return New instance of OpenedProjectsScanningScope
     */
    public static OpenedProjectsScanningScope create() {
        return new OpenedProjectsScanningScope( 
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "LBL_OpenedProjectsScope" ), //NOI18N
                NbBundle.getBundle( MainProjectScanningScope.class ).getString( "HINT_OpenedProjectsScope" ), //NOI18N
                Utilities.loadImage( "org/netbeans/modules/tasklist/projectint/opened_projects_scope.png" ) //NOI18N
                );
    }
    
    public Iterator<FileObject> iterator() {
        return new OpenedProjectsIterator( editedFiles );
    }
    
    @Override
    public boolean isInScope( FileObject resource ) {
        if( null == resource || null == currentProjects )
            return false;
        for( Project p : currentProjects ) {
            Sources sources = ProjectUtils.getSources( p );
            SourceGroup[] groups = sources.getSourceGroups( Sources.TYPE_GENERIC );
            for( SourceGroup group : groups ) {
                FileObject rootFolder = group.getRootFolder();
                if( FileUtil.isParentOf( rootFolder, resource ) || rootFolder.equals( resource ) )
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
            setLookupContent( OpenProjects.getDefault().getOpenProjects() );
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
        if( OpenProjects.PROPERTY_OPEN_PROJECTS.equals( e.getPropertyName() ) ) {
            if( null != callback ) {
                setLookupContent( OpenProjects.getDefault().getOpenProjects() );
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
    
    private void setLookupContent( Project[] newProjects ) {
        if( null != currentProjects ) {
            for( Project p : currentProjects ) {
                lookupContent.remove( p );
            }
        }
        if( null != newProjects ) {
            for( Project p : newProjects ) {
                lookupContent.add( p );
            }
        }
        currentProjects = newProjects;
    }
}

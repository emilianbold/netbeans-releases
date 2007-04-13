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
import java.util.Iterator;
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

/**
 * Task scanning scope for the main project and all opened projects that depend on it.
 * 
 * @author S. Aubrecht
 */
public class MainProjectScanningScope extends TaskScanningScope implements PropertyChangeListener {
    
    private Callback callback;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup;
    private Project currentProject;
    
    private MainProjectScanningScope( String displayName, String description, Image icon ) {
        super( displayName, description, icon, true );
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
        return new MainProjectIterator();
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
            setLookupContent( OpenProjects.getDefault().getMainProject() );
        } else if( null == newCallback && null != callback ) {
            OpenProjects.getDefault().removePropertyChangeListener( this );
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
        }
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

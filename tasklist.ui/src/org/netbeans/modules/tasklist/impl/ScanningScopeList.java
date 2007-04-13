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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tasklist.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author S. Aubrecht
 */
public final class ScanningScopeList {
    
    public static final String PROP_SCOPE_LIST = "scopeList"; //NOI18N
    
    private static final String SCOPE_LIST_PATH = "TaskList/ScanningScopes"; //NOI18N
    
    private static ScanningScopeList theInstance;
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private FolderLookup folderLookup;
    private Lookup.Result<TaskScanningScope> lookupRes;
    
    /** Creates a new instance of ScanningScopeList */
    private ScanningScopeList() {
    }
    
    public static ScanningScopeList getDefault() {
        if( null == theInstance ) {
            theInstance = new ScanningScopeList();
        }
        return theInstance;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        propertySupport.removePropertyChangeListener( pcl );
    }
    
    public List<TaskScanningScope> getTaskScanningScopes() {
        if( null == lookupRes ) {
            lookupRes = initLookup();
            lookupRes.addLookupListener( new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    fireScopeListChange();
                }
            });
        }
        return new ArrayList<TaskScanningScope>( lookupRes.allInstances() );
    }
    
    public TaskScanningScope getDefaultScope() {
        List<TaskScanningScope> scopes = getTaskScanningScopes();
        for( TaskScanningScope ss : scopes ) {
            if( Accessor.isDefault( ss ) ) {
                return ss;
            }
        }
        return scopes.isEmpty() ? null : scopes.get( 0 );
    }
    
    private Lookup.Result<TaskScanningScope> initLookup() {
        DataObject.Container folder = getScopeListFolder();
        Lookup lkp = null;
        if( null != folder ) {
            folderLookup = new FolderLookup( folder );
            lkp = folderLookup.getLookup();
        } else {
            lkp = Lookups.fixed( new Object[] { CurrentEditorScanningScope.create() } );
        }
        Lookup.Template<TaskScanningScope> template = new Lookup.Template<TaskScanningScope>( TaskScanningScope.class );
        Lookup.Result<TaskScanningScope> res = lkp.lookup( template );
        return res;
    }
    
    private DataObject.Container getScopeListFolder() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            FileObject folder = FileUtil.createFolder( root, SCOPE_LIST_PATH );
            return DataFolder.findContainer( folder );
        } catch( IOException ioE ) {
            Logger.getLogger( ScanningScopeList.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE );
        };
        return null;
    }
    
    private void fireScopeListChange() {
        propertySupport.firePropertyChange( PROP_SCOPE_LIST, null, getTaskScanningScopes() );
    }
}

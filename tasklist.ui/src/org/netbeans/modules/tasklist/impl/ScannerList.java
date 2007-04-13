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

package org.netbeans.modules.tasklist.impl;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.FolderLookup;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author S. Aubrecht
 */
public final class ScannerList <T>  {
    
    public static final String PROP_TASK_SCANNERS = "TaskScannerList"; //NOI18N
    
    private static final String SCANNER_LIST_PATH = "TaskList/Scanners"; //NOI18N

    private static ScannerList<FileTaskScanner> fileInstance;
    private static ScannerList<PushTaskScanner> pushInstance;
    
    private PropertyChangeSupport propertySupport;
    
    private FolderLookup folderLookup;
    private Lookup.Result<T> lkpResult;
    
    private List<T> scanners;
    private Class<T> clazz;
    
    /** Creates a new instance of ProviderList */
    private ScannerList( Class<T> clazz ) {
        this.clazz = clazz;
    }
    
    public static ScannerList<FileTaskScanner> getFileScannerList() {
        if( null == fileInstance ) {
            fileInstance = new ScannerList<FileTaskScanner>( FileTaskScanner.class );
        }
        return fileInstance;
    }
    
    public static ScannerList<PushTaskScanner> getPushScannerList() {
        if( null == pushInstance ) {
            pushInstance = new ScannerList<PushTaskScanner>( PushTaskScanner.class );
        }
        return pushInstance;
    }
    
    public List<? extends T> getScanners() {
        init();
        return scanners;
    }
    
//    private Class<? extends T> getCheckedClass() {
//        return T.class;
//    }
    
    private void init() {
        if( null == scanners ) {
            if( null == lkpResult ) {
                lkpResult = initLookup();
                lkpResult.addLookupListener( new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        scanners = null;
                        firePropertyChange();
                    }
                });
            }
            scanners = new ArrayList<T>( lkpResult.allInstances() );
        }
    }
    
    public void addPropertyChangeListener( PropertyChangeListener pcl ) {
        if( null == propertySupport )
            propertySupport = new PropertyChangeSupport( this );
        propertySupport.addPropertyChangeListener( pcl );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener pcl ) {
        if( null != propertySupport )
            propertySupport.removePropertyChangeListener( pcl );
    }
    
    private void firePropertyChange() {
        if( null != propertySupport ) {
            propertySupport.firePropertyChange( PROP_TASK_SCANNERS, null, getScanners() );
        }
    }

    private Lookup.Result<T> initLookup() {
        DataObject.Container folder = getProviderListFolder();
        Lookup lkp = null;
        if( null != folder ) {
            folderLookup = new FolderLookup( folder );
            lkp = folderLookup.getLookup();
        } else {
            lkp = Lookup.EMPTY;
        }
        
        Lookup.Template<T> template = new Lookup.Template<T>( clazz );
        Lookup.Result<T> res = lkp.lookup( template );
        return res;
    }
    
    private DataObject.Container getProviderListFolder() {
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        try {
            FileObject folder = FileUtil.createFolder( root, SCANNER_LIST_PATH );
            return DataFolder.findContainer( folder );
        } catch( IOException ioE ) {
            Logger.getLogger( ScannerList.class.getName() ).log( Level.INFO, ioE.getMessage(), ioE ); //NOI18N
        };
        return null;
    }
}

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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

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
        Lookup lkp = Lookups.forPath( SCANNER_LIST_PATH );
        
        Lookup.Template<T> template = new Lookup.Template<T>( clazz );
        Lookup.Result<T> res = lkp.lookup( template );
        return res;
    }
}

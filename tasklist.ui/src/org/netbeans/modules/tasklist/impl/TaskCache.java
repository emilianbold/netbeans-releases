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

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;

/**
 * 
 * @author S. Aubrecht
 */
class TaskCache {
    
    //TODO use URLs instead of FileObjects
    private WeakHashMap<FileObject, ScanResult> cache = new WeakHashMap<FileObject, ScanResult>();
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    /** Creates a new instance of TaskCache */
    TaskCache() {
    }
    
    public boolean isUpToDate( FileObject resource, FileTaskScanner scanner ) {
        boolean retValue = false;
        lock.readLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            retValue = scanRes.isUpToDate( resource, scanner );
        }
        lock.readLock().unlock();
        return retValue;
    }
    
    /**
     * The given file was just scanned for tasks of the given type.
     * 
     * @param tasks Tasks found in the file, may be null.
     */
    public void scanned( FileObject resource, FileTaskScanner scanner, List<? extends Task> tasks ) {
        lock.writeLock().lock();
        
        ScanResult scanRes = cache.get( resource );
        if( null == scanRes ) {
            scanRes = new ScanResult();
            cache.put( resource, scanRes );
        }
        scanRes.put( scanner, tasks );
        
        lock.writeLock().unlock();
    }
    
    public void getTasks( FileObject resource, FileTaskScanner scanner, List<Task> tasks ) {
        lock.readLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            scanRes.get( scanner, tasks );
        }
        lock.readLock().unlock();
    }
    
    /**
     * All files must be rescanned for the given task type
     */
    public void clear( FileTaskScanner scanner ) {
        lock.writeLock().lock();
        
        ArrayList<FileObject> toRemove = null;
        for( FileObject rc : cache.keySet() ) {
            ScanResult scanRes = cache.get( rc );
            scanRes.remove( scanner );
            if( scanRes.isEmpty() ) {
                if( null == toRemove ) {
                    toRemove = new ArrayList<FileObject>();
                }
                toRemove.add( rc );
            }
        }
        if( null != toRemove ) {
            for( FileObject rc : toRemove ) {
                cache.remove( rc );
            }
        }
        lock.writeLock().unlock();
    }
    
    public void clear( FileTaskScanner scanner, FileObject[] resources ) {
        lock.writeLock().lock();
        
        for( FileObject rc : resources ) {
            ScanResult scanRes = cache.get( rc );
            if( null != scanRes ) {
                scanRes.remove( scanner );
                if( scanRes.isEmpty() ) {
                    cache.remove( rc );
                }
            }
        }
        lock.writeLock().unlock();
    }
    
    public void clear( FileObject resource ) {
        lock.writeLock().lock();
        ScanResult scanRes = cache.get( resource );
        if( null != scanRes ) {
            cache.remove( resource );
        }
        lock.writeLock().unlock();
    }
}

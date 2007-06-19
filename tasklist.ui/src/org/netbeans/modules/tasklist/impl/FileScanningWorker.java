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

import org.netbeans.modules.tasklist.filter.TaskFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author S. Aubrecht
 */
class FileScanningWorker implements Runnable {
    
    private TaskCache cache;
    private TaskList taskList;
    private boolean isCancel = false;
    
    private TaskManagerImpl.FileScannerProgress progress;
    private Set<FileTaskScanner> preparedScanners = new HashSet<FileTaskScanner>();
    
    private Iterator<FileObject> resourceIterator;
    private Queue<FileObject> priorityResourceIterator = new LinkedList<FileObject>();
    private Map<FileObject, Collection<FileTaskScanner>> priorityResource2scanner 
            = new HashMap<FileObject, Collection<FileTaskScanner>>();
    
    private TaskFilter filter;
    
    private final Object SCAN_LOCK = new Object();
    private final Object SLEEP_LOCK = new Object();
    
    /** Creates a new instance of Scanner */
    public FileScanningWorker( TaskCache cache, TaskList taskList, TaskFilter filter, TaskManagerImpl.FileScannerProgress progress ) {
        this.cache = cache;
        this.taskList = taskList;
        this.filter = filter;
        this.progress = progress;
    }

    public void scan( Iterator<FileObject> resources, TaskFilter filter ) {
        abort();
        
        synchronized( SLEEP_LOCK ) {
            
            this.filter = filter;
        
            List<? extends FileTaskScanner> providers = ScannerList.getFileScannerList().getScanners();
            for( FileTaskScanner ts : providers ) {
                if( filter.isEnabled( ts ) && !preparedScanners.contains( ts ) ) {
                    ts.notifyPrepare();
                    preparedScanners.add( ts );
                }
            }
            this.resourceIterator = resources;
            
            wakeup();
        }
    }
    
    public void priorityScan( FileTaskScanner scanner, FileObject... res ) {
        boolean wakeupNeeded = false;
        synchronized( SCAN_LOCK ) {

            wakeupNeeded = isCancel || !hasNext();

            if( filter.isEnabled( scanner ) ) {
                if( !preparedScanners.contains( scanner ) ) {
                    scanner.notifyPrepare();
                    preparedScanners.add( scanner );
                }
                for( FileObject rc : res ) {
                    Collection<FileTaskScanner> scanners = priorityResource2scanner.get( rc );
                    if( null == scanners ) {
                        scanners = new ArrayList<FileTaskScanner>( 10 );
                        priorityResource2scanner.put( rc, scanners );
                    }
                    if( !priorityResourceIterator.contains( rc ) ) {
                        priorityResourceIterator.offer( rc );
                    }
                }
            }

        }
        
        if( wakeupNeeded ) {
            wakeup();
        }
    }
    
    public void priorityScan( FileObject... res ) {
        boolean wakeupNeeded = false;
        synchronized( SCAN_LOCK ) {

            wakeupNeeded = isCancel || !hasNext();

            List<? extends FileTaskScanner> scanners = ScannerList.getFileScannerList().getScanners();
            for( FileTaskScanner ts : scanners ) {
                if( filter.isEnabled( ts ) && !preparedScanners.contains( ts ) ) {
                    ts.notifyPrepare();
                    preparedScanners.add( ts );
                }
            }

            for( FileObject rc : res ) {
                priorityResource2scanner.remove( rc );
                if( !priorityResourceIterator.contains( rc ) ) {
                    priorityResourceIterator.offer( rc );
                }
            }

        }
        
        if( wakeupNeeded ) {
            wakeup();
        }
    }
    
    public void run() {
        synchronized( SLEEP_LOCK ) {
            while( true ) {

                if( killed ) {
                    return;
                }
                
                progress.started();
                
                ScanItem item = new ScanItem();
                while( true ) {

                    synchronized( SCAN_LOCK ) {
                        if( getNext( item ) ) {
                            if( !scan( item ) ) {
                                isCancel = true;
                            }
                        } else {
                            isCancel = true;
                        }
                    }

                    if( isCancel ) {
                        break;
                    }
                }

                cleanUp();
                
                try {
                    SLEEP_LOCK.wait();
                } catch( InterruptedException e ) {
                    //ignore
                }
            }
        }
    }
    
    private void wakeup() {
        synchronized( SLEEP_LOCK ) {
            isCancel = false;
            SLEEP_LOCK.notifyAll();
        }
    }
    
    void abort() {
        isCancel = true;
    }
    
    private boolean killed = false;
    void kill() {
        abort();
        killed = true;
        wakeup();
    }
    
    private List<Task> scannedTasks = new LinkedList<Task>();
    
    private boolean scan( ScanItem item ) {
        if( isCancel )
            return false;

        boolean atLeastOneProviderIsActive = false;

        for( FileTaskScanner scanner : item.scanners ) {
            //check filter for enabled providers
            if( !filter.isEnabled( scanner ) )
                continue;

            //check filter for visible items limit
            if( filter.isTaskCountLimitReached( taskList.countTasks( scanner ) ) )
                continue;

            atLeastOneProviderIsActive = true;
            scannedTasks.clear();

            if( cache.isUpToDate( item.resource, scanner ) ) {
                cache.getTasks( item.resource, scanner, scannedTasks );
            } else {
                List<? extends Task> newTasks = null;
                try {
                    if( item.resource.isValid() )
                        newTasks = scanner.scan( item.resource );
                } catch( Throwable e ) {
                    //don't let uncaught exceptions break the thread synchronization
                    Exceptions.printStackTrace( e );
                }
                if( null == newTasks ) {
                    cache.getTasks( item.resource, scanner, scannedTasks );
                } else {
                    scannedTasks.addAll( newTasks );
                    cache.scanned( item.resource, scanner, scannedTasks );
                }
            }

            if( isCancel ) {
                return false;
            }
            taskList.update( scanner, item.resource, scannedTasks, filter );
        }
        return atLeastOneProviderIsActive;
    }
    
    private void cleanUp() {
        progress.finished();
        
        synchronized( this ) {
            resourceIterator = null;
            priorityResourceIterator.clear();
            priorityResource2scanner.clear();
        }
        notifyFinished();
    }
    
    private void notifyFinished() {
        for( FileTaskScanner ts : preparedScanners ) {
            ts.notifyFinish();
        }
        preparedScanners.clear();
    }
    
    private boolean getNext( ScanItem item ) {
        item.resource = priorityResourceIterator.poll();
        item.scanners = preparedScanners;

        if( null != item.resource ) {
            item.scanners = priorityResource2scanner.get( item.resource );
            if( null == item.scanners )
                item.scanners = preparedScanners;
        } else if( null != resourceIterator && resourceIterator.hasNext() ) {
            item.resource = resourceIterator.next();
        }
        return null != item.resource;
    }
    
    private boolean hasNext() {
        return priorityResourceIterator.size() > 0
            || (null != resourceIterator && resourceIterator.hasNext() );
    }
    
    private static class ScanItem {
        FileObject resource;
        Collection<FileTaskScanner> scanners;
    }
}

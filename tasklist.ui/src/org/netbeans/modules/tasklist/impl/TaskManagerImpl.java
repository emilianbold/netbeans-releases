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

import java.util.List;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Default implementation of Task Manager
 *
 * @author S. Aubrecht
 */
public class TaskManagerImpl extends TaskManager {
    
    public static final String PROP_SCOPE = "taskScanningScope"; //NOI18N
    
    public static final String PROP_FILTER = "filter"; //NOI18N
    
    public static final String PROP_WORKING_STATUS = "workingStatus"; //NOI18N
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private FileScanningWorker worker;
    private TaskCache taskCache = new TaskCache();
    private TaskList taskList = new TaskList();
    private TaskScanningScope scope = Accessor.getEmptyScope();
    private TaskFilter filter = TaskFilter.EMPTY;
    
    private static TaskManagerImpl theInstance;
    
    private Set<PushTaskScanner> workingScanners = new HashSet<PushTaskScanner>(10);
    private boolean fileScannerWorking = false;
    private boolean workingStatus = false;
    
    public static TaskManagerImpl getInstance() {
        if( null == theInstance )
            theInstance = new TaskManagerImpl();
        return theInstance;
    }
    
    public void observe( final TaskScanningScope newScope, final TaskFilter newFilter ) {
        RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                doObserve( newScope, newFilter );
            }
        });
    }
    
    private void doObserve( TaskScanningScope newScope, TaskFilter newFilter ) {
        TaskScanningScope oldScope = scope;
        TaskFilter oldFilter = filter;
        synchronized( this ) {
            if( null == newScope || Accessor.getEmptyScope().equals( newScope ) ) {
                scope.attach( null );
                //turn off
                stopWorker();
                //stop listening to file system events
                listenToFileSystemChanges( scope, false );
                
                workingScanners.clear();
                fileScannerWorking = false;
                
                //detach simple/file scanners
                for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
                    scanner.setScope( null, null );
                }
                for( FileTaskScanner scanner : ScannerList.getFileScannerList().getScanners() ) {
                    scanner.attach( null );
                }
                scope = Accessor.getEmptyScope();
                filter = TaskFilter.EMPTY;
                
                setWorkingStatus( false );
            } else {
                //turn on or switch scope/filter
                if( null == newFilter )
                    newFilter = TaskFilter.EMPTY;
                
                if( !scope.equals(newScope) || !filter.equals(newFilter) ) {
                    
                    taskList.clear();
                    
                    if( !newScope.equals( scope ) ) {
                        scope.attach( null );
                        newScope.attach( Accessor.createCallback( this, newScope ) );
                    }
                    
                    workingScanners.clear();
                    fileScannerWorking = false;
                
                    setWorkingStatus( false );

                    scope = newScope;
                    filter = newFilter;
                    
                    attachFileScanners( newFilter, filter );
                    attachPushScanners( newScope, newFilter, filter );

                    //start listening to file system events
                    listenToFileSystemChanges( scope, true );

                    startWorker();
                    worker.scan( scope.iterator(), filter );
                }
            }
        }
        propertySupport.firePropertyChange( PROP_SCOPE, oldScope, newScope );
        propertySupport.firePropertyChange( PROP_FILTER, oldFilter, newFilter );
    }
    
    private void attachFileScanners( TaskFilter newFilter, TaskFilter oldFilter ) {
        for( FileTaskScanner scanner : getFileScanners() ) {
            if( oldFilter.isEnabled( scanner ) && !newFilter.isEnabled( scanner ) )
                scanner.attach( null );
            else if( newFilter.isEnabled( scanner ) )
                scanner.attach( Accessor.createCallback( this, scanner ) );
        }
    }
    
    private void attachPushScanners( TaskScanningScope newScope, TaskFilter newFilter, TaskFilter oldFilter ) {
        for( PushTaskScanner scanner : getPushScanners() ) {
            if( oldFilter.isEnabled( scanner ) && !newFilter.isEnabled( scanner ) )
                scanner.setScope( null, null );
            else if( newFilter.isEnabled( scanner ) )
                scanner.setScope( newScope, Accessor.createCallback( this, scanner ) );
        }
    }
    
    Iterable<? extends FileTaskScanner> getFileScanners() {
        return ScannerList.getFileScannerList().getScanners();
    }
    
    Iterable<? extends PushTaskScanner> getPushScanners() {
        return ScannerList.getPushScannerList().getScanners();
    }
    
    public void abort() {
        RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                doAbort();
            }
        });
    }
    
    private void doAbort() {
        if( null != worker )
            worker.abort();
        
        for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
            scanner.setScope( null, null );
        }
        
        workingScanners.clear();
        fileScannerWorking = false;
        setWorkingStatus( false );
    }
    
    boolean isObserved() {
        return !Accessor.getEmptyScope().equals( getScope() );
    }
    
    public TaskScanningScope getScope() {
        return scope;
    }
    
    public TaskList getTasks() {
        return taskList;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listener ) {
        propertySupport.addPropertyChangeListener( listener );
    }
    
    public void addPropertyChangeListener( String propName, PropertyChangeListener listener ) {
        propertySupport.addPropertyChangeListener( propName, listener );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listener ) {
        propertySupport.removePropertyChangeListener( listener );
    }
    
    public void removePropertyChangeListener( String propName, PropertyChangeListener listener ) {
        propertySupport.removePropertyChangeListener( propName, listener );
    }
    
    private void startWorker() {
        if( null == worker ) {
            worker = new FileScanningWorker( taskCache, taskList, filter, new FileScannerProgress() );
            RequestProcessor.getDefault().post( worker );
        }
    }
    
    private void stopWorker() {
        if( null != worker ) {
            worker.kill();
            worker = null;
        }
    }
    
    private void maybeScanResource( final FileObject rc, final boolean clearCache ) {
        RequestProcessor.getDefault().post( new Runnable() {
            public void run() {
                if( isObserved() && scope.isInScope( rc ) ) {
                    synchronized( TaskManagerImpl.this ) {
                        if( clearCache ) {
                            taskCache.clear( rc );
                            taskList.clear( rc );
                        }
                        
                        startWorker();
                        worker.priorityScan( rc );
                    }
                }
            }
        });
    }
    
    private FileChangeListener fileListener = null;
    private FileChangeListener getFileChangeListener() {
        if( null == fileListener ) {
            fileListener = new FileChangeListener() {

                public void fileFolderCreated(FileEvent fe) {
                    maybeScanResource( fe.getFile(), false );
                }

                public void fileDataCreated(FileEvent fe) {
                    maybeScanResource( fe.getFile(), false );
                }

                public void fileChanged(FileEvent fe) {
                    FileObject rc = fe.getFile();
                    
                    maybeScanResource( rc, true );
                }

                public void fileDeleted(final FileEvent fe) {
                    RequestProcessor.getDefault().post( new Runnable() {
                        public void run() {
                            synchronized( TaskManagerImpl.this ) {
                                FileObject rc = fe.getFile();
                                taskCache.clear( rc );
                                taskList.clear( rc );
                            }
                        }
                    });
                }

                public void fileRenamed(FileRenameEvent fe) {
                    //TODO rename in current model and in cache instead of rescan??
                    maybeScanResource( fe.getFile(), false );
                }

                public void fileAttributeChanged(FileAttributeEvent fe) {
                    //ignore
                }
            };
        }
        return fileListener;
    }
    
    private void listenToFileSystemChanges( final TaskScanningScope scanningScope,  final boolean addListener ) {
        FileSystem fs = getFileSystem( scanningScope );
        if( null != fs ) {
            if( addListener ) {
                fs.addFileChangeListener( getFileChangeListener() );
            } else {
                if( null != fileListener ) {
                    fs.removeFileChangeListener( getFileChangeListener() );
                    fileListener = null;
                }
            }
        }
    }
    
    private FileSystem getFileSystem( TaskScanningScope scanningScope ) {
        if( null != scanningScope ) {
            Iterator<FileObject> resources = scanningScope.iterator();
            if( resources.hasNext() ) {
                try {
                    FileObject rc = resources.next();
                    if( null != rc )
                        return rc.getFileSystem();
                }
                catch( FileStateInvalidException fsiE ) {
                    getLogger().log( Level.WARNING, fsiE.getMessage(), fsiE );
                }
            }
        }
        return null;
    }
    
    public TaskFilter getFilter() {
        return filter;
    }

    public void refresh( final FileTaskScanner scanner, final FileObject... resources) {
        synchronized( this ) {
            taskCache.clear( scanner, resources );
            taskList.clear( scanner, resources );
            if( isObserved() && isEnabled( scanner ) ) {
                
                final ArrayList<FileObject> resourcesInScope = new ArrayList<FileObject>( resources.length );
                for( int i=0; i<resources.length && null != scope; i++ ) {
                    if( scope.isInScope( resources[i] ) ) {
                        resourcesInScope.add( resources[i] );
                    }
                }
                if( !resourcesInScope.isEmpty() ) {
                
                    Runnable r = new Runnable() {
                        public void run() {
                            startWorker();
                            worker.priorityScan( scanner, resourcesInScope.toArray( new FileObject[resourcesInScope.size()] ) );
                        }
                    };
                    RequestProcessor.getDefault().post( r );
                }
            }
        }
    }
    
    private boolean isEnabled( FileTaskScanner scanner ) {
        return getFilter().isEnabled( scanner );
    }

    public void refresh( FileTaskScanner scanner ) {
        synchronized( this ) {
            taskCache.clear( scanner );
            taskList.clear( scanner );
            
            if( isObserved() && isEnabled( scanner ) ) {
                
                Runnable r = new Runnable() {
                    public void run() {
                        startWorker();
                        worker.scan( scope.iterator(), filter );
                    }
                };
                RequestProcessor.getDefault().post( r );
            }
        }
    }

    public void refresh( final TaskScanningScope scopeToRefresh ) {
        if( this.scope.equals( scopeToRefresh ) ) {
            RequestProcessor.getDefault().post( new Runnable() {
                public void run() {
                    doRefresh( scopeToRefresh );
                }
            });
        }
    }
    
    private void doRefresh( TaskScanningScope scopeToRefresh ) {
        synchronized( this ) {
            if( this.scope.equals( scopeToRefresh ) ) {
                listenToFileSystemChanges( scope, false );
                listenToFileSystemChanges( scope, true );
                taskList.clear();
                if( isObserved() ) {
                    for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
                        scanner.setScope( null, null );
                        if( getFilter().isEnabled( scanner ) )
                            scanner.setScope( scopeToRefresh, Accessor.createCallback( this, scanner ) );
                    }
                    startWorker();
                    worker.scan( scope.iterator(), filter );
                }
            }
        }
    }

    public void started(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.add( scanner );
            setWorkingStatus( true );
        }
    }

    public void finished(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.remove( scanner );
            setWorkingStatus( isWorking() );
        }
    }

    public void setTasks( PushTaskScanner scanner, FileObject resource, List<? extends Task> tasks ) {
        if( isObserved() && scope.isInScope( resource ) )
            taskList.setTasks( scanner, resource, tasks, filter );
    }
    
    public void clearAllTasks( PushTaskScanner scanner ) {
        taskList.clear( scanner );
    }
    
    private Logger getLogger() {
        return Logger.getLogger( TaskManagerImpl.class.getName() );
    }
    
    private void setWorkingStatus( boolean newStatus ) {
        synchronized( workingScanners ) {
            if( newStatus != workingStatus ) {
                boolean oldStatus = workingStatus;
                workingStatus = newStatus;
                propertySupport.firePropertyChange( PROP_WORKING_STATUS, oldStatus, newStatus );
                //for unit testing
                if( !workingStatus ) {
                    workingScanners.notifyAll();
                }
            }
        }
    }
    
    private boolean isWorking() {
        synchronized( workingScanners ) {
            return !workingScanners.isEmpty() || fileScannerWorking;
        }
    }
    
    /**
     * For unit testing only
     */
    void waitFinished() {
        synchronized( workingScanners ) {
            try         {
                workingScanners.wait();
            }
            catch( InterruptedException e ) {
                Exceptions.printStackTrace( e );
            }
        }
    }
    
    class FileScannerProgress {
        public void started() {
            synchronized( workingScanners ) {
                fileScannerWorking = true;
                setWorkingStatus( true );
            }
        }
        
        public void finished() {
            synchronized( workingScanners ) {
                fileScannerWorking = false;
                setWorkingStatus( isWorking() );
            }
        }
    }
}

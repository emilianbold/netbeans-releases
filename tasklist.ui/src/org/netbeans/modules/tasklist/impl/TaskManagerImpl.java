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

package org.netbeans.modules.tasklist.impl;

import java.util.List;
import org.netbeans.modules.tasklist.filter.TaskFilter;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.tasklist.trampoline.TaskManager;
import org.netbeans.spi.tasklist.FileTaskScanner;
import org.netbeans.spi.tasklist.PushTaskScanner;
import org.netbeans.spi.tasklist.Task;
import org.netbeans.spi.tasklist.TaskScanningScope;
import org.openide.filesystems.FileObject;
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
    
    private PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );
    
    private TaskList taskList = new TaskList();
    private TaskScanningScope scope = Accessor.getEmptyScope();
    private TaskFilter filter = TaskFilter.EMPTY;
    
    private static TaskManagerImpl theInstance;
    
    private final Set<PushTaskScanner> workingScanners = new HashSet<PushTaskScanner>(10);
    private boolean fileScannerWorking = false;

    private Loader loader;

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
                stopLoading();
                
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

                taskList.clear();
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
                
                    scope = newScope;
                    filter = newFilter;
                    
                    attachFileScanners( newFilter );
                    attachPushScanners( newScope, newFilter );

                    startLoading();
                }
            }
        }
        propertySupport.firePropertyChange( PROP_SCOPE, oldScope, newScope );
        propertySupport.firePropertyChange( PROP_FILTER, oldFilter, newFilter );
    }
    
    private void attachFileScanners( TaskFilter newFilter ) {
        for( FileTaskScanner scanner : getFileScanners() ) {
            if( !newFilter.isEnabled( scanner ) )
                scanner.attach( null );
            else if( newFilter.isEnabled( scanner ) )
                scanner.attach( Accessor.createCallback( this, scanner ) );
        }
    }
    
    private void attachPushScanners( TaskScanningScope newScope, TaskFilter newFilter ) {
        for( PushTaskScanner scanner : getPushScanners() ) {
            if( !newFilter.isEnabled( scanner ) ){
                scanner.setScope( null, null );
            }else if( newFilter.isEnabled( scanner ) ){
                scanner.setScope( newScope, Accessor.createCallback( this, scanner ) );
            }
        }
    }
    
    Iterable<? extends FileTaskScanner> getFileScanners() {
        return ScannerList.getFileScannerList().getScanners();
    }
    
    Iterable<? extends PushTaskScanner> getPushScanners() {
        return ScannerList.getPushScannerList().getScanners();
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
    
    private void startLoading() {
        if( null != loader )
            loader.cancel();

        loader = new Loader( scope, filter, taskList );
        RequestProcessor.getDefault().post(loader);
    }
    
    private void stopLoading() {
        if( null != loader )
            loader.cancel();
        loader = null;
    }
    
    public TaskFilter getFilter() {
        return filter;
    }

    public void refresh( final FileTaskScanner scanner, final FileObject... resources) {
        synchronized( this ) {
            taskList.clear( scanner, resources );
            //TODO clear index cache
            if( isObserved() && isEnabled( scanner ) ) {
                
                final ArrayList<FileObject> resourcesInScope = new ArrayList<FileObject>( resources.length );
                for( int i=0; i<resources.length && null != scope; i++ ) {
                    if( scope.isInScope( resources[i] ) ) {
                        resourcesInScope.add( resources[i] );
                    }
                }
                if( !resourcesInScope.isEmpty() ) {
                    //TODO request rescan
                }
            }
        }
    }
    
    private boolean isEnabled( FileTaskScanner scanner ) {
        return getFilter().isEnabled( scanner );
    }

    public void refresh( FileTaskScanner scanner ) {
        synchronized( this ) {
            taskList.clear( scanner );
            //TODO clear index cache
            if( isObserved() && isEnabled( scanner ) ) {
                //TODO request rescan
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
                taskList.clear();
                if( isObserved() ) {
                    for( PushTaskScanner scanner : ScannerList.getPushScannerList().getScanners() ) {
                        scanner.setScope( null, null );
                        if( getFilter().isEnabled( scanner ) )
                            scanner.setScope( scopeToRefresh, Accessor.createCallback( this, scanner ) );
                    }
                    startLoading();
                }
            }
        }
    }

    public void started(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.add( scanner );
        }
    }

    public void finished(PushTaskScanner scanner) {
        synchronized( workingScanners ) {
            workingScanners.remove( scanner );
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
            if( !isWorking() )
                return;
            _waitFinished();
        }
    }
    
    /**
     * For unit testing only
     */
    void _waitFinished() {
        synchronized( workingScanners ) {
            try {
                workingScanners.wait();
            }
            catch( InterruptedException e ) {
                Exceptions.printStackTrace( e );
            }
        }
    }
}

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
package org.netbeans.modules.timers;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/** A class for watching instances.
 *
 * @author Petr Hrebejk
 */
public class InstanceWatcher {

    private List<WeakReference<Object>> references;
    private ReferenceQueue queue;
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private transient List<WeakReference<ChangeListener>> changeListenerList;

    
    /** Creates a new instance of InstanceWatcher */
    public InstanceWatcher() {
        references = new ArrayList<WeakReference<Object>>();
        queue = new ReferenceQueue();
        new FinalizingToken();
    }
           
    public synchronized void add( Object instance ) {
        if ( ! contains( instance ) ) {
            references.add( new WeakReference( instance, queue ) );
        }
    }
    
    private synchronized boolean contains( Object o ) {
        for( WeakReference r : references ) {
            if ( r.get() == o ) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized int size() {
        removeNulls();
        return references.size();
    }
    
    /*
    public Iterator iterator() {
        
    }
    */
    
    /**
     * Registers ChangeListener to receive events. Notice that the listeners are
     * held weakly. Make sure that you create hard reference to yopur listener.
     * @param listener The listener to register.
     */
    public synchronized void addChangeListener(javax.swing.event.ChangeListener listener) {
        if (changeListenerList == null ) {
            changeListenerList = new ArrayList<WeakReference<ChangeListener>>();
        }
        changeListenerList.add(new WeakReference( listener ) );
    }

    /**
     * Removes ChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public synchronized void removeChangeListener(ChangeListener listener) {
        
        if ( listener == null ) {
            return;
        }
        
        if (changeListenerList != null ) {
            for( WeakReference<ChangeListener> r : changeListenerList ) {
                if ( listener.equals( r.get() )  ) {
                    changeListenerList.remove( r );
                }
            }
        }
        
    }
    
    // Private methods ---------------------------------------------------------    
    
    private static <T> void cleanAndCopy( List<? extends Reference<T>> src, List<? super T> dest ) {
        for( int i = src.size() - 1; i >= 0; i-- ) {
            T o = src.get(i).get();
            if( o == null ) {
                src.remove(i);
            }
            else if ( dest != null ) {
                dest.add( 0, o );
            }
        }
    }
    
    
    private synchronized void removeNulls() {
        cleanAndCopy( references, null ); 
    }
    
    private boolean cleanQueue() {
        boolean retValue = false;
        
        while( queue.poll() != null ) {
            retValue = true;
        }
        
        return retValue;
    }
    
    private void refresh() {
        if ( cleanQueue() ) {
            removeNulls();
            fireChangeListenerStateChanged();
        }
        
        new FinalizingToken();
    }
    
    private void fireChangeListenerStateChanged() {
        List<ChangeListener> list = new LinkedList<ChangeListener>();
        synchronized (this) {
            if (changeListenerList == null) {
                return;
            }            
            cleanAndCopy( changeListenerList, list );            
        }
        
        ChangeEvent e = new ChangeEvent( this );
        for (ChangeListener ch : list ) {
            ch.stateChanged (e);
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    
    private class FinalizingToken implements Runnable {
                
        public void finalize() {
            executor.submit( this ); 
        }
        
        public void run() { 
            refresh();
        }
        
    }
               
}

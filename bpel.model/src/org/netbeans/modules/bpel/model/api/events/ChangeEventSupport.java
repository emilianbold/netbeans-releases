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
package org.netbeans.modules.bpel.model.api.events;

import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.openide.ErrorManager;

/**
 * @author ads
 */
public class ChangeEventSupport {

    /**
     * Add <code>listener</code> to listener list for propogating events.
     * Listener is hold as WeakReference, so one should keep 
     * reference to listener somewhere. Otherwise this listener 
     * will be present only in listener list in this class and it will
     * be collected bu GC.
     * So anonymous class cannot be used here.
     * One needs to remember that there is possibility to remove listener.
     * If you don't have possiblity to remove listener then you will not 
     * get events.  
     * 
     * @param listener
     *            listener for add.
     */
    public void addChangeEventListener( ChangeEventListener listener ) {
        /*if ( isReadLockAcquired.get() ){ 
            // if we inside event handling then  this is bad.
            throw new IllegalStateException("Trying to add listener " +// NOI18N
                    "inside event dispatching. This is illegal state.");//NOI18N
        }*/
        writeLock.lock();
        try {
            myListeners.put(listener, null);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Add <code>listener</code> to listener list for propogating events.
     * Added <code>listener</code> will be notified with events that concern
     * only <b>parent</b> with <code>clazz</code> type. F.e. if you intrested
     * only about events that concern "link"'s then you need to set
     * <code>clazz</code> to LinkContainer class.
     * 
     * @param listener
     *            listener for add.
     * @param clazz
     *            this is intrested type for <code>listener</code>
     */
    public void addChangeEventListener( ChangeEventListener listener,
            Class<? extends BpelEntity> clazz )
    {
        /*if ( isReadLockAcquired.get() ){ 
            // if we inside event handling then  this is bad.
            throw new IllegalStateException("Trying to add listener " +// NOI18N
                    "inside event dispatching. This is illegal state.");//NOI18N
        }*/
        writeLock.lock();
        try {
            myListeners.put(listener, clazz);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Removes <code>listener</code> from listener list.
     * 
     * @param listener
     *            listener for remove.
     */
    public void removeChangeEventListener( ChangeEventListener listener ) {
        assert listener != null;
        /*if ( isReadLockAcquired.get() ){ 
            // if we inside event handling then  this is bad.
            throw new IllegalStateException("Trying to remove listener " +// NOI18N
                    "inside event dispatching. This is illegal state.");//NOI18N
        }*/
        writeLock.lock();
        try {
            myListeners.remove(listener);
        }
        finally {
            writeLock.unlock();
        }
    }

    /**
     * Notify each listener in listerner list with <code>event</code>.
     * 
     * @param event
     *            event for firing.
     */
    public void fireChangeEvent( ChangeEvent event ) {
        assert event != null;
        //readLock.lock();
        //try {
            // isReadLockAcquired.set( true );
        ChangeEventListener[] listeners;
        
        writeLock.lock();
        try {
            Set<ChangeEventListener> set = myListeners.keySet();
            listeners = set.toArray( new ChangeEventListener[ set.size()] );
        }
        finally {
            writeLock.unlock();
        }
        //for (Entry<ChangeEventListener, Class<? extends BpelEntity>> entry : set)
        for (ChangeEventListener listener : listeners ) {
            //ChangeEventListener listener = entry.getKey();
            try {
                /*if (clazz == null) {
                    notifyChangeEvent(listener, event);
                }
                else if (event.getParent().getElementType().equals(clazz)) {
                    notifyChangeEvent(listener, event);
                }*/
                notifyChangeEvent( listener , event );
            }
            catch (Exception e) {
                // when some listener throws exception while event handling
                // we catch it and inform about this, but don't allow
                // to stop handling this event for other listeners.
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    private void notifyChangeEvent( ChangeEventListener listener,
            ChangeEvent event )
    {
        if ( listener == null){
            return;
        }
        if (event instanceof PropertyRemoveEvent) {
            listener.notifyPropertyRemoved((PropertyRemoveEvent) event);
        }
        else if (event instanceof PropertyUpdateEvent) {
            listener.notifyPropertyUpdated((PropertyUpdateEvent) event);
        }
        else if (event instanceof EntityInsertEvent) {
            listener.notifyEntityInserted((EntityInsertEvent) event);
        }
        else if (event instanceof EntityRemoveEvent) {
            listener.notifyEntityRemoved((EntityRemoveEvent) event);
        }
        else if (event instanceof EntityUpdateEvent) {
            listener.notifyEntityUpdated((EntityUpdateEvent) event);
        }
        else if (event instanceof ArrayUpdateEvent) {
            listener.notifyArrayUpdated((ArrayUpdateEvent) event);
        }
    }

    private final ReentrantReadWriteLock myLock = new ReentrantReadWriteLock();
    private final Lock writeLock = myLock.writeLock();

    private WeakHashMap<ChangeEventListener, Class<? extends BpelEntity>> 
        myListeners = new WeakHashMap<ChangeEventListener, 
            Class<? extends BpelEntity>>();
}

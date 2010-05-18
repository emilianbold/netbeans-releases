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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author Vitaly Bychkov
 */
public class ProjectCloseSupport {

    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<ProjectCloseListener> myListeners = new ArrayList<ProjectCloseListener>();

    /**
     * Add a ProjectCloseListener to the listener list.
     * The listener is registered for close event.
     * The same listener object may be added more than once, and will be called
     * as many times as it is added.
     * If <code>listener</code> is null, assert exception will occure
     * is taken.
     *
     * @param listener  The ProjectCloseListener to be added
     */
    public void addProjectCloseListener(ProjectCloseListener listener) {
        assert listener != null : "Try to add null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(listener);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Remove a ProjectCloseListener from the listener list.
     * This removes a ProjectCloseListener that was registered
     * for close project event.
     * If <code>listener</code> was added more than once to the same event
     * source, it will be notified one less time after being removed.
     * If <code>listener</code> is null, assert exception will occur 
     * If <code>listener</code> was never added, no exception is
     * thrown and no action is taken.
     *
     * @param listener  The ProjectCloseListener to be removed
     */
    public void removeProjectCloseListener(ProjectCloseListener listener) {
        assert listener != null : "Try to remove null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            writeLock.unlock();
        }
    }
    
    private void removeAllProjectCloseListeners() {
        writeLock.lock();
        try {
            myListeners.removeAll(myListeners);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Invoke projectClose() method on every registered listeners.
     * After it all registered listeners are removed from listeners list.
     */
    public void fireProjectClosed() {
        writeLock.lock();
        try {
            for (ProjectCloseListener listener : myListeners) {
                listener.projectClosed();
            }
            removeAllProjectCloseListeners();
        } finally {
            writeLock.unlock();
        }
    }

    
}

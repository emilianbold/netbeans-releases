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

package org.netbeans.modules.refactoring.api.impl;

import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.openide.ErrorManager;

/**
 * Support class for progress notifications
 * @author Martin Matula, Jan Becicka
 */
public final class ProgressSupport {
    /** Utility field holding list of ProgressListeners. */
    private transient ArrayList progressListenerList = null;
    private int counter;


    public ProgressSupport() {
        progressListenerList = new ArrayList();
    }
    public synchronized void addProgressListener(ProgressListener listener) {
        progressListenerList.add(listener);
    }
    
    /** Removes ProgressListener from the list of listeners.
     * @param listener The listener to remove.
     *
     */
    public synchronized void removeProgressListener(ProgressListener listener) {
        progressListenerList.remove(listener);
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(Object source, int type, int count) {
        counter = -1;
        ArrayList list;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.START, type, count);
        synchronized (this) {
            list = (ArrayList) progressListenerList.clone();
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            try {
                ((ProgressListener) it.next()).start(event);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     *
     * @param type Type of operation that is starting.
     * @param count Number of steps the operation consists of.
     *
     */
    public void fireProgressListenerStart(int type, int count) {
        fireProgressListenerStart(this, type, count);
    }
    
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source, int count) {
        counter = count;
        ArrayList list;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STEP, 0, count);
        synchronized (this) {
            list = (ArrayList) progressListenerList.clone();
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            try {
                ((ProgressListener) it.next()).step(event);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStep(Object source) {
        fireProgressListenerStep(source, counter+1);
    }
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop(Object source) {
        ArrayList list;
        ProgressEvent event = new ProgressEvent(source, ProgressEvent.STOP);
        synchronized (this) {
            list = (ArrayList) progressListenerList.clone();
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            try {
                ((ProgressListener) it.next()).stop(event);
            } catch (RuntimeException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }
    
    /** Notifies all registered listeners about the event.
     */
    public void fireProgressListenerStop() {
        fireProgressListenerStop(this);
    }
}

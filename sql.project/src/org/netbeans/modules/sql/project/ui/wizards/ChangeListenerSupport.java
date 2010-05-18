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
package org.netbeans.modules.sql.project.ui.wizards;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class ChangeListenerSupport {

    public ChangeListenerSupport() {
    }

    private Lock writeLock = new ReentrantReadWriteLock().writeLock();
    private List<ChangeListener> myListeners = new ArrayList<ChangeListener>();

    public void addChangeListener(ChangeListener listener) {
        assert listener != null : "Try to add null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.add(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        assert listener != null : "Try to remove null listener."; // NOI18N
        writeLock.lock();
        try {
            myListeners.remove(listener);
        } finally {
            writeLock.unlock();
        }
    }

    public void fireChangeEvent(ChangeEvent e)
    {
        ChangeListener[] tmp = new ChangeListener[myListeners.size()];
        synchronized (myListeners){
            tmp = myListeners.toArray(tmp);
        }

        for (ChangeListener listener : tmp) {
            listener.stateChanged(e);
        }
    }

//    public void fireChangeEvent2(ChangeEvent e) {
//        writeLock.lock();
//        try {
//            for (ChangeListener listener : myListeners) {
//                listener.stateChanged(e);
//            }
//        } finally {
//            writeLock.unlock();
//        }
//    }
}

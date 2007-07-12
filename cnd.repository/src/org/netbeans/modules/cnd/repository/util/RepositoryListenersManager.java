/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.repository.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.repository.api.RepositoryException;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;

/**
 *
 * @author Nickolay Dalmatov
 */
public class RepositoryListenersManager {
    private static final RepositoryListenersManager instance = new RepositoryListenersManager();
    private RepositoryListener  theListener = null;
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /** Creates a new instance of RepositoryListenersManager */
    private RepositoryListenersManager() {
    }
    
    public static RepositoryListenersManager getInstance() {
        return instance;
    }
    
    public void registerListener (final RepositoryListener listener){
        try{
            rwLock.writeLock().lock();
            theListener = listener;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    public void unregisterListener(final RepositoryListener listener){
        try {
            rwLock.writeLock().lock();
            theListener = null;
        } finally {
            rwLock.writeLock().unlock();
        }
    }
    
    public boolean fireUnitOpenedEvent(final String unitName){
        boolean toOpen = true;
        try {
            rwLock.readLock().lock();
            if (theListener != null) {
                toOpen =  theListener.unitOpened(unitName);
            }
        } finally {
            rwLock.readLock().unlock();
        }
        
        return toOpen;
    }
    
    public void fireUnitClosedEvent(final String unitName) {
        try {
            rwLock.readLock().lock();
            if (theListener != null) {
                theListener.unitClosed(unitName);
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }
    
    public void fireAnException(final String unitName, final RepositoryException exception) {
        try {
            rwLock.readLock().lock();
            if (theListener != null) {
                theListener.anExceptionHappened(unitName, exception);
            } else {
                if (exception.getCause() != null) {
                    exception.getCause().printStackTrace(System.err);
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }        
    }
}

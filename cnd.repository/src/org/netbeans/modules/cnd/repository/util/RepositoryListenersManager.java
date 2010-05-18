/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

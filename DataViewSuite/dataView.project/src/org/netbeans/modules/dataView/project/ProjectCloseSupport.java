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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dataView.project;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

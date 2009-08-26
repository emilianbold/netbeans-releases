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
package org.openide.filesystems;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;


/**
 * Support class for impl. of FileChangeListener
 * @author  rm111737
 */
class FCLSupport {
    enum Op {DATA_CREATED, FOLDER_CREATED, FILE_CHANGED, FILE_DELETED, FILE_RENAMED, ATTR_CHANGED}

    /** listeners */
    ListenerList<FileChangeListener> listeners;

    /* Add new listener to this object.
    * @param l the listener
    */
    synchronized final void addFileChangeListener(FileChangeListener fcl) {
        if (listeners == null) {
            listeners = new ListenerList<FileChangeListener>();
        }

        listeners.add(fcl);
    }

    /* Remove listener from this object.
    * @param l the listener
    */
    synchronized final void removeFileChangeListener(FileChangeListener fcl) {
        if (listeners != null) {
            listeners.remove(fcl);
        }
    }

    final void dispatchEvent(FileEvent fe, Op operation, Collection<Runnable> postNotify) {
        List<FileChangeListener> fcls;

        synchronized (this) {
            if (listeners == null) {
                return;
            }

            fcls = listeners.getAllListeners();
        }

        for (FileChangeListener l : fcls) {
            dispatchEvent(l, fe, operation, postNotify);
        }
    }

    final static void dispatchEvent(final FileChangeListener fcl, final FileEvent fe, final Op operation, Collection<Runnable> postNotify) {
        boolean async = fe.isAsynchronous();
        DispatchEventWrapper dw = new DispatchEventWrapper(fcl, fe, operation);
        dw.dispatchEvent(async, postNotify);
    }
    
    /** @return true if there is a listener
    */
    synchronized final boolean hasListeners() {
        return listeners != null && listeners.hasListeners();
    }
    
    private static class DispatchEventWrapper {
        final FileChangeListener fcl;
        final FileEvent fe;
        final Op operation;
        DispatchEventWrapper(final FileChangeListener fcl, final FileEvent fe, final Op operation) {
            this.fcl =fcl;
            this.fe =fe;
            this.operation =operation;
        }
        void dispatchEvent(boolean async, Collection<Runnable> postNotify) {
            if (async) {
                q.offer(this);
                task.schedule(300);
            } else {
                dispatchEventImpl(fcl, fe, operation, postNotify);
            }
        }        
        
        private void dispatchEventImpl(FileChangeListener fcl, FileEvent fe, Op operation, Collection<Runnable> postNotify) {
            try {
                fe.setPostNotify(postNotify);
                switch (operation) {
                    case DATA_CREATED:
                        fcl.fileDataCreated(fe);
                        break;
                    case FOLDER_CREATED:
                        fcl.fileFolderCreated(fe);
                        break;
                    case FILE_CHANGED:
                        fcl.fileChanged(fe);
                        break;
                    case FILE_DELETED:
                        fcl.fileDeleted(fe);
                        break;
                    case FILE_RENAMED:
                        fcl.fileRenamed((FileRenameEvent) fe);
                        break;
                    case ATTR_CHANGED:
                        fcl.fileAttributeChanged((FileAttributeEvent) fe);
                        break;
                    default:
                        throw new AssertionError(operation);
                }
            } catch (RuntimeException x) {
                Exceptions.printStackTrace(x);
            } finally {
                fe.setPostNotify(null);
            }
        }
        
    }
    private static RequestProcessor RP = new RequestProcessor("Async FileEvent dispatcher", 1, false, false); // NOI18N
    private static final Queue<DispatchEventWrapper> q = new ConcurrentLinkedQueue<DispatchEventWrapper>();
    private static RequestProcessor.Task task = RP.create(new Runnable() {
        public void run() {
            DispatchEventWrapper dw = q.poll();
            Set<Runnable> post = new HashSet<Runnable>();
            while (dw != null) {
                dw.dispatchEvent(false, post);
                dw = q.poll();
            }
            for (Runnable r : post) {
                r.run();
            }
        }
    });           
}

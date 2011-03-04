/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.impl.fs;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Vladimir Kvashin
 */
public class RefreshManager {

    private final ExecutionEnvironment env;
    private final RequestProcessor.Task updateTask;
    
    private final LinkedList<RemoteFileObjectBase> queue = new LinkedList<RemoteFileObjectBase>();
    private final Set<RemoteFileObjectBase> set = new HashSet<RemoteFileObjectBase>();
    private final Object queueLock = new Object();
    
    private final class RefreshWorker implements Runnable {
        public void run() {
            while (true) {
                RemoteFileObjectBase fo;
                synchronized (queueLock) {
                   fo = queue.poll();
                   if (fo == null) {
                       break;
                   }
                   set.remove(fo);
                }
                try {
                    fo.refreshImpl();
                } catch (ConnectException ex) {
                    clear();
                    break;
                } catch (InterruptedException ex) {
                    RemoteLogger.finest(ex);
                    break;
                } catch (CancellationException ex) {
                    RemoteLogger.finest(ex);
                    break;
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                } catch (ExecutionException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }
    
    private void clear() {
        synchronized (queueLock) {
            queue.clear();
            set.clear();
        }
    }

    public RefreshManager(ExecutionEnvironment env) {
        this.env = env;
        updateTask = new RequestProcessor("Remote File System RefreshManager " + env.getDisplayName(), 1).create(new RefreshWorker()); //NOI18N
    }        
    
    public void scheduleRefresh(RemoteFileObjectBase fo) {
        if ( ! ConnectionManager.getInstance().isConnectedTo(env)) {
            RemoteLogger.getInstance().warning("scheduleRefresh(FileObject) is called while host is not connected");
        }        
        synchronized (queueLock) {
            queue.add(fo);
            set.add(fo);
            updateTask.schedule(0);
        }
    }
    
    public void scheduleRefresh(Collection<RemoteFileObjectBase> fileObjects) {
        if ( ! ConnectionManager.getInstance().isConnectedTo(env)) {
            RemoteLogger.getInstance().warning("scheduleRefresh(Collection<FileObject>) is called while host is not connected");
        }        
        synchronized (queueLock) {
            queue.addAll(fileObjects);
            set.addAll(fileObjects);
        }
        updateTask.schedule(0);
    }    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VirtualMachine;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Watches execution in a single thread and detects whether it is blocked
 * by some other suspended threads.
 *
 * @author Martin Entlicher
 */
public class SingleThreadWatcher implements Runnable {

    private static final int DELAY = 3000;

    private final RequestProcessor threadWatchRP = new RequestProcessor("Debugger Single Thread Watch", 1);

    private JPDAThreadImpl t;
    private RequestProcessor.Task watchTask;

    public SingleThreadWatcher(JPDAThreadImpl t) {
        this.t = t;
        watchTask = threadWatchRP.post(this, DELAY);
    }

    public synchronized void destroy() {
        if (watchTask == null) {
            return ;
        }
        watchTask.cancel();
        watchTask = null;
        t.setLockerThreads(null);
    }

    public void run() {
        synchronized (this) {
            if (watchTask == null) {
                return ;
            }
        }
        boolean areLocks = checkLocks();
        if (!areLocks) {
            synchronized (this) {
                if (watchTask != null) {
                    watchTask.schedule(DELAY);
                }
            }
        }
    }

    private boolean checkLocks() {
        ThreadReference tr = t.getThreadReference();
        VirtualMachine vm = tr.virtualMachine();
        List<JPDAThread> threads = null;
        //synchronized (t.getDebugger().LOCK) { - can not synchronize on that - method invocation uses this lock.
            vm.suspend();
            try {
                ObjectReference waitingMonitor = tr.currentContendedMonitor();
                if (waitingMonitor != null) {
                    List<ThreadReference> lockedThreads = findLockPath(tr, waitingMonitor);
                    if (lockedThreads != null) {
                        threads = new ArrayList<JPDAThread>(lockedThreads.size());
                        for (ThreadReference ltr : lockedThreads) {
                            JPDAThread lt = t.getDebugger().getThread(ltr);
                            threads.add(lt);
                        }
                    }
                }
            } catch (IncompatibleThreadStateException ex) {
            } finally {
                vm.resume();
            }
        //}
        t.setLockerThreads(threads);
        return threads != null;
    }

    private List<ThreadReference> findLockPath(ThreadReference tr, ObjectReference waitingMonitor) throws IncompatibleThreadStateException {
        List<ThreadReference> threads = new ArrayList<ThreadReference>();
        Map<ObjectReference, ThreadReference> monitorMap = new HashMap<ObjectReference, ThreadReference>();
        for (ThreadReference t : tr.virtualMachine().allThreads()) {
            List<ObjectReference> monitors = t.ownedMonitors();
            for (ObjectReference m : monitors) {
                monitorMap.put(m, t);
            }
        }
        while (tr != null && waitingMonitor != null) {
            tr = monitorMap.get(waitingMonitor);
            if (tr != null) {
                threads.add(tr);
                waitingMonitor = tr.currentContendedMonitor();
            }
        }
        return threads;
    }
}

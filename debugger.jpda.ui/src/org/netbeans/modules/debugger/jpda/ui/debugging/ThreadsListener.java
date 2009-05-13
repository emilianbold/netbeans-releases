/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.DeadlockDetector;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.openide.util.Exceptions;

public class ThreadsListener extends DebuggerManagerAdapter {

    private static ThreadsListener instance;
    final LinkedList<JPDAThread> currentThreadsHistory = new LinkedList();
    final BreakpointHits hits = new BreakpointHits();
    private Map<JPDADebugger, DebuggerListener> debuggerToListener = new WeakHashMap<JPDADebugger, DebuggerListener>();
    private JPDADebugger currentDebugger = null;
    private DebuggingView debuggingView;
    
    public ThreadsListener() {
        instance = this;
    }

    public static ThreadsListener getDefault() {
        return instance;
    }
    
    public void setDebuggingView(DebuggingView debuggingView) {
        this.debuggingView = debuggingView;
        InfoPanel infoPanel = debuggingView.getInfoPanel();
        infoPanel.setBreakpointHits(getHits());
    }
    
    public synchronized void changeDebugger(JPDADebugger deb) {
        if (currentDebugger == deb) {
            return;
        }
        if (deb != null) {
            JPDAThread currThread = deb.getCurrentThread();
            if (currThread != null) {
                synchronized(currentThreadsHistory) {
                    currentThreadsHistory.remove(currThread);
                    currentThreadsHistory.addFirst(currThread);
                }
            }
        }
        if (debuggingView == null) {
            this.currentDebugger = deb;
            return;
        }
        if (currentDebugger != null) {
            InfoPanel infoPanel = debuggingView.getInfoPanel();
            infoPanel.setShowDeadlock(false);
            infoPanel.setShowThreadLocks(null, null);
            infoPanel.setShowStepBrkp(null, null, null);
        }
        if (deb != null) {
            InfoPanel infoPanel = debuggingView.getInfoPanel();
            DebuggerListener listener = debuggerToListener.get(deb);
            if (listener != null) {
                DeadlockDetector detector = deb.getThreadsCollector().getDeadlockDetector();
                detector.addPropertyChangeListener(this);
                if (detector.getDeadlocks() != null) {
                    infoPanel.setShowDeadlock(true);
                }
                infoPanel.setShowThreadLocks(listener.lockedThread, listener.lockerThreads);
                infoPanel.setShowStepBrkp(listener.debugger, listener.stepBrkpThread, listener.stepBrkpBreakpoint);
            }
            infoPanel.recomputeMenuItems(getHits());
        } else {
            // Release reference to DebuggingView when there's no debugger.
            debuggingView = null;
        }
        this.currentDebugger = deb;
    }
    
    public synchronized List<JPDAThread> getCurrentThreadsHistory() {
        synchronized(currentThreadsHistory) {
            List<JPDAThread> result = new ArrayList<JPDAThread>(currentThreadsHistory.size());
            for (JPDAThread thread : currentThreadsHistory) {
                if (thread.isSuspended()) {
                    result.add(thread);
                }
            }
            return result;
        }
    }
    
    public synchronized List<JPDAThread> getThreads() {
        List<JPDAThread> result = new ArrayList<JPDAThread>();
        for (JPDADebugger debugger : debuggerToListener.keySet()) {
            if (debugger != null && debugger.getState() != JPDADebugger.STATE_DISCONNECTED) {
                result.addAll(debugger.getThreadsCollector().getAllThreads());
            }
        }
        return result;
    }

    private void addBreakpointHit(JPDAThread thread) {
        if (thread != null && !hits.contains(thread)) {
            // System.out.println("Hit added: " + thread.getName());
            hits.add(thread);
            if (debuggingView != null) {
                debuggingView.getInfoPanel().addBreakpointHit(thread, hits.size());
            }
        }
    }

    private void removeBreakpointHit(JPDAThread thread) {
        if (thread != null && hits.contains(thread)) {
            // System.out.println("Hit removed: " + thread.getName());
            hits.remove(thread);
            if (debuggingView != null) {
                debuggingView.getInfoPanel().removeBreakpointHit(thread, hits.size());
            }
        }
    }
    
    public synchronized List<JPDAThread> getHits() {
        List<JPDAThread> result = new ArrayList<JPDAThread>();
        for (JPDAThread thread : hits.stoppedThreads) {
            result.add(thread);
        }
        return result;
    }
    
    public synchronized int getHitsCount() {
        return hits.size();
    }
    
    public synchronized boolean isBreakpointHit(JPDAThread thread) {
        return hits.contains(thread);
    }
    
    public synchronized void goToHit() {
        hits.goToHit();
    }
    
    public JPDADebugger getDebugger() {
        return currentDebugger;
    }

    @Override
    public String[] getProperties () {
        return new String[] {DebuggerManager.PROP_DEBUGGER_ENGINES};
    }

    @Override
    public synchronized void engineAdded(DebuggerEngine engine) {
        JPDADebugger deb = engine.lookupFirst(null, JPDADebugger.class);
        if (deb != null) {
            DebuggerListener listener = new DebuggerListener(deb);
            debuggerToListener.put(deb, listener);
            if (debuggingView != null) {
                debuggingView.updateSessionsComboBox();
            }
        }
    }

    @Override
    public synchronized void engineRemoved(DebuggerEngine engine) {
        JPDADebugger deb = engine.lookupFirst(null, JPDADebugger.class);
        if (deb != null) {
            DebuggerListener listener = debuggerToListener.remove(deb);
            if (listener != null) {
                listener.unregister();
            }
            if (debuggingView != null) {
                debuggingView.updateSessionsComboBox();
            }
        }
    }
    
    // **************************************************************************
    // inner classes
    // **************************************************************************

    class DebuggerListener implements PropertyChangeListener {

        private JPDADebugger debugger;
        Set<JPDAThread> threads = new HashSet<JPDAThread>();
        List<JPDAThread> lockerThreads;
        JPDAThread lockedThread;
        JPDAThread stepBrkpThread;
        JPDABreakpoint stepBrkpBreakpoint;

        DebuggerListener(JPDADebugger debugger) {
            this.debugger = debugger;
            debugger.addPropertyChangeListener(this);
            List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
            for (JPDAThread thread : allThreads) {
                threads.add(thread);
                ((Customizer)thread).addPropertyChangeListener(this);
            }
            DeadlockDetector detector = debugger.getThreadsCollector().getDeadlockDetector();
            detector.addPropertyChangeListener(this);
        }

        public synchronized void propertyChange(PropertyChangeEvent evt) {
            //System.out.println("PROP. NAME: " + evt.getPropertyName() + ", " + evt.getSource().getClass().getSimpleName());
            String propName = evt.getPropertyName();
            Object source = evt.getSource();

            if (source instanceof JPDADebugger) {
                if (JPDADebugger.PROP_THREAD_STARTED.equals(propName)) {
                    //System.out.println("STARTED: " + evt.getNewValue());
                    final JPDAThread jpdaThread = (JPDAThread)evt.getNewValue();
                    if (threads.add(jpdaThread)) {
                        ((Customizer)jpdaThread).addPropertyChangeListener(this);
                        // System.out.println("WATCHED: " + jpdaThread.getName());
                    }
                } else if (JPDADebugger.PROP_THREAD_DIED.equals(propName)) {
                    //System.out.println("DIED: " + evt.getOldValue());
                    JPDAThread jpdaThread = (JPDAThread)evt.getOldValue();
                    if (threads.remove(jpdaThread)) {
                        synchronized(currentThreadsHistory) {
                            currentThreadsHistory.remove(jpdaThread);
                        }
                        ((Customizer)jpdaThread).removePropertyChangeListener(this);
                        // System.out.println("RELEASED: " + jpdaThread.getName());
                    }
                } else if (JPDADebugger.PROP_CURRENT_THREAD.equals(propName)) {
                    JPDAThread currentThread = debugger.getCurrentThread();
                    removeBreakpointHit(currentThread);
                    synchronized(currentThreadsHistory) {
                        currentThreadsHistory.remove(currentThread);
                        currentThreadsHistory.addFirst(currentThread);
                    }
                } else if (JPDADebugger.PROP_STATE.equals(propName) &&
                        debugger != null && debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    unregister();
                }
            } else if (source instanceof JPDAThread) {
                final JPDAThread thread = (JPDAThread)source;
                if (JPDAThread.PROP_BREAKPOINT.equals(propName)) {
                    // System.out.println("THREAD: " + thread.getName() + ", curr: " + isCurrent(thread) + ", brk: " + isAtBreakpoint(thread));
                    if (!isCurrent(thread)) {
                        if (isAtBreakpoint(thread)) {
                            addBreakpointHit(thread);
                        } else {
                            removeBreakpointHit(thread);
                        }
                    } else {
                        removeBreakpointHit(thread);
                    }
                    if (debugger == currentDebugger && debuggingView != null) {
                        debuggingView.refreshView(); // [TODO]
                    }
                } else if (JPDAThread.PROP_SUSPENDED.equals(propName)) {
                    if (!thread.isSuspended()) {
                        removeBreakpointHit(thread);
                    }
                    if (debugger == currentDebugger && debuggingView != null) {
                        debuggingView.refreshView(); // [TODO]
                    }
                } else if ("lockerThreads".equals(propName)) { // NOI18N
                    // Calling List<JPDAThread> getLockerThreads()
                    List<JPDAThread> currLockerThreads;
                    try {
                        java.lang.reflect.Method lockerThreadsMethod = thread.getClass().getMethod("getLockerThreads", new Class[] {}); // NOI18N
                        currLockerThreads = (List<JPDAThread>) lockerThreadsMethod.invoke(thread, new Object[] {});
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        currLockerThreads = null;
                    }
                    setShowThreadLocks(thread, currLockerThreads);
                } else if ("stepSuspendedByBreakpoint".equals(propName)) {
                    setShowStepBrkp(thread, (JPDABreakpoint) evt.getNewValue());
                }
            } else if (source instanceof DeadlockDetector) {
                if (DeadlockDetector.PROP_DEADLOCK.equals(propName)) {
                    setShowDeadlock(true);
                }
            }
        }

        private synchronized void unregister() {
            if (debugger == null) return ;
            for (JPDAThread thread : threads) {
                ((Customizer) thread).removePropertyChangeListener(this);
            }
            synchronized(currentThreadsHistory) {
                for (JPDAThread thread : threads) {
                    currentThreadsHistory.remove(thread);
                }
            }
            synchronized(hits) {
                for (JPDAThread thread : threads) {
                    removeBreakpointHit(thread);
                }
            }
            threads.clear();
            lockedThread = null;
            lockerThreads = null;
            stepBrkpThread = null;
            stepBrkpBreakpoint = null;
            debugger.removePropertyChangeListener(this);
            debugger.getThreadsCollector().getDeadlockDetector().removePropertyChangeListener(this);
            debugger = null;
        }
        
        private boolean isCurrent(JPDAThread thread) {
            return debugger.getCurrentThread() == thread;
        }

        private boolean isAtBreakpoint(JPDAThread thread) {
            JPDABreakpoint breakpoint = thread.getCurrentBreakpoint();
            return breakpoint != null && !breakpoint.isHidden();
        }

        private void setShowDeadlock(boolean detected) {
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowDeadlock(detected);
            }
        }

        private void setShowThreadLocks(JPDAThread thread, List<JPDAThread> currLockerThreads) {
            lockerThreads = currLockerThreads;
            lockedThread = thread;
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowThreadLocks(thread, lockerThreads);
            }
        }
        
        private void setShowStepBrkp(JPDAThread thread, JPDABreakpoint breakpoint) {
            stepBrkpThread = thread;
            stepBrkpBreakpoint = breakpoint;
            if (debugger == currentDebugger && debuggingView != null) {
                debuggingView.getInfoPanel().setShowStepBrkp(debugger, thread, breakpoint);
            }
        }

    }

    static class BreakpointHits {
        private Set<JPDAThread> stoppedThreadsSet = new HashSet<JPDAThread>();
        private LinkedList<JPDAThread> stoppedThreads = new LinkedList<JPDAThread>();
        
        public void goToHit() {
            JPDAThread thread = stoppedThreads.getLast();
            thread.makeCurrent();
        }
        
        public boolean contains(JPDAThread thread) {
            return stoppedThreadsSet.contains(thread);
        }
        
        public boolean add(JPDAThread thread) {
            if (stoppedThreadsSet.add(thread)) {
                stoppedThreads.addFirst(thread);
                return true;
            }
            return false;
        }
        
        public boolean remove(JPDAThread thread) {
            if (stoppedThreadsSet.remove(thread)) {
                stoppedThreads.remove(thread);
                return true;
            }
            return false;
        }
        
        public void clear() {
            stoppedThreadsSet.clear();
            stoppedThreads.clear();
        }
        
        public int size() {
            return stoppedThreads.size();
        }
        
    }

}

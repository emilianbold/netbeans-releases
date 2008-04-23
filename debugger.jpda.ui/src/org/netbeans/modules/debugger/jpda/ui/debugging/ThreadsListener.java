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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.openide.util.RequestProcessor;

public final class ThreadsListener implements PropertyChangeListener {

    private JPDADebugger debugger = null;
    private Set<JPDAThread> threads = new HashSet<JPDAThread>();
    private Set<JPDAThread> stoppedThreadsSet = new HashSet<JPDAThread>();
    private LinkedList<JPDAThread> stoppedThreads = new LinkedList<JPDAThread>();
    private DebuggingView debuggingView;
    
    public ThreadsListener(DebuggingView debuggingView) {
        this.debuggingView = debuggingView;
    }

    public synchronized void changeDebugger(JPDADebugger deb) {
        if (debugger == deb) {
            return;
        }
        if (debugger != null) {
            for (JPDAThread thread : threads) {
                ((Customizer)thread).removePropertyChangeListener(this);
            }
            clearAllHits();
            setShowDeadlock(false);
            debugger.removePropertyChangeListener(this);
        }
        this.debugger = deb;
        if (deb != null) {
            deb.addPropertyChangeListener(this);
            List<JPDAThread> allThreads = deb.getAllThreads();
            for (JPDAThread thread : allThreads) {
                threads.add(thread);
                ((Customizer)thread).addPropertyChangeListener(this);
            }
        }
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

                    RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                        public void run() {
                            if (!isCurrent(jpdaThread) && isAtBreakpoint(jpdaThread)) {
                                // System.out.println("ADD HIT: " + jpdaThread.getName());
                                addBreakpointHit(jpdaThread);
                            }
                        }
                    });
                    task.schedule(100);
                }
            } else if (JPDADebugger.PROP_THREAD_DIED.equals(propName)) {
                //System.out.println("DIED: " + evt.getOldValue());

                JPDAThread jpdaThread = (JPDAThread)evt.getOldValue();
                if (threads.remove(jpdaThread)) {
                    ((Customizer)jpdaThread).removePropertyChangeListener(this);
                    
                    // System.out.println("RELEASED: " + jpdaThread.getName());
                    
                }
            } else if (JPDADebugger.PROP_CURRENT_THREAD.equals(propName)) {
                removeBreakpointHit(debugger.getCurrentThread());
            }
        } else if (source instanceof JPDAThread) {
            final JPDAThread thread = (JPDAThread)source;
            if (JPDAThread.PROP_SUSPENDED.equals(propName)) {
                
                // System.out.println("THREAD: " + thread.getName() + ", curr: " + isCurrent(thread) + ", brk: " + isAtBreakpoint(thread));
                
                RequestProcessor.Task task = RequestProcessor.getDefault().create(new Runnable() {
                    public void run() {
                        if (!isCurrent(thread)) {
                            if (isAtBreakpoint(thread)) {
                                addBreakpointHit(thread);
                            } else {
                                removeBreakpointHit(thread);
                            }
                        } else {
                            removeBreakpointHit(thread); // [TODO]
                        }
                        
                        // detect deadlocks
                        if (DeadlockChecker.detectDeadlock(threads)) {
                            setShowDeadlock(true);
                        } else {
                            setShowDeadlock(false); // [TODO]
                        }
                        
                    }
                });
                task.schedule(100);
                
            } // if
        }
    }
    
    public synchronized int getHitsCount() {
        return stoppedThreads.size();
    }
    
    private boolean isCurrent(JPDAThread thread) {
        return debugger.getCurrentThread() == thread;
    }

    private boolean isAtBreakpoint(JPDAThread thread) {
        return thread.getCurrentBreakpoint() != null;
    }
    
    private void addBreakpointHit(JPDAThread thread) {
        if (thread != null && !stoppedThreadsSet.contains(thread)) {
            
            // System.out.println("Hit added: " + thread.getName());
            
            stoppedThreadsSet.add(thread);
            stoppedThreads.addFirst(thread);
            debuggingView.getInfoPanel().addBreakpointHit(thread, stoppedThreadsSet.size());
        }
    }
    
    private void removeBreakpointHit(JPDAThread thread) {
        if (thread != null && stoppedThreadsSet.contains(thread)) {
            
            // System.out.println("Hit removed: " + thread.getName());
            
            stoppedThreadsSet.remove(thread);
            stoppedThreads.remove(thread);
            debuggingView.getInfoPanel().removeBreakpointHit(thread, stoppedThreadsSet.size());
        }
    }
    
    private void clearAllHits() {
        threads.clear();
        stoppedThreadsSet.clear();
        stoppedThreads.clear();
        debuggingView.getInfoPanel().clearBreakpointHits();
    }

    private void setShowDeadlock(boolean detected) {
        debuggingView.getInfoPanel().setShowDeadlock(detected);
    }
    
    
    // **************************************************************************
    
    static class DeadlockChecker {

        private Map<Long, Node> monitorToNode;
        
        private void build(Set<JPDAThread> threads) {
            monitorToNode = new HashMap<Long, Node>();
            for (JPDAThread thread : threads) {
                ObjectVariable contendedMonitor = thread.getContendedMonitor();
                ObjectVariable[] ownedMonitors = thread.getOwnedMonitors();
                if (contendedMonitor == null || ownedMonitors.length == 0) {
                    continue;
                } // if
                Node contNode = monitorToNode.get(contendedMonitor.getUniqueID());
                if (contNode == null) {
                    contNode = new Node(null, contendedMonitor);
                    monitorToNode.put(contendedMonitor.getUniqueID(), contNode);
                }
                for (int x = 0; x < ownedMonitors.length; x++) {
                    Node node = monitorToNode.get(ownedMonitors[x].getUniqueID());
                    if (node == null) {
                        node = new Node(thread, ownedMonitors[x]);
                        monitorToNode.put(ownedMonitors[x].getUniqueID(), node);
                    } else if (node.thread == null) {
                        node.thread = thread;
                    } else {
                        continue;
                    }
                    node.setOutgoing(contNode);
                    contNode.addIncomming(node);
                } // for
            } // for
        }

        private boolean detectDeadlock() {
            Set<Node> simpleNodesSet = new HashSet<Node>();
            LinkedList<Node> simpleNodesList = new LinkedList<Node>();
            for (Entry<Long, Node> entry : monitorToNode.entrySet()) {
                Node node = entry.getValue();
                if (node.isSimple()) {
                    simpleNodesSet.add(node);
                    simpleNodesList.add(node);
                }
            } // for
            while (simpleNodesList.size() > 0) {
                Node currNode = simpleNodesList.removeFirst();
                simpleNodesSet.remove(currNode);
                
                if (currNode.outgoing != null) {
                    currNode.outgoing.removeIncomming(currNode);
                    if (currNode.outgoing.isSimple() && !simpleNodesSet.contains(currNode.outgoing)) {
                        simpleNodesSet.add(currNode.outgoing);
                        simpleNodesList.add(currNode.outgoing);
                    }
                }
                for (Node node : currNode.incomming) {
                    node.setOutgoing(null);
                    if (node.isSimple() && !simpleNodesSet.contains(node)) {
                        simpleNodesSet.add(node);
                        simpleNodesList.add(node);
                    }
                }
                monitorToNode.remove(currNode.ownedMonitor.getUniqueID());
            } // while
            
            
            //***************************************
//            if (!monitorToNode.isEmpty()) {
//                System.out.println("DEADLOCK:");
//                Set<JPDAThread> threads = new HashSet<JPDAThread>();
//                for (Entry<Long, Node> entry : monitorToNode.entrySet()) {
//                    Node node = entry.getValue();
//                    if (node.thread != null) {
//                        System.out.println("\t" + node.thread.getName());
//                    }
//                } // for
//            }
            //***************************************
            
            
            return !monitorToNode.isEmpty();
        }
        
        public static boolean detectDeadlock(Set<JPDAThread> threads) {
            DeadlockChecker checker = new DeadlockChecker();
            checker.build(threads);
            return checker.detectDeadlock();
        }
        
        // **********************************************************************
        
        static class Node {
            JPDAThread thread;
            ObjectVariable ownedMonitor;
            Collection<Node> incomming = new HashSet<Node>();
            Node outgoing = null;
            
            Node (JPDAThread thread, ObjectVariable ownedMonitor) {
                this.thread = thread;
                this.ownedMonitor = ownedMonitor;
            }
            
            public void setOutgoing(Node node) {
                outgoing = node;
            }
            
            public void addIncomming(Node node) {
                incomming.add(node);
            }
            
            public void removeIncomming(Node node) {
                incomming.remove(node);
            }
            
            public boolean isSimple() {
                return outgoing == null || incomming.size() == 0;
            }
        }
        
    }
    
}

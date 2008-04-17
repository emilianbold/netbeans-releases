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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;

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

                JPDAThread jpdaThread = (JPDAThread)evt.getNewValue();
                if (threads.add(jpdaThread)) {
                    if (!isCurrent(jpdaThread) && isAtBreakpoint(jpdaThread)) {
                        
                        // System.out.println("ADD HIT: " + jpdaThread.getName());
                        
                        addBreakpointHit(jpdaThread);
                    }
                    
                    ((Customizer)jpdaThread).addPropertyChangeListener(this);
                    
                    // System.out.println("WATCHED: " + jpdaThread.getName());
                    
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
            JPDAThread thread = (JPDAThread)source;
            if (JPDAThread.PROP_SUSPENDED.equals(propName)) {
                
                // System.out.println("THREAD: " + thread.getName() + ", curr: " + isCurrent(thread) + ", brk: " + isAtBreakpoint(thread));
                
                if (!isCurrent(thread)) {
                    if (isAtBreakpoint(thread)) {
                        addBreakpointHit(thread);
                    } else {
                        removeBreakpointHit(thread);
                    }
                }
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
            stoppedThreadsSet.add(thread);
            stoppedThreads.addFirst(thread);
            debuggingView.refreshBreakpointHits();
        }
    }
    
    private void removeBreakpointHit(JPDAThread thread) {
        if (thread != null && stoppedThreadsSet.contains(thread)) {
            stoppedThreadsSet.remove(thread);
            stoppedThreads.remove(thread);
            debuggingView.refreshBreakpointHits();
        }
    }
    
    private void clearAllHits() {
        threads.clear();
        stoppedThreadsSet.clear();
        stoppedThreads.clear();
        debuggingView.refreshBreakpointHits();
    }
    
}

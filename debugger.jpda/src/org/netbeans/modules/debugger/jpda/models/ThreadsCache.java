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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Executor;

/**
 *
 * @author martin
 */
public class ThreadsCache implements Executor {
    
    public static final String PROP_THREAD_STARTED = "threadStarted";   // NOI18N
    public static final String PROP_THREAD_DIED = "threadDied";         // NOI18N
    public static final String PROP_GROUP_ADDED = "groupAdded";         // NOI18N
    
    private VirtualMachine vm;
    private JPDADebuggerImpl debugger;
    private Map<ThreadGroupReference, List<ThreadGroupReference>> groupMap;
    private Map<ThreadGroupReference, List<ThreadReference>> threadMap;
    private List<ThreadReference> allThreads;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ThreadsCache(JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        groupMap = new HashMap<ThreadGroupReference, List<ThreadGroupReference>>();
        threadMap = new HashMap<ThreadGroupReference, List<ThreadReference>>();
        allThreads = new ArrayList<ThreadReference>();
        VirtualMachine vm = debugger.getVirtualMachine();
        if (vm != null) {
            setVirtualMachine(vm);
        }
    }
    
    public synchronized void setVirtualMachine(VirtualMachine vm) {
        if (this.vm == vm) return ;
        try {
            this.vm = vm;
            ThreadStartRequest tsr = vm.eventRequestManager().createThreadStartRequest();
            ThreadDeathRequest tdr = vm.eventRequestManager().createThreadDeathRequest();
            tsr.setSuspendPolicy(ThreadStartRequest.SUSPEND_NONE);
            tdr.setSuspendPolicy(ThreadStartRequest.SUSPEND_NONE);
            debugger.getOperator().register(tsr, this);
            debugger.getOperator().register(tdr, this);
            tsr.enable();
            tdr.enable();
            init();
        } catch (VMDisconnectedException e) {
            this.vm = null;
        }
    }
    
    private synchronized void init() {
        allThreads = new ArrayList<ThreadReference>(vm.allThreads());
        List<ThreadGroupReference> groups;
        groupMap.put(null, groups = new ArrayList(vm.topLevelThreadGroups()));
        for (ThreadGroupReference group : groups) {
            initGroups(group);
        }
        List<ThreadReference> mainThreads = new ArrayList();
        threadMap.put(null, mainThreads);
        for (ThreadReference thread : allThreads) {
            try {
                if (thread.threadGroup() == null) {
                    mainThreads.add(thread);
                }
            } catch (ObjectCollectedException e) {
            }
        }
    }
    
    private void initGroups(ThreadGroupReference group) {
        try {
            List<ThreadGroupReference> groups = new ArrayList(group.threadGroups());
            List<ThreadReference> threads = new ArrayList(group.threads());
            groupMap.put(group, groups);
            threadMap.put(group, threads);
            for (ThreadGroupReference g : groups) {
                initGroups(g);
            }
        } catch (ObjectCollectedException e) {
        }
    }

    public synchronized List<ThreadReference> getAllThreads() {
        return Collections.unmodifiableList(new ArrayList(allThreads));
    }
    
    public synchronized List<ThreadGroupReference> getTopLevelThreadGroups() {
        List<ThreadGroupReference> topGroups = groupMap.get(null);
        if (topGroups == null) {
            if (vm == null) {
                return Collections.EMPTY_LIST;
            }
            topGroups = new ArrayList(vm.topLevelThreadGroups());
            groupMap.put(null, topGroups);
        }
        return Collections.unmodifiableList(new ArrayList(topGroups));
    }
    
    public synchronized List<ThreadReference> getThreads(ThreadGroupReference group) {
        List<ThreadReference> threads = threadMap.get(group);
        if (threads == null) {
            threads = Collections.emptyList();
        } else {
            threads = Collections.unmodifiableList(new ArrayList(threads));
        }
        return threads;
    }

    public synchronized List<ThreadGroupReference> getGroups(ThreadGroupReference group) {
        List<ThreadGroupReference> groups = groupMap.get(group);
        if (groups == null) {
            groups = Collections.emptyList();
        } else {
            groups = Collections.unmodifiableList(new ArrayList(groups));
        }
        return groups;
    }
    
    private List<ThreadGroupReference> addGroups(ThreadGroupReference group) {
        List<ThreadGroupReference> addedGroups = new ArrayList<ThreadGroupReference>();
        ThreadGroupReference parent = group.parent();
        if (groupMap.get(parent) == null) {
            addedGroups.addAll(addGroups(parent));
        }
        List<ThreadGroupReference> parentsGroups = groupMap.get(parent);
        if (!parentsGroups.contains(group)) {
            parentsGroups.add(group);
            addedGroups.add(group);
            List<ThreadGroupReference> groups = new ArrayList();
            List<ThreadReference> threads = new ArrayList();
            groupMap.put(group, groups);
            threadMap.put(group, threads);
        }
        return addedGroups;
    }

    public boolean exec(Event event) {
        if (event instanceof ThreadStartEvent) {
            ThreadReference thread = ((ThreadStartEvent) event).thread();
            ThreadGroupReference group;
            try {
                group = thread.threadGroup();
            } catch (ObjectCollectedException ocex) {
                group = null;
            }
            List<ThreadGroupReference> addedGroups = null;
            synchronized (this) {
                if (group != null) {
                    addedGroups = addGroups(group);
                }
                List<ThreadReference> threads = threadMap.get(group);
                if (!threads.contains(thread)) { // could be added by init()
                    threads.add(thread);
                }
                if (!allThreads.contains(thread)) { // could be added by init()
                    allThreads.add(thread);
                }
            }
            if (addedGroups != null) {
                for (ThreadGroupReference g : addedGroups) {
                    pcs.firePropertyChange(PROP_GROUP_ADDED, null, g);
                }
            }
            pcs.firePropertyChange(PROP_THREAD_STARTED, null, thread);
        }
        if (event instanceof ThreadDeathEvent) {
            ThreadReference thread = ((ThreadDeathEvent) event).thread();
            ThreadGroupReference group;
            try {
                group = thread.threadGroup();
            } catch (ObjectCollectedException ocex) {
                group = null;
            }
            synchronized (this) {
                List<ThreadReference> threads;
                if (group != null) {
                    threads = threadMap.get(group);
                } else {
                    threads = null;
                    for (List<ThreadReference> testThreads : threadMap.values()) {
                        if (testThreads.contains(thread)) {
                            threads = testThreads;
                        }
                    }
                }
                if (threads != null) {
                    threads.remove(thread);
                }
                allThreads.remove(thread);
            }
            pcs.firePropertyChange(PROP_THREAD_DIED, thread, null);
        }
        return true;
    }

    public void removed(EventRequest eventRequest) {
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}

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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.openide.util.RequestProcessor;


/**
 * This class represents JPDA Debugger Implementation.
 *
 * @author Jan Jancura
 */
public class ThreadsTreeModel implements TreeModel {

    private static boolean verbose = 
        (System.getProperty ("netbeans.debugger.viewrefresh") != null) &&
        (System.getProperty ("netbeans.debugger.viewrefresh").indexOf ('t') >= 0);
    
    private JPDADebuggerImpl    debugger;
    private Map<Object, ChildrenTree> childrenCache = new WeakHashMap<Object, ChildrenTree>();
    private Listener            listener;
    private Collection<ModelListener> listeners = new HashSet<ModelListener>();
    
    
    public ThreadsTreeModel (ContextProvider lookupProvider) {
        debugger = (JPDADebuggerImpl) lookupProvider.
            lookupFirst (null, JPDADebugger.class);
    }

    public Object getRoot () {
        return ROOT;
    }
    
    public Object[] getChildren (Object o, int from, int to)
    throws UnknownTypeException {
        /*
        if (node.equals (ROOT)) {
            return debugger.getTopLevelThreadGroups();
        } else if (node instanceof JPDAThreadGroup) {
            JPDAThreadGroup tg = (JPDAThreadGroup) node;
            JPDAThreadGroup[] tgs = tg.getThreadGroups();
            JPDAThread[] ts = tg.getThreads();
            int n = tgs.length + ts.length;
            from = Math.min(n, from);
            to = Math.min(n, to);
            Object[] ch = new Object[to - from];
            if (from < tgs.length) {
                if (to >= tgs.length) {
                    System.arraycopy(tgs, from, ch, 0, tgs.length - from);
                } else {
                    System.arraycopy(tgs, from, ch, 0, to - from);
                }
            }
            if (to > tgs.length) {
                to -= tgs.length;
                int pos = tgs.length - from;
                if (from >= tgs.length) {
                    from -= tgs.length;
                } else {
                    from = 0;
                }
                System.arraycopy(ts, from, ch, pos, to - from);
            }
            return ch;
        } else {
            throw new UnknownTypeException (node);
        }
        */
        
        Object[] ch;
        synchronized (childrenCache) {
            //ch = (List) childrenCache.get(o);
            ChildrenTree cht = childrenCache.get(o);
            if (cht != null) {
                ch = cht.getChildren();
            } else {
                ch = null;
            }
        }
        if (ch == null) {
            ch = computeChildren(o);
            if (ch == null) {
                throw new UnknownTypeException (o);
            } else {
                synchronized (childrenCache) {
                    ChildrenTree cht = new ChildrenTree(o);
                    cht.setChildren(ch);
                    childrenCache.put(o, cht);
                }
            }
        }
        int l = ch.length;
        from = Math.min(l, from);
        to = Math.min(l, to);
        if (from == 0 && to == l) {
            return ch;
        } else {
            Object[] ch1 = new Object[to - from];
            System.arraycopy(ch, from, ch1, 0, to - from);
            ch = ch1;
        }
        return ch;
    }
    
    private Object[] computeChildren(Object node) {
        /*
        List ch;
        try {
            if (node.equals (ROOT)) {
                VirtualMachine vm = debugger.getVirtualMachine ();
                if (vm != null)
                    ch = vm.topLevelThreadGroups ();
                else
                    ch = Collections.EMPTY_LIST;
            } else
            if (node instanceof ThreadGroupReference) {
                ThreadGroupReference tgr = (ThreadGroupReference) node;
                ch = new ArrayList (tgr.threadGroups ());
                ch.addAll(tgr.threads ());
            } else
                ch = null;
        } catch (VMDisconnectedException ex) {
            ch = Collections.EMPTY_LIST;
        }
        return ch;
         */
        if (node.equals (ROOT)) {
            
            if (verbose) {
                com.sun.jdi.VirtualMachine vm = debugger.getVirtualMachine();
                if (vm == null) {
                    System.err.println("\nThreadsTreeModel.computeChildren():\nVM is null!\n");
                } else {
                    List<ThreadReference> threads = vm.allThreads();
                    System.err.println("\nThreadsTreeModel.computeChildren() ALL Threads:");
                    for (ThreadReference t : threads) {
                        System.err.println("  "+t.name()+" is suspended: "+t.isSuspended()+", suspend count = "+t.suspendCount());
                    }
                    System.err.println("");
                }
            }
            
            return debugger.getTopLevelThreadGroups();
        } else if (node instanceof JPDAThreadGroup) {
            JPDAThreadGroup tg = (JPDAThreadGroup) node;
            JPDAThreadGroup[] tgs = tg.getThreadGroups();
            JPDAThread[] ts = tg.getThreads();
            int n = tgs.length + ts.length;
            Object[] ch = new Object[n];
            System.arraycopy(tgs, 0, ch, 0, tgs.length);
            System.arraycopy(ts, 0, ch, tgs.length, ts.length);
            return ch;
        } else {
            return new Object[0];
        }
    }
    
    private void recomputeChildren() {
        synchronized (childrenCache) {
            recomputeChildren(getRoot());
        }
    }
    
    private void recomputeChildren(Object node) {
        ChildrenTree cht = childrenCache.get(node);
        if (cht != null) {
            Set keys = childrenCache.keySet();
            Object[] oldCh = cht.getChildren();
            Object[] newCh = computeChildren(node);
            cht.setChildren(newCh);
            for (int i = 0; i < newCh.length; i++) {
                if (keys.contains(newCh[i])) {
                    recomputeChildren(newCh[i]);
                }
            }
        }
        /*
            Set nodes = childrenCache.keySet();
            for (Iterator it = nodes.iterator(); it.hasNext(); ) {
                Object node = it.next();
                // potreba brat jako tree - vyhodit stare nody hierarchicky
                // takto bych se ptal i na stare out-of-date nody!
            }
            //List ch = (List) childrenCache.get(o);
        */
    }
    
    /**
     * Returns number of children for given node.
     * 
     * @param   node the parent node
     * @throws  UnknownTypeException if this TreeModel implementation is not
     *          able to resolve children for given node type
     *
     * @return  true if node is leaf
     */
    public int getChildrenCount (Object node) throws UnknownTypeException {
        // Performance, see issue #59058.
        return Integer.MAX_VALUE;
        /*
        Object[] ch;
        synchronized (childrenCache) {
            ChildrenTree cht = childrenCache.get(node);
            if (cht != null) {
                ch = cht.getChildren();
            } else {
                ch = null;
            }
        }
        if (ch == null) {
            ch = computeChildren(node);
            if (ch == null) {
                throw new UnknownTypeException (node);
            } else {
                synchronized (childrenCache) {
                    ChildrenTree cht = new ChildrenTree(node);
                    cht.setChildren(ch);
                    childrenCache.put(node, cht);
                }
            }
        }
        return ch.length;
         */
        /*
        try {
            List ch;
            if (node.equals (ROOT)) {
                VirtualMachine vm = debugger.getVirtualMachine ();
                if (vm != null)
                    ch = vm.topLevelThreadGroups ();
                else
                    ch = Collections.EMPTY_LIST;
            } else
            if (node instanceof ThreadGroupReference) {
                ThreadGroupReference tgr = (ThreadGroupReference) node;
                ch = new ArrayList (tgr.threadGroups ());
                ch.addAll(tgr.threads ());
            } else
            throw new UnknownTypeException (node);
            synchronized (lastCachedLock) {
                lastCachedChildrenNode = node;
                lastCachedChildren = ch;
            }
            return ch.size();
        } catch (VMDisconnectedException ex) {
            return 0;
        }
         */
    }
    
    public boolean isLeaf (Object o) throws UnknownTypeException {
        if (o instanceof JPDAThread) return true;
        if (o instanceof JPDAThreadGroup) return false;
        if (o == ROOT) return false;
        throw new UnknownTypeException (o);
    }

    public void addModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.add (l);
            if (listener == null) {
                listener = new Listener (this, debugger);
            }
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (listeners) {
            listeners.remove (l);
            if (listeners.size () == 0) {
                listener.destroy ();
                listener = null;
            }
        }
    }
    
    public void fireTreeChanged () {
        recomputeChildren();
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.TreeChanged(this);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }

    /**
     * Listens on JPDADebugger state property and updates all threads hierarchy.
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        private WeakReference<ThreadsTreeModel> model;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public Listener (
            ThreadsTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            model = new WeakReference<ThreadsTreeModel>(tm);
            debugger.addPropertyChangeListener (this);
            if (debugger.getState() == debugger.STATE_RUNNING) {
                task = createTask();
                task.schedule(500);
            }
        }
        
        private ThreadsTreeModel getModel () {
            ThreadsTreeModel tm = model.get ();
            if (tm == null) {
                destroy ();
            }
            return tm;
        }
        
        void destroy () {
            debugger.removePropertyChangeListener (this);
            synchronized (this) {
                if (task != null) {
                    // cancel old task
                    task.cancel ();
                    if (verbose)
                        System.out.println("TTM cancel old task " + task);
                    task = null;
                }
            }
        }
        
        private RequestProcessor.Task createTask() {
            RequestProcessor.Task task =
                new RequestProcessor("Threads Refresh", 1).create(
                                new RefreshTree());
            if (verbose)
                System.out.println("TTM  create task " + task);
            return task;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            if ( (e.getPropertyName () == debugger.PROP_STATE) &&
                 (debugger.getState () == debugger.STATE_STOPPED)
            ) {
                final ThreadsTreeModel tm = getModel ();
                if (tm == null) return;
                synchronized (this) {
                    if (task == null) {
                        task = createTask();
                    }
                    task.schedule(500);
                }
            } else 
            if ( (e.getPropertyName () == debugger.PROP_STATE) &&
                 (debugger.getState () == debugger.STATE_RUNNING)
            ) {
                final ThreadsTreeModel tm = getModel ();
                if (tm == null) return;
                synchronized (this) {
                    if (task == null) {
                        task = createTask();
                    }
                    task.schedule (2000);
                }
            }
        }
        
        private class RefreshTree implements Runnable {
            public RefreshTree () {}
            
            public void run() {
                ThreadsTreeModel tm = getModel ();
                if (tm == null) return;
                if (verbose)
                    System.out.println("TTM do R task " + task);
                tm.fireTreeChanged ();
                synchronized (Listener.this) {
                    if (debugger.getState () == debugger.STATE_RUNNING) {
                        if (task != null) {
                            task.schedule (2000);
                        }
                    }
                }
            }
        }
    }
    
    private static class ChildrenTree {
        
        private Object node;
        private Object[] ch;
        
        public ChildrenTree(Object node) {
            this.node = node;
        }
        
        public void setChildren(Object[] ch) {
            this.ch = ch;
        }
        
        public Object[] getChildren() {
            return ch;
        }
        
    }
    
}


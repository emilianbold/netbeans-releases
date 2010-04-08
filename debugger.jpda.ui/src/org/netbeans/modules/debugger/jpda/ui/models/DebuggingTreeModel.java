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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.models.SourcesModel.AbstractColumn;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author martin
 */
public class DebuggingTreeModel extends CachedChildrenTreeModel {
    
    public static final String SORT_ALPHABET = "sort.alphabet";
    public static final String SORT_SUSPEND = "sort.suspend";
    public static final String SHOW_SYSTEM_THREADS = "show.systemThreads";
    public static final String SHOW_THREAD_GROUPS = "show.threadGroups";
    public static final String SHOW_SUSPENDED_THREADS_ONLY = "show.suspendedThreadsOnly";
    
    private static final Set<String> SYSTEM_THREAD_NAMES = new HashSet<String>(Arrays.asList(new String[] {
                                                           "Reference Handler",
                                                           "Signal Dispatcher",
                                                           "Finalizer",
                                                           "Java2D Disposer",
                                                           "TimerQueue",
                                                           "Attach Listener"}));
    private static final Set<String> SYSTEM_MAIN_THREAD_NAMES = new HashSet<String>(Arrays.asList(new String[] {
                                                           "DestroyJavaVM",
                                                           "AWT-XAWT",
                                                           "AWT-Shutdown"}));
    
    private final JPDADebugger debugger;
    private Listener listener;
    private PreferenceChangeListener prefListener;
    private final PropertyChangeListener debuggerListener = new DebuggerFinishListener();
    private final Collection<ModelListener> listeners = new HashSet<ModelListener>();
    private final Map<JPDAThread, ThreadStateListener> threadStateListeners = new WeakHashMap<JPDAThread, ThreadStateListener>();
    private final Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N

    private RequestProcessor RP;
    
    public DebuggingTreeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        RP = lookupProvider.lookupFirst(null, RequestProcessor.class);
        debugger.addPropertyChangeListener(JPDADebugger.PROP_STATE, debuggerListener);
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
            debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, debuggerListener);
        } else {
            prefListener = new DebuggingPreferenceChangeListener();
            preferences.addPreferenceChangeListener(prefListener);
        }
    }

    @Override
    protected Object[] computeChildren(Object parent) throws UnknownTypeException {
        //System.err.println("DebuggingTreeModel.computeChildren("+parent+")");
        if (parent == ROOT) {
            boolean showThreadGroups = preferences.getBoolean(SHOW_THREAD_GROUPS, false);
            if (showThreadGroups) {
                return getTopLevelThreadsAndGroups();
            } else {
                JPDAThread[] threads = debugger.getThreadsCollector().getAllThreads().toArray(new JPDAThread[0]);
                for (JPDAThread t : threads) {
                    watchState(t);
                }
                return threads;
            }
        }
        if (parent instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) parent;
            watchState(t);
            try {
                return t.getCallStack();
            } catch (AbsentInformationException aiex) {
                return new Object[0];
            }
        }
        if (parent instanceof JPDAThreadGroup) {
            JPDAThread[] threads = ((JPDAThreadGroup)parent).getThreads();
            for (JPDAThread t : threads) {
                watchState(t);
            }
            Object[] groups = ((JPDAThreadGroup)parent).getThreadGroups();
            Object[] result = new Object[threads.length + groups.length];
            System.arraycopy(threads, 0, result, 0, threads.length);
            System.arraycopy(groups, 0, result, threads.length, groups.length);
            return result;
        }
        if (parent instanceof CallStackFrame) {
            return new Object[0];
        }
        throw new UnknownTypeException(parent.toString());
    }
    
    protected Object[] reorder(Object[] nodes) {
        boolean showSystemThreads = preferences.getBoolean(SHOW_SYSTEM_THREADS, false);
        boolean showSuspendedThreadsOnly = preferences.getBoolean(SHOW_SUSPENDED_THREADS_ONLY, false);
        if (!showSystemThreads || showSuspendedThreadsOnly) {
            nodes = filterThreadsAndGroups(nodes, !showSystemThreads, showSuspendedThreadsOnly);
        }
        boolean alphabet = preferences.getBoolean(SORT_ALPHABET, true);
        if (!alphabet) {
            boolean suspend = preferences.getBoolean(SORT_SUSPEND, false);
            if (suspend) {
                Object[] newNodes = new Object[nodes.length];
                System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
                nodes = newNodes;
                Arrays.sort(nodes, new ThreadSuspendComparator());
            }
        } else {
            Object[] newNodes = new Object[nodes.length];
            System.arraycopy(nodes, 0, newNodes, 0, nodes.length);
            nodes = newNodes;
            Arrays.sort(nodes, new ThreadAlphabetComparator());
        }
        return nodes;
    }
    
    private Object[] filterThreadsAndGroups(Object[] nodes, boolean filterSystem, boolean filterRunning) {
        List list = null;
        JPDAThread currentThread = debugger.getCurrentThread();
        for (Object node : nodes) {
            if (node instanceof JPDAThread) {
                JPDAThread t = (JPDAThread)node;
                watchState(t);
                if (!t.isSuspended() && (filterSystem && isSystem(t) ||
                        (filterRunning && t != currentThread))) {
                    if (list == null) {
                        list = new ArrayList(Arrays.asList(nodes));
                    }
                    list.remove(node);
                } // if
            } else if (filterRunning && node instanceof JPDAThreadGroup) {
                if (!containsThread((JPDAThreadGroup)node, currentThread)) {
                    if (list == null) {
                        list = new ArrayList(Arrays.asList(nodes));
                    }
                    list.remove(node);
                }
            }
        } // for
        return (list != null) ? list.toArray() : nodes;
    }

    private boolean containsThread(JPDAThreadGroup group, JPDAThread currentThread) {
        JPDAThread[] threads = group.getThreads();
        for (int x = 0; x < threads.length; x++) {
            if (threads[x].isSuspended() || threads[x] == currentThread) {
                return true;
            }
        }
        JPDAThreadGroup[] groups = group.getThreadGroups();
        for (int x = 0; x < groups.length; x++) {
            if (containsThread(groups[x], currentThread)) {
                return true;
            }
        }
        return false;
    }

    private Object[] getTopLevelThreadsAndGroups() {
        List result = new LinkedList();
        Set groups = new HashSet();
        for (JPDAThread thread : debugger.getThreadsCollector().getAllThreads()) {
            JPDAThreadGroup group = thread.getParentThreadGroup();
            if (group == null) {
                result.add(thread);
            } else {
                while (group.getParentThreadGroup() != null) {
                    group = group.getParentThreadGroup();
                } // while
                groups.add(group);
            } // if
        } // for
        result.addAll(groups);
        return result.toArray();
    }
    
    private boolean isSystem(JPDAThread t) {
        if (SYSTEM_THREAD_NAMES.contains(t.getName())) {
            JPDAThreadGroup g = t.getParentThreadGroup();
            return (g != null && "system".equals(g.getName()));
        }
        if (SYSTEM_MAIN_THREAD_NAMES.contains(t.getName())) {
            JPDAThreadGroup g = t.getParentThreadGroup();
            return (g != null && "main".equals(g.getName()));
        }
        return false;
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            return 0;
        }
        if (node instanceof JPDAThread) {
            if (!((JPDAThread) node).isSuspended()) {
                return 0;
            }
        }
        return Integer.MAX_VALUE;
    }

    public Object getRoot() {
        return ROOT;
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            return true;
        }
        if (node instanceof JPDAThread) {
            if (!((JPDAThread) node).isSuspended() && !isMethodInvoking((JPDAThread) node)) {
                return true;
            }
        }
        return false;
    }

    public void addModelListener(ModelListener l) {
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
            if (listeners.size () == 0 && listener != null) {
                listener.destroy ();
                listener = null;
            }
        }
    }

    public void fireNodeChanged (Object node) {
        //System.err.println("FIRE node changed ("+node+")");
        //Thread.dumpStack();
        try {
            recomputeChildren();
        } catch (UnknownTypeException ex) {
            Exceptions.printStackTrace(ex);
            return ;
        }
        ModelListener[] ls;
        synchronized (listeners) {
            ls = listeners.toArray(new ModelListener[0]);
        }
        ModelEvent ev = new ModelEvent.NodeChanged(this, node);
        for (int i = 0; i < ls.length; i++) {
            ls[i].modelChanged (ev);
        }
    }


    /**
     * Listens on JPDADebugger state property and updates all threads hierarchy.
     */
    private static class Listener implements PropertyChangeListener {
        
        private JPDADebugger debugger;
        //private ThreadsCache tc;
        private WeakReference<DebuggingTreeModel> model;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        private Set<Object> nodesToRefresh;
        private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
        
        public Listener (
            DebuggingTreeModel tm,
            JPDADebugger debugger
        ) {
            this.debugger = debugger;
            //this.tc = debugger.getThreadsCache();
            model = new WeakReference<DebuggingTreeModel>(tm);
            debugger.addPropertyChangeListener(this);
            //tc.addPropertyChangeListener(this);
        }

        private DebuggingTreeModel getModel () {
            DebuggingTreeModel tm = model.get ();
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
                    task = null;
                }
            }
        }
        
        private RequestProcessor.Task createTask() {
            RequestProcessor rp = null;
            try {
                Session s = (Session) debugger.getClass().getMethod("getSession").invoke(debugger);
                rp = s.lookupFirst(null, RequestProcessor.class);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
            if (rp == null) {
                rp = RequestProcessor.getDefault();
            }
            RequestProcessor.Task task = rp.create(new RefreshTree());
            return task;
        }
        
        public void propertyChange (PropertyChangeEvent e) {
            //System.err.println("ThreadsTreeModel.propertyChange("+e+")");
            //System.err.println("    "+e.getPropertyName()+", "+e.getOldValue()+" => "+e.getNewValue());
            JPDAThreadGroup tg;
            if (e.getPropertyName() == JPDADebugger.PROP_THREAD_STARTED) {
                JPDAThread t = (JPDAThread) e.getNewValue();
                tg = t.getParentThreadGroup();
            } else if (e.getPropertyName() == JPDADebugger.PROP_THREAD_DIED) {
                JPDAThread t = (JPDAThread) e.getOldValue();
                tg = t.getParentThreadGroup();
                while (tg != null && tg.getThreads().length == 0 && tg.getThreadGroups().length == 0) {
                    tg = tg.getParentThreadGroup();
                }
            } else if (e.getPropertyName() == JPDADebugger.PROP_THREAD_GROUP_ADDED) {
                tg = (JPDAThreadGroup) e.getNewValue();
                tg = tg.getParentThreadGroup();
            } else {
                return ;
            }
            Collection nodes = new ArrayList();
            if (tg == null || !preferences.getBoolean(SHOW_THREAD_GROUPS, false)) {
                nodes.add(ROOT);
            } else if (tg != null) {
                do {
                    nodes.add(tg);
                    tg = tg.getParentThreadGroup();
                } while (tg != null);
            }
            synchronized (this) {
                if (task == null) {
                    task = createTask();
                }
                if (nodesToRefresh == null) {
                    nodesToRefresh = new LinkedHashSet<Object>();
                }
                nodesToRefresh.addAll(nodes);
                task.schedule(100);
            }
        }
        
        private class RefreshTree implements Runnable {
            public RefreshTree () {}
            
            public void run() {
                DebuggingTreeModel tm = getModel ();
                if (tm == null) return;
                List nodes;
                synchronized (Listener.this) {
                    nodes = new ArrayList(nodesToRefresh);
                    nodesToRefresh.clear();
                }
                for (Object node : nodes) {
                    tm.fireNodeChanged(node);
                }
            }
        }
    }

    
    private void fireThreadStateChanged (JPDAThread node) {
        if (preferences.getBoolean(SHOW_SUSPENDED_THREADS_ONLY, false)) {
            fireNodeChanged(ROOT);
        } else if (!preferences.getBoolean(SHOW_SYSTEM_THREADS, false)
                   && isSystem(node)) {

            fireNodeChanged(ROOT);
        }
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        try {
            recomputeChildren(node);
        } catch (UnknownTypeException ex) {
            refreshCache(node);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.CHILDREN_MASK |
                ModelEvent.NodeChanged.EXPANSION_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private void watchState(JPDAThread t) {
        synchronized (threadStateListeners) {
            if (!threadStateListeners.containsKey(t)) {
                threadStateListeners.put(t, new ThreadStateListener(t));
            }
        }
    }
    
    private class ThreadStateListener implements PropertyChangeListener {
        
        private Reference<JPDAThread> tr;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public ThreadStateListener(JPDAThread t) {
            this.tr = new WeakReference(t);
            ((Customizer) t).addPropertyChangeListener(WeakListeners.propertyChange(this, t));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (!evt.getPropertyName().equals(JPDAThread.PROP_SUSPENDED)) return ;
            JPDAThread t = tr.get();
            if (t == null) return ;
            // Refresh the children of the thread (stack frames) when the thread
            // gets suspended or is resumed
            // When thread is resumed because of a method invocation, do the
            // refresh only if the method takes a long time.
            boolean isMethodInvoking = "methodInvoke".equals(evt.getPropagationId());
            boolean suspended = t.isSuspended();
            if (suspended || !isMethodInvoking) {
                synchronized (this) {
                    if (task == null) {
                        task = RP.create(new Refresher());
                    }
                    task.schedule(suspended ? 200 : 1000);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                JPDAThread t = tr.get();
                if (t != null) {
                    fireThreadStateChanged(t);
                }
            }
        }
    }

    public static boolean isMethodInvoking(JPDAThread t) {
        try {
            return (Boolean) t.getClass().getMethod("isMethodInvoking").invoke(t);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        } catch (NoSuchMethodException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }
    
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DefaultDebuggingColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "DefaultDebuggingColumn";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).
                getString ("CTL_Debugging_Column_Name_Name");
        }

        /**
         * Returns tooltip for given column.
         *
         * @return  tooltip for given node
         */
        @Override
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString
                ("CTL_Debugging_Column_Name_Desc");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return null;
        }
    }
    
    /**
     * Defines model for one table view column. Can be used together with 
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view representation.
     */
    public static class DebuggingSuspendColumn extends AbstractColumn {

        /**
         * Returns unique ID of this column.
         *
         * @return unique ID of this column
         */
        public String getID () {
            return "suspend";
        }

        /** 
         * Returns display name of this column.
         *
         * @return display name of this column
         */
        public String getDisplayName () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Name");
        }

        /**
         * Returns type of column items.
         *
         * @return type of column items
         */
        public Class getType () {
            return Boolean.TYPE;
        }

        /**
         * Returns tooltip for given column. Default implementation returns 
         * <code>null</code> - do not use tooltip.
         *
         * @return  tooltip for given node or <code>null</code>
         */
        @Override
        public String getShortDescription () {
            return NbBundle.getBundle (DebuggingTreeModel.class).getString 
                ("CTL_Debugging_Column_Suspend_Desc");
        }

        /**
         * True if column should be visible by default. Default implementation 
         * returns <code>true</code>.
         *
         * @return <code>true</code> if column should be visible by default
         */
        public boolean initiallyVisible () {
            return true;
        }
    }
    
    private static final class ThreadAlphabetComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof JPDAThreadGroup) {
                if (o2 instanceof JPDAThreadGroup) {
                    String tgn1 = ((JPDAThreadGroup) o1).getName();
                    String tgn2 = ((JPDAThreadGroup) o2).getName();
                    return java.text.Collator.getInstance().compare(tgn1, tgn2);
                }
                return 1;
            } else if (o2 instanceof JPDAThreadGroup) {
                return -1;
            }
            if (!(o1 instanceof JPDAThread) && !(o2 instanceof JPDAThread)) {
                return 0;
            }
            String n1 = ((JPDAThread) o1).getName();
            String n2 = ((JPDAThread) o2).getName();
            return java.text.Collator.getInstance().compare(n1, n2);
        }
        
    }

    private static final class ThreadSuspendComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof JPDAThreadGroup) {
                if (o2 instanceof JPDAThreadGroup) {
                    return 0;
                }
                return 1;
            } else if (o2 instanceof JPDAThreadGroup) {
                return -1;
            }
            if (!(o1 instanceof JPDAThread) && !(o2 instanceof JPDAThread)) {
                return 0;
            }
            boolean s1 = ((JPDAThread) o1).isSuspended();
            boolean s2 = ((JPDAThread) o2).isSuspended();
            if (s1 && !s2) return -1;
            if (!s1 && s2) return +1;
            return 0;
        }
        
    }
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (SORT_ALPHABET.equals(key) || SORT_SUSPEND.equals(key) ||
                    SHOW_SYSTEM_THREADS.equals(key) || SHOW_THREAD_GROUPS.equals(key) ||
                    SHOW_SUSPENDED_THREADS_ONLY.equals(key) ||
                    DebuggingNodeModel.SHOW_PACKAGE_NAMES.equals(key)) {
                // We have to catch the Throwables, so that the AbstractPreferences.EventDispatchThread
                // is not killed. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6467096
                try {
                    fireNodeChanged(ROOT);
                } catch (ThreadDeath td) {
                    throw td;
                } catch (Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        }

    }

    private final class DebuggerFinishListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (JPDADebugger.PROP_STATE.equals(evt.getPropertyName())) {
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) {
                    if (prefListener != null) {
                        try {
                            preferences.removePreferenceChangeListener(prefListener);
                        } catch (IllegalArgumentException e) {
                        }
                    }
                    debugger.removePropertyChangeListener(JPDADebugger.PROP_STATE, this);
                }
            }
        }
        
    }
    
}

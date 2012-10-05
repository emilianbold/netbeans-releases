/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.api.debugger;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/**
 * Manages some set of actions. Loads some set of ActionProviders registered
 * for some context, and allows to call isEnabled and doAction methods on them.
 *
 * @author   Jan Jancura
 */
public final class ActionsManager {


    /** Action constant for Step Over Action. */
    public static final Object              ACTION_STEP_OVER = "stepOver";
    
    /** Action constant for breakpoint hit action. */
    public static final Object              ACTION_RUN_INTO_METHOD = "runIntoMethod";
    
    /** Action constant for Step Into Action. */
    public static final Object              ACTION_STEP_INTO = "stepInto";
    
    /** Action constant for Step Out Action. */
    public static final Object              ACTION_STEP_OUT = "stepOut";
    
    /** Action constant for Step Operation Action. */
    public static final Object              ACTION_STEP_OPERATION = "stepOperation";
    
    /** Action constant for Continue Action. */
    public static final Object              ACTION_CONTINUE = "continue";
    
    /** Action constant for Start Action. */
    public static final Object              ACTION_START = "start";
    
    /** Action constant for Kill Action. */
    public static final Object              ACTION_KILL= "kill";
    
    /** Action constant for Make Caller Current Action. */
    public static final Object              ACTION_MAKE_CALLER_CURRENT = "makeCallerCurrent";
    
    /** Action constant for Make Callee Current Action. */
    public static final Object              ACTION_MAKE_CALLEE_CURRENT = "makeCalleeCurrent";
    
    /** Action constant for Pause Action. */
    public static final Object              ACTION_PAUSE = "pause";
    
    /** Action constant for Run to Cursor Action. */
    public static final Object              ACTION_RUN_TO_CURSOR = "runToCursor";
    
    /** Action constant for Pop Topmost Call Action. */
    public static final Object              ACTION_POP_TOPMOST_CALL = "popTopmostCall";
    
    /** Action constant for Fix Action. */
    public static final Object              ACTION_FIX = "fix";
    
    /** Action constant for Restart Action. */
    public static final Object              ACTION_RESTART = "restart";

    /** Action constant for Toggle Breakpoint Action. */
    public static final Object              ACTION_TOGGLE_BREAKPOINT = "toggleBreakpoint";
    
    /** Action constant for New Watch Action.
     * @since 1.24 */
    public static final Object              ACTION_NEW_WATCH = "newWatch";

    /** Action constant for Evaluate Action.
     *  @since 1.29 */
    public static final Object              ACTION_EVALUATE = "evaluate";


    // variables ...............................................................
    
    private final Vector<ActionsManagerListener>    listener = new Vector<ActionsManagerListener>();
    private final HashMap<String, List<ActionsManagerListener>> listeners = new HashMap<String, List<ActionsManagerListener>>();
    private HashMap<Object, ArrayList<ActionsProvider>>  actionProviders;
    private final Object            actionProvidersLock = new Object();
    private final AtomicBoolean     actionProvidersInitialized = new AtomicBoolean(false);
    private MyActionListener        actionListener = new MyActionListener ();
    private Lookup                  lookup;
    private boolean                 doiingDo = false;
    private boolean                 destroy = false;
    private List<? extends ActionsProvider> aps;
    private PropertyChangeListener  providersChangeListener;
    
    /**
     * Create a new instance of ActionManager.
     * This is called from synchronized blocks of other classes that need to have
     * just one instance of this. Therefore do not put any foreign calls here.
     */
    ActionsManager (Lookup lookup) {
        this.lookup = lookup;
    }
    
    
    // main public methods .....................................................

    /**
     * Performs action on this DebbuggerEngine.
     *
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     * @return true if action has been performed
     */
    public final void doAction (final Object action) {
        doiingDo = true;
        ArrayList<ActionsProvider> l = getActionProvidersForActionWithInit(action);
        boolean done = false;
        if (l != null) {
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                ActionsProvider ap = l.get(i);
                if (ap.isEnabled (action)) {
                    done = true;
                    ap.doAction (action);
                }
            }
        }
        if (done) {
            fireActionDone (action);
        }
        doiingDo = false;
        if (destroy) destroyIn ();
    }
    
    /**
     * Post action on this DebuggerEngine.
     * This method does not block till the action is done,
     * if {@link #canPostAsynchronously} returns true.
     * Otherwise it behaves like {@link #doAction}.
     * The returned task, or
     * {@link ActionsManagerListener} can be used to
     * be notified when the action is done.
     *
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     *
     * @return a task, that can be checked for whether the action finished
     *         or not.
     *
     * @since 1.5
     */
    public final Task postAction(final Object action) {
        doiingDo = true;
        ArrayList<ActionsProvider> l = getActionProvidersForActionWithInit(action);
        boolean posted = false;
        int k;
        if (l != null) {
            k = l.size ();
        } else {
            k = 0;
        }
        List<ActionsProvider> postedActions = new ArrayList<ActionsProvider>(k);
        final AsynchActionTask task = new AsynchActionTask(postedActions);
        if (l != null) {
            int i;
            for (i = 0; i < k; i++) {
                ActionsProvider ap = l.get (i);
                if (ap.isEnabled (action)) {
                    postedActions.add(ap);
                    posted = true;
                }
            }
            if (posted) {
                final int[] count = new int[] { 0 };
                Runnable notifier = new Runnable() {
                    @Override
                    public void run() {
                        synchronized (count) {
                            if (--count[0] == 0) {
                                task.actionDone();
                                fireActionDone (action);
                                doiingDo = false;
                                if (destroy) destroyIn ();
                            }
                        }
                    }
                };
                count[0] = k = postedActions.size();
                for (i = 0; i < k; i++) {
                    postedActions.get(i).postAction (action, notifier);
                }
            }
        }
        if (!posted) {
            doiingDo = false;
            if (destroy) destroyIn ();
            task.actionDone();
        }
        return task;
    }
                                                                                
    /**
     * Returns true if given action can be performed on this DebuggerEngine.
     * 
     * @param action action constant (default set of constants are defined
     *    in this class with ACTION_ prefix)
     * @return true if given action can be performed on this DebuggerEngine
     */
    public final boolean isEnabled (final Object action) {
        boolean doInit = false;
        synchronized (actionProvidersLock) {
            if (actionProviders == null) {
                actionProviders = new HashMap<Object, ArrayList<ActionsProvider>>();
                doInit = true;
            }
        }
        if (doInit) {
            if (SwingUtilities.isEventDispatchThread()) {
                // Need to initialize lazily when called in AWT
                // A state change will be fired after actions providers are initialized.
                new RequestProcessor(ActionsManager.class).post(new Runnable() {
                    @Override
                    public void run() {
                        initActionImpls();
                    }
                });
            } else {
                initActionImpls();
            }
        }
        ArrayList<ActionsProvider> l = getActionProvidersForAction(action);
        if (l != null) {
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                ActionsProvider ap = l.get (i);
                if (ap.isEnabled (action)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Stops listening on all actions, stops firing events.
     */
    public void destroy () {
        if (!doiingDo) destroyIn ();
        destroy = true;
    }

    
    // ActionsManagerListener support ..........................................

    /**
     * Add ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void addActionsManagerListener (ActionsManagerListener l) {
        listener.addElement (l);
    }

    /**
     * Removes ActionsManagerListener.
     *
     * @param l listener instance
     */
    public void removeActionsManagerListener (ActionsManagerListener l) {
        listener.removeElement (l);
    }

    /** 
     * Add ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to add
     */
    public void addActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        synchronized (listeners) {
            List<ActionsManagerListener> ls = listeners.get (propertyName);
            if (ls == null) {
                ls = new ArrayList<ActionsManagerListener>();
                listeners.put (propertyName, ls);
            }
            ls.add(l);
        }
    }

    /** 
     * Remove ActionsManagerListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the ActionsManagerListener to remove
     */
    public void removeActionsManagerListener (
        String propertyName, 
        ActionsManagerListener l
    ) {
        synchronized (listeners) {
            List<ActionsManagerListener> ls = listeners.get (propertyName);
            if (ls == null) return;
            ls.remove(l);
            if (ls.isEmpty()) {
                listeners.remove(propertyName);
            }
        }
    }

    
    // firing support ..........................................................

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionDone (
        final Object action
    ) {
        initListeners ();
        List<ActionsManagerListener> l = new ArrayList<ActionsManagerListener>(listener);
        List<ActionsManagerListener> l1;
        synchronized (listeners) {
            l1 = listeners.get(ActionsManagerListener.PROP_ACTION_PERFORMED);
            if (l1 != null) {
                l1 = new ArrayList<ActionsManagerListener>(l1);
            }
        }
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            l.get(i).actionPerformed(action);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                l1.get(i).actionPerformed(action);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was removed
     */
    private void fireActionStateChanged (
        final Object action
    ) {
        boolean enabled = isEnabled (action);
        initListeners ();
        List<ActionsManagerListener> l = new ArrayList<ActionsManagerListener>(listener);
        List<ActionsManagerListener> l1;
        synchronized (listeners) {
            l1 = listeners.get(ActionsManagerListener.PROP_ACTION_STATE_CHANGED);
            if (l1 != null) {
                l1 = new ArrayList<ActionsManagerListener>(l1);
            }
        }
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            l.get(i).actionStateChanged(action, enabled);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                l1.get(i).actionStateChanged(action, enabled);
            }
        }
    }
    
    
    // private support .........................................................
    
    private ArrayList<ActionsProvider> getActionProvidersForAction(Object action) {
        ArrayList<ActionsProvider> l;
        synchronized (actionProvidersLock) {
            l = actionProviders.get(action);
            if (l != null) {
                l = (ArrayList<ActionsProvider>) l.clone ();
            }
        }
        return l;
    }
    
    private ArrayList<ActionsProvider> getActionProvidersForActionWithInit(Object action) {
        boolean doInit = false;
        synchronized (actionProvidersLock) {
            if (actionProviders == null) {
                actionProviders = new HashMap<Object, ArrayList<ActionsProvider>>();
                doInit = true;
            }
        }
        if (doInit) {
            initActionImpls ();
        } else {
            if (!actionProvidersInitialized.get()) {
                synchronized (actionProvidersInitialized) {
                    if (!actionProvidersInitialized.get()) {
                        try {
                            actionProvidersInitialized.wait();
                        } catch (InterruptedException ex) {}
                    }
                }
            }
        }
        return getActionProvidersForAction(action);
    }
    
    private void registerActionsProvider (Object action, ActionsProvider p) {
        synchronized (actionProvidersLock) {
            ArrayList<ActionsProvider> l = actionProviders.get (action);
            if (l == null) {
                l = new ArrayList<ActionsProvider>();
                actionProviders.put (action, l);
            }
            l.add (p);
        }
        p.addActionsProviderListener (actionListener);
        fireActionStateChanged (action);
    }
    
    private void registerActionsProviders(List<? extends ActionsProvider> aps) {
        synchronized (aps) {
            for (ActionsProvider ap : aps) {
                Iterator ii = ap.getActions ().iterator ();
                while (ii.hasNext ())
                    registerActionsProvider (ii.next (), ap);
            }
        }
    }

    private void initActionImpls () {
        aps = lookup.lookup(null, ActionsProvider.class);
        providersChangeListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    synchronized (actionProvidersLock) {
                        actionProviders.clear();
                    }
                    registerActionsProviders(aps);
                }
        };
        ((Customizer) aps).addPropertyChangeListener(providersChangeListener);
        registerActionsProviders(aps);
        synchronized (actionProvidersInitialized) {
            actionProvidersInitialized.set(true);
            actionProvidersInitialized.notifyAll();
        }
    }

    private boolean listerersLoaded = false;
    private List lazyListeners;
    
    private synchronized void initListeners () {
        if (listerersLoaded) return;
        listerersLoaded = true;
        lazyListeners = lookup.lookup (null, LazyActionsManagerListener.class);
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyActionsManagerListener l = (LazyActionsManagerListener)
                lazyListeners.get (i);
            if (l == null) {
                // instance could not be created.
                continue;
            }
            String[] props = l.getProperties ();
            if (props == null) {
                addActionsManagerListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                addActionsManagerListener (props [j], l);
            }
        }
    }
    
    private void destroyIn () {
        ((Customizer) aps).removePropertyChangeListener(providersChangeListener);
        synchronized (this) {
            if (lazyListeners != null) {
                int i, k = lazyListeners.size ();
                for (i = 0; i < k; i++) {
                    LazyActionsManagerListener l = (LazyActionsManagerListener)
                        lazyListeners.get (i);
                    if (l == null) {
                        // instance could not be created.
                        continue;
                    }
                    String[] props = l.getProperties ();
                    if (props == null) {
                        removeActionsManagerListener (l);
                        continue;
                    }
                    int j, jj = props.length;
                    for (j = 0; j < jj; j++)
                        removeActionsManagerListener (props [j], l);
                    l.destroy ();
                }
                lazyListeners = new ArrayList ();
            }
        }
        synchronized (actionProvidersLock) {
            Collection<ArrayList<ActionsProvider>> apsc = actionProviders.values();
            for (ArrayList<ActionsProvider> aps : apsc) {
                for (ActionsProvider ap : aps) {
                    ap.removeActionsProviderListener(actionListener);
                }
            }
        }
    }

    
    // innerclasses ............................................................
    
    private static class AsynchActionTask extends Task implements Cancellable {
        
        private Collection postedActions;
        
        public AsynchActionTask(Collection postedActions) {
            this.postedActions = postedActions;
        }
        
        void actionDone() {
            notifyFinished();
        }

        @Override
        public boolean cancel() {
            for (Iterator it = postedActions.iterator(); it.hasNext(); ) {
                Object action = it.next();
                if (action instanceof Cancellable) {
                    if (!((Cancellable) action).cancel()) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            return true;
        }
    }
    
    class MyActionListener implements ActionsProviderListener {
        @Override
        public void actionStateChange (Object action, boolean enabled) {
            fireActionStateChanged (action);
        }
    }
}


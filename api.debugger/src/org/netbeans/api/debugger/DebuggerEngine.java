/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger;

import java.beans.*;
import java.io.*;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/** 
 * Debugger Engine represents implementation of one debugger (Java Debugger, 
 * CPP Debugger). It can support debugging of one or more 
 * {@link Session}s, in one or more languages. 
 * It provides root of threads hierarchy (call stacks, locals)
 * and manages debugger actions.
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td>
 *
 * <b>Support for actions:</b>
 *    DebuggerEngine manages list of actions. 
 *    Debugger action (implemented by 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}) can be registerred to 
 *    DebuggerEngine during a start of debugger. See 
 *    {@link org.netbeans.spi.debugger.ActionsProvider}.
 *    DebuggerEngine can be used to call some debugger action 
 *    ({@link #doAction}), to distinguish availability of action 
 *    ({@link #isEnabled}) and to find last called action 
 *    ({@link #getLastAction}).
 *    Example how to call Kill Action on this engine:
 *    <pre>
 *    engine.doAction (DebuggerEngine.ACTION_KILL);</pre>
 *
 * <br>
 * <b>Support for aditional services:</b>
 *    DebuggerEngine is final class. That is why the standard method how to 
 *    extend its functionality is using lookup methods ({@link #lookup} and 
 *    {@link #lookupFirst}).
 *    There are two ways how to register some service provider for some
 *    type of DebuggerEngine:
 *    <ul>
 *      <li>Register 'live' instance of service provider during creation of 
 *        new instance of DebuggerEngine (see method
 *        {@link org.netbeans.spi.debugger.DebuggerEngineProvider#getServices}).
 *      </li>
 *      <li>Register service provider in Manifest-inf/debugger/{{@link 
 *        #getTypeID}} folder. See Debugger SPI for more information about
 *        registration.</li>
 *    </ul>
 *
 * <br>
 * <b>Support for listening:</b>
 *    DebuggerEngine propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link DebuggerEngineListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td>
 *
 * This class is final, so it does not have any external provider.
 * Debugger Plug-ins and UI modules are clients of this class.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td>
 *
 * A new instance(s) of DebuggerEngine class are created in Debugger Core 
 * module only, during the process of starting of debugging (see
 * {@link DebuggerManager#startDebugging}.
 *
 * DebuggerEngine is removed automatically from {@link DebuggerManager} when the 
 * the last action is ({@link #ACTION_KILL}).
 *
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can
 * be added in future.
 *
 * </td></tr></tbody></table>
 *
 * @author   Jan Jancura
 */
public final class DebuggerEngine extends LookupProvider {
    
    /** Action constant for breakpoint hit action. */
    public static final Object              ACTION_BREAKPOINT_HIT = "breakpointHit";
    
    /** Action constant for Step Over Action. */
    public static final Object              ACTION_STEP_OVER = "stepOver";
    
    /** Action constant for Step Into Action. */
    public static final Object              ACTION_STEP_INTO = "stepInto";
    
    /** Action constant for Step Out Action. */
    public static final Object              ACTION_STEP_OUT = "stepOut";
    
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
    
    /** Name of property for the session's state. */
    //public static final String              PROP_STATE = "state"; // NOI18N
    /** Name of property for the set of watches in the system. */
//    public static final String              PROP_WATCHES = "watches"; // NOI18N
    
    /** Name of property for current thread. */
//    public static final String              PROP_CURRENT_THREAD = "currentThread";
    
    
    // variables ...............................................................
    
    private String                  typeID;
//    private Session                 session;
    private Vector                  listener = new Vector ();
    private HashMap                 listeners = new HashMap ();
//    private PropertyChangeSupport   pcs;
    
//    private Object                  lastAction;
    private HashMap                 actionProviders;
    private MyActionListener        actionListener = new MyActionListener ();
    
//    private Vector                  watches;
//    private ThreadsProducer         threadsProducer;
//    private AbstractThread          currentThread = null;
    private Lookup                  lookup;
    Lookup                          privateLookup;
    
    
//    {
//        pcs = new PropertyChangeSupport (this);
//        watches = new Vector ();
//    }

    DebuggerEngine (
        String typeID, 
        Session s, 
        Object[] services,
        Lookup sessionLookup
    ) {
        this.typeID = typeID;
//        session = s;
        privateLookup = (services == null) ? 
            (Lookup) new Lookup.MetaInf (typeID, this) :
            new Lookup.Compound (
                new Lookup.Instance (services),
                new Lookup.MetaInf (typeID, this)
            );
        this.lookup = new Lookup.Compound (
            sessionLookup,
            privateLookup
        );
    }
    
    /**
     * Creates a new instance of DebuggerEngine with given services and id.
     * ID should be unique representation of the type of this engine, like
     * "debuggerjpda.netbeans.org".
     * 
     * @param id unique identifier of type of this debugger engine
     * @param services set of services
     * @return a new instance of DebuggerEngine
     */
//    public static DebuggerEngine create (String id, Object[] services) {
//        return new DebuggerEngine (id, services);
//    }
    
    /**
     * Returns list of services of given type.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public List lookup (Class service) {
        return lookup.lookup (null, service);
    }
    
    /**
     * Returns one service of given type.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public Object lookupFirst (Class service) {
        return lookup.lookupFirst (null, service);
    }
    
    /**
     * Returns list of services of given type from given folder.
     *
     * @param service a type of service to look for
     * @return list of services of given type
     */
    public List lookup (String folder, Class service) {
        return lookup.lookup (folder, service);
    }
    
    /**
     * Returns one service of given type from given folder.
     *
     * @param service a type of service to look for
     * @return ne service of given type
     */
    public Object lookupFirst (String folder, Class service) {
        return lookup.lookupFirst (folder, service);
    }

    /**
     * Returns identifier of type of this engine. This id is used for 
     * identification of engine during registration of services in 
     * Meta-inf/debugger.
     *
     * @return identifier of type of this engine
     */
    public String getTypeID () {
        return typeID;
    }
    
//    /**
//     * Return instance of session this engine has been created for.
//     *
//     * @return instance of session this engine has been created for
//     */
//    public Session getOriginalSession () {
//        return session;
//    }
    
    
    // support for actions .....................................................

    /**
     * Performs action on this DebbuggerEngine.
     *
     * @param action action constant (default set of constanct are defined
     *    in this class with ACTION_ prefix)
     * @return true if action has been performed
     */
   public final boolean doAction (Object action) {
        return fireActionDone (action, doActionIn (action));
    }

    private final boolean doActionIn (Object action) {
        if (actionProviders == null) initActionImpls ();
//        lastAction = action;
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l != null) {
            l = (ArrayList) l.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (((ActionsProvider) l.get (i)).doAction (this, action))
                    return true;
        }
        return false;
    }
    
    private void registerActionsProvider (Object action, ActionsProvider p) {
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l == null) {
            l = new ArrayList ();
            actionProviders.put (action, l);
        }
        l.add (p);
        fireActionStateChanged (action, p.isEnabled (this, action));
        p.addActionsProviderListener (actionListener);
    }
    
    private void unregisterActionsProvider (Object action, ActionsProvider p) {
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l == null) return; 
        l.remove (p);
        if (l.size () == 0)
            actionProviders.remove (action);
        p.removeActionsProviderListener (actionListener);
    }
    
    /**
     * Returns action constant of last action called on this DebuggerEngine.
     * 
     * @return action constant of last action called on this DebuggerEngine
     */
//    public Object getLastAction () {
//        return lastAction;
//    }
    
    /**
     * Returns true if given action can be performed on this DebuggerEngine.
     * 
     * @param action action constant (default set of constanct are defined
     *    in this class with ACTION_ prefix)
     * @return true if given action can be performed on this DebuggerEngine
     */
    public final boolean isEnabled (final Object action) {
        if (actionProviders == null) initActionImpls ();
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l != null) {
            l = (ArrayList) l.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (((ActionsProvider) l.get (i)).isEnabled (this, action))
                    return true;
        }
        return false;
    }
    
    private void initActionImpls () {
        actionProviders = new HashMap ();
        Iterator i = lookup (ActionsProvider.class).iterator ();
        List l = new ArrayList ();
        while (i.hasNext ()) {
            ActionsProvider ap = (ActionsProvider) i.next ();
            Iterator ii = ap.getActions ().iterator ();
            while (ii.hasNext ())
                registerActionsProvider (ii.next (), ap);
        }
    }

    
    // watches .................................................................
//
//    /**
//    * Returns watch for given expression or null.
//    *
//    * @return watch for given expression or null
//    */
//    public Watch findWatch (String expression) {
//        Watch[] ws = getWatches ();
//        int i, k = ws.length;
//        for (i = 0; i < k; i++)
//            if (expression.equals (ws [i]))
//                return ws [i];
//        return null;
//    }
//
//    /**
//    * Returns array of all watches.
//    *
//    * @return array of all watches.
//    */
//    public Watch[] getWatches () {
//        Watch[] w;
//        if (watches == null) return new Watch [0]; // PATCH for deser.
//                                                   // from core/1
//        synchronized (watches) {
//            w = new Watch [watches.size ()];
//            watches.copyInto (w);
//        }
//        return w;
//    }
//
//    /**
//    * Removes all watches.
//    */
//    public void removeAllWatches () {
//        Vector v = (Vector) watches.clone ();
//        int i, k = v.size ();
//        for (i = k - 1; i >= 0; i--)
//            ((Watch) v.elementAt (i)).remove ();
//    }
//
//    /**
//     * Adds watch.
//     *
//     * @param b watch to be added
//     */
//    protected void addWatch (Watch w) {
//        watches.addElement (w);
//        fireWatchCreated (w);
//    }
//
//    /**
//    * Removes watch.
//    *
//    * @param b watch to be removed
//    */
//    public void removeWatch (Watch w) {
//        watches.removeElement (w);
//        fireWatchRemoved (w);
//    }
    
    
    // threads .................................................................
    
    /**
     * Returns root of all threads.
     *
     * @return root of all threads
     */
//    public ThreadsProducer getThreadsRoot () {
//        if (threadsProducer == null) {
//            ThreadsProvider tp = (ThreadsProvider) lookupFirst 
//                (ThreadsProvider.class);
//            threadsProducer = tp.getThreadsRoot ();
//        }
//        return threadsProducer;
//    }

    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
//    public AbstractThread getCurrentThread () {
//        return currentThread;
//    }

    /**
     * Sets current thread. If thread is null, unsets curent thread.
     * 
     * @param thread thread to be current
     */
//    public void setCurrentThread (AbstractThread thread) {
//        if (currentThread == thread) return;
//        Object old = currentThread;
//        currentThread = thread;
//        firePropertyChange (PROP_CURRENT_THREAD, old, thread);
//    }
    
    
    // PCH listener support ....................................................

    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
//    public void addPropertyChangeListener (PropertyChangeListener l) {
//        pcs.addPropertyChangeListener (l);
//    }

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
//    public void removePropertyChangeListener (PropertyChangeListener l) {
//        pcs.removePropertyChangeListener (l);
//    }

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
//    public void addPropertyChangeListener (
//        String propertyName, PropertyChangeListener l
//    ) {
//        pcs.addPropertyChangeListener (propertyName, l);
//    }

    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
//    public void removePropertyChangeListener (
//        String propertyName, PropertyChangeListener l
//    ) {
//        pcs.removePropertyChangeListener (propertyName, l);
//    }
    
    /**
     * Fires property change.
     */
//    private void firePropertyChange (String name, Object o, Object n) {
//        pcs.firePropertyChange (name, o, n);
//    }

    
    // DebuggerEngineListener support ..........................................

    /**
     * Add DebuggerEngineListener.
     *
     * @param l listener instance
     */
    public void addEngineListener (DebuggerEngineListener l) {
        listener.addElement (l);
    }

    /**
     * Removes DebuggerEngineListener.
     *
     * @param l listener instance
     */
    public void removeEngineListener (DebuggerEngineListener l) {
        listener.removeElement (l);
    }

    /** 
     * Add DebuggerEngineListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the DebuggerEngineListener to add
     */
    public void addEngineListener (
        String propertyName, 
        DebuggerEngineListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) {
            listener = new Vector ();
            listeners.put (propertyName, listener);
        }
        listener.addElement (l);
    }

    /** 
     * Remove DebuggerEngineListener.
     *
     * @param propertyName a name of property to listen on
     * @param l the DebuggerEngineListener to remove
     */
    public void removeEngineListener (
        String propertyName, 
        DebuggerEngineListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) return;
        listener.removeElement (l);
        if (listener.size () == 0)
            listeners.remove (propertyName);
    }

    
    // firing support ..........................................................
    
    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a watch
     * {@link DebuggerListener#watchAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param Watch  a watch that was created
     */
//    private void fireWatchCreated (final Watch watch) {
//        initDebuggerEngineListeners ();
//        Vector l = (Vector) listener.clone ();
//        Vector l1 = (Vector) listeners.get (PROP_WATCHES);
//        if (l1 != null)
//            l1 = (Vector) l1.clone ();
//        int i, k = l.size ();
//        for (i = 0; i < k; i++)
//            ((DebuggerEngineListener)l.elementAt (i)).watchAdded (this, watch);
//        if (l1 != null) {
//            k = l1.size ();
//            for (i = 0; i < k; i++)
//                ((DebuggerEngineListener)l1.elementAt (i)).watchAdded (this, watch);
//        }
//        pcs.firePropertyChange (PROP_WATCHES, null, null);
//    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a watch
     * {@link DebuggerListener#watchRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param Watch  a watch that was removed
     */
//    private void fireWatchRemoved (final Watch watch) {
//        initDebuggerEngineListeners ();
//        Vector l = (Vector) listener.clone ();
//        Vector l1 = (Vector) listeners.get (PROP_WATCHES);
//        if (l1 != null)
//            l1 = (Vector) l1.clone ();
//        int i, k = l.size ();
//        for (i = 0; i < k; i++)
//            ((DebuggerEngineListener)l.elementAt (i)).watchRemoved (this, watch);
//        if (l1 != null) {
//            k = l1.size ();
//            for (i = 0; i < k; i++)
//                ((DebuggerEngineListener)l1.elementAt (i)).watchRemoved (this, watch);
//        }
//        pcs.firePropertyChange (PROP_WATCHES, null, null);
//    }

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
    private boolean fireActionDone (
       // final Object oldAction, 
        final Object action, 
        boolean succeed
    ) {
        initDebuggerEngineListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            DebuggerEngineListener.PROP_ACTION_PERFORMED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((DebuggerEngineListener)l.elementAt (i)).actionPerformed ( 
                this, action, succeed
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((DebuggerEngineListener)l1.elementAt (i)).actionPerformed 
                    (this, action, succeed);
        }
        if ((action == ACTION_KILL) && succeed)
            destroyDebuggerEngineListeners ();
        return succeed;
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
        final Object action, 
        boolean enabled
    ) {
        initDebuggerEngineListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            DebuggerEngineListener.PROP_ACTION_STATE_CHANGED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((DebuggerEngineListener)l.elementAt (i)).actionStateChanged ( 
                this, action, enabled
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((DebuggerEngineListener)l1.elementAt (i)).actionStateChanged 
                    (this, action, enabled);
        }
    }
    
    
    // private support .........................................................
    
    private boolean listerersLoaded = false;
    private List lazyListeners;
    
    private void initDebuggerEngineListeners () {
        if (listerersLoaded) return;
        listerersLoaded = true;
        lazyListeners = lookup (LazyDebuggerEngineListener.class);
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyDebuggerEngineListener l = (LazyDebuggerEngineListener)
                lazyListeners.get (i);
            String[] props = l.getProperties ();
            if (props == null) {
                addEngineListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                addEngineListener (props [j], l);
            }
        }
    }
    
    private synchronized void destroyDebuggerEngineListeners () {
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyDebuggerEngineListener l = (LazyDebuggerEngineListener)
                lazyListeners.get (i);
            String[] props = l.getProperties ();
            if (props == null) {
                removeEngineListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                removeEngineListener (props [j], l);
                l.destroy ();
            }
        }
        lazyListeners = new ArrayList ();
    }

    
    // innerclasses ............................................................

    /**
     * This class notifies about DebuggerEngine remove from the system, and
     * about changes in language support. Instance of Destructor can be 
     * obtained from: {@link org.netbeans.spi.debugger.DebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}, or
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider#setDestructor(DebuggerEngine.Destructor)}.
     */
    public class Destructor {
        
        /**
         * Removes DebuggerEngine form all sessions.
         */
        public void killEngine () {
            Session[] ss = DebuggerManager.getDebuggerManager ().getSessions ();
            int i, k = ss.length;
            for (i = 0; i < k; i++)
                ss [i].removeEngine (DebuggerEngine.this);
        }
        
        /**
         * Removes given language support from given session.
         *
         * @param s a session
         * @param language a language to be removed
         */
        public void killLanguage (Session s, String language) {
            s.removeLanguage (language, DebuggerEngine.this);
        }
    }
    
    class MyActionListener implements ActionsProviderListener {
        public void actionStateChange (Object action, boolean enabled) {
            fireActionStateChanged (action, enabled);
        }
    }
}


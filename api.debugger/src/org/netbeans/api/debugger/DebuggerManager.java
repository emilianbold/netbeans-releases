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
import java.net.URL;
import java.util.*;
import java.util.HashMap;
import java.util.Set;
import org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider;
import org.netbeans.spi.debugger.DelegatingSessionProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.debugger.SessionProvider;


/**
 * The root class of Debugger APIs. DebuggerManager manages list of 
 * {@link org.netbeans.api.debugger.Session}s, 
 * {@link org.netbeans.api.debugger.Breakpoint}s and
 * {@link org.netbeans.api.debugger.Watch}es.
 *  
 *
 * <p><br><table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tbody><tr bgcolor="#ccccff">
 * <td colspan="2"><font size="+2"><b>Description </b></font></td>
 * </tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Functionality</b></font></td><td> 
 *
 * <b>Start & finish debugging:</b>
 *    DebuggerManager manages a process of starting a new debugging (
 *    {@link #startDebugging}). It cooperates with all installed
 *    {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s to create a new 
 *    {@link org.netbeans.api.debugger.Session} (or Sessions) and a new 
 *    {@link org.netbeans.api.debugger.DebuggerEngine} (or Engines).
 *    It supports kill all sessions too ({@link #finishAllSessions}).
 *
 * <br><br>
 * <b>Sessions management:</b>
 *    DebuggerManager keeps list of all 
 *    {@link org.netbeans.api.debugger.Session}s ({@link #getSessions}),
 *    and manages current session ({@link #getCurrentSession},
 *    {@link #setCurrentSession}).
 *
 * <br><br>
 * <b>Engine management:</b>
 *    DebuggerManager provides current engine ({@link #getCurrentEngine}).
 *    Current engine is derivated from current session. So,
 *    <i>
 *    debuggerManager.getCurrentEngine () == debuggerManager.
 *    getCurrentSession.getCurrentEngine ()
 *    </i>
 *    should be always true.
 *
 * <br><br>
 * <b>Breakpoints management:</b>
 *    DebuggerManager keeps list of all shared breakpoints 
 *    ({@link #getBreakpoints}).
 *    Breakpoint can be added ({@link #addBreakpoint}) and removed
 *    ({@link #removeBreakpoint}).
 *
 * <br><br>
 * <b>Watches management:</b>
 *    DebuggerManager keeps list of all shared watches ({@link #getWatches}).
 *    Watch can be created & added ({@link #createWatch}).
 *
 * <br><br>
 * <b>Support for listening:</b>
 *    DebuggerManager propagates all changes to two type of listeners - general
 *    {@link java.beans.PropertyChangeListener} and specific
 *    {@link org.netbeans.api.debugger.DebuggerManagerListener}.
 *
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Clinents / Providers</b></font></td><td> 
 *
 * DebuggerCore module should be the only one provider of this abstract class.
 * This class should be called from debugger plug-in modules and from debugger
 * UI modules. 
 * 
 * <br>
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Lifecycle</b></font></td><td> 
 *
 * The only one instance of DebuggerManager should exist, and it should be 
 * created in {@link #getDebuggerManager} method.
 * 
 * </td></tr><tr><td align="left" valign="top" width="1%"><font size="+1">
 * <b>Evolution</b></font></td><td>
 *
 * No method should be removed from this class, but some functionality can 
 * be added.
 *
 * </td></tr></tbody></table>
 *
 * @author Jan Jancura
 */
public final class DebuggerManager {

    
    /** Name of property for the set of breakpoints in the system. */
    public static final String                PROP_BREAKPOINTS = "breakpoints"; // NOI18N

    /** Name of property for current debugger engine. */
    public static final String                PROP_CURRENT_ENGINE = "currentEngine";

    /** Name of property for current debugger session. */
    public static final String                PROP_CURRENT_SESSION = "currentSession";

    /** Name of property for current thread. */
    //public static final String                PROP_CURRENT_THREAD = "currentThread";
    
    /** Name of property for set of running debugger engines. */
//    public static final String                PROP_DEBUGGER_ENGINES = "debuggerEngines";
    
    /** Name of property for set of running debugger sessions. */
    public static final String                PROP_SESSIONS = "sessions";

    /** Name of property for the set of watches in the system. */
    public static final String                PROP_WATCHES = "watches"; // NOI18N
    
    
    private static DebuggerManager            debuggerManager;
    
    
    private Session             currentSession;
    private DebuggerEngine      currentEngine;
    private Session[]           sessions = new Session [0];
    private Vector              breakpoints = new Vector ();
    private Vector              watches = new Vector ();
    static private ArrayList    debuggerPlugIns;
    private SessionListener     sessionListener = new SessionListener ();
    private Vector              listener = new Vector ();
    private HashMap             listeners = new HashMap ();
//    private PropertyChangeSupport pcs = new PropertyChangeSupport (this);
    
    private Lookup              lookup = new Lookup.MetaInf (null, null);
    
    
    /**
     * Returns default instance of DebuggerManager.
     *
     * @return default instance of DebuggerManager
     */
    public static DebuggerManager getDebuggerManager () {
        if (debuggerManager == null) 
            debuggerManager = new DebuggerManager ();
        return debuggerManager;
    }

    /**
     * Creates a new instance of DebuggerManager.
     */
    private DebuggerManager () {
    }

    
    // lookup management .............................................
    
    /**
     * Finds all registrations of given service in Meta-inf/debugger/ folder 
     * and returns instances of it.
     */
     public List lookup (Class service) {
        return lookup.lookup (null, service);
    }
    
    /**
     * Finds first occurence of service in Meta-inf/debugger/ folder 
     * and returns instance of it.
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
    
    
    // session / engine management .............................................
    
    /** 
     * Start a new debugging for given 
     * {@link org.netbeans.api.debugger.DebuggerInfo}. DebuggerInfo provides
     * information needed to start new debugging. DebuggerManager finds
     * all {@link org.netbeans.spi.debugger.SessionProvider}s and 
     *  {@link org.netbeans.spi.debugger.DelegatingSessionProvider}s
     * installed for given DebuggerInfo, and creates a new 
     * {@link Session}(s). 
     * After that it looks for all 
     * {@link org.netbeans.spi.debugger.DebuggerEngineProvider}s and 
     * {@link org.netbeans.spi.debugger.DelegatingDebuggerEngineProvider}s
     * installed for Session, and crates a new 
     * {@link DebuggerEngine}(s).
     *
     * @param info debugger startup info
     */
    public void startDebugging (DebuggerInfo info) {
        //S ystem.out.println("@StartDebugging info: " + info);
        
        // init sessions
        ArrayList sessionProviders = new ArrayList ();
        ArrayList engines = new ArrayList ();
        Lookup l = info.getLookup ();
        sessionProviders.addAll (
            l.lookup (
                null,
                SessionProvider.class
            )
        );
        sessionProviders.addAll (
            l.lookup (
                null,
                DelegatingSessionProvider.class
            )
        );
        Session sessionToStart = null;
        int i, k = sessionProviders.size ();
        for (i = 0; i < k; i++) {
            Session s = null;
            if (sessionProviders.get (i) instanceof DelegatingSessionProvider) {
                s = ((DelegatingSessionProvider) sessionProviders.get (i)).
                    getSession (info);
                l = new Lookup.Compound (
                    l,
                    s.privateLookup
                );
                //S ystem.out.println("@  StartDebugging DelegaingSession: " + s);
            } else {
                SessionProvider sp = (SessionProvider) sessionProviders.get (i);
                s = new Session (
                    sp.getSessionName (),
                    sp.getLocationName (),
                    sp.getTypeID (),
                    sp.getServices (),
                    l
                );
                sessionToStart = s;
                l = s.getLookup ();
                addSession (s);
                //S ystem.out.println("@  StartDebugging new Session: " + s);
            }
            
            // init DebuggerEngines
            ArrayList engineProviders = new ArrayList ();
            engineProviders.addAll (
                l.lookup (null, DebuggerEngineProvider.class)
            );
            engineProviders.addAll (
                l.lookup (null, DelegatingDebuggerEngineProvider.class)
            );
            int j, jj = engineProviders.size ();
            for (j = 0; j < jj; j++) {
                DebuggerEngine engine = null;
                String[] languages = null; 
                if (engineProviders.get (j) instanceof DebuggerEngineProvider) {
                    DebuggerEngineProvider ep = (DebuggerEngineProvider) 
                        engineProviders.get (j);
                    Object[] services = ep.getServices ();
                    engine = new DebuggerEngine (
                        ((DebuggerEngineProvider) engineProviders.get (j)).
                            getEngineTypeID (),
                        s,
                        services,
                        l
                    );
                    languages = ep.getLanguages ();
                    ep.setDestructor (engine.new Destructor ());
                    engines.add (engine);
                    //S ystem.out.println("@    StartDebugging new Engine: " + engine);
                } else {
                    DelegatingDebuggerEngineProvider dep = 
                        (DelegatingDebuggerEngineProvider) 
                        engineProviders.get (j);
                    languages = dep.getLanguages ();
                    engine = dep.getEngine ();
                    dep.setDestructor (engine.new Destructor ());
                    //S ystem.out.println("@    StartDebugging DelegatingEngine: " + engine);
                }
                int w, ww = languages.length;
                for (w = 0; w < ww; w++)
                    s.addLanguage (languages [w], engine);
            }
        }
        
        k = engines.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerEngine) engines.get (i)).doAction 
                (DebuggerEngine.ACTION_START);
        }
        
        if (sessionToStart == null) return;
        setCurrentSession (sessionToStart);
    }

    /**
     * Kills all {@link org.netbeans.api.debugger.Session}s and
     * {@link org.netbeans.api.debugger.DebuggerEngine}s.
     */
    public void finishAllSessions () {
        Session[] ds = getSessions ();
        
        if (ds.length == 0) return;

        // finish all non persistent sessions
        int i, k = ds.length;
        for (i = 0; i < k; i++)
            ds [i].getCurrentEngine ().doAction (DebuggerEngine.ACTION_KILL);
    }
    
    /**
     * Returns current debugger session or <code>null</code>.
     *
     * @return current debugger session or <code>null</code>
     */
    public Session getCurrentSession () {
        return currentSession;
    }

    /**
     * Sets current debugger session.
     *
     * @param session a session to be current
     */
    public void setCurrentSession (Session session) {
        // 1) check if the session is registerred
        if (session != null) {
            int i, k = sessions.length;
            for (i = 0; i < k; i++)
                if (session == sessions [i]) break;
            if (i == k) 
                return;
        }
        
        // fire all changes
        Session old = getCurrentSession ();
//        AbstractThread oldThread = getCurrentThread ();
        currentSession = session;
        //S ystem.out.println("DebuggerManagerImpl:setCurrentSession " + session);
        updateCurrentEngine ();
        firePropertyChange (PROP_CURRENT_SESSION, old, currentSession);
        //S ystem.out.println("DebuggerManagerImpl:currentThread " + oldThread + " => " + getCurrentThread ());
        
        // set current threads root
//        if (threadProducer != null)
//            threadProducer.setRemoteThreadGroup (
//                getCurrentEngine ().getThreadsRoot ()
//            );
    }

    /**
     * Returns set of running debugger sessions.
     *
     * @return set of running debugger sessions
     */
    public Session[] getSessions () {
        return sessions;
    }
    
    /**
     * Returns current debugger engine or <code>null</code>.
     *
     * @return current debugger engine or <code>null</code>
     */
    public DebuggerEngine getCurrentEngine () {
        return currentEngine;
    }

    /**
     * Returns set of running debugger engines.
     *
     * @return set of running debugger engines
     */
//    public abstract Set getDebuggerEngines ();

    
    // threads management ......................................................
    
    /**
     * Returns current thread or <code>null</code>. Current thread 
     * should be contained in {@link #getThreadsRoot} hierarchy.
     *
     * @return current thread or <code>null</code>
     */
//    public abstract AbstractThread getCurrentThread ();
    
    /**
     * Returns root of threads hierarchy.
     *
     * @return root of threads hierarchy.
     */
//    public abstract ThreadsProducer getThreadsRoot ();

    
    // breakpoints management ..................................................
    
    /** 
     * Adds a new breakpoint.
     *
     * @param breakpoint a new breakpoint
     */
    public void addBreakpoint (
        Breakpoint breakpoint
    ) {
        breakpoints.addElement (breakpoint);
        fireBreakpointCreated (breakpoint);
    }
    
    /** 
     * Removes breakpoint.
     *
     * @param breakpoint a breakpoint to be removed
     */
    public void removeBreakpoint (
        Breakpoint breakpoint
    ) {
        breakpoints.removeElement (breakpoint);
        breakpoint.dispose ();
        fireBreakpointRemoved (breakpoint);
    }

    /** 
     * Gets all registered breakpoints.
     *
     * @return all breakpoints
     */
    public Breakpoint[] getBreakpoints () {
        Breakpoint[] b;
        synchronized (breakpoints) {
            b = new Breakpoint [breakpoints.size ()];
            breakpoints.copyInto (b);
        }
        return b;
    }

    
    // watches management ......................................................

    /** 
     * Creates a watch with its expression set to an initial value.
     * Also allows creation of a hidden watch (not presented to the user), 
     * for example for internal use in the editor to obtain values of variables
     * under the mouse pointer.
     *
     * @param expr expression to watch for (the format is the responsibility 
     *    of the debugger plug-in implementation, but it is typically 
     *    a variable name).
     * @param hidden <code>true</code> if the watch should be hidden for
     *    a user
     * @return the new watch
     */
    public Watch createWatch (String expr) {
        Watch w = new Watch (expr);
        watches.addElement (w);
        fireWatchCreated (w);
        return w;
    }

    /**
    * Gets all shared watches in the system.
    *
    * @return all watches
    */
    public Watch[] getWatches () {
        Watch[] w;
        if (watches == null) return new Watch [0];
        synchronized (watches) {
            w = new Watch [watches.size ()];
            watches.copyInto (w);
        }
        return w;
    }

    /**
    * Removes all watches from the system.
    */
    public void removeAllWatches () {
        //if (!watchesInitialized) initWatches ();
        Vector v = (Vector) watches.clone ();
        int i, k = v.size ();
        for (i = k - 1; i >= 0; i--)
            ((Watch) v.elementAt (i)).remove ();
    }

    /**
    * Removes watch.
    *
    * @param b watch to be removed
    */
    void removeWatch (Watch w) {
        watches.removeElement (w);
        fireWatchRemoved (w);
    }

    
    // listeners ...............................................................


//    /**
//    * Adds property change listener.
//    *
//    * @param l new listener.
//    */
//    public void addPropertyChangeListener (PropertyChangeListener l) {
//        pcs.addPropertyChangeListener (l);
//    }
//
//    /**
//    * Removes property change listener.
//    *
//    * @param l removed listener.
//    */
//    public void removePropertyChangeListener (PropertyChangeListener l) {
//        pcs.removePropertyChangeListener (l);
//    }
//
//    /**
//    * Adds property change listener.
//    *
//    * @param l new listener.
//    */
//    public void addPropertyChangeListener (
//        String propertyName, 
//        PropertyChangeListener l
//    ) {
//        pcs.addPropertyChangeListener (propertyName, l);
//    }
//
//    /**
//    * Removes property change listener.
//    *
//    * @param l removed listener.
//    */
//    public void removePropertyChangeListener (
//        String propertyName, 
//        PropertyChangeListener l
//    ) {
//        pcs.removePropertyChangeListener (propertyName, l);
//    }
    
    /**
    * Fires property change.
    */
    private void firePropertyChange (String name, Object o, Object n) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (name);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, name, o, n
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((DebuggerManagerListener)l.elementAt (i)).propertyChange (ev);
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((DebuggerManagerListener)l1.elementAt (i)).propertyChange (ev);
        }
    }

    /**
    * This listener notificates about changes of breakpoints, watches and threads.
    *
    * @param l listener object.
    */
    public void addDebuggerListener (DebuggerManagerListener l) {
        listener.addElement (l);
    }

    /**
    * Removes debugger listener.
    *
    * @param l listener object.
    */
    public void removeDebuggerListener (DebuggerManagerListener l) {
        listener.removeElement (l);
    }

    /** 
     * Add a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to add
     */
    public void addDebuggerListener (
        String propertyName, 
        DebuggerManagerListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) {
            listener = new Vector ();
            listeners.put (propertyName, listener);
        }
        listener.addElement (l);
    }

    /** 
     * Remove a debuggerManager listener to changes of watches and breakpoints.
     *
     * @param propertyName a name of property to listen on
     * @param l the debuggerManager listener to remove
     */
    public void removeDebuggerListener (
        String propertyName, 
        DebuggerManagerListener l
    ) {
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) return;
        listener.removeElement (l);
        if (listener.size () == 0)
            listeners.remove (propertyName);
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was created
     */
//    private void fireCurrentThreadChanged (
//        final AbstractThread oldThread,
//        final AbstractThread newThread
//    ) {
//        initDebuggerManagerListeners ();
//        Vector l = (Vector) listener.clone ();
//        Vector l1 = (Vector) listeners.get (PROP_CURRENT_THREAD);
//        if (l1 != null)
//            l1 = (Vector) l1.clone ();
//        int i, k = l.size ();
//        for (i = 0; i < k; i++)
//            ((DebuggerManagerListener)l.elementAt (i)).currentThreadChanged 
//                (oldThread, newThread);
//        if (l1 != null) {
//            k = l1.size ();
//            for (i = 0; i < k; i++)
//                ((DebuggerManagerListener)l1.elementAt (i)).currentThreadChanged 
//                    (oldThread, newThread);
//        }
//        pcs.firePropertyChange (PROP_CURRENT_THREAD, oldThread, newThread);
//    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a breakpoint
     * {@link DebuggerManagerListener#breakpointAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param breakpoint  a breakpoint that was created
     */
    private void fireBreakpointCreated (final Breakpoint breakpoint) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_BREAKPOINTS);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).breakpointAdded 
                (breakpoint);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).breakpointAdded 
                    (breakpoint);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
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
    private void fireBreakpointRemoved (final Breakpoint breakpoint) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_BREAKPOINTS);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_BREAKPOINTS, null, null
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener)l.elementAt (i)).breakpointRemoved 
                (breakpoint);
            ((DebuggerManagerListener) l.elementAt (i)).propertyChange (ev);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener)l1.elementAt (i)).breakpointRemoved 
                    (breakpoint);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a watch
     * {@link DebuggerManagerListener#watchAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param Watch  a watch that was created
     */
    private void fireWatchCreated (final Watch watch) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_WATCHES);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener)l.elementAt (i)).watchAdded 
                (watch);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener)l1.elementAt (i)).watchAdded 
                    (watch);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a watch
     * {@link DebuggerManagerListener#watchRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param Watch  a watch that was removed
     */
    private void fireWatchRemoved (final Watch watch) {
        initDebuggerManagerListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_WATCHES);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_WATCHES, null, null
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener)l.elementAt (i)).watchRemoved 
                (watch);
            ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener)l1.elementAt (i)).watchRemoved 
                    (watch);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a session
     * {@link DebuggerManagerListener#engineAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param session a session that was created
     */
    private void fireSessionAdded (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        //S ystem.out.println("DebuggerManagerImpl:sessionAdded " + session);
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_SESSIONS);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).sessionAdded 
                (session);
            ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).sessionAdded
                    (session);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a session
     * {@link DebuggerManagerListener#engineRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param session a session that was removed
     */
    private void fireSessionRemoved (
        final Session session,
        final Session[] old,
        final Session[] ne
    ) {
        initDebuggerManagerListeners ();
        //S ystem.out.println("DebuggerManagerImpl:sessionRemoved " + session);
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (PROP_SESSIONS);
        PropertyChangeEvent ev = new PropertyChangeEvent (
            this, PROP_SESSIONS, old, ne
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++) {
            ((DebuggerManagerListener) l.elementAt (i)).sessionRemoved 
                (session);
            ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
        }
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++) {
                ((DebuggerManagerListener) l1.elementAt (i)).sessionRemoved 
                    (session);
                ((DebuggerManagerListener) l1.elementAt (i)).propertyChange (ev);
            }
        }
    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a debugger engine
     * {@link DebuggerManagerListener#engineAdded was added}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param session a session that was created
     */
//    private void fireEngineAdded (
//        final DebuggerEngine engine,
//        final Set old,
//        final Set ne
//    ) {
//        S ystem.out.println("DebuggerManagerImpl:engineAdded " + engine);
//        Vector l = (Vector) listener.clone ();
//        Vector l1 = (Vector) listeners.get (PROP_DEBUGGER_ENGINES);
//        if (l1 != null)
//            l1 = (Vector) l1.clone ();
//        int i, k = l.size ();
//        for (i = 0; i < k; i++)
//            ((DebuggerManagerListener) l.elementAt (i)).engineAdded 
//                (engine);
//        if (l1 != null) {
//            k = l1.size ();
//            for (i = 0; i < k; i++)
//                ((DebuggerManagerListener) l1.elementAt (i)).engineAdded
//                    (engine);
//        }
//        pcs.firePropertyChange (PROP_DEBUGGER_ENGINES, old, ne);
//    }

    /**
     * Notifies registered listeners about a change.
     * Notifies {@link #listener registered listeners} that a session
     * {@link DebuggerManagerListener#engineRemoved was removed}
     * and {@link #pcs property change listeners} that its properties
     * {@link PropertyChangeSupport#firePropertyChange(String, Object, Object)}
     * were changed.
     *
     * @param session a session that was removed
     */
//    private void fireEngineRemoved (
//        final DebuggerEngine engine,
//        final Set old,
//        final Set ne
//    ) {
//        S ystem.out.println("DebuggerManagerImpl:engineRemoved " + engine);
//        Vector l = (Vector) listener.clone ();
//        Vector l1 = (Vector) listeners.get (PROP_DEBUGGER_ENGINES);
//        if (l1 != null)
//            l1 = (Vector) l1.clone ();
//        int i, k = l.size ();
//        for (i = 0; i < k; i++)
//            ((DebuggerManagerListener) l.elementAt (i)).engineRemoved 
//                (engine);
//        if (l1 != null) {
//            k = l1.size ();
//            for (i = 0; i < k; i++)
//                ((DebuggerManagerListener) l1.elementAt (i)).engineRemoved 
//                    (engine);
//        }
//        pcs.firePropertyChange (PROP_DEBUGGER_ENGINES, old, ne);
//    }

    
    // helper methods ....................................................
     
    /**
     * Returns all registered DebuggerManager Implementations ({@link DebuggerPlugIn}).
     *
     * @return all registered DebuggerManager Implementations ({@link DebuggerPlugIn})
     */
//    private static ArrayList loadMetaInf (String resourceName) {
//        ArrayList l = new ArrayList ();
//        try {
//            ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
//            S ystem.out.println("");
//            S ystem.out.println("loadMetaInf " + resourceName);
//            Enumeration e = cl.getResources (resourceName);
//            while (e.hasMoreElements ()) {
//                URL url = (URL) e.nextElement();
//                //S ystem.out.println("  url: " + url);
//                BufferedReader br = new BufferedReader (
//                    new InputStreamReader (url.openStream ())
//                );
//                String s = br.readLine ();
//                while (s != null) {
//                    S ystem.out.println("  class:" + s);
//                    Object o = cl.loadClass (s).newInstance ();
//                    l.add (o);
//                    s = br.readLine ();
//                }
//            }
//            return l; 
//        } catch (IOException e) {
//            e.printStackTrace ();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace ();
//        } catch (InstantiationException e) {
//            e.printStackTrace ();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace ();
//        }
//        throw new InternalError ("Can not read from Meta-inf!");
//    }
//    
//    private static List getProviders (Class cl, String folderName) {
//        ArrayList l = new ArrayList ();
//        l.addAll (loadMetaInf (
//            "META-INF/debugger/" + cl.getName ()
//        ));
//        if (folderName != null)
//            l.addAll (loadMetaInf (
//                "META-INF/debugger/" + 
//                folderName + 
//                "/" + cl.getName ()
//            ));
//        return l; 
//    }
    
    private boolean listerersLoaded = false;
    
    private void initDebuggerManagerListeners () {
        if (listerersLoaded) return;
        listerersLoaded = true;
        List listeners = lookup (LazyDebuggerManagerListener.class);
        int i, k = listeners.size ();
        for (i = 0; i < k; i++) {
            LazyDebuggerManagerListener l = (LazyDebuggerManagerListener)
                listeners.get (i);
            String[] props = l.getProperties ();
            if ((props == null) || (props.length == 0)) {
                addDebuggerListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                addDebuggerListener (props [j], l);
            }
        }
    }

//    /**
//     * Returns running debugger engines.
//     *
//     * @return running debugger engines
//     */
//    public Set getDebuggerEngines () {
//        return debuggerEngines;
//    }
    
    private void addSession (Session session) {
        int i, k = sessions.length;
        for (i = 0; i < k; i++)
            if (session == sessions [i]) return;
            
        Session[] nds = new Session [sessions.length + 1];
        System.arraycopy (sessions, 0, nds, 0, sessions.length);
        nds [sessions.length] = session;
        
        session.addPropertyChangeListener (sessionListener);
        
        Session[] o = sessions;
        sessions = nds;
        checkEngines ();
        fireSessionAdded (session, o, sessions);
    }
    
    private void checkEngines () {
//        Set old = debuggerEngines;
//        HashSet n = new HashSet ();
//        Session[] sessions = getSessions ();
//        int i, k = sessions.length;
//        for (i = 0; i < k; i++)
//            n.addAll (Arrays.asList (sessions [i].getAllEngines ()));
//        
//        debuggerEngines = n;
//        // remove listeners
//        Iterator it = old.iterator ();
//        while (it.hasNext ()) {
//            DebuggerEngine e = (DebuggerEngine) it.next ();
//            if (n.contains (e)) continue;
//            e.removePropertyChangeListener (sessionListener);
//            fireEngineRemoved (e, old, n);
//        }
//        
//        // add listeners
//        it = n.iterator ();
//        while (it.hasNext ()) {
//            DebuggerEngine e = (DebuggerEngine) it.next ();
//            if (old.contains (e)) continue;
//            e.addPropertyChangeListener (sessionListener);
//            fireEngineAdded (e, old, n);
//        }
        updateCurrentEngine ();
    }
    
    private void removeSession (Session session) {
        // find index of given debugger and new instance of currentDebugger
        Session nCurrentSesson = null;
        int i, k = sessions.length;
        for (i = 0; i < k; i++)
            if (sessions [i] == session) break;
            else 
            if (nCurrentSesson == null) 
                nCurrentSesson = sessions [i];
        if (i == k) return; // this debugger is not registered
            
        // set new current debugger     
        if (session == getCurrentSession ()) {
            if ((nCurrentSesson == null) && (k > 1))
                nCurrentSesson = sessions [1];
            setCurrentSession (nCurrentSesson);
        }
            
        Session[] nds = new Session [sessions.length - 1];
        System.arraycopy (sessions, 0, nds, 0, i);
        if ((sessions.length - i) > 1)
            System.arraycopy (
                sessions, i + 1, nds, i, sessions.length - i - 1
            );
        
        session.removePropertyChangeListener (sessionListener);
        checkEngines ();
        
        Session[] o = sessions;
        sessions = nds;
        fireSessionRemoved (session, o, sessions);
    }
    
    private void updateCurrentEngine () {
        DebuggerEngine ne = null;
        if (getCurrentSession () != null)
            ne = getCurrentSession ().getCurrentEngine ();
       // if (currentEngine == ne) return;
        DebuggerEngine old = currentEngine;
//        AbstractThread oldT = getCurrentThread ();
        currentEngine = ne;
        //S ystem.out.println("DebuggerManagerImpl:currentEngine " + currentEngine);
        firePropertyChange (PROP_CURRENT_ENGINE, old, currentEngine);
//        fireCurrentThreadChanged (oldT, getCurrentThread ());
//        if (old != null)
//            old.removePropertyChangeListener (
//                DebuggerEngine.PROP_CURRENT_THREAD,
//                sessionListener
//            );
//        if (currentEngine != null)
//            currentEngine.addPropertyChangeListener (
//                DebuggerEngine.PROP_CURRENT_THREAD,
//                sessionListener
//            );
    }


    
    // innerclasses ............................................................

    /**
     * Listens on all engines and sessions for: 
     * current thread changes 
     * start / finish of engines
     * last action
     * current engine
     */  
    private class SessionListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent e) {
//            if (e.getSource () instanceof DebuggerEngine) {
//                DebuggerEngine eng = (DebuggerEngine) e.getSource ();
//                if (e.getPropertyName ().equals (PROP_CURRENT_THREAD)) {
//                    // updates curremt thread
//                    if (eng == getCurrentEngine ()) {
//                        S ystem.out.println("DebuggerManagerImpl:currentThread " + e.getOldValue () + " => " + e.getNewValue ());
//                        fireCurrentThreadChanged (
//                            (AbstractThread) e.getOldValue (), 
//                            (AbstractThread) e.getNewValue ()
//                        );
//                    }
//                }
//            } else
            if (e.getSource () instanceof Session) {
                if ( (!e.getPropertyName ().equals
                      (Session.PROP_CURRENT_LANGUAGE)) &&
                     (!e.getPropertyName ().equals
                      (Session.PROP_SUPPORTED_LANGUAGES))
                ) return;
                // update list of engines and current engine
                checkEngines ();
                Session s = (Session) e.getSource ();
                if (s.getSupportedLanguages ().length == 0)
                    removeSession (s);
            }
        }
    }
}


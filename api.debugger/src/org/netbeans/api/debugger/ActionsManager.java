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
 *    {@link ActionsManagerListener}.
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
public final class ActionsManager {
    
    
    // variables ...............................................................
    
    private Vector                  listener = new Vector ();
    private HashMap                 listeners = new HashMap ();
    private HashMap                 actionProviders;
    private MyActionListener        actionListener = new MyActionListener ();
    private Lookup                  lookup;

    
    ActionsManager (Lookup lookup) {
        this.lookup = lookup;
    }
    
    
    // main public methods .....................................................

    /**
     * Performs action on this DebbuggerEngine.
     *
     * @param action action constant (default set of constanct are defined
     *    in this class with ACTION_ prefix)
     * @return true if action has been performed
     */
    public final void doAction (Object action) {
        doActionIn (action);
        fireActionDone (action);
    }
    
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
                if (((ActionsProvider) l.get (i)).isEnabled (action))
                    return true;
        }
        return false;
    }
    
    /**
     * Stops listening on all actions, stops firing events.
     */
    public void destroy () {
        destroyDebuggerEngineListeners ();
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
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) {
            listener = new Vector ();
            listeners.put (propertyName, listener);
        }
        listener.addElement (l);
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
        Vector listener = (Vector) listeners.get (propertyName);
        if (listener == null) return;
        listener.removeElement (l);
        if (listener.size () == 0)
            listeners.remove (propertyName);
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
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            ActionsManagerListener.PROP_ACTION_PERFORMED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((ActionsManagerListener) l.elementAt (i)).actionPerformed ( 
                action
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((ActionsManagerListener) l1.elementAt (i)).actionPerformed 
                    (action);
        }
//        if ((action == DebuggerManager.ACTION_KILL) && succeed)
//            destroyDebuggerEngineListeners ();
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
        initListeners ();
        Vector l = (Vector) listener.clone ();
        Vector l1 = (Vector) listeners.get (
            ActionsManagerListener.PROP_ACTION_STATE_CHANGED
        );
        if (l1 != null)
            l1 = (Vector) l1.clone ();
        int i, k = l.size ();
        for (i = 0; i < k; i++)
            ((ActionsManagerListener) l.elementAt (i)).actionStateChanged ( 
                action, enabled
            );
        if (l1 != null) {
            k = l1.size ();
            for (i = 0; i < k; i++)
                ((ActionsManagerListener) l1.elementAt (i)).actionStateChanged 
                    (action, enabled);
        }
    }
    
    
    // private support .........................................................

    private final void doActionIn (Object action) {
        if (actionProviders == null) initActionImpls ();
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l != null) {
            l = (ArrayList) l.clone ();
            int i, k = l.size ();
            for (i = 0; i < k; i++)
                if (((ActionsProvider) l.get (i)).isEnabled (action))
                    ((ActionsProvider) l.get (i)).doAction (action);
        }
    }
    
    private void registerActionsProvider (Object action, ActionsProvider p) {
        ArrayList l = (ArrayList) actionProviders.get (action);
        if (l == null) {
            l = new ArrayList ();
            actionProviders.put (action, l);
        }
        l.add (p);
        fireActionStateChanged (action, p.isEnabled (action));
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
    
    private void initActionImpls () {
        actionProviders = new HashMap ();
        Iterator i = lookup.lookup (null, ActionsProvider.class).iterator ();
        List l = new ArrayList ();
        while (i.hasNext ()) {
            ActionsProvider ap = (ActionsProvider) i.next ();
            Iterator ii = ap.getActions ().iterator ();
            while (ii.hasNext ())
                registerActionsProvider (ii.next (), ap);
        }
    }
    
    private boolean listerersLoaded = false;
    private List lazyListeners;
    
    private void initListeners () {
        if (listerersLoaded) return;
        listerersLoaded = true;
        lazyListeners = lookup.lookup (null, LazyActionsManagerListener.class);
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyActionsManagerListener l = (LazyActionsManagerListener)
                lazyListeners.get (i);
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
    
    private synchronized void destroyDebuggerEngineListeners () {
        int i, k = lazyListeners.size ();
        for (i = 0; i < k; i++) {
            LazyActionsManagerListener l = (LazyActionsManagerListener)
                lazyListeners.get (i);
            String[] props = l.getProperties ();
            if (props == null) {
                removeActionsManagerListener (l);
                continue;
            }
            int j, jj = props.length;
            for (j = 0; j < jj; j++) {
                removeActionsManagerListener (props [j], l);
                l.destroy ();
            }
        }
        lazyListeners = new ArrayList ();
    }

    
    // innerclasses ............................................................
    
    class MyActionListener implements ActionsProviderListener {
        public void actionStateChange (Object action, boolean enabled) {
            fireActionStateChanged (action, enabled);
        }
    }
}


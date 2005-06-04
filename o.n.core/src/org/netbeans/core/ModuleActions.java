/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.Action;

import org.openide.actions.ActionManager;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;
import org.openide.util.Lookup;

import org.netbeans.core.startup.ManifestSection;


/**
 * Holds list of all actions added by modules.
 * @author Jaroslav Tulach, Jesse Glick
 */
public class ModuleActions extends ActionManager
/*implements PropertyChangeListener*/ {
    
    /** array of all actions added by modules */
    private static SystemAction[] array;
    /** of (ModuleItem, List (ManifestSection.ActionSection)) */
    private static Map map = new HashMap (7);
    /** current module */
    private static Object module = null;
    /** Map of currently running actions, (maps action event to action) */
    private Map runningActions = new HashMap();

    public static ModuleActions getDefaultInstance() {
        ActionManager mgr = ActionManager.getDefault();
        assert mgr instanceof ModuleActions : "Got wrong ActionManager instance: " + mgr + " from " + Lookup.getDefault();
        return (ModuleActions)mgr;
    }

    /** Array with all activated actions.
    * Can contain null that will be replaced by separators.
    */
    public SystemAction[] getContextActions () {
        SystemAction[] a = array;
        if (a != null) {
            return a;
        }
        array = a = createActions ();
        return a;
    }
    
    /** Invokes action in a RequestPrecessor dedicated to performing
     * actions.
     */
    public void invokeAction(final Action a, final ActionEvent e) {
        try {
            org.openide.util.Mutex.EVENT.readAccess (new Runnable() {
                public void run() {
                    showWaitCursor(e);
                }
            });
            addRunningAction(a, e);
            
            a.actionPerformed (e);
        } finally {
            removeRunningAction(e);
            org.openide.util.Mutex.EVENT.readAccess (new Runnable() {
                public void run() {
                    hideWaitCursor(e);
                }
            });
        }
    }
    
    /** Listens on change of modules and if changed,
    * fires change to all listeners.
    */
    private void fireChange () {
        firePropertyChange(PROP_CONTEXT_ACTIONS, null, null);
    }
    
    /** Adds action to <code>runningAction</code> map using event as a key.
     * @param rp <code>RequestProcessor</code> which runs the actio task
     * @param action action to put in map 
     * @param evt action event used as key in the map */
    private void addRunningAction(Action action, ActionEvent evt) {
        synchronized(runningActions) {
            runningActions.put(evt, action);
        }
    }
    
    /** Removes action from <code>runningAction</code> map for key.
     * @param evt action event used as a key in map */
    private void removeRunningAction(ActionEvent evt) {
        synchronized(runningActions) {
            runningActions.remove(evt);
        }
    }

    /** Gets collection of currently running actions. */
    public Collection getRunningActions() {
        synchronized(runningActions) {
            return new ArrayList(runningActions.values());
        }
    }
     
    /** Change enabled property of an action
    *
    public void propertyChange (PropertyChangeEvent ev) {
        if (SystemAction.PROP_ENABLED.equals (ev.getPropertyName ())) {
            fireChange ();
        }
    }
    */

    /** Attaches to processing of a module.
     * The actual object passed is arbitrary, so long as
     * it is different for every installed modules (as this
     * controls the grouping of actions with separators).
     * Passing null means stop processing a given module.
     */
    public static synchronized void attachTo (Object m) {
        module = m;
    }

    /** Adds new action to the list.
    */
    public synchronized static void add (ManifestSection.ActionSection a) {
        List list = (List)map.get (module);
        if (list == null) {
            list = new ArrayList ();
            map.put (module, list);
        }
        list.add (a);
        //a.addPropertyChangeListener (INSTANCE);

        array = null;
        getDefaultInstance().fireChange (); // PENDING this is too often
    }

    /** Removes new action from the list.
    */
    public synchronized static void remove (ManifestSection.ActionSection a) {
        List list = (List)map.get (module);
        if (list == null) {
            return;
        }
        list.remove (a);
        //a.removePropertyChangeListener (INSTANCE);

        if (list.isEmpty ()) {
            map.remove (module);
        }

        array = null;
        getDefaultInstance().fireChange (); // PENDING this is too often
    }

    /** Creates the actions.
    */
    private synchronized static SystemAction[] createActions () {
        Iterator it = map.values ().iterator ();

        ArrayList arr = new ArrayList (map.size () * 5);

        while (it.hasNext ()) {
            List l = (List)it.next ();

            Iterator actions = l.iterator ();
            while (actions.hasNext()) {
                ManifestSection.ActionSection s = (ManifestSection.ActionSection)actions.next();
                
                try {
                    arr.add (s.getInstance ());
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify (ErrorManager.INFORMATIONAL, ex);
                }
            }
            
            
            if (it.hasNext ()) {
                // add separator between modules
                arr.add (null);
            }

        }

        return (SystemAction[])arr.toArray (new SystemAction[arr.size ()]);
    }

    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.openide.util.actions.MouseCursorUtils"); // NOI18N
    
    /**
     * Running show/hide count for glass panes in use.
     * Maps arbitrary keys to glass panes.
     * Several keys may map to the same glass pane - the wait cursor is shown
     * so long as there are any.
     */
    private static final Map glassPaneUses = new HashMap(); // Map<Object,Component>
    
    /**
     * Try to find the active window's glass pane.
     * @return a glass pane, or null
     */
    private static java.awt.Component activeGlassPane() {
        java.awt.Window w = java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        if (w instanceof javax.swing.RootPaneContainer) {
            return ((javax.swing.RootPaneContainer)w).getGlassPane();
        } else {
            return null;
        }
    }
    
    /**
     * Sets wait cursor visible on the window associated with an event, if any.
     * @param key something to pass to {@link #hideWaitCursor} to turn it off
     */
    public static void showWaitCursor(Object key) {
        assert java.awt.EventQueue.isDispatchThread();
        assert !glassPaneUses.containsKey(key);
        java.awt.Component c = activeGlassPane();
        if (c == null) {
            if (err.isLoggable(ErrorManager.WARNING)) {
                err.log(ErrorManager.WARNING, "showWaitCursor could not find a suitable glass pane; key=" + key);
            }
            return;
        }
        if (glassPaneUses.values().contains(c)) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("wait cursor already displayed on " + c);
            }
        } else {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("wait cursor will be displayed on " + c);
            }
            c.setCursor(org.openide.util.Utilities.createProgressCursor(c));
            c.setVisible(true);
        }
        glassPaneUses.put(key, c);
    }
    
    /**
     * Resets cursor to default.
     * @param key the same key passed to {@link #showWaitCursor}
     */
    public static void hideWaitCursor(Object key) {
        assert java.awt.EventQueue.isDispatchThread();
        java.awt.Component c = (java.awt.Component)glassPaneUses.get(key);
        if (c == null) {
            return;
        }
        glassPaneUses.remove(key);
        if (glassPaneUses.values().contains(c)) {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("wait cursor still displayed on " + c);
            }
        } else {
            if (err.isLoggable(ErrorManager.INFORMATIONAL)) {
                err.log("wait cursor will be hidden on " + c);
            }
            c.setVisible(false);
            c.setCursor(null);
        }
    }
    
    
}


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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.ref.*;
import java.util.*;
import javax.swing.Action;
import javax.swing.event.*;

import org.openide.actions.ActionManager;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;
import org.openide.ErrorManager;

import org.netbeans.core.modules.ManifestSection;
import org.netbeans.core.ui.MouseCursorUtils;


/** Holds list of all actions added by modules.
*
* @author jtulach, jglick
*/
public class ModuleActions extends ActionManager
/*implements PropertyChangeListener*/ {
    
    /** Property name of currently running actions. */
    public static String PROP_RUNNING_ACTIONS = "runningActions"; // NOI18N
    
    /** array of all actions added by modules */
    private static SystemAction[] array;
    /** of (ModuleItem, List (ManifestSection.ActionSection)) */
    private static Map map = new HashMap (7);
    /** current module */
    private static Object module;

    private RequestProcessor rp =
		    new RequestProcessor("Module-Actions", Integer.MAX_VALUE);

    /** Map of currently running actions, (maps action event to action) */
    private Map runningActions = new HashMap(4);
    
    static {
        module = PROP_RUNNING_ACTIONS;
    }
    
    public static ModuleActions getDefault () {
        return (ModuleActions)org.openide.util.Lookup.getDefault ().lookup (ActionManager.class);
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
        Runnable r = new Runnable () {
                public void run () {
                    // [PENDING] Ugly, but see e.g. TemplateWizard.DefaultIterator.instantiate:
                    // NodeAction called w/ ActionEvent whose source is Node or Node[] should
                    // be called directly, even if action is not globally enabled, since the
                    // particular node selection is passed in. Ideally would also call enable(Node[])
                    // before doing this, but that would require this method to be public.
                    if (a.isEnabled () ||
                        ((a instanceof NodeAction) && e != null &&
                         ((e.getSource () instanceof Node) ||
                          (e.getSource () instanceof Node[])))) {
                        //System.err.println ("invokeAction -> run: " + a);
                              
                        try {
                            MouseCursorUtils.showWaitCursor();
                            addRunningAction(a, e);
                            a.actionPerformed (e);
                        } finally {
                            MouseCursorUtils.hideWaitCursor();
                            removeRunningAction(e);
                            firePropertyChange(PROP_RUNNING_ACTIONS, null, null);
                        }
                    } else {
                        Toolkit.getDefaultToolkit ().beep ();
                    }
                }
            };
        rp.post (r);
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
        // Ignore actions which are marked, currently exit action.
        // When the pending dialog will be used for switching project and
        // also unmounting FS's mark also those actions with this flag.
        if(action.getValue("ModuleActions.ignore") != null) { // NOI18N
            return;
        }
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
    
    /** Tries to stop all processors executing currently running
     * action tasks. */
    public void killRunningActions() {
	rp.stop();
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
        if (module == null) {
            // well known value
            module = PROP_RUNNING_ACTIONS;
        }
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
        getDefault().fireChange (); // PENDING this is too often
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
        getDefault().fireChange (); // PENDING this is too often
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
    
}


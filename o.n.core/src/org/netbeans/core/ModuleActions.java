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

package org.netbeans.core;

import java.beans.*;
import java.util.*;
import javax.swing.event.*;

import org.openide.actions.ActionManager;
import org.openide.modules.ManifestSection;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;

/** Holds list of all actions added by modules.
*
* @author jtulach
*/
class ModuleActions extends ActionManager
/*implements PropertyChangeListener*/ {
    /** array of all actions added by modules */
    private static SystemAction[] array;
    /** of (ModuleItem, List (SystemAction)) */
    private static HashMap map = new HashMap (7);
    /** current module */
    private static Object module;

    /** */
    private RequestProcessor requestProcessor;
    
    /** instance */
    static final ModuleActions INSTANCE = new ModuleActions ();

    static {
        module = INSTANCE;
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
    public void invokeAction(final javax.swing.Action a, final java.awt.event.ActionEvent e) {
    //    a.actionPerformed(e);
        Task task = getRequestProcessor().post(new Runnable () {
            public void run () {
                a.actionPerformed(e);
            }
        });
    }

    /** Keeps track of the one instance of the RequestPrecessor.*/
    private RequestProcessor getRequestProcessor() {
        if (requestProcessor == null) {
            synchronized (this) {
                if (requestProcessor == null) {
                    requestProcessor = new RequestProcessor();
                }
            }
        }
        return requestProcessor;
    }
    
    /** Listens on change of modules and if changed,
    * fires change to all listeners.
    */
    private static void fireChange () {
        INSTANCE.firePropertyChange(PROP_CONTEXT_ACTIONS, null, null);
    }

    /** Change enabled property of an action
    *
    public void propertyChange (PropertyChangeEvent ev) {
        if (SystemAction.PROP_ENABLED.equals (ev.getPropertyName ())) {
            fireChange ();
        }
    }
    */

    /** Attaches to processing of a module
    */
    public static synchronized void attachTo (ModuleItem mi) {
        module = mi;
        if (module == null) {
            // well known value
            module = INSTANCE;
        }
    }

    /** Adds new action to the list.
    */
    public synchronized static void add (ManifestSection.ActionSection as) throws InstantiationException {
        List list = (List)map.get (module);
        if (list == null) {
            list = new LinkedList ();
            map.put (module, list);
        }
        list.add (as.getAction ());
        //as.getAction ().addPropertyChangeListener (INSTANCE);

        array = null;
        fireChange (); // PENDING this is too often
    }

    /** Removes new action from the list.
    */
    public synchronized static void remove (ManifestSection.ActionSection as) throws InstantiationException {
        List list = (List)map.get (module);
        if (list == null) {
            return;
        }
        list.remove (as.getAction ());
        //as.getAction ().removePropertyChangeListener (INSTANCE);

        if (list.isEmpty ()) {
            map.remove (module);
        }

        array = null;
        fireChange (); // PENDING this is too often
    }

    /** Creates the actions.
    */
    private synchronized static SystemAction[] createActions () {
        java.util.Iterator it = map.values ().iterator ();

        LinkedList arr = new LinkedList ();

        while (it.hasNext ()) {
            List l = (List)it.next ();

            arr.addAll (l);

            if (it.hasNext ()) {
                // add separator between modules
                arr.add (null);
            }

        }

        return (SystemAction[])arr.toArray (new SystemAction[arr.size ()]);
    }
}


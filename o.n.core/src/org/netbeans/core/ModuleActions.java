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

import java.util.*;

import org.openide.actions.ActionManager;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;

import org.netbeans.core.modules.ManifestSection;


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

    public static ModuleActions getDefaultInstance() {
        return (ModuleActions)ActionManager.getDefault();
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
    
    /** Listens on change of modules and if changed,
    * fires change to all listeners.
    */
    private void fireChange () {
        firePropertyChange(PROP_CONTEXT_ACTIONS, null, null);
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
    
}


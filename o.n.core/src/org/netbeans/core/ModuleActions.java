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

/** Holds list of all actions added by modules.
*
* @author jtulach, jglick
*/
public class ModuleActions extends ActionManager
/*implements PropertyChangeListener*/ {
    /** array of all actions added by modules */
    private static SystemAction[] array;
    /** of (ModuleItem, List (SystemAction)) */
    private static HashMap map = new HashMap (7);
    /** current module */
    private static Object module;

    private int rpCounter = 0;
    /** pool of _unused_ processor threads */
    private Set requestProcessors = new HashSet (4); // Set<Reference<RequestProcessor>>
    
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
    public void invokeAction(final Action a, final ActionEvent e) {
    //    a.actionPerformed(e);
        //System.err.println ("invokeAction: " + a);
        final RequestProcessor rp = findRequestProcessor ();
        //System.err.println ("invokeAction -> run: rp=" + rp);
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
                        a.actionPerformed (e);
                    } else {
                        //System.err.println ("invokeAction -> beep: " + a);
                        Mutex.EVENT.readAccess (new Runnable () {
                                public void run () {
                                    Toolkit.getDefaultToolkit ().beep ();
                                }
                            });
                    }
                    //System.err.println ("invokeAction -> run done: " + a);
                    releaseRequestProcessor (rp);
                }
            };
        rp.post (r);
    }

    private RequestProcessor findRequestProcessor () {
        //System.err.println ("findRequestProcessor");
        synchronized (requestProcessors) {
            Iterator it = requestProcessors.iterator ();
            if (it.hasNext ()) {
                Reference r = (Reference) it.next ();
                it.remove ();
                RequestProcessor rp = (RequestProcessor) r.get ();
                if (rp != null) {
                    //System.err.println ("reuse existing");
                    return rp;
                }
                //System.err.println ("was collected");
            }
        }
        //System.err.println ("make new");
        return new RequestProcessor ("org.netbeans.core.ModuleActions-" + ++rpCounter); // NOI18N
    }

    private void releaseRequestProcessor (RequestProcessor rp) {
        synchronized (requestProcessors) {
            //System.err.println ("releasing: " + rp);
            requestProcessors.add (new SoftReference (rp));
        }
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
            module = INSTANCE;
        }
    }

    /** Adds new action to the list.
    */
    public synchronized static void add (SystemAction a) {
        List list = (List)map.get (module);
        if (list == null) {
            list = new LinkedList ();
            map.put (module, list);
        }
        list.add (a);
        //a.addPropertyChangeListener (INSTANCE);

        array = null;
        fireChange (); // PENDING this is too often
    }

    /** Removes new action from the list.
    */
    public synchronized static void remove (SystemAction a) {
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
        fireChange (); // PENDING this is too often
    }

    /** Creates the actions.
    */
    private synchronized static SystemAction[] createActions () {
        Iterator it = map.values ().iterator ();

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


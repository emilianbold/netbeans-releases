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

package org.netbeans.modules.search;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openide.actions.FindAction;
import org.openide.text.CloneableEditor;
import org.openide.util.actions.SystemAction;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;


/**
 * Manages <em>FindAction</em> - enables and disables it by current set of
 * selected nodes.
 * <p>
 * FindAction's performer is changed according to current set of
 * selected nodes. If there is no node selected or if no registered
 * {@linkplain org.openidex.search.SearchType search type} is able to perform
 * search on the selected nodes, the action is temporarily disabled.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 * @see SearchPerformer#enabled
 * @see org.openide.actions.FindAction
 * @see org.openide.windows.TopComponent.Registry
 */
public class FindActionManager implements PropertyChangeListener {

    /** Search perfomer. */
    private SearchPerformer performer;
    
    /** Search hook. */
    private static FindActionManager theHook = null;

    /** the system FindAction */
    private FindAction findAction;
    
    /**
     * Creates a new <code>FindActionManager</code>.
     *
     * @param  performer  object delegated to perform the <em>find</em> action
     *                    and to decide whether the action is to be enabled
     *                    or disabled
     * @see  SearchPerformer#performAction(SystemAction)
     * @see  SearchPerformer#enabled
     */
    public FindActionManager(SearchPerformer performer) {
        this.performer = performer;
    }

    /** Hooks performer at <code>FindAction</code>.
     * Conditionally hooks performer to FindAction.
     * Condition: active top component is ExplorerManager.Provider AND
     * some criteria is enabled be current nodes
     */
    public void hook() {
        findAction = (FindAction) SystemAction.get(FindAction.class);
        if (findAction == null) { // hook target does not exist
            throw new RuntimeException("Should not happen: Cannot locate FindAction."); // NOI18N
        }
        setHookListener(this);
        // TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    /**
     * Unhooks performer. After invoking, this class is useless. */
    public void unhook() {
        setHookListener(null);
        performer = null;
        
        /*
         * We just need to run method 'someoneActivated' in the AWT event
         * dispatching thread. We use Mutex.EVENT for this task.
         * 
         * We use Mutex.Action rather than Runnable. The reason is that
         * Runnable could be run asynchronously which is undesirable during
         * uninstallation (we do not want any instance/class from this module to
         * be in use by the time ModuleInstall.uninstalled() returns).
         */
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                someoneActivated();
                return null;
            }
        });
        findAction = null;
        // hook();
        // TopComponent.getRegistry().removePropertyChangeListener(this);
    }

    /** Sets hook listener. */
    private static void setHookListener(FindActionManager hook) {
        TopComponent.Registry regs = TopComponent.getRegistry();

        if (theHook != null) {
            regs.removePropertyChangeListener(theHook);
        }

        if (hook != null) {
            regs.addPropertyChangeListener(hook);
        }

        theHook = hook;
    }

    /** Determine if activated topcomponent represent Editor
     * if not call overwriting routine.
     * It prevents battle about FindAction performer.
     */
    private void someoneActivated() {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if(activated !=null && !(activated instanceof CloneableEditor)) {
            overwriteFindPerformer();
        }
    }

    /** Sets performer as find performer if no performer is set. */
    private void overwriteFindPerformer() {

        if(performer == null) { // handle deinstalation
            if (findAction != null) 
                findAction.setActionPerformer(null);
            return;
        }

        Object currPerformer = findAction.getActionPerformer();

        if(performer.enabled(TopComponent.getRegistry().getCurrentNodes())) {
            findAction.setActionPerformer(performer);
        } else if (currPerformer != null && currPerformer.getClass().equals(SearchPerformer.class)) {
            findAction.setActionPerformer(null);
        }
    }

    /** Implements <code>PropertyChangeListener</code>. Be interested in current_nodes property change. */
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        
        if(TopComponent.Registry.PROP_CURRENT_NODES.equals(propName)  ||
           TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
                someoneActivated();
        }
    }

}

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Action;
import javax.swing.ActionMap;

import org.openide.actions.FindAction;
import org.openide.text.CloneableEditor;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakSet;
import org.openide.util.actions.SystemAction;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;


/**
 * Manages <em>FindAction</em> - enables and disables it by current set of
 * selected nodes.
 *
 * @author  Petr Kuzel
 * @author  Marian Petras
 * @see org.openide.actions.FindAction
 * @see org.openide.windows.TopComponent.Registry
 */
final class FindActionManager implements PropertyChangeListener, Runnable {

    /**
     */
    private static FindActionManager instance;
    /** Search perfomer. */
    private final SearchPerformer performer;
    /** holds set of windows for which their ActionMap was modified */
    private final Set activatedOnWindows = new WeakSet(8);
    
    /** */
    private Object findActionMapKey;
    
    /**
     */
    private FindActionManager() {
        performer = (SearchPerformer)
                    SharedClassObject.findObject(SearchPerformer.class, true);
    }
    
    /**
     */
    static FindActionManager getInstance() {
        if (instance == null) {
            instance = new FindActionManager();
        }
        return instance;
    }

    /**
     */
    void init() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
        
        Mutex.EVENT.writeAccess(this);
    }

    /**
     */
    void cleanup() {
        //System.out.println("cleanup");
        TopComponent.getRegistry().removePropertyChangeListener(this);
        
        /*
         * We just need to run method 'cleanupWindowRegistry' in the AWT event
         * dispatching thread. We use Mutex.EVENT for this task.
         * 
         * We use Mutex.Action rather than Runnable. The reason is that
         * Runnable could be run asynchronously which is undesirable during
         * uninstallation (we do not want any instance/class from this module to
         * be in use by the time ModuleInstall.uninstalled() returns).
         */
        Mutex.EVENT.readAccess(new Mutex.Action() {
            public Object run() {
                cleanupWindowRegistry();
                return null;
            }
        });
    }
    
    /**
     */
    public void run() {
        someoneActivated();
    }
    
    /**
     */
    private void cleanupWindowRegistry() {
        for (Iterator i = activatedOnWindows.iterator(); i.hasNext(); ) {
            TopComponent tc = (TopComponent) i.next();
            ActionMap tcActionMap = tc.getActionMap();
            Action mappedAction = tcActionMap.get(findActionMapKey);
            if (mappedAction.getClass() == SearchPerformer.class) {
                //System.out.println("removed mapping for window " + tc.getDisplayName());
                tcActionMap.put(findActionMapKey, null);
            }
        }
        activatedOnWindows.clear();
    }

    /**
     */
    private void someoneActivated() {
        //System.out.println("someoneActivated");
        TopComponent window = TopComponent.getRegistry().getActivated();

        if ((window != null)
                && checkIsApplicableOnWindow(window)
                && activatedOnWindows.add(window)) {
            window.getActionMap().put(getFindActionMapKey(), performer);
            //System.out.println("added mapping for window " + window.getDisplayName());
        }
    }
    
    /**
     * Checks whether the Find action is applicable on the given window.
     */
    private boolean checkIsApplicableOnWindow(TopComponent window) {
        return !(window instanceof CloneableEditor);
    }
    
    /** Implements <code>PropertyChangeListener</code>. Be interested in current_nodes property change. */
    public void propertyChange(PropertyChangeEvent evt) {
        if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())){
            someoneActivated();
        }
    }
    
    /**
     */
    private Object getFindActionMapKey() {
        if (findActionMapKey == null) {
            SharedClassObject findAction = 
                    SharedClassObject.findObject(FindAction.class);
            assert findAction != null;
            
            findActionMapKey = ((FindAction) findAction).getActionMapKey();
        }
        return findActionMapKey;
    }

}

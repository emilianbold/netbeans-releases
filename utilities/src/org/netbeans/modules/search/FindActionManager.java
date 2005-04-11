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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.ErrorManager;

import org.openide.actions.FindAction;
import org.openide.text.CloneableEditor;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakSet;
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
    
    /** */
    private static final String MAPPED_FIND_ACTION
            = FindActionManager.class.getName() + " - FindActionImpl";  //NOI18N

    /**
     */
    private static FindActionManager instance;
    /** Search perfomer. */
    private final SearchPerformer performer;
    /** holds set of windows for which their ActionMap was modified */
    private final WeakSet activatedOnWindows = new WeakSet(8);
    
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
        //System.out.println("Utilities: Cleaning window registry");
        final Object findActionKey = getFindActionMapKey();
        
        for (Iterator i = activatedOnWindows.iterator(); i.hasNext(); ) {
            TopComponent tc = (TopComponent) i.next();
            //System.out.println("     ** " + tc.getName());
            
            Action origFindAction = null, currFindAction = null;
            
            Object origFindActionRef = tc.getClientProperty(MAPPED_FIND_ACTION);
            if (origFindActionRef instanceof Reference) {
                Object origFindActionObj = ((Reference)origFindActionRef).get();
                if (origFindActionObj instanceof Action) {
                    origFindAction = (Action) origFindActionObj;
                }
            }
            
            if (origFindAction != null) {
                currFindAction = tc.getActionMap().get(findActionKey);
            }
            
            if ((currFindAction != null) && (currFindAction == origFindAction)){
                tc.getActionMap().put(findActionKey, null);
                //System.out.println("         - successfully cleared");
            } else {
                //System.out.println("         - DID NOT MATCH");
                ErrorManager.getDefault().log(
                        ErrorManager.WARNING,
                        "ActionMap mapping of FindAction changed" +     //NOI18N
                                " for window " + tc.getName());         //NOI18N
            }
            
            if (origFindActionRef != null) {
                tc.putClientProperty(MAPPED_FIND_ACTION, null);
            }
        }
        activatedOnWindows.clear();
    }

    /**
     */
    private void someoneActivated() {
        TopComponent window = TopComponent.getRegistry().getActivated();

        if ((window == null) || (window instanceof CloneableEditor)) {
            return;
        }
            
        Object key = getFindActionMapKey();
        ActionMap actionMap = window.getActionMap();

        if ((actionMap.get(key) == null) && activatedOnWindows.add(window)) {
            //System.out.println("Utilities: Registered window " + window.getName());
            
            Action a = performer.createContextAwareInstance(window.getLookup());

            actionMap.put(key, a);
            window.putClientProperty(MAPPED_FIND_ACTION, new WeakReference(a));
        }
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

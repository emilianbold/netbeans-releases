/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.ErrorManager;

import org.openide.actions.FindAction;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakSet;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;
import static java.util.logging.Level.FINER;


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
    private static final Logger LOG
            = Logger.getLogger(FindActionManager.class.getName());
    
    /** */
    private static final String MAPPED_FIND_ACTION
            = FindActionManager.class.getName() + " - FindActionImpl";  //NOI18N

    /**
     */
    private static FindActionManager instance;
    /** Search perfomer. */
    private final FindInFilesAction findAction;
    /** holds set of windows for which their ActionMap was modified */
    private final Set<TopComponent> activatedOnWindows
            = new WeakSet<TopComponent>(8);
    
    /** */
    private Object findActionMapKey;
    
    /**
     */
    private FindActionManager() {
        findAction = SharedClassObject.findObject(FindInFilesAction.class, true);
    }
    
    /**
     */
    static FindActionManager getInstance() {
        LOG.finer("getInstance()");
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
        
        for (TopComponent tc : activatedOnWindows) {
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
        if (LOG.isLoggable(FINER)) {
            String windowId;
            if (window == null) {
                windowId = "<null>";
            } else {
                String windowName = window.getDisplayName();
                if (windowName == null) {
                    windowName = window.getHtmlDisplayName();
                }
                if (windowName == null) {
                    windowName = window.getName();
                }
                if (windowName != null) {
                    windowName = '"' + windowName + '"';
                } else {
                    windowName = "<noname>";
                }
                windowId = windowName + '(' + window.getClass().getName() + ')';
            }
            LOG.finer("someoneActivated (" + windowId + ')');
        }

        if ((window == null) || (window instanceof CloneableEditorSupport.Pane)) {
            return;
        }
            
        Object key = getFindActionMapKey();
        ActionMap actionMap = window.getActionMap();

        if ((actionMap.get(key) == null) && activatedOnWindows.add(window)) {
            //System.out.println("Utilities: Registered window " + window.getName());
            
            Action a = findAction.createContextAwareInstance(window.getLookup(),
                                                             true);

            actionMap.put(key, a);
            window.putClientProperty(MAPPED_FIND_ACTION,
                                     new WeakReference<Action>(a));
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
            FindAction systemFindAction = 
                    SharedClassObject.findObject(FindAction.class, true);
            assert systemFindAction != null;

            findActionMapKey = systemFindAction.getActionMapKey();
        }
        return findActionMapKey;
    }

}

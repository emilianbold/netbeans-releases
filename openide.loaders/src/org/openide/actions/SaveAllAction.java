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

package org.openide.actions;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.LifecycleManager;

import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/** Save all open objects.
* @see DataObject#getRegistry
* @see LifecycleManager#saveAll
*
* @author   Jan Jancura, Ian Formanek
*/
public final class SaveAllAction extends CallableSystemAction {
    /** to make sure only one instance of this class can run at a time */
    private static final Object RUNNING = new Object ();

    /** Reference to the change listener
    * (we treat it weakly, so we have to to prevent it from
    * being finalized before finalization of this action) */
    private ChangeListener chl;

    /* Creates new HashMap and inserts some properties to it.
    * @return the hash map
    */
    protected void initialize () {
        super.initialize ();
        // false by default
        putProperty (PROP_ENABLED, Boolean.FALSE);
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (ChangeListener)(org.openide.util.WeakListeners.change(chl, DataObject.getRegistry ())));
    }

    public String getName() {
        return NbBundle.getMessage(org.openide.loaders.DataObject.class, "SaveAll");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveAllAction.class);
    }

    protected String iconResource () {
        return "org/openide/actions/saveAll.gif"; // NOI18N
    }

    public void performAction() {
        synchronized (RUNNING) {
            while (getProperty (RUNNING) != null) {
                try {
                    RUNNING.wait ();
                } catch (InterruptedException ex) {
                    org.openide.ErrorManager.getDefault ().notify (ex);
                }
            }
            putProperty (RUNNING, RUNNING);
        }
        try {
            LifecycleManager.getDefault().saveAll();
        } finally {
            synchronized (RUNNING) {
                putProperty (RUNNING, null);
                RUNNING.notifyAll ();
            }
            
        }
    }
    
    protected boolean asynchronous() {
        return true;
    }

    /* Listens to the chnages in list of modified data objects
    * and enables / disables this action appropriately */
    final class ModifiedListL implements ChangeListener {
        public void stateChanged(final ChangeEvent evt) {
            Mutex.EVENT.writeAccess(new Runnable() {
                public void run() {
                    setEnabled(((java.util.Set)evt.getSource()).size() > 0);
                }
            });
        }
    } // end of ModifiedListL inner class
}

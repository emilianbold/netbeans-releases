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


package org.netbeans.core.windows.actions;


import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.Mutex;
import org.openide.windows.TopComponent;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.netbeans.core.windows.Constants;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.KeyEvent;


/**
 * @author   Tim Boudreau
 */
public class CloseAllButThisAction extends AbstractAction
implements PropertyChangeListener, Runnable {

    public CloseAllButThisAction() {
        putValue(NAME, NbBundle.getMessage(CloseAllButThisAction.class,
            "CTL_CloseAllButThisAction")); //NOI18N

        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    private TopComponent tc;
    public CloseAllButThisAction(TopComponent topComp) {
        tc = topComp;
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class,
        "LBL_CloseAllButThisAction")); //NOI18N
        
    }

    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent topC = tc;
        if (topC == null) {
            // for the updating sate action instance..
            topC = TopComponent.getRegistry().getActivated();
        }
        if(topC != null) {
            ActionUtils.closeAllExcept(topC);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        Mutex.EVENT.readAccess(this);
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllButThis", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllButThis"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    public void run() {
        TopComponent tc = TopComponent.getRegistry().getActivated();
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        setEnabled(tc instanceof TopComponent.Cloneable
            && mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR);
    }

}


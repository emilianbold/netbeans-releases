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
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * @author   Peter Zavadsky
 */
public class CloseWindowAction extends AbstractAction
implements PropertyChangeListener {

    public CloseWindowAction() {
        putValue(NAME, NbBundle.getMessage(CloseWindowAction.class, "CTL_CloseWindowAction"));
        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        updateEnabled();
    }
    
    private TopComponent tc;
    // dno't update enable state, is tied to one component only
    public CloseWindowAction(TopComponent topcomp) {
        tc = topcomp;
        //Include the name in the label for the popup menu - it may be clicked over
        //a component that is not selected
        putValue(Action.NAME, NbBundle.getMessage(ActionUtils.class,
        "LBL_CloseWindowAction")); //NOI18N
        setEnabled(true);
    }
    
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        TopComponent topC = tc;
        if (topC == null) {
            // the updating instance will get the TC to close from winsys
            topC = TopComponent.getRegistry().getActivated();
        }
        if(topC != null) {
            ActionUtils.closeWindow(topC);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateEnabled();
        }
    }
    
    private void updateEnabled() {
        setEnabled(TopComponent.getRegistry().getActivated() != null);
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseWindow", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseWindowAction
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseWindow");
        } else {
            return super.getValue(key);
        }
    }
    
}


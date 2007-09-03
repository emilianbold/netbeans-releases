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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        TopComponent topC = obtainTC();
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
    
    public void run() {
        TopComponent tc = obtainTC();
        ModeImpl mode = (ModeImpl)WindowManagerImpl.getInstance().findMode(tc);
        
        setEnabled(tc instanceof TopComponent.Cloneable
            && mode != null && mode.getKind() == Constants.MODE_KIND_EDITOR
            && (mode.getOpenedTopComponents().size() > 1));
    }
    
    private TopComponent obtainTC () {
        return tc == null ? TopComponent.getRegistry().getActivated() : tc;
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

}


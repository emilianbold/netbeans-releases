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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.core.windows.ModeImpl;
import org.netbeans.core.windows.WindowManagerImpl;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action perform undock or dock, either of given or active top component.
 * Undock means that TopCompoment is moved to new, separate floating window,
 * Dock means move into main window area.
 *
 */
public final class UndockWindowAction extends AbstractAction {

    private final TopComponent tc;

    /**
     * Creates instance of action to Undock/Dock of currently active top
     * component in the system. For use in main menu.
     */
    public UndockWindowAction () {
        this.tc = null;
    }

    /**
     * Undock/Dock of given TopComponent.
     * For use in the context menus.
     */
    public UndockWindowAction (TopComponent tc) {
        this.tc = tc;
    }
    
    public void actionPerformed (ActionEvent e) {
        // contextTC shound never be null thanks to isEnabled impl
        WindowManagerImpl wmi = WindowManagerImpl.getInstance();
        TopComponent contextTC = getTC2WorkWith();
        boolean isDocked = wmi.isDocked(contextTC);
        int kind = ((ModeImpl)wmi.findMode(contextTC)).getKind();

        if (isDocked) {
            wmi.userUndockedTopComponent(contextTC, kind);
        } else {
            wmi.userDockedTopComponent(contextTC, kind);
        }
    }
    
    /** Overriden to share accelerator between instances of this action.
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("UndockWindowAction", newValue); //NOI18N
        } else {
            super.putValue(key, newValue);
        }
    }

    /** Overriden to share accelerator between instances of this action.
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("UndockWindowAction"); //NOI18N
        } else {
            return super.getValue(key);
        }
    }

    public boolean isEnabled() {
        updateName();
        return getTC2WorkWith() != null;
    }

    private void updateName() {
        TopComponent contextTC = getTC2WorkWith();
        boolean isDocked = contextTC != null ? WindowManagerImpl.getInstance().isDocked(contextTC) : true;
        putValue(Action.NAME,
                NbBundle.getMessage(UndockWindowAction.class,
                isDocked ? "CTL_UndockWindowAction" : "CTL_UndockWindowAction_Dock"));
    }

    private TopComponent getTC2WorkWith () {
        if (tc != null) {
            return tc;
        }
        return WindowManager.getDefault().getRegistry().getActivated();
    }

}

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


import javax.swing.AbstractAction;
import javax.swing.Action;

import org.openide.util.NbBundle;


/**
 * @author   Peter Zavadsky
 */
public class CloseAllDocumentsAction extends AbstractAction {

    public CloseAllDocumentsAction() {
        putValue(NAME, NbBundle.getMessage(CloseAllDocumentsAction.class, "CTL_CloseAllDocumentsAction"));
    }
    
    /** Perform the action. Sets/unsets maximzed mode. */
    public void actionPerformed(java.awt.event.ActionEvent ev) {
        ActionUtils.closeAllDocuments();
    }

    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */ 
    public void putValue(String key, Object newValue) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            ActionUtils.putSharedAccelerator("CloseAllDocuments", newValue);
        } else {
            super.putValue(key, newValue);
        }
    }
    
    /** Overriden to share accelerator with 
     * org.netbeans.core.windows.actions.ActionUtils.CloseAllDocumentsAction
     */ 
    public Object getValue(String key) {
        if (Action.ACCELERATOR_KEY.equals(key)) {
            return ActionUtils.getSharedAccelerator("CloseAllDocuments");
        } else {
            return super.getValue(key);
        }
    }
    
}


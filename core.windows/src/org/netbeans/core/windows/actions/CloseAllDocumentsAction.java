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

import javax.swing.*;


/**
 * @author   Peter Zavadsky
 */
public class CloseAllDocumentsAction extends AbstractAction {

    /**
     * default constructor with label containing mnemonics.
     */
    public CloseAllDocumentsAction() {
        this(true);
    }

    /**
     * can decide whether to have label with mnemonics or without it.
     */ 
    public CloseAllDocumentsAction(boolean withMnemonic) {
        String key; 
        if (withMnemonic) {
            key = "CTL_CloseAllDocumentsAction"; //NOI18N
        } else {
            key = "LBL_CloseAllDocumentsAction"; //NOI18N
        }
        putValue(NAME, NbBundle.getMessage(CloseAllDocumentsAction.class, key));
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


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

import javax.swing.*;
import org.netbeans.core.windows.WindowManagerImpl;


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

    @Override
    public boolean isEnabled() {
        return WindowManagerImpl.getInstance().getEditorTopComponents().length > 0;
    }
    
}


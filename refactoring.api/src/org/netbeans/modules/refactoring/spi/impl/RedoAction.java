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

package org.netbeans.modules.refactoring.spi.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

public class RedoAction extends CallableSystemAction implements PropertyChangeListener {

    private UndoManager undoManager;

    public RedoAction() {
        putValue(Action.NAME, getString("LBL_Redo")); //NOI18N
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        undoManager = UndoManager.getDefault();
        undoManager.addPropertyChangeListener(this);
        updateState();
    }
    
    public void propertyChange (PropertyChangeEvent event) {
        updateState();
    }
    
    private void updateState() {
        String desc = undoManager.getRedoDescription();
        String name = getString("LBL_Redo");
        if (desc != null) {
            name += " [" + desc + "]"; //NOI18N
        }
        
        final String n = name;
        Runnable r = new Runnable() {
            public void run() {
                setEnabled(undoManager.isRedoAvailable());
                putValue(Action.NAME, n);
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
    
    private static final String getString(String key) {
        return NbBundle.getMessage(RedoAction.class, key);
    }
    
    public void performAction() {
        undoManager.redo();
        undoManager.saveAll();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public String getName() {
        return (String) getValue(Action.NAME);
    }
    
    protected boolean asynchronous() {
        return true;
    }
}

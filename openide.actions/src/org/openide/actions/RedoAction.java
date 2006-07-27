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
package org.openide.actions;

import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import org.openide.util.Exceptions;


/** Redo an edit.
*
* @see UndoAction
* @author   Ian Formanek, Jaroslav Tulach
*/
public class RedoAction extends CallableSystemAction {
    private static String SWING_DEFAULT_LABEL = UIManager.getString("AbstractUndoableEdit.redoText"); //NOI18N

    public boolean isEnabled() {
        UndoAction.initializeUndoRedo();

        return super.isEnabled();
    }

    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        String redo = UndoAction.getUndoRedo().getRedoPresentationName();

        if ((redo != null) && (SWING_DEFAULT_LABEL != null) && redo.startsWith(SWING_DEFAULT_LABEL)) {
            redo = redo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }

        return NbBundle.getMessage(RedoAction.class, "Redo", redo);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RedoAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/redo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            UndoRedo undoRedo = UndoAction.getUndoRedo();

            if (undoRedo.canRedo()) {
                undoRedo.redo();
            }
        } catch (CannotRedoException ex) {
            Exceptions.printStackTrace(ex);
        }

        UndoAction.updateStatus();
    }

    protected boolean asynchronous() {
        return false;
    }
}

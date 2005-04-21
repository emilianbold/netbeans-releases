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

import org.openide.ErrorManager;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;


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
            redo = redo.substring(0, SWING_DEFAULT_LABEL.length()).trim();
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
            ErrorManager.getDefault().notify(ex);
        }

        UndoAction.updateStatus();
    }

    protected boolean asynchronous() {
        return false;
    }
}

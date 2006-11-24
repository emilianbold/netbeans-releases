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
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

import java.beans.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.*;
import javax.swing.undo.*;
import org.openide.util.Exceptions;


/** Undo an edit.
*
* @see UndoRedo
* @author   Ian Formanek, Jaroslav Tulach
*/
public class UndoAction extends CallableSystemAction {
    /** initialized listener */
    private static Listener listener;

    /** last edit */
    private static UndoRedo last = UndoRedo.NONE;
    private static String SWING_DEFAULT_LABEL = UIManager.getString("AbstractUndoableEdit.undoText"); //NOI18N
    private static UndoAction undoAction = null;
    private static RedoAction redoAction = null;

    public boolean isEnabled() {
        initializeUndoRedo();

        return super.isEnabled();
    }

    /** Initializes the object.
    */
    static synchronized void initializeUndoRedo() {
        if (listener != null) {
            return;
        }

        listener = new Listener();

        Registry r = WindowManager.getDefault().getRegistry();

        r.addPropertyChangeListener(org.openide.util.WeakListeners.propertyChange(listener, r));
        last = getUndoRedo();
        last.addChangeListener(listener);

        updateStatus();
    }

    /** Update status of action.
    */
    static synchronized void updateStatus() {
        if (undoAction == null) {
            undoAction = (UndoAction) findObject(UndoAction.class, false);
        }

        if (redoAction == null) {
            redoAction = (RedoAction) findObject(RedoAction.class, false);
        }

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    UndoRedo ur = getUndoRedo();

                    if (undoAction != null) {
                        undoAction.setEnabled(ur.canUndo());
                    }

                    if (redoAction != null) {
                        redoAction.setEnabled(ur.canRedo());
                    }
                }
            }
        );
    }

    /** Finds current undo/redo.
    */
    static UndoRedo getUndoRedo() {
        TopComponent el = WindowManager.getDefault().getRegistry().getActivated();

        return (el == null) ? UndoRedo.NONE : el.getUndoRedo();
    }

    public String getName() {
        //#40823 related. AbstractUndoableEdit prepends "Undo/Redo" strings before the custom text,
        // resulting in repetitive text in UndoAction/RedoAction. attempt to remove the AbstractUndoableEdit text
        // keeping our text because it has mnemonics.
        String undo = getUndoRedo().getUndoPresentationName();
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "getUndoRedo().getUndoPresentationName() returns " + undo);
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "SWING_DEFAULT_LABEL is " + SWING_DEFAULT_LABEL);

        if ((undo != null) && (SWING_DEFAULT_LABEL != null) && undo.startsWith(SWING_DEFAULT_LABEL)) {
            undo = undo.substring(SWING_DEFAULT_LABEL.length()).trim();
        }
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Name adapted by SWING_DEFAULT_LABEL is " + undo);
        String presentationName = NbBundle.getMessage(UndoAction.class, "Undo", undo);
        
        Logger.getLogger (UndoAction.class.getName ()).log (Level.FINE, "Result name is " + presentationName);

        return presentationName;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(UndoAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/undo.gif"; // NOI18N
    }

    public void performAction() {
        try {
            UndoRedo undoRedo = getUndoRedo();

            if (undoRedo.canUndo()) {
                undoRedo.undo();
            }
        } catch (CannotUndoException ex) {
            Exceptions.printStackTrace(ex);
        }

        updateStatus();
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Listener on changes of selected workspace element and
    * its changes.
    */
    private static final class Listener implements PropertyChangeListener, ChangeListener {
        Listener() {
        }

        public void propertyChange(PropertyChangeEvent ev) {
            updateStatus();
            last.removeChangeListener(this);
            last = getUndoRedo();
            last.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent ev) {
            updateStatus();
        }
    }
}

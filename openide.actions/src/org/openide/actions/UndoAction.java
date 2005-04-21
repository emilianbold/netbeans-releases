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
import org.openide.util.WeakListeners;
import org.openide.util.actions.CallableSystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponent.Registry;
import org.openide.windows.WindowManager;

import java.beans.*;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.*;
import javax.swing.undo.*;


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

        if ((undo != null) && (SWING_DEFAULT_LABEL != null) && undo.startsWith(SWING_DEFAULT_LABEL)) {
            undo = undo.substring(0, SWING_DEFAULT_LABEL.length()).trim();
        }

        return NbBundle.getMessage(UndoAction.class, "Undo", undo);
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
            ErrorManager.getDefault().notify(ex);
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

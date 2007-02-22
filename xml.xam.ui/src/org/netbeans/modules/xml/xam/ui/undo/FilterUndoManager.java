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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xam.ui.undo;

import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;

/**
 * A proxy for another UndoRedo.Manager instance. Unless otherwise noted,
 * all methods delegate to the original manager.
 *
 * @author  Nathan Fiedler
 */
public class FilterUndoManager extends UndoRedo.Manager {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** The original undo/redo manager. */
    private UndoRedo.Manager original;

    public FilterUndoManager(UndoRedo.Manager original) {
        assert original != null;
        this.original = original;
    }

    /**
     * Returns the original UndoRedo.Manager instance.
     *
     * @return  original manager.
     */
    protected UndoRedo.Manager getOriginal() {
        return original;
    }

    public void addChangeListener(ChangeListener changeListener) {
        original.addChangeListener(changeListener);
    }

    public boolean addEdit(UndoableEdit anEdit) {
        return original.addEdit(anEdit);
    }

    public boolean canRedo() {
        return original.canRedo();
    }

    public boolean canUndo() {
        return original.canUndo();
    }

    public boolean canUndoOrRedo() {
        return original.canUndoOrRedo();
    }

    public void die() {
        original.die();
    }

    public void discardAllEdits() {
        original.discardAllEdits();
    }

    public void end() {
        original.end();
    }

    public int getLimit() {
        return original.getLimit();
    }

    public String getPresentationName() {
        return original.getPresentationName();
    }

    public String getRedoPresentationName() {
        return original.getRedoPresentationName();
    }

    public String getUndoOrRedoPresentationName() {
        return original.getUndoOrRedoPresentationName();
    }

    public String getUndoPresentationName() {
        return original.getUndoPresentationName();
    }

    public boolean isInProgress() {
        return original.isInProgress();
    }

    public boolean isSignificant() {
        return original.isSignificant();
    }

    public void redo() throws CannotRedoException {
        original.redo();
    }

    public void removeChangeListener(ChangeListener changeListener) {
        original.removeChangeListener(changeListener);
    }

    public boolean replaceEdit(UndoableEdit anEdit) {
        return original.replaceEdit(anEdit);
    }

    public void setLimit(int l) {
        original.setLimit(l);
    }

    public void undo() throws CannotUndoException {
        original.undo();
    }

    public void undoOrRedo() throws CannotRedoException, CannotUndoException {
        original.undoOrRedo();
    }

    public void undoableEditHappened(UndoableEditEvent event) {
        original.undoableEditHappened(event);
    }
}

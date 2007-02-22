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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Generic undoable edit that delegates to the given undoable edit.
 */
public class FilterUndoableEdit implements UndoableEdit {
    protected UndoableEdit delegate;

    // Copied from CloneableEditorSupport in openide/text

    FilterUndoableEdit() {
    }

    public void undo() throws CannotUndoException {
        if (delegate != null) {
            delegate.undo();
        }
    }

    public boolean canUndo() {
        if (delegate != null) {
            return delegate.canUndo();
        } else {
            return false;
        }
    }

    public void redo() throws CannotRedoException {
        if (delegate != null) {
            delegate.redo();
        }
    }

    public boolean canRedo() {
        if (delegate != null) {
            return delegate.canRedo();
        } else {
            return false;
        }
    }

    public void die() {
        if (delegate != null) {
            delegate.die();
        }
    }

    public boolean addEdit(UndoableEdit anEdit) {
        if (delegate != null) {
            return delegate.addEdit(anEdit);
        } else {
            return false;
        }
    }

    public boolean replaceEdit(UndoableEdit anEdit) {
        if (delegate != null) {
            return delegate.replaceEdit(anEdit);
        } else {
            return false;
        }
    }

    public boolean isSignificant() {
        if (delegate != null) {
            return delegate.isSignificant();
        } else {
            return true;
        }
    }

    public String getPresentationName() {
        if (delegate != null) {
            return delegate.getPresentationName();
        } else {
            return ""; // NOI18N
        }
    }

    public String getUndoPresentationName() {
        if (delegate != null) {
            return delegate.getUndoPresentationName();
        } else {
            return ""; // NOI18N
        }
    }

    public String getRedoPresentationName() {
        if (delegate != null) {
            return delegate.getRedoPresentationName();
        } else {
            return ""; // NOI18N
        }
    }
}

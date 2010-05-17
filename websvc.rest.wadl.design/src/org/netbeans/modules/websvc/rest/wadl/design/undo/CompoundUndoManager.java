/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.rest.wadl.design.undo;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import org.openide.awt.UndoRedo;

/**
 * A proxy for another UndoRedo.Manager instance, which permits a set
 * of undoable edits to be treated as a "compound" edit, when this
 * manager is not in the "compound" mode. This is useful for the
 * source editor to treat document edits individually, but the model
 * editor can treat all of the document edits as a single change.
 *
 * @author  Nathan Fiedler
 */
public class CompoundUndoManager extends FilterUndoManager {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** If true, undo manager is operating in "compound" mode. */
    private boolean isCompound;
    /** True when the undoable edit has been reached during the undo/redo
     * of a compound set. */
    private boolean undoRedoComplete;
    /** If true, the begin or end compound edit was encountered and the
     * undo()/redo() methods need to operate on the entire set. */
    private boolean undoRedoCompound;
    /** If true, addEdit() will wrap the next UndoableEdit in a begin
     * compound undoable edit. */
    private boolean consumeNextEdit;
    /** If true, indicates that an undoable edit has been added since
     * beginCompound() was called, and as such, an end edit is required. */
    private boolean appendEndEdit;
    /** If true, an end edit was undone without the corresponding start
     * edit, indicating that if a new edit is added, a new end edit must
     * be added to close the compound set. */
    private boolean openCompound;
    /** True if undo/redo operation is in progress while outside of
     * compound mode. In this case, the begin/end compound edits are
     * significant as they must be performed. */
    private boolean compoundInProgress;

    /**
     * Creates a new instance of CompoundUndoManager.
     *
     * @param  original  UndoRedo.Manager to be proxied.
     */
    public CompoundUndoManager(UndoRedo.Manager original) {
        super(original);
    }

    /**
     * Starts the compound mode of this manager, in which all subsequent
     * edits will be treated as a single edit when undo/redo is performed
     * outside of compound mode.
     */
    public synchronized void beginCompound() {
        if (!isCompound) {
            isCompound = true;
            consumeNextEdit = true;
            appendEndEdit = false;
        }
    }

    /**
     * Stops the compound mode of this manager.
     *
     * @see #beginCompound()
     */
    public synchronized void endCompound() {
        if (isCompound) {
            isCompound = false;
            if (appendEndEdit) {
                // We are leaving compound mode with document edits on
                // the queue, so treat this as the 'open' state.
                openCompound = true;
            }
        }
    }

    /**
     * Indicates if this undo manager is currently in compound mode.
     *
     * @return  true if in compound mode, false otherwise.
     */
    public synchronized boolean isCompound() {
        return isCompound;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        // Respond to the additon of the edit, and then add it (or its
        // replacement) to the queue via the superclass.
        anEdit = processEdit(anEdit);
        return super.addEdit(anEdit);
    }

    @Override
    public void discardAllEdits() {
        super.discardAllEdits();
        if (isCompound) {
            // In compound mode, be ready to consume the next edit.
            consumeNextEdit = true;
        }
        // Reset the append state as there are no edits on the queue.
        appendEndEdit = false;
        // No more edits, no more open compound issue.
        openCompound = false;
    }

    /**
     * Process the edit that is about to be added to the queue.
     *
     * @param  anEdit  the undoable edit to process.
     * @return  the edit, possibly replaced by another.
     */
    private UndoableEdit processEdit(UndoableEdit anEdit) {
        if (isCompound) {
            if (openCompound) {
                // Transition back to the state of having a begin edit
                // followed by document edits, and ready to add an end
                // edit to the queue, when needed.
                openCompound = false;
                appendEndEdit = true;
            } else if (!appendEndEdit) {
                appendEndEdit = true;
                if (consumeNextEdit) {
                    consumeNextEdit = false;
                    // Replace the original with our filter edit.
                    anEdit = new BeginCompoundEdit(anEdit);
                }
            }
        } else if (appendEndEdit) {
            // Reset to avoid looping if addEdit() is called again.
            appendEndEdit = false;
            // An edit, probably a model edit, is about to be added.
            // We left compound mode and there were document edits on
            // the queue, so we must close that compound set.
            openCompound = false;
            super.addEdit(new EndCompoundEdit());
        }
        return anEdit;
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent event) {
        // Process the edit without adding it to the queue.
        UndoableEdit anEdit = processEdit(event.getEdit());
        // Need to provide our replacement edit, if any, otherwise we
        // will lose it and never get the chance to add it again.
        event = new UndoableEditEvent(event.getSource(), anEdit);
        // Delegate to the superclass, which will add the edit to the
        // queue, and fire off the necessary state change event.
        super.undoableEditHappened(event);
    }

    @Override
    public synchronized void redo() throws CannotRedoException {
        if (isCompound) {
            // Within compound mode, handle redo as usual.
            super.redo();
        } else {
            compoundInProgress = true;
            try {
                // Outside of compound mode, redo the entire set of
                // compound edits, if the next edit marks the beginning.
                undoRedoCompound = false;
                super.redo();
                if (undoRedoCompound || openCompound) {
                    undoRedoComplete = false;
                    while (!undoRedoComplete && canRedo()) {
                        super.redo();
                    }
                }
            } finally {
                compoundInProgress = false;
            }
        }
    }

    @Override
    public synchronized void undo() throws CannotUndoException {
        if (isCompound) {
            // Within compound mode, handle undo as usual.
            super.undo();
        } else {
            compoundInProgress = true;
            try {
                // Outside of compound mode, undo the entire set of
                // compound edits, if the previous edit marks the end.
                undoRedoCompound = false;
                super.undo();
                if (undoRedoCompound || openCompound) {
                    undoRedoComplete = false;
                    while (!undoRedoComplete && canUndo()) {
                        super.undo();
                    }
                }
            } finally {
                compoundInProgress = false;
            }
        }
    }

    /**
     * Sentinel edit that marks the beginning of a compound set of edits.
     */
    private class BeginCompoundEdit extends FilterUndoableEdit {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new instance of BeginCompoundEdit.
         *
         * @param  delegate  the original undoable edit.
         */
        public BeginCompoundEdit(UndoableEdit delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public boolean isSignificant() {
            if (delegate != null) {
                return compoundInProgress || delegate.isSignificant();
            } else {
                return compoundInProgress;
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            undoRedoCompound = true;
            openCompound = true;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            undoRedoComplete = true;
            openCompound = false;
        }
    }

    /**
     * Sentinel edit that marks the end of a compound set of edits.
     * This is not a filter edit since the fix for issue 8692 prevents
     * us from replacing a document edit. Rather, we are simply added
     * to the end of the queue and indicate to our manager that the
     * entire compound set needs to be undone (or redo is finished).
     */
    private class EndCompoundEdit extends AbstractUndoableEdit {
        /** silence compiler warnings */
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isSignificant() {
            // We are significant during a compound undo/redo, so that
            // the boolean flags are set appropriately. Outside of the
            // undo/redo, we do not have to be significant, and in fact
            // that is desirable. We may get replaced if the set is left
            // open, but the undo manager will compensate later.
            return compoundInProgress;
        }

        @Override
        public void redo() throws CannotUndoException {
            super.redo();
            undoRedoComplete = true;
            openCompound = false;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            undoRedoCompound = true;
            openCompound = true;
        }
    }
}

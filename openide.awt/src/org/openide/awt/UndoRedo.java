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
package org.openide.awt;

import org.openide.util.RequestProcessor;
import org.openide.util.Task;

import java.util.LinkedList;

import javax.swing.event.*;
import javax.swing.undo.*;


/** Undo and Redo manager for top components and workspace elements.
 * It allows <code>UndoAction</code> and <code>RedoAction</code> to listen to editing changes of active
 * components and to changes in their ability to do undo and redo.
 *
 * <p>
 * <b>Related links:</b>
 * <ul>
 *   <li><a href="@org-openide-actions@/org/openide/actions/UndoAction.html">org.openide.actions.UndoAction</a></li>
 *   <li><a href="@org-openide-actions@/org/openide/actions/RedoAction.html">org.openide.actions.RedoAction</a></li>
 *   <li><a href="@org-openide-windows@/org/openide/windows/TopComponent.html#getUndoRedo()">org.openide.windows.TopComponent.getUndoRedo()</a></li>
 * </ul> 
 *
 * @author Jaroslav Tulach
*/
public interface UndoRedo {
    /** Empty implementation that does not allow
    * any undo or redo actions.
    */
    public static final UndoRedo NONE = new Empty();

    /** Test whether the component currently has edits which may be undone.
    * @return <code>true</code> if undo is allowed
    */
    public boolean canUndo();

    /** Test whether the component currently has undone edits which may be redone.
    * @return <code>true</code> if redo is allowed
    */
    public boolean canRedo();

    /** Undo an edit.
    * @exception CannotUndoException if it fails
    */
    public void undo() throws CannotUndoException;

    /** Redo a previously undone edit.
    * @exception CannotRedoException if it fails
    */
    public void redo() throws CannotRedoException;

    /** Add a change listener.
    * The listener will be notified every time the undo/redo
    * ability of this object changes.
    * @param l the listener to add
    */
    public void addChangeListener(ChangeListener l);

    /** Remove a change listener.
    * @param l the listener to remove
    * @see #addChangeListener
    */
    public void removeChangeListener(ChangeListener l);

    /** Get a human-presentable name describing the
    * undo operation.
    * @return the name
    */
    public String getUndoPresentationName();

    /** Get a human-presentable name describing the
    * redo operation.
    * @return the name
    */
    public String getRedoPresentationName();

    /** An undo manager which fires a change event each time it consumes a new undoable edit.
    */
    public static class Manager extends UndoManager implements UndoRedo {
        static final long serialVersionUID = 6721367974521509720L;

        /** listener list */
        private final EventListenerList list = new EventListenerList();

        /** vector of Edits to run */
        private LinkedList<UndoableEditEvent> runus = new LinkedList<UndoableEditEvent>(); // for fix of #8692

        /** task that clears the queue */
        private Task task = Task.EMPTY; // for fix of #8692

        /** Called from undoableEditHappened() inner class */
        private void superUndoableEditHappened(UndoableEditEvent ue) {
            super.undoableEditHappened(ue);
        }

        /** Called from discardAllEdits() inner class */
        private void superDiscardAllEdits() {
            super.discardAllEdits();
        }

        /** Consume an undoable edit.
        * Delegates to superclass and notifies listeners.
        * @param ue the edit
        */
        public void undoableEditHappened(final UndoableEditEvent ue) {
            /* Edits are posted to request processor and the deadlock
             * in #8692 between undoredo and document that fires
             * the undoable edit should be avoided this way.
             */
            synchronized (runus) {
                runus.add(ue);
            }

            updateTask();
        }

        /** Discard all the existing edits from the undomanager. */
        public void discardAllEdits() {
            synchronized (runus) {
                runus.add(null);
            }

            updateTask();
        }

        public boolean canUndo() {
            /* First it must be checked that there are
             * undoable edits waiting to be added to undoredo.
             */
            boolean empty;

            synchronized (runus) {
                empty = runus.isEmpty();
            }

            if (!empty) {
                task.waitFinished();
            }

            return super.canUndo();
        }

        private void fireChange() {
            Object[] l = list.getListenerList();

            if (l.length == 0) {
                return;
            }

            ChangeEvent ev = new ChangeEvent(this);

            for (int i = l.length - 1; i >= 0; i -= 2) {
                ((ChangeListener) l[i]).stateChanged(ev);
            }
        }

        private void updateTask() {
            /* The following task is finished when there are no
             * undoable edits waiting to be added to undoredo.
             */
            class R implements Runnable {
                public void run() {
                    for (;;) {
                        UndoableEditEvent ue;

                        synchronized (runus) {
                            if (runus.isEmpty()) {
                                break;
                            }

                            ue = runus.removeFirst();
                        }

                        if (ue == null) {
                            superDiscardAllEdits();
                        } else {
                            superUndoableEditHappened(ue);
                        }

                        fireChange();
                    }
                }
            }

            R r = new R();
            r.run();

            //Use internal not default RequestProcessor to solve deadlock #10826
            //task = internalRequestProcessor.post (r, 0, Thread.MAX_PRIORITY);
        }

        /* Attaches change listener to the this object.
        * The listener is notified everytime the undo/redo
        * ability of this object changes.
        */

        //#32313 - synchronization of this method was removed
        public void addChangeListener(ChangeListener l) {
            list.add(ChangeListener.class, l);
        }

        /* Removes the listener
        */
        public void removeChangeListener(ChangeListener l) {
            list.remove(ChangeListener.class, l);
        }

        public String getUndoPresentationName() {
            return this.canUndo() ? super.getUndoPresentationName() : ""; // NOI18N
        }

        public String getRedoPresentationName() {
            return this.canRedo() ? super.getRedoPresentationName() : ""; // NOI18N
        }
    }

    // XXX cannot be made private in an interface, consider removing later

    /** Empty implementation that does not support any undoable edits.
    * @deprecated Use {@link UndoRedo#NONE} rather than instantiating this.
    */
    public static final class Empty extends Object implements UndoRedo {
        public boolean canUndo() {
            return false;
        }

        public boolean canRedo() {
            return false;
        }

        public void undo() throws CannotUndoException {
            throw new CannotUndoException();
        }

        public void redo() throws CannotRedoException {
            throw new CannotRedoException();
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public String getUndoPresentationName() {
            return ""; // NOI18N
        }

        public String getRedoPresentationName() {
            return ""; // NOI18N
        }
    }
}

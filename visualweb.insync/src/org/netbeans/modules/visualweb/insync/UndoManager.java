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
package org.netbeans.modules.visualweb.insync;

import java.util.ArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import org.openide.awt.UndoRedo;

import org.netbeans.modules.visualweb.insync.models.FacesModel;

/**
 * Undo manager: maintains a list of undoable events, and performs undo/redo as requested by the
 * user. This class implements the UndoRedo interface from NetBeans, which is used by the
 * UndoAction. This manager plugs into the IDE via the designer: when a designer form receives
 * focus, it will set this undo manager as the active undo-redo object for the undo action.
 *
 * @author Tor Norbye
 */
public class UndoManager implements UndoRedo {


    /** Possibly empty list of UndoEvents */
    private final ArrayList undoStack = new ArrayList();

    /** Temporarily holds undo events that have been tentatively removed from the undoStack. */
    private final ArrayList undoStackRemoved = new ArrayList();

    /**
     * Pointer to where we are in the undo stack. It points to the next redoable item. Thus, when
     * there are 5 events, and the current index is 5, we can't redo any events, but we can undo 5
     * events. When the current index is 0, we can redo five and undo 0.
     */
    private int current = 0;
    private UndoEvent currentEvent;
    private int depth = 0;

    public UndoManager() {
    }

    /**
     * Return event that is in progress. After a finishUndoableTask this will return null.
     */
    public UndoEvent getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Inform the undo manager that a new undoable task is beginning.
     *
     * @param description A short user visible description of the task - may for example appear in
     *            the tooltip over the undo/redo buttons.
     * @param model The Model associated with the task; when the task is undone/redone, the model is
     *            synced.
     */
    public UndoEvent startUndoableTask(String description, Model model) {
        if (currentEvent != null) {
            // XXX no ! I should keep a reference count here to make sure
            // only the outermost reference gets it
            depth++;
            return currentEvent;
        }

        //clear undoStackRemoved, so we can add any events we are about to remove from undoStack
        undoStackRemoved.clear();

        // Truncate redo "future" when you initiate a new undoable edit
        if (canRedo()) {
            // Remove the items from "current" out to the end of the stack;
            // unfortunately there's no way to set the size of an array list
            // so we've gotta delete them. Do it in the reverse order so
            // we don't cause any shifting.
            for (int i = undoStack.size()-1; i >= current; i--) {
                undoStackRemoved.add(0, undoStack.remove(i));
            }
        }

        UndoEvent event = new UndoEvent(description, model);
        currentEvent = event;
        undoStack.add(event);
        current++;
        depth++;

        return event;
    }

    /** Inform the undo manager that the given event is done. */
    public void finishUndoableTask(UndoEvent event) {
        depth--;
        if (depth > 0) {
            return;
        }
        if (event != null) {
            if (currentEvent == event) {
                currentEvent = null;
            }
            // Don't record undo events for items that don't
            // cause any changes!!
            if (!event.hasChanges() && current > 0) {
                //make sure "current" is the last item in the stack, and that the last item is the "event"
                if (current != undoStack.size() || event != undoStack.get(current-1)) {
                    org.openide.ErrorManager.getDefault().log("org.netbeans.modules.visualweb.insync.UndoManager.finishUndoableTask: current event is not last in undo stack");  //NOI18N
                }
                else {
                    //remove the event
                    undoStack.remove(event);
                    //add back any removed events
                    undoStack.addAll(undoStackRemoved);
                }
                current--;
            }
        }

        fireStateChanged();
    }

    private void fireStateChanged() {
        ChangeEvent event = null;
        for (int i = listenerList.size()-1; i >= 0; i--) {
            ChangeListener listener = (ChangeListener)listenerList.get(i);
            if (event == null) {
                event = new ChangeEvent(this);
            }
            listener.stateChanged(event);
        }
    }

    /**
     * This method is called when one of the buffers this undo manager cares about has been edited.
     * If we detect that this is a user-initiated editing operation of the buffer itself, we flush
     * the undo queues. Otherwise, the user can go into say the backing file, undo three editing
     * operations, and type 5 characters, then go back to the design view and try to user our undo
     * queue, and the undo events are totally unsynchronized.
     */
    public void notifyBufferEdited(SourceUnit unit) {
        if (currentEvent == null) {
            clearStack();
        }
    }

    public void notifyUndoableEditEvent(SourceUnit unit) {
        if (currentEvent != null) {
            currentEvent.notifyBufferUpdated(unit);
        } else {
            org.openide.ErrorManager.getDefault().log("Unexpected undoable event with no current event");
        }
    }

    private void clearStack() {
        if (undoStack.size() > 0) {
            undoStack.clear();
            current = 0;
            fireStateChanged();
        }
    }

    //------------------------------------------------------------------------------------- UndoRedo

    public boolean canRedo() {
        return current < undoStack.size();
    }
    
    public boolean canUndo() {
        return current > 0;
    }
    
    public String getRedoPresentationName() {
        if (canRedo()) {
            UndoEvent event = (UndoEvent)undoStack.get(current);
            return event.getDescription();
        } else {
            return "";
        }
    }
    
    public String getUndoPresentationName() {
        if (canUndo()) {
            UndoEvent event = (UndoEvent)undoStack.get(current-1);
            return event.getDescription();
        } else {
            return "";
        }
    }
    
    private ArrayList listenerList = new ArrayList(3);

    public void addChangeListener(ChangeListener changeListener) {
        listenerList.add(changeListener);
    }
    
    public void removeChangeListener(ChangeListener changeListener) {
        listenerList.remove(changeListener);
    }
    
    public void undo() throws CannotUndoException {
        current--;
        UndoEvent event = (UndoEvent)undoStack.get(current);
        currentEvent = event;
        event.undo();
        currentEvent = null;
        syncModel(event);

        fireStateChanged();
    }

    public void redo() throws CannotRedoException {
        UndoEvent event = (UndoEvent)undoStack.get(current);
        currentEvent = event;
        event.redo();
        currentEvent = null;
        current++;
        syncModel(event);
        
        fireStateChanged();
    }
    
    private void syncModel(UndoEvent event) {
        Model model = event.getModel();
        model.sync();
        // HACK
        FacesModel fm = (FacesModel)model;
        fm.getMarkupUnit().setClean();
        fm.getJavaUnit().setClean();
    }
}

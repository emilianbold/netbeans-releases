/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.form.FormModel;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Jan Stola
 */
public class UndoRedoSupport {
    private static Map<FormModel,UndoRedoSupport> supportMap = new WeakHashMap<FormModel,UndoRedoSupport>();
    GridActionPerformer performer;
    UndoAction undoAction;
    RedoAction redoAction;
    boolean undoInProgress;
    boolean redoInProgress;
    int undoableEdits;
    int redoableEdits;
    private ArrayList<UndoRedoPerformedListener> listeners;
    
    private UndoRedoSupport(FormModel model) {
        model.getUndoRedoManager().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (undoInProgress) {
                    undoableEdits--;
                    redoableEdits++;
                } else if (redoInProgress) {
                    undoableEdits++;
                    redoableEdits--;
                } else {
                    undoableEdits++;
                    redoableEdits=0;
                }
                undoAction.updateEnabled();
                redoAction.updateEnabled();
            }
        });
        undoAction = new UndoAction(SystemAction.get(org.openide.actions.UndoAction.class));
        redoAction = new RedoAction(SystemAction.get(org.openide.actions.RedoAction.class));
    }

    static UndoRedoSupport getSupport(FormModel model) {
        UndoRedoSupport support = supportMap.get(model);
        if (support == null) {
            support = createUndoRedoSupport(model);
            supportMap.put(model, support);
        }
        return support;
    }
    
    private static UndoRedoSupport createUndoRedoSupport(FormModel model) {
        return new UndoRedoSupport(model);
    }
    
    public void reset(GridActionPerformer performer) {
        this.performer = performer;
        undoableEdits = 0;
        redoableEdits = 0;
        undoAction.updateEnabled();
        redoAction.updateEnabled();
    }
    
    public Action getUndoAction() {
        return undoAction;
    }
    
    public Action getRedoAction() {
        return redoAction;
    }
    
    static abstract class DelegateAction extends AbstractAction {
        protected Action delegate;
        
        protected DelegateAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        @Override
        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }
    }
    
    class UndoAction extends DelegateAction {
        
        UndoAction(Action delegate) {
            super(delegate);
            updateEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undoInProgress = true;
                performer.performAction(new DelegateGridAction(delegate));
                for (int i=0; i < listeners.size(); i++) {
                    UndoRedoPerformedListener l = listeners.get(i);
                    l.UndoRedoPerformed(true);
                }
            } finally {
                undoInProgress = false;
            }
        }
        
        final void updateEnabled() {
            setEnabled(undoableEdits>0);
        }
        
    }
    
    class RedoAction extends DelegateAction {
        
        RedoAction(Action delegate) {
            super(delegate);
            updateEnabled();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                redoInProgress = true;
                performer.performAction(new DelegateGridAction(delegate));
                for (int i=0; i < listeners.size(); i++) {
                    UndoRedoPerformedListener l = listeners.get(i);
                    l.UndoRedoPerformed(false);
                }
            } finally {
                redoInProgress = false;
            }
        }
        
        final void updateEnabled() {
            setEnabled(redoableEdits>0);
        }
        
    }
    
    static class DelegateGridAction extends AbstractGridAction {
        protected Action delegate;
        
        protected DelegateGridAction(Action delegate) {
            this.delegate = delegate;
        }

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int oldColumns = info.getColumnCount();
            int oldRows = info.getRowCount();
            GridUtils.removePaddingComponents(gridManager);

            // Undo/redo itself
            delegate.actionPerformed(null);
            gridManager.updateLayout(true);
            
            // Remove deleted components from selection
            Set<Component> newSelection = new HashSet<Component>();
            for (Component comp : context.getSelectedComponents()) {
                if (comp.getParent() != null) {
                    newSelection.add(comp);
                }
            }
            context.setSelectedComponents(newSelection);
                    
            GridUtils.revalidateGrid(gridManager);
            gridManager.updateGaps(false);
            int newColumns = info.getColumnCount();
            int newRows = info.getRowCount();
            int columns = Math.max(oldColumns, newColumns);
            int rows = Math.max(oldRows, newRows);
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            return null;
        }
        
    }
    
    public synchronized void addUndoRedoListener(UndoRedoPerformedListener l) {
        if (listeners == null)
            listeners = new ArrayList<UndoRedoPerformedListener>();
        listeners.add(l);
    }

    public synchronized void removeUndoRedoListener(UndoRedoPerformedListener l) {
        if (listeners != null)
            listeners.remove(l);
    }

    public interface UndoRedoPerformedListener extends java.util.EventListener {
        /** Notification about performed Undo/Redo.
         * @param undo - true for Undo, false for Redo
         */
        public void UndoRedoPerformed(boolean undo);
    }

}

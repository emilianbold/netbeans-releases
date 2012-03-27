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
package org.netbeans.modules.refactoring.spi.impl;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import org.netbeans.modules.refactoring.api.ProgressEvent;
import org.netbeans.modules.refactoring.api.ProgressListener;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.spi.BackupFacility;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jan Becicka
 */
public final class UndoManager {

    /**
     * stack of undo items
     */
    private LinkedList<LinkedList<UndoItem>> undoList;
    /**
     * stack of redo items
     */
    private LinkedList<LinkedList<UndoItem>> redoList;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private boolean wasUndo = false;
    private boolean wasRedo = false;
    private boolean transactionStart;
    private IdentityHashMap<LinkedList, String> descriptionMap;
    private String description;
    private ProgressListener progress;
    private static UndoManager instance;

    /**
     * Singleton instance
     * @return
     */
    public static synchronized UndoManager getDefault() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    /**
     * Creates a new instance of UndoManager
     */
    private UndoManager() {
        undoList = new LinkedList<LinkedList<UndoItem>>();
        redoList = new LinkedList<LinkedList<UndoItem>>();
        descriptionMap = new IdentityHashMap<LinkedList, String>();
    }

    private UndoManager(ProgressListener progress) {
        this();
        this.progress = progress;
    }

    /**
     * Setter for undo description. For instance "Rename"
     * @param desc 
     */
    public void setUndoDescription(String desc) {
        description = desc;
    }

    /**
     * Getter for undo description.
     * @return
     */
    public String getUndoDescription() {
        if (undoList.isEmpty()) {
            return null;
        }
        return descriptionMap.get(undoList.getFirst());
    }

    /**
     * Getter for Redo description.
     * @return
     */
    public String getRedoDescription() {
        if (redoList.isEmpty()) {
            return null;
        }
        return descriptionMap.get(redoList.getFirst());
    }

    /**
     * called to mark transaction start
     */
    public void transactionStarted() {
        transactionStart = true;
    }

    /**
     * called to mark end of transaction
     */
    public void transactionEnded(boolean fail) {
        description = null;
        if (fail && !undoList.isEmpty()) {
            //XXX todo 
            //undoList.removeFirst();
        } else {
            // [TODO] (jb) this code disables undos for changes using org.openide.src
            if (isUndoAvailable() && getUndoDescription() == null) {
                descriptionMap.remove(undoList.removeFirst());
            }
        }
        fireChange();
    }

    /**
     * undo last transaction
     */
    public void undo() {
        //System.out.println("************* Starting UNDO");
        if (isUndoAvailable()) {
            if (JOptionPane.showConfirmDialog(
                    WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(UndoManager.class, "MSG_ReallyUndo", getUndoDescription()),
                    NbBundle.getMessage(UndoManager.class, "MSG_ConfirmUndo"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                throw new CannotUndoException();
            }

            Runnable run = new Runnable() {

                public void run() {
                    boolean fail = true;
                    try {
                        transactionStarted();
                        wasUndo = true;
                        LinkedList undo = (LinkedList) undoList.getFirst();
                        fireProgressListenerStart(0, undo.size());
                        Iterator undoIterator = undo.iterator();
                        UndoItem item;
                        redoList.addFirst(new LinkedList<UndoItem>());
                        descriptionMap.put(redoList.getFirst(), descriptionMap.remove(undo));
                        while (undoIterator.hasNext()) {
                            fireProgressListenerStep();
                            item = (UndoItem) undoIterator.next();
                            item.undo();
                            if (item instanceof SessionUndoItem) {
                                addItem(item);
                            }
                        }
                        undoList.removeFirst();
                        fail = false;
                    } finally {
                        try {
                            wasUndo = false;
                            transactionEnded(fail);
                        } finally {
                            fireProgressListenerStop();
                            fireChange();
                        }
                    }

                }
            };

//            if (SwingUtilities.isEventDispatchThread()) {
//                ProgressUtils.runOffEventDispatchThread(run,
//                        "Undoing... ",
//                        new AtomicBoolean(),
//                        false);
//            } else {
            run.run();
//            }
        }
    }

    /**
     * redo last undo
     */
    public void redo() {
        //System.out.println("************* Starting REDO");
        if (isRedoAvailable()) {
            if (JOptionPane.showConfirmDialog(
                    WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(UndoManager.class, "MSG_ReallyRedo", getRedoDescription()),
                    NbBundle.getMessage(UndoManager.class, "MSG_ConfirmRedo"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                throw new CannotRedoException();
            }
            Runnable run = new Runnable() {

                public void run() {
                    boolean fail = true;
                    try {
                        transactionStarted();
                        wasRedo = true;
                        LinkedList<UndoItem> redo = redoList.getFirst();
                        fireProgressListenerStart(1, redo.size());
                        Iterator<UndoItem> redoIterator = redo.iterator();
                        UndoItem item;
                        description = descriptionMap.remove(redo);
                        while (redoIterator.hasNext()) {
                            fireProgressListenerStep();
                            item = redoIterator.next();
                            item.redo();
                            if (item instanceof SessionUndoItem) {
                                addItem(item);
                            }
                        }
                        redoList.removeFirst();
                        fail = false;
                    } finally {
                        try {
                            wasRedo = false;
                            transactionEnded(fail);
                        } finally {
                            fireProgressListenerStop();
                            fireChange();
                        }
                    }
                }
            };

//            if (SwingUtilities.isEventDispatchThread()) {
//                ProgressUtils.runOffEventDispatchThread(run,
//                        "Redoing... ",
//                        new AtomicBoolean(),
//                        false);
//            } else {
            run.run();
//            }
        }
    }

    /**
     * clean undo/redo stacks
     */
    public void clear() {
        undoList.clear();
        redoList.clear();
        descriptionMap.clear();
        BackupFacility.getDefault().clear();
        fireChange();
    }

    public void addItem(RefactoringSession session) {
        addItem(new SessionUndoItem(session));
    }

    /**
     * add new item to undo/redo list
     */
    private void addItem(UndoItem item) {
        if (wasUndo) {
            LinkedList<UndoItem> redo = this.redoList.getFirst();
            redo.addFirst(item);
        } else {
            if (transactionStart) {
                undoList.addFirst(new LinkedList<UndoItem>());
                descriptionMap.put(undoList.getFirst(), description);
                transactionStart = false;
            }
            LinkedList<UndoItem> undo = this.undoList.getFirst();
            undo.addFirst(item);
        }
        if (!(wasUndo || wasRedo)) {
            redoList.clear();
        }
    }

    public boolean isUndoAvailable() {
        return !undoList.isEmpty();
    }

    public boolean isRedoAvailable() {
        return !redoList.isEmpty();
    }

    public void addChangeListener(ChangeListener cl) {
        changeSupport.addChangeListener(cl);
    }

    public void removeChangeListener(ChangeListener cl) {
        changeSupport.removeChangeListener(cl);
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    private void fireProgressListenerStart(int type, int count) {
        stepCounter = 0;
        if (progress == null) {
            return;
        }
        progress.start(new ProgressEvent(this, ProgressEvent.START, type, count));
    }
    private int stepCounter = 0;

    /**
     * Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStep() {
        if (progress == null) {
            return;
        }
        progress.step(new ProgressEvent(this, ProgressEvent.STEP, 0, ++stepCounter));
    }

    /**
     * Notifies all registered listeners about the event.
     */
    private void fireProgressListenerStop() {
        if (progress == null) {
            return;
        }
        progress.stop(new ProgressEvent(this, ProgressEvent.STOP));
    }

    private interface UndoItem {

        void undo();

        void redo();
    }

    private final class SessionUndoItem implements UndoItem {

        private RefactoringSession change;

        public SessionUndoItem(RefactoringSession change) {
            this.change = change;
        }

        @Override
        public void undo() {
            change.undoRefactoring(false);
        }

        @Override
        public void redo() {
            change.doRefactoring(false);
        }
    }
}

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

package org.netbeans.modules.bugtracking.util;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;

/**
 * Support for compound undo/redo in text components
 * @author Ondrej Vrabec
 * @author Tomas Stupka
 * 
 */
public class UndoRedoSupport {

    private static final Pattern DELIMITER_PATTERN = Pattern.compile("[ ,:;.!?\n\t]"); //NOI18N
    
    private final DelegateManager delegateManager;
    private static final String ACTION_NAME_UNDO = "undo.action"; //NOI18N
    private static final String ACTION_NAME_REDO = "redo.action"; //NOI18N

    private static Map<Issue, UndoRedoSupport> managers = new WeakHashMap<Issue, UndoRedoSupport>();

    private UndoRedoSupport () {
        delegateManager = new DelegateManager();
    }
    
    public synchronized static UndoRedo getUndoRedo(Issue issue) {
        UndoRedoSupport support = getSupport(issue);
        return support.delegateManager;
    }
    
    public synchronized static UndoRedoSupport getSupport (Issue issue) {
        UndoRedoSupport support = managers.get(issue);
        if(support == null) {
            support = new UndoRedoSupport();
            managers.put(issue, support);
        }
        return support;
    }
    
    /**
     * Registers undo/redo manager on the given component. You should always call unregister once undo/redo is not needed.
     * @param issue
     * @param component
     * @return
     */
    public void register (JTextComponent component) {
        delegateManager.add(new CompoundUndoManager(component));
    }

    /**
     * Unregisters undo/redo manager on the component, removes registered listeners, etc.
     */
    public void unregisterAll (Issue issue) {
        managers.remove(issue);
        delegateManager.removeAll();
    }
    
    /**
     * Unregisters undo/redo manager on the component, removes registered listeners, etc.
     */
    public void unregister (JTextComponent component) {
        delegateManager.remove(component);
    }
    
    private class CompoundUndoManager extends UndoRedo.Manager implements FocusListener {
        private ChangeSupport support = new ChangeSupport(this);
        private CompoundEdit edit;
        private int lastOffset, lastLength;
        private final JTextComponent component;

        public CompoundUndoManager(JTextComponent component) {
            this.component = component;
        }
        
        @Override
        public void undoableEditHappened(UndoableEditEvent e) {
            assert component != null;
            
            if (edit == null) {
                startNewEdit(component, e.getEdit());
                processDocumentChange(component);
                return;
            }
            AbstractDocument.DefaultDocumentEvent event = (AbstractDocument.DefaultDocumentEvent) e.getEdit();
            if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
                edit.addEdit(e.getEdit());
                return;
            }
            int offsetChange = component.getCaretPosition() - lastOffset;
            int lengthChange = component.getDocument().getLength() - lastLength;

            if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
                lastOffset = component.getCaretPosition();
                lastLength = component.getDocument().getLength();
                super.undoableEditHappened(e);
                processDocumentChange(component);
            } else {
                // last change consists of multiple chars, start new compound edit
                startNewEdit(component, e.getEdit());
            }
        }

        private void startNewEdit (JTextComponent component, UndoableEdit atomicEdit) {
            if (edit != null) {
                // finish the last edit
                edit.end();
            }
            edit = new MyCompoundEdit();
            edit.addEdit(atomicEdit);
            super.undoableEditHappened(new UndoableEditEvent(component, edit));
            lastOffset = component.getCaretPosition();
            lastLength = component.getDocument().getLength();
        }

        private void processDocumentChange(JTextComponent component) {
            boolean endEdit = lastOffset == 0;
            if (!endEdit) {
                try {
                    String lastChar = component.getDocument().getText(lastOffset - 1, 1);
                    endEdit = DELIMITER_PATTERN.matcher(lastChar).matches();
                } catch (BadLocationException ex) {
                }
            }
            if (endEdit) {
                // ending the current compound edit, next will be started
                edit.end();
                edit = null;
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            super.addChangeListener(l);
            support.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            super.removeChangeListener(l);
            support.removeChangeListener(l);
        }
        
        @Override
        public synchronized boolean canRedo() {
            boolean can = super.canRedo();
            if(!can) {
                return can;
            }
            return super.canRedo() && (component != null ? component.hasFocus() : false);
        }

        @Override
        public synchronized boolean canUndo() {
            return super.canUndo() && (component != null ? component.hasFocus() : false);
        }
        
        @Override
        public void focusGained(FocusEvent e) {
            support.fireChange();
        }
        @Override
        public void focusLost(FocusEvent e) {
            support.fireChange();
        }        
        boolean hasFocus() {
            return component.hasFocus();
        }
        
        private class MyCompoundEdit extends CompoundEdit {

            @Override
            public boolean isInProgress() {
                return false;
            }

            @Override
            public void undo() throws CannotUndoException {
                if (edit != null) {
                    edit.end();
                }
                super.undo();
                edit = null;
            }
        }
    }

    
    private class DelegateManager implements UndoRedo {
        private final List<CompoundUndoManager> delegates = new LinkedList<CompoundUndoManager>();
        
        @Override
        public boolean canUndo() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.canUndo();
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canRedo() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.canRedo();
                    }
                }
            }
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        cm.undo();
                        return;
                    }
                }
            }
        }

        @Override
        public void redo() throws CannotRedoException {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        cm.redo();
                        return;
                    }
                }
            }
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.addChangeListener(l);
                }
            }
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.removeChangeListener(l);
                }
            }
        }
        
        void discardAllEdits() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    cm.discardAllEdits();
                }
            }
        }    
        
        @Override
        public String getUndoPresentationName() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.getUndoPresentationName();
                    }
                }
            }
            return "";            
        }

        @Override
        public String getRedoPresentationName() {
            synchronized(delegates) {
                for (CompoundUndoManager cm : delegates) {
                    if(cm.hasFocus()) {
                        return cm.getRedoPresentationName();
                    }
                }
            }
            return "";
        }

        private void add(CompoundUndoManager cum) {
            cum.component.getDocument().addUndoableEditListener(cum);
            cum.component.addFocusListener(cum);            
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_UNDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.META_DOWN_MASK), ACTION_NAME_UNDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UNDO, 0), ACTION_NAME_UNDO);
            cum.component.getActionMap().put(ACTION_NAME_UNDO, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (delegateManager.canUndo()) {
                        delegateManager.undo();
                    }
                }
            });
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), ACTION_NAME_REDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.META_DOWN_MASK), ACTION_NAME_REDO);
            cum.component.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_AGAIN, 0), ACTION_NAME_UNDO);
            cum.component.getActionMap().put(ACTION_NAME_REDO, new AbstractAction(){
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (delegateManager.canRedo()) {
                        delegateManager.redo();
                    }
                }
            });
            
            synchronized(delegates) {
                delegates.add(cum);
            }
            
        }

        private void removeAll() {
            discardAllEdits();
            synchronized(delegates) {
                Iterator<CompoundUndoManager> it = delegates.iterator();
                while (it.hasNext()) {
                    CompoundUndoManager cum = it.next();
                    cum.component.getDocument().removeUndoableEditListener(cum);
                    cum.component.removeFocusListener(cum);
                    cum.component.getActionMap().remove(ACTION_NAME_UNDO);
                    cum.component.getActionMap().remove(ACTION_NAME_REDO);
                    it.remove();
                }
            }
        }

        private void remove(JTextComponent component) {
            synchronized(delegates) {
                Iterator<CompoundUndoManager> it = delegates.iterator();
                while (it.hasNext()) {
                    CompoundUndoManager cum = it.next();
                    if(component == cum.component) {
                        cum.component.getDocument().removeUndoableEditListener(cum);
                        cum.component.removeFocusListener(cum);
                        cum.component.getActionMap().remove(ACTION_NAME_UNDO);
                        cum.component.getActionMap().remove(ACTION_NAME_REDO);
                        it.remove();
                        break;
                    }
                }
            }
        }
    }
    
}

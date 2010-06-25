/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.openide.awt;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.undo.UndoableEdit;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import org.netbeans.junit.NbTestCase;
import static org.junit.Assert.*;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class UndoRedoTest extends NbTestCase implements ChangeListener {
    private int cnt;

    public UndoRedoTest(String n) {
        super(n);
    }

    public void testUndoDeliversChanges() {
        UndoRedo.Manager ur = new UndoRedo.Manager();
        doUndoRedoTest(ur);
    }

    public void testUndoDeliversChangesWithTooManyEdits() {
        UndoRedo.Manager ur = new UndoRedo.Manager() {
            @Override
            public boolean canUndo() {
                if (super.canUndo()) {
                    undoableEditHappened(new UndoableEditEvent(this, new MyEdit(true)));
                }
                return super.canUndo();
            }
        };
        doUndoRedoTest(ur);
    }

    private void doUndoRedoTest(UndoRedo.Manager ur) {
        assertFalse("Nothing to undo", ur.canUndo());
        ur.addChangeListener(this);
        MyEdit me = new MyEdit();
        ur.undoableEditHappened(new UndoableEditEvent(this, me));
        assertEquals("One change", 1, cnt);
        assertTrue("Can undo now", ur.canUndo());
        ur.undo();
        assertFalse("Cannot undo", ur.canUndo());
        assertEquals("Snd change", 2, cnt);

        assertTrue("But redo", ur.canRedo());
        ur.redo();
        assertEquals("Third change", 3, cnt);
        assertEquals("One undo", 1, me.undo);
        assertEquals("One redo", 1, me.redo);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        cnt++;
    }
    private static final class MyEdit implements UndoableEdit, PropertyChangeListener {
        private int undo;
        private int redo;
        private int cnt;
        private boolean ignore;

        public MyEdit() {
            this(false);
        }

        public MyEdit(boolean ignore) {
            this.ignore = ignore;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("enabled".equals(evt.getPropertyName())) {
                cnt++;
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            undo++;
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void redo() throws CannotRedoException {
            redo++;
        }

        @Override
        public boolean canRedo() {
            return true;
        }

        @Override
        public void die() {
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof MyEdit && ((MyEdit)anEdit).ignore) {
                return true;
            }
            return false;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            return true;
        }

        @Override
        public String getPresentationName() {
            return "My Edit";
        }

        @Override
        public String getUndoPresentationName() {
            return "My Undo";
        }

        @Override
        public String getRedoPresentationName() {
            return "My Redo";
        }
    }

}
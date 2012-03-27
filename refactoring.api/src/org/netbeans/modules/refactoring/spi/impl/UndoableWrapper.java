/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.spi.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.document.UndoableEditWrapper;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

/**
 *
 * @author Jan Becicka
 */
@MimeRegistration(mimeType="", service=UndoableEditWrapper.class)
public class UndoableWrapper implements UndoableEditWrapper {

    private AtomicBoolean active = new AtomicBoolean();
    private Map<BaseDocument, UndoableEditDelegate> docToFirst = new HashMap();

    public UndoableWrapper() {
    }
    

    @Override
    public UndoableEdit wrap(UndoableEdit ed, Document doc) {
        if (!active.get())
            return ed;
        if (doc.getProperty(BaseDocument.StreamDescriptionProperty) == null) {
            //no dataobject
            return ed;
        } 
        UndoableEditDelegate current = new UndoableEditDelegate(ed, (BaseDocument) doc);
        UndoableEditDelegate first = docToFirst.get(doc);
        if (first == null) {
            docToFirst.put((BaseDocument) doc, current);
        }
        return current;
    }

    public void close() {
        for (UndoableEditDelegate first: docToFirst.values()) {
            first.end();
        }
        docToFirst.clear();
    }

    public void setActive(boolean b) {
        active.set(b);
    }

    public class UndoableEditDelegate implements UndoableEdit {

        private UndoManager undoManager;
        private CloneableEditorSupport ces;
        private UndoableEdit delegate;
        private CompoundEdit inner;

        private UndoableEditDelegate(UndoableEdit ed, BaseDocument doc) {
            undoManager = UndoManager.getDefault();
            DataObject dob = (DataObject) doc.getProperty(BaseDocument.StreamDescriptionProperty);
            ces = dob.getLookup().lookup(CloneableEditorSupport.class);
            //this.delegate = ed;
            this.inner = new CompoundEdit();
            inner.addEdit(ed);
            delegate = ed;
        }

        @Override
        public void undo() throws CannotUndoException {
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            if (focusedComponent != null) {
                if (focusedComponent.getDocument() == ces.getDocument()) {
                    //call global undo only for focused component
                    undoManager.undo();
                }
            }
            //delegate.undo();
            inner.undo();
        }

        @Override
        public boolean canUndo() {
            //return delegate.canUndo();
            return inner.canUndo();
        }

        @Override
        public void redo() throws CannotRedoException {
            JTextComponent focusedComponent = EditorRegistry.focusedComponent();
            if (focusedComponent != null) {
                if (focusedComponent.getDocument() == ces.getDocument()) {
                    //call global undo only for focused component
                    undoManager.redo();
                }
            }
            //delegate.redo();
            inner.redo();
        }

        @Override
        public boolean canRedo() {
            //return delegate.canRedo();
            return inner.canRedo();
        }

        @Override
        public void die() {
            //delegate.die();
            inner.die();
        }

        @Override
        public boolean addEdit(UndoableEdit ue) {
            if (ue instanceof UndoableEditDelegate) {
                return inner.addEdit(((UndoableEditDelegate) ue).unwrap());
            }
            return false;
            //return delegate.addEdit(ue);
        }
        
        public UndoableEdit unwrap() {
            return delegate;
        }

        @Override
        public boolean replaceEdit(UndoableEdit ue) {
            return inner.replaceEdit(ue);
            //return delegate.replaceEdit(ue);
        }

        @Override
        public boolean isSignificant() {
            return inner.isSignificant();
            //return delegate.isSignificant();
        }

        @Override
        public String getPresentationName() {
            return undoManager.getUndoDescription();
        }

        @Override
        public String getUndoPresentationName() {
            return undoManager.getUndoDescription();
        }

        @Override
        public String getRedoPresentationName() {
            return undoManager.getRedoDescription();
        }

        private void end() {
            inner.end();
        }
    }
}

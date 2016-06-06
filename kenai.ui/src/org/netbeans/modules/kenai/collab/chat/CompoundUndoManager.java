/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.kenai.collab.chat;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

/*
 **  This class will merge individual edits into a single larger edit.
 **  That is, characters entered sequentially will be grouped together and
 **  undone as a group. Any attribute changes will be considered as part
 **  of the group and will therefore be undone when the group is undone.
 */
public class CompoundUndoManager extends UndoManager implements UndoableEditListener, DocumentListener {

    public CompoundEdit compoundEdit;
    private JTextComponent editor;
    //  These fields are used to help determine whether the edit is an
    //  incremental edit. For each character added the offset and length
    //  should increase by 1 or decrease by 1 for each character removed.
    private int lastOffset;
    private int lastLength;

    public CompoundUndoManager(JTextComponent editor) {
        this.editor = editor;
        editor.getDocument().addUndoableEditListener(this);
    }

    /*
     **  Add a DocumentLister before the undo is done so we can position
     **  the Caret correctly as each edit is undone.
     */
    @Override
    public void undo() {
        editor.getDocument().addDocumentListener(this);
        super.undo();
        editor.getDocument().removeDocumentListener(this);
    }

    /*
     **  Add a DocumentLister before the redo is done so we can position
     **  the Caret correctly as each edit is redone.
     */
    @Override
    public void redo() {
        editor.getDocument().addDocumentListener(this);
        super.redo();
        editor.getDocument().removeDocumentListener(this);
    }

    /*
     **  Whenever an UndoableEdit happens the edit will either be absorbed
     **  by the current compound edit or a new compound edit will be started
     */
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        //  Start a new compound edit

        if (compoundEdit == null) {
            compoundEdit = startCompoundEdit(e.getEdit());
            lastLength = editor.getDocument().getLength();
            return;
        }

        //  Check for an attribute change

        AbstractDocument.DefaultDocumentEvent event =
                (AbstractDocument.DefaultDocumentEvent) e.getEdit();

        if (event.getType().equals(DocumentEvent.EventType.CHANGE)) {
            compoundEdit.addEdit(e.getEdit());
            return;
        }

        //  Check for an incremental edit or backspace.
        //  The change in Caret position and Document length should be either
        //  1 or -1 .

        int offsetChange = editor.getCaretPosition() - lastOffset;
        int lengthChange = editor.getDocument().getLength() - lastLength;

        if (Math.abs(offsetChange) == 1 && Math.abs(lengthChange) == 1) {
            compoundEdit.addEdit(e.getEdit());
            lastOffset = editor.getCaretPosition();
            lastLength = editor.getDocument().getLength();
            return;
        }

        //  Not incremental edit, end previous edit and start a new one

        compoundEdit.end();
        compoundEdit = startCompoundEdit(e.getEdit());
    }

    public void startNewCompoundEdit() {
        if (compoundEdit != null) {
            compoundEdit.end();
            compoundEdit = null;
        }
    }

    /*
     **  Each CompoundEdit will store a group of related incremental edits
     **  (ie. each character typed or backspaced is an incremental edit)
     */
    private CompoundEdit startCompoundEdit(UndoableEdit anEdit) {
        //  Track Caret and Document information of this compound edit

        lastOffset = editor.getCaretPosition();
        lastLength = editor.getDocument().getLength();

        //  The compound edit is used to store incremental edits

        compoundEdit = new MyCompoundEdit();
        compoundEdit.addEdit(anEdit);

        //  The compound edit is added to the UndoManager. All incremental
        //  edits stored in the compound edit will be undone/redone at once

        addEdit(compoundEdit);
        return compoundEdit;
    }

    //  Implement DocumentListener
    //
    // 	Updates to the Document as a result of Undo/Redo will cause the
    //  Caret to be repositioned
    public void insertUpdate(final DocumentEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                int offset = e.getOffset() + e.getLength();
                offset = Math.min(offset, editor.getDocument().getLength());
                editor.setCaretPosition(offset);
            }
        });
    }

    public void removeUpdate(DocumentEvent e) {
        editor.setCaretPosition(e.getOffset());
    }

    public void changedUpdate(DocumentEvent e) {
    }

    class MyCompoundEdit extends CompoundEdit {

        @Override
        public boolean isInProgress() {
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            //  End the edit so future edits don't get absorbed by this edit

            if (compoundEdit != null) {
                compoundEdit.end();
            }

            super.undo();
            //  Always start a new compound edit after an undo

            compoundEdit = null;
        }
    }
}

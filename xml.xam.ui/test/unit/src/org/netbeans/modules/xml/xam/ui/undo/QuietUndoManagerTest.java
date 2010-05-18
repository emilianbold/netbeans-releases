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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.xam.ui.undo;

import java.io.IOException;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import junit.framework.TestCase;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.ui.TestCatalogModel;
import org.netbeans.modules.xml.xam.ui.Util;
import org.openide.awt.UndoRedo;

/**
 * Tests QuietUndoManager class, both in isolation and with XAM model.
 * Additional tests of QuietUndoManager can be found in schema/core.
 *
 * @author Nathan Fiedler
 */
public class QuietUndoManagerTest extends TestCase {
    private SchemaModel model;
    private AbstractDocument document;
    private QuietUndoManager manager;

    public QuietUndoManagerTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * Delete the document text and discard all undoable edits.
     */
    private void cleanup() {
        manager.endCompound();
        document.removeUndoableEditListener(manager);
        if (model != null) {
            model.removeUndoableEditListener(manager);
        }
        manager.discardAllEdits();
        manager.die();
        manager = null;
        try {
            document.replace(0, document.getLength(), null, null);
        } catch (BadLocationException ble) {
            // ignore, it's irrelevant
        }
        document = null;
        model = null;
    }

    /**
     * Ends the compound mode of the undo manager and discards all edits.
     */
    private void endTestCase() {
        manager.endCompound();
        document.removeUndoableEditListener(manager);
        if (model != null) {
            model.removeUndoableEditListener(manager);
        } else {
            // Without a backing model, the document can be scrapped.
            try {
                document.replace(0, document.getLength(), null, null);
            } catch (BadLocationException ble) {
                // ignore, it's irrelevant
            }
        }
        manager.discardAllEdits();
        // Clear out the cached document instances to start fresh.
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    /**
     * Tests the QuietUndoManager with a simple DefaultStyledDocument and
     * no XAM-based model, to ensure manager has correct logic.
     */
    public void testWithoutModel() {
        document = new DefaultStyledDocument();
        manager = new QuietUndoManager(new UndoRedo.Manager());
        manager.setDocument(document);

        // The following test cases are documented in the undo/redo
        // specification (xml/www/specs/undo/index.html).
        // Each of these test cases should succeed individually.
        // Comment out one or more to test a particular case.

        // Case A: Initially the actions should be disabled.
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case B: Undo/redo of a model edit.
        viewModel();
        manager.undoableEditHappened(new UndoableEditEvent(this,
                new AbstractUndoableEdit()));
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case C: Undo/redo of a source edit.
        viewSource();
        try {
            document.insertString(0, ": case C1", null);
            document.insertString(0, ": case C2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case C2: case C1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case D, cannot test this in unit tests.

        // Case E, cannot test this in unit tests.

        // Case F: Undo/redo with a mix of document and model edits.
        viewSource();
        try {
            document.insertString(0, ": case F1", null);
            document.insertString(0, ": case F2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        manager.undoableEditHappened(new UndoableEditEvent(this,
                new AbstractUndoableEdit()));
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case F2: case F1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case G, cannot test this in unit tests.

        // Case AA: Single document edit.
        viewSource();
        try {
            document.insertString(0, "a", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals("a", document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case AC: An open compound edit is properly closed.
        viewSource();
        try {
            document.insertString(0, ": case AC1", null);
            document.insertString(0, ": case AC2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        try {
            document.insertString(0, ": case AC3", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case AC3: case AC1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case AD: Switching views leaves undo history intact.
        viewSource();
        try {
            document.insertString(0, ": case AD1", null);
            document.insertString(0, ": case AD2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case AD2: case AD1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case AE: Open compound edit and switching views should close compound set.
        viewSource();
        try {
            document.insertString(0, ": case AE1", null);
            document.insertString(0, ": case AE2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case AE2: case AE1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        // Case AF: Document edit is undone, then replaced by model edit.
        viewSource();
        try {
            document.insertString(0, ": case AF1", null);
            document.insertString(0, ": case AF2", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        manager.undoableEditHappened(new UndoableEditEvent(this,
                new AbstractUndoableEdit() {
            private static final long serialVersionUID = 1L;

            public void redo() throws CannotRedoException {
                super.redo();
                try {
                    document.insertString(0, ": case AF3", null);
                } catch (BadLocationException ble) {
                    fail(ble.toString());
                }
            }

            public void undo() throws CannotUndoException {
                super.undo();
                try {
                    document.replace(0, 10, null, null);
                } catch (BadLocationException ble) {
                    fail(ble.toString());
                }
            }
        }));
        try {
            document.insertString(0, ": case AF3", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            assertEquals(": case AF3: case AF1",
                    document.getText(0, document.getLength()));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        cleanup();
    }

    /**
     * Tests the QuietUndoManager with a schema model and NetBeans editor
     * document, to ensure manager operates correctly in near-native
     * environment.
     */
    public void testWithModel() {
        try {
            model = Util.loadSchemaModel(Util.PO_XSD);
        } catch (Exception e) {
            fail(e.toString());
        }
        assertNotNull("failed to load SchemaModel", model);
        document = (AbstractDocument) model.getModelSource().getLookup().
                lookup(AbstractDocument.class);
        assertNotNull("ModelSource did not contain AbstractDocument", document);
        // We are most interested in the issues that BaseDocument creates.
        assertTrue(document instanceof BaseDocument);
        manager = new QuietUndoManager(new UndoRedo.Manager());
        manager.setDocument(document);

        // The following test cases are documented in the undo/redo
        // specification (xml/www/specs/undo/index.html).
        // Each of these test cases should succeed individually.
        // Comment out one or more to test a particular case.

        // Case A: Initially the actions should be disabled.
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case B: Undo/redo of a model edit.
        viewModel();
        model.startTransaction();
        Util.createGlobalElement(model, "elementB");
        model.endTransaction();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case C: Undo/redo of a source edit.
        viewSource();
        try {
            document.insertString(0, "<!-- C1 -->", null);
            document.insertString(0, "<!-- C2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case D, cannot test this in unit tests.

        // Case E, cannot test this in unit tests.

        // Case F: Undo/redo with a mix of document and model edits.
        viewSource();
        try {
            document.insertString(0, "<!-- F1 -->", null);
            document.insertString(0, "<!-- F2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        model.startTransaction();
        Util.createGlobalElement(model, "elementF1");
        model.endTransaction();
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case G, cannot test this in unit tests.

        // Case AA: Single document edit.
        viewSource();
        try {
            document.insertString(0, "\n", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case AC: An open compound edit is properly closed.
        viewSource();
        try {
            document.insertString(0, "<!-- AC1 -->", null);
            document.insertString(0, "<!-- AC2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        try {
            document.insertString(0, "<!-- AC3 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case AD: Switching views leaves undo history intact.
        viewSource();
        try {
            document.insertString(0, "<!-- AD1 -->", null);
            document.insertString(0, "<!-- AD2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case AE: Open compound edit and switching views should close compound set.
        viewSource();
        try {
            document.insertString(0, "<!-- AE1 -->", null);
            document.insertString(0, "<!-- AE2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case AF: Document edit is undone, then replaced by model edit.
        viewSource();
        try {
            document.insertString(0, "<!-- AF1 -->", null);
            document.insertString(0, "<!-- AF2 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        viewModel();
        model.startTransaction();
        Util.createGlobalElement(model, "elementAF");
        model.endTransaction();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        // Case AG: Model edit followed by document edit
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        model.startTransaction();
        Util.createGlobalElement(model, "elementAG1");
        model.endTransaction();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            document.insertString(0, "<!-- AG1 -->", null);
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewModel();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.redo();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        endTestCase();

        cleanup();
    }

    /**
     * Tests the conditions under which issue 83963 occurs.
     */
    public void testIssue83963() {
        try {
            model = Util.loadSchemaModel(Util.PO_XSD);
        } catch (Exception e) {
            fail(e.toString());
        }
        assertNotNull("failed to load SchemaModel", model);
        document = (AbstractDocument) model.getModelSource().getLookup().
                lookup(AbstractDocument.class);
        assertNotNull("ModelSource did not contain AbstractDocument", document);
        // We are most interested in the issues that BaseDocument creates.
        assertTrue(document instanceof BaseDocument);
        manager = new QuietUndoManager(new UndoRedo.Manager());
        manager.setDocument(document);

        String original = null;
        try {
            original = document.getText(0, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        viewModel();
        assertFalse(manager.canUndo());
        assertFalse(manager.canRedo());
        model.startTransaction();
        Util.createGlobalElement(model, "element83963");
        model.endTransaction();
        Schema schema = model.getSchema();
        boolean found = false;
        for (GlobalElement ge : schema.getElements()) {
            if (ge.getName().equals("element83963")) {
                found = true;
                break;
            }
        }
        assertTrue("failed to add global element", found);
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        viewSource();
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        try {
            int length = document.getLength();
            String text = document.getText(0, length);
            assertTrue(text.contains("element83963"));
            int mark = text.lastIndexOf("element83963");
            assertTrue(mark > 0 && mark < length);
            mark += "element83963\"/>".length();
            // Must create a document edit that overlaps the model edit,
            // and it must introduce a new DOM element (e.g. comment).
            // For example, the following two lines will not cause the error.
            //document.insertString(mark, "\n  \n", null);
            //assertEquals(length + 4, document.getLength());
            document.insertString(mark, "\n<!-- -->\n", null);
            assertEquals(length + 10, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(manager.canUndo());
        assertFalse(manager.canRedo());
        manager.undo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
        manager.undo();
        assertFalse(manager.canUndo());
        assertTrue(manager.canRedo());
        String actual = null;
        try {
            actual = document.getText(0, document.getLength());
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        assertTrue(actual.equals(original));
        manager.redo();
        assertTrue(manager.canUndo());
        assertTrue(manager.canRedo());
// XXX: uncomment the redo() to get the error seen in issue 83963
//        manager.redo();
        assertTrue(manager.canUndo());
// XXX: Once the above redo() is working again, uncomment this code.
//        assertFalse(manager.canRedo());
        // Note that the 'xsd:' namespace is not used in this schema.
        String expected =
                "  <!-- etc. -->\n" +
                "    <element name=\"element83963\"/>\n" +
// XXX: Once the above redo() is working again, uncomment this code.
//                "<!-- -->\n" +
//                "\n" +
                "</schema>";
        try {
            actual = document.getText(0, document.getLength());
            assertTrue(actual.endsWith(expected));
        } catch (BadLocationException ble) {
            fail(ble.toString());
        }
        endTestCase();

        cleanup();
    }

    /**
     * Simulate switching to the model view.
     */
    private void viewModel() {
        manager.endCompound();
        document.removeUndoableEditListener(manager);
        if (model != null) {
            try {
                // Sync any changes made to the document.
                model.sync();
            } catch (IOException ioe) {
                fail(ioe.toString());
            }
            // Ensure manager is not registered twice.
            model.removeUndoableEditListener(manager);
            model.addUndoableEditListener(manager);
        }
    }

    /**
     * Simulate switching to the source view.
     */
    private void viewSource() {
        if (model != null) {
            model.removeUndoableEditListener(manager);
        }
        // Ensure manager is not registered twice.
        document.removeUndoableEditListener(manager);
        document.addUndoableEditListener(manager);
        manager.beginCompound();
    }
}

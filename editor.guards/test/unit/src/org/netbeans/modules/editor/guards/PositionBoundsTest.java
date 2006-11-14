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
package org.netbeans.modules.editor.guards;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;
import junit.framework.TestCase;
import org.netbeans.api.editor.guards.Editor;
import org.netbeans.api.editor.guards.GuardUtils;
import org.netbeans.modules.editor.guards.GuardedSectionsImpl;
import org.openide.text.NbDocument;

/**
 *
 * @author Jan Pokorsky
 */
public class PositionBoundsTest extends TestCase {
    
    private Editor editor;
    private GuardedSectionsImpl guardsImpl;
    
    /** Creates a new instance of PositionBoundsTest */
    public PositionBoundsTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        this.editor = new Editor();
        this.guardsImpl = new GuardedSectionsImpl(this.editor);
        GuardUtils.initManager(this.editor, this.guardsImpl);
    }
    
    public void testCreatePositionBounds() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 2), bounds.getText());
        assertEquals("getText2", "ac", bounds.getText());
    }
    
    public void testChangesInPositionBounds() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        editor.doc.insertString(2, "b", null);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 3), bounds.getText());
        assertEquals("getText2", "abc", bounds.getText());
    }
    
    public void testSetText() throws BadLocationException {
        editor.doc.insertString(0, "_abcd", null);
        PositionBounds bounds = PositionBounds.create(1, 4, guardsImpl);
        // test position bounds content changes; doc="_abcd"; pb="abc"
        bounds.setText("xy");
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_xyd".length(), editor.doc.getLength());
    }
    
    public void testInsertionBeforeBounds() throws BadLocationException {
        editor.doc.insertString(0, "_xyd", null);
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        // test insertion before bounds; doc="_xyd"; pb="xy"
        editor.doc.insertString(1, "a", null);
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
    }
    
    public void testSetEmptyText() throws BadLocationException {
        editor.doc.insertString(0, "_axyd", null);
        PositionBounds bounds = PositionBounds.create(2, 4, guardsImpl);
        
        // test cleaning position bounds; doc="_axyd"; pb="xy"
        bounds.setText("");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 2, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        assertEquals("doc length", "_ad".length(), editor.doc.getLength());
        
        bounds.setText("xy");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
    }
    
    public void testDocumentClean() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        
        editor.doc.remove(0, editor.doc.getLength());
        assertEquals("start", 0, bounds.getBegin().getOffset());
        assertEquals("end", 0, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
    }
        
    public void testComplexSetText() throws BadLocationException {
        editor.doc.insertString(0, "_acd", null);
        
        // test create position bounds
        PositionBounds bounds = PositionBounds.create(1, 3, guardsImpl);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 2), bounds.getText());
        assertEquals("getText2", "ac", bounds.getText());
        
        // test document changes inside the position bounds
        editor.doc.insertString(2, "b", null);
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", editor.doc.getText(1, 3), bounds.getText());
        assertEquals("getText2", "abc", bounds.getText());
        
        // test position bounds content changes; doc="_abcd"; pb="abc"
        bounds.setText("xy");
        assertEquals("start", 1, bounds.getBegin().getOffset());
        assertEquals("end", 3, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_xyd".length(), editor.doc.getLength());
        
        // test insertion before bounds; doc="_xyd"; pb="xy"
        editor.doc.insertString(1, "a", null);
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
        
        // test cleaning position bounds; doc="_axyd"; pb="xy"
        bounds.setText("");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 2, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        assertEquals("doc length", "_ad".length(), editor.doc.getLength());
        
        // test cleaning document
        bounds.setText("xy");
        assertEquals("start", 2, bounds.getBegin().getOffset());
        assertEquals("end", 4, bounds.getEnd().getOffset());
        assertEquals("getText", "xy", bounds.getText());
        assertEquals("doc length", "_axyd".length(), editor.doc.getLength());
        
        editor.doc.remove(0, editor.doc.getLength());
        assertEquals("start", 0, bounds.getBegin().getOffset());
        assertEquals("end", 0, bounds.getEnd().getOffset());
        assertEquals("getText", "", bounds.getText());
        
    }
    
    public void testSetTextWithGuardMarks() throws Throwable {
        final Throwable[] ts = new Throwable[1];
        NbDocument.runAtomic(editor.doc, new Runnable() {
            public void run() {
                try {
                    doTestSetTextWithGuardMarks();
                } catch (Throwable ex) {
                    ts[0] = ex;
                }
            }
        });
        if (ts[0] != null) {
            throw ts[0];
        }
    }
    
    private void doTestSetTextWithGuardMarks() throws BadLocationException {
        StyledDocument doc = editor.doc;
        doc.insertString(0, "abcdef", null);
        Position p = doc.createPosition(1);
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        NbDocument.markGuarded(doc, 1, 3);
        assertTrue(GuardUtils.isGuarded(doc, 1));
        
        doc.insertString(1, "x", null);
        assertEquals(2, p.getOffset());
        assertTrue(GuardUtils.isGuarded(doc, 2));
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        
        doc.insertString(4, "x", null);
        assertEquals(2, p.getOffset());
        assertTrue(GuardUtils.isGuarded(doc, 4));
        assertTrue(GuardUtils.isGuarded(doc, 3));
        assertTrue(GuardUtils.isGuarded(doc, 5));
        assertTrue(GuardUtils.isGuarded(doc, 2));
        assertTrue(!GuardUtils.isGuarded(doc, 1));
        GuardUtils.dumpGuardedAttr(doc);
        
        doc.remove(1, 1);
        assertEquals(1, p.getOffset());
    }
    
}

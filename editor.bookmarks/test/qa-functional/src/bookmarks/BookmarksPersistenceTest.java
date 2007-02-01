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

package bookmarks;

import java.awt.event.KeyEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.lib.editor.bookmarks.api.BookmarkList;

/**
 * Test of typing at begining/end and other typing tests.
 *
 * @author Miloslav Metelka
 */
public class BookmarksPersistenceTest extends EditorBookmarksTestCase {
    
    public BookmarksPersistenceTest(String testMethodName) {
        super(testMethodName);
    }
    
    public void testPersistence() {
        int[] bookmarkLines = new int[] { 1, 7, 9 };
        
        openDefaultProject();
        
        openDefaultSampleFile();
        try {
            
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_F2, KeyEvent.CTRL_MASK);
            }
            
        } finally {
            closeFileWithDiscard();
        }
        
        openDefaultSampleFile();
        try {
            
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, bookmarkLines);
            
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testBookmarkMove() {
        int bookmarkLine = 14;
        int lineToDelete = 12;
        
        openDefaultProject();
        
        openDefaultSampleFile();
        try {
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            editorOper.setCaretPosition(getLineOffset(doc, bookmarkLine));
            txtOper.pushKey(KeyEvent.VK_F2, KeyEvent.CTRL_MASK);
            editorOper.setCaretPosition(getLineOffset(doc,lineToDelete));
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK);
            doc = txtOper.getDocument();
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, new int[]{bookmarkLine-1});
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testBookmarkMerge() {
        int[] bookmarkLines = new int[] { 9, 10, 11 };
        
        openDefaultProject();
        
        openDefaultSampleFile();
        try {
            
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_F2, KeyEvent.CTRL_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[0]));
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK);
            txtOper.pushKey(KeyEvent.VK_E, KeyEvent.CTRL_MASK);            
            BookmarkList bml = BookmarkList.get(doc);
            checkBookmarksAtLines(bml, new int[]{bookmarkLines[0]});
        } finally {
            closeFileWithDiscard();
        }
    }
    
    public void testNextBookmark() {
        int[] bookmarkLines = new int[] { 9, 10, 11 };
        int[] expectedLines = new int[] { 9, 10, 11, 9};
        
        openDefaultProject();
        
        openDefaultSampleFile();
        try {
            
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_F2, KeyEvent.CTRL_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc,1));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_F2);
                int j = expectedLines[i];
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                assertEquals("Caret is at bad location", j, actLine);                
            }           
        } finally {
            closeFileWithDiscard();
        }            
    }
    
    public void testPreviousBookmark() {
        int[] bookmarkLines = new int[] { 9, 10, 11 };
        int[] expectedLines = new int[] { 11, 10, 9, 11};
        
        openDefaultProject();
        
        openDefaultSampleFile();
        try {
            
            EditorOperator editorOper = getDefaultSampleEditorOperator();
            JEditorPaneOperator txtOper = editorOper.txtEditorPane();
            Document doc = txtOper.getDocument();
            for (int i = 0; i < bookmarkLines.length; i++) {
                editorOper.setCaretPosition(getLineOffset(doc, bookmarkLines[i]));
                txtOper.pushKey(KeyEvent.VK_F2, KeyEvent.CTRL_MASK);
            }
            editorOper.setCaretPosition(getLineOffset(doc,14));
            for (int i = 0; i < expectedLines.length; i++) {
                txtOper.pushKey(KeyEvent.VK_F2,KeyEvent.SHIFT_MASK);
                int j = expectedLines[i];
                int actLine = getLineIndex(doc, txtOper.getCaretPosition());
                assertEquals("Caret is at bad location", j, actLine);                
            }           
        } finally {
            closeFileWithDiscard();
        }
    }
    
    
    
    private void checkBookmarksAtLines(BookmarkList bml, int[] expectedLineIndexes) {
        assertEquals("Invalid bookmark count", expectedLineIndexes.length, bml.getBookmarkCount());
        for (int i = 0; i < expectedLineIndexes.length; i++) {
            int expectedLineIndex = expectedLineIndexes[i];
            int lineIndex = bml.getBookmark(i).getLineIndex();
            assertEquals("Bookmark line index " + lineIndex
                    + " differs from expected " + expectedLineIndex,
                    lineIndex,
                    expectedLineIndex
                    );
        }
    }
    
    
    private int getLineOffset(Document doc, int lineIndex) {
        Element root = doc.getDefaultRootElement();
        return root.getElement(lineIndex).getStartOffset();
    }
    
    private int getLineIndex(Document doc, int offset) {
        Element root = doc.getDefaultRootElement();
        return root.getElementIndex(offset);
    }
}

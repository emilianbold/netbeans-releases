/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package bookmarks;

import java.awt.event.KeyEvent;
import javax.swing.text.Document;
import javax.swing.text.Element;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
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
    
    private void checkBookmarksAtLines(BookmarkList bml, int[] expectedLineIndexes) {
        assertEquals("Invalid bookmark count", expectedLineIndexes.length, bml.getBookmarkCount());
        for (int i = 0; i < expectedLineIndexes.length; i++) {
            int expectedLineIndex = expectedLineIndexes[i];
            int lineIndex = bml.getBookmark(i).getLineIndex();
            assertEquals ("Bookmark line index " + lineIndex
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

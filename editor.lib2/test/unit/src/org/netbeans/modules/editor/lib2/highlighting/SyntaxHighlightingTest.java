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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ConcurrentModificationException;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import junit.textui.TestRunner;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.simple.SimplePlainTokenId;
import org.netbeans.lib.lexer.test.simple.SimpleTokenId;
import org.netbeans.spi.editor.highlighting.HighlightsChangeEvent;
import org.netbeans.spi.editor.highlighting.HighlightsChangeListener;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author vita
 */
public class SyntaxHighlightingTest extends NbTestCase {
    
    public static void main(String... args) {
        TestRunner.run(SyntaxHighlightingTest.class);
    }
    
    /** Creates a new instance of SyntaxHighlightingTest */
    public SyntaxHighlightingTest(String name) {
        super(name);
    }
    
    public void testSimple() {
        checkText("+ - / * public", SimpleTokenId.language());
    }
    
    public void testEmbedded() {
        checkText("/**//* this is a comment */", SimpleTokenId.language());
    }
    
    public void testComplex() {
        checkText(
            "public       /**/ +/-  private /** hello */ something /* this is a comment */ \"hi hi hi\" xyz    ", 
            SimpleTokenId.language());
    }

    public void testNoPrologEpilogEmbedding() {
        checkText(
            "hello world 0-1-2-3-4-5-6-7-8-9-A-B-C-D-E-F      Ooops", 
            SimplePlainTokenId.language());
    }
    
    public void testConcurrentModifications() throws BadLocationException {
        Document doc = createDocument(SimpleTokenId.language(), "NetBeans NetBeans NetBeans");
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);
        
        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            try {
                hs.moveNext();
                fail("ConcurrentModificationException has not been thrown from moveNext()");
            } catch (ConcurrentModificationException e) {
                // pass
            }
        }        
        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            try {
                hs.getStartOffset();
                fail("ConcurrentModificationException has not been thrown from getStartOffset()");
            } catch (ConcurrentModificationException e) {
                // pass
            }
        }        
        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            try {
                hs.getEndOffset();
                fail("ConcurrentModificationException has not been thrown from getEndOffset()");
            } catch (ConcurrentModificationException e) {
                // pass
            }
        }        
        {
            HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
            assertTrue("There should be some highlights", hs.moveNext());

            // Modify the document
            doc.insertString(0, "Hey", SimpleAttributeSet.EMPTY);

            try {
                hs.getAttributes();
                fail("ConcurrentModificationException has not been thrown from getAttributes()");
            } catch (ConcurrentModificationException e) {
                // pass
            }
        }        
    }

    public void testEvents() throws BadLocationException {
        final String text = "Hello !";
        Document doc = createDocument(SimpleTokenId.language(), text);
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);
        L listener = new L();
        layer.addHighlightsChangeListener(listener);
        
        assertHighlights(
            TokenHierarchy.create(text, SimpleTokenId.language()).tokenSequence(), 
            layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE), 
            true, 
            ""
        );
        
        assertEquals("There should be no events", 0, listener.eventsCnt);
        
        final String addedText = "World";
        doc.insertString(6, addedText, SimpleAttributeSet.EMPTY);
        
        assertEquals("Wrong number of events", 1, listener.eventsCnt);
        assertTrue("Wrong change start offset", 6 >= listener.lastStartOffset);
        assertTrue("Wrong change end offset", 6 + addedText.length() <= listener.lastEndOffset);
    }
    
    private void checkText(String text, Language<? extends TokenId> lang) {
        System.out.println("Checking text: '" + text + "'\n");
        Document doc = createDocument(lang, text);
        SyntaxHighlighting layer = new SyntaxHighlighting(doc);

        HighlightsSequence hs = layer.getHighlights(Integer.MIN_VALUE, Integer.MAX_VALUE);
        TokenHierarchy<Void> tokens = TokenHierarchy.create(text, lang);
        assertHighlights(tokens.tokenSequence(), hs, true, "");
        assertFalse("Unexpected highlights at the end of the sequence", hs.moveNext());
        System.out.println("------------------------\n");
    }
    
    private Document createDocument(Language lang, String text) {
        try {
            DefaultStyledDocument doc = new DefaultStyledDocument();
            doc.putProperty(Language.class, lang);
            doc.insertString(0, text, SimpleAttributeSet.EMPTY);
            return doc;
        } catch (BadLocationException e) {
            fail(e.getMessage());
            return null;
        }
    }
    
    private void assertHighlights(TokenSequence<? extends TokenId> ts, HighlightsSequence hs, boolean moveHs, String indent) {
        while (ts.moveNext()) {
            boolean hasHighlight;
            if (moveHs) {
                hasHighlight = hs.moveNext();
            } else {
                hasHighlight = moveHs = true;
            }
            assertTrue("Wrong number of highlights", hasHighlight);
            
            System.out.println(indent + "Token    : <" + 
                ts.offset() + ", " + 
                (ts.offset() + ts.token().length()) + ", '" + 
                ts.token().text() + "', " + 
                ts.token().id().name() + ">");
            
            TokenSequence<? extends TokenId> embeddedSeq = ts.embedded();
            if (embeddedSeq == null) {
                System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                // XXX: compare attributes as well
            } else {
                int prologueLength = embeddedPrologLength(ts, embeddedSeq);
                int epilogLength = embeddedEpilogLength(ts, embeddedSeq);
                
                if (prologueLength != -1 && epilogLength != -1) {
                    if (prologueLength > 0) {
                        System.out.println(indent + "Prolog   : <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                        assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                        assertEquals("Wrong ending offset", ts.offset() + prologueLength, hs.getEndOffset());
                        // XXX: compare attributes as well
                    }
                    
                    assertHighlights(ts.embedded(), hs, prologueLength > 0, indent + "  ");
                    
                    if (epilogLength > 0) {
                        assertTrue("Wrong number of highlights", hs.moveNext());
                        System.out.println(indent + "Epilog   : <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                        
                        assertEquals("Wrong starting offset", ts.offset() + ts.token().length() - epilogLength, hs.getStartOffset());
                        assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                        // XXX: compare attributes as well
                    }
                } else {
                    System.out.println(indent + "Highlight: <" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
                    assertEquals("Wrong starting offset", ts.offset(), hs.getStartOffset());
                    assertEquals("Wrong ending offset", ts.offset() + ts.token().length(), hs.getEndOffset());
                    // XXX: compare attributes as well
                }
            }
        }
    }
    
    private int embeddedPrologLength(
        TokenSequence<? extends TokenId> embeddingSeq, 
        TokenSequence<? extends TokenId> embeddedSeq) 
    {
        if (embeddedSeq.moveFirst()) {
            return embeddedSeq.offset() - embeddingSeq.offset();
        } else {
            return -1;
        }
    }
    
    private int embeddedEpilogLength(
        TokenSequence<? extends TokenId> embeddingSeq, 
        TokenSequence<? extends TokenId> embeddedSeq) 
    {
        if (embeddedSeq.moveLast()) {
            return (embeddingSeq.offset() + embeddingSeq.token().length()) - (embeddedSeq.offset() + embeddedSeq.token().length());
        } else {
            return -1;
        }
    }

    private void dumpSequence(HighlightsSequence hs) {
        System.out.println("Dumping sequence: " + hs + " {");
        while(hs.moveNext()) {
            System.out.println("<" + hs.getStartOffset() + ", " + hs.getEndOffset() + ">");
        }
        System.out.println("} End of sequence: " + hs + " dump ------------");
    }

    private static final class L implements HighlightsChangeListener {
        public int eventsCnt = 0;
        public int lastStartOffset;
        public int lastEndOffset;
        
        public void highlightChanged(HighlightsChangeEvent event) {
            eventsCnt++;
            lastStartOffset = event.getStartOffset();
            lastEndOffset = event.getEndOffset();
        }
    } // End of L class
}

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

package org.netbeans.modules.editor.java;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.java.BracketCompletion;


/**
 * Test java bracket completion.
 *
 * @autor Miloslav Metelka
 */
public class JavaBracketCompletionUnitTest extends JavaBaseDocumentUnitTestCase {
    
    public JavaBracketCompletionUnitTest(String testMethodName) {
        super(testMethodName);
    }
    
    // ------- Tests for completion of right parenthesis ')' -------------
    
    public void testRightParenSimpleMethodCall() {
        setLoadDocumentText("m()|)");
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenSwingInvokeLaterRunnable() {
        setLoadDocumentText("SwingUtilities.invokeLater(new Runnable()|))");
        assertTrue(isSkipRightParen());
    }

    public void testRightParenSwingInvokeLaterRunnableRun() {
        setLoadDocumentText(
            "SwingUtilities.invokeLater(new Runnable() {\n"
          + "    public void run()|)\n"
          + "})"  
            
        );
        assertTrue(isSkipRightParen());
    }
    
    public void testRightParenIfMethodCall() {
        setLoadDocumentText(
            " if (a()|) + 5 > 6) {\n"
          + " }"
        );
        assertTrue(isSkipRightParen());
    }

    public void testRightParenNoSkipNonBracketChar() {
        setLoadDocumentText("m()| ");
        assertFalse(isSkipRightParen());
    }

    public void testRightParenNoSkipDocEnd() {
        setLoadDocumentText("m()|");
        assertFalse(isSkipRightParen());
    }

    
    // ------- Tests for completion of right brace '}' -------------
    
    public void testAddRightBraceIfLeftBrace() {
        setLoadDocumentText("if (true) {|");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceWhiteSpace() {
        setLoadDocumentText("if (true) { \t|\n");
        assertTrue(isAddRightBrace());
    }
    
    public void testAddRightBraceIfLeftBraceLineComment() {
        setLoadDocumentText("if (true) { // line-comment|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceBlockComment() {
        setLoadDocumentText("if (true) { /* block-comment */|\n");
        assertTrue(isAddRightBrace());
    }

    public void testAddRightBraceIfLeftBraceAlreadyPresent() {
        setLoadDocumentText(
            "if (true) {|\n"
          + "}"
        );
        assertFalse(isAddRightBrace());
    }

    public void testAddRightBraceCaretInComment() {
        setLoadDocumentText(
            "if (true) { /* in-block-comment |\n"
        );
        assertFalse(isAddRightBrace());
    }
    
    public void testSimpleAdditionOfOpeningParenthesisAfterWhile () throws Exception {
        setLoadDocumentText (
            "while |"
        );
        typeChar('(');
        assertDocumentTextAndCaret ("Even a closing ')' should be added", 
            "while (|)"
        );
    }


    // ------- Private methods -------------
    
    private void typeChar(char ch) throws Exception {
        int pos = getCaretOffset();
        getDocument ().insertString(pos, String.valueOf(ch), null);
        BracketCompletion.charInserted(getDocument(), pos, getCaret(), ch);
    }
    
    private boolean isSkipRightParen() {
        return isSkipRightBracketOrParen(true);
    }
    
    private boolean isSkipRightBracket() {
        return isSkipRightBracketOrParen(false);
    }
    
    private boolean isSkipRightBracketOrParen(boolean parenthesis) {
        TokenID bracketTokenId = parenthesis
        ? JavaTokenContext.RPAREN
        : JavaTokenContext.RBRACKET;
        
        try {
            return BracketCompletion.isSkipClosingBracket(getDocument(),
            getCaretOffset(), bracketTokenId);
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }
    
    private boolean isAddRightBrace() {
        try {
            return BracketCompletion.isAddRightBrace(getDocument(),
            getCaretOffset());
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail();
            return false; // should never be reached
        }
    }

}

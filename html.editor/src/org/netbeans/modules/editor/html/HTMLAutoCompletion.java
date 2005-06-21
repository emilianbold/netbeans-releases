/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.html;


import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Settings;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.HTMLSyntaxSupport;
import org.netbeans.editor.ext.html.HTMLTokenContext;



/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean.
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
class HTMLAutoCompletion {
    
    //this semaphore signals that the HTML tag attribute quotes was inserted
    //during last charInserted call
    private static boolean equalsSignInserted = false;
    
    /**
     * A hook method called after a character was inserted into the
     * document. The function checks for special characters for
     * completion ()[]'"{} and other conditions and optionally performs
     * changes to the doc and or caret (complets braces, moves caret,
     * etc.)
     * @param doc the document where the change occurred
     * @param dotPos position of the character insertion
     * @param caret caret
     * @param ch the character that was inserted
     * @throws BadLocationException if dotPos is not correct
     */
    static void charInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        if (doc.getSyntaxSupport() instanceof ExtSyntaxSupport) {
            if (ch == '=') {
                completeQuotes(doc, dotPos, caret, ch);
            } else if(ch == '"') {
                //user has pressed quotation mark
                handleQuotationMark(doc, dotPos, caret, ch);
            } else {
                //user has pressed a key so I need to cancel the "quotation consuming mode"
                equalsSignInserted = false;
            }
        }
    }
    
    private static void handleQuotationMark(BaseDocument doc, int dotPos, Caret caret, char ch) throws BadLocationException{
        if(equalsSignInserted) {
            //remove the quotation mark
            doc.remove(dotPos,1);
            caret.setDot(dotPos);
            equalsSignInserted = true;
        } else {
            //test whether the user typed an ending quotation in the attribute value
            TokenItem token = ((HTMLSyntaxSupport)doc.getSyntaxSupport()).getTokenChain(dotPos-1, dotPos);
            if(token != null &&
                    token.getTokenID() == HTMLTokenContext.VALUE) {
                //test if the user inserted the qutation in an attribute value and before
                //an already existing end quotation
                //the text looks following in such a situation:
                //
                //  atrname="abcd|"", where offset of the | == dotPos
                if("\"\"".equals(doc.getText(dotPos, 2))) {
                    doc.remove(dotPos,1);
                    caret.setDot(dotPos+1);
                }
            }
        }
        //reset the semaphore
        equalsSignInserted = false;
    }
    
    private static void completeQuotes(BaseDocument doc, int dotPos, Caret caret, char ch) throws BadLocationException{
        TokenItem token = ((HTMLSyntaxSupport)doc.getSyntaxSupport()).getTokenChain(dotPos-1, dotPos);
        //the given dotpos states the offset where the char was inserted, I need the offset after the new char
        int dotPosAfterTypedChar = dotPos + 1;
        if(token != null &&
                token.getTokenID() == HTMLTokenContext.ARGUMENT) {
            doc.insertString( dotPosAfterTypedChar, "\"\"" , null);
            caret.setDot(dotPosAfterTypedChar + 1);
            equalsSignInserted = true;
        }
    }
    
    
}

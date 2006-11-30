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

package org.netbeans.modules.editor.html;


import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;

import org.netbeans.api.html.lexer.HTMLTokenId;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.ExtSyntaxSupport;

/**
 * This static class groups the whole aspect of bracket
 * completion. It is defined to clearly separate the functionality
 * and keep actions clean.
 * The methods of the class are called from different actions as
 * KeyTyped, DeletePreviousChar.
 */
class HTMLAutoCompletion {
    
    //an index of lastly completed equals sign
    private static int  equalsSignInsertedOffset = -1;
    
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
     * @throws BadLocationException
     */
    static void charInserted(BaseDocument doc,
            int dotPos,
            Caret caret,
            char ch) throws BadLocationException {
        if (doc.getSyntaxSupport() instanceof ExtSyntaxSupport) {
            if (ch == '=') {
                completeQuotes(doc, dotPos, caret);
            } else if(ch == '"') {
                //user has pressed quotation mark
                handleQuotationMark(doc, dotPos, caret);
            } else {
                //user has pressed a key so I need to cancel the "quotation consuming mode"
                equalsSignInsertedOffset = -1;
            }
        }
    }
    
    //called when user deleted something in the document
    static void charDeleted(BaseDocument doc, int dotPos, Caret caret, char ch) {
        equalsSignInsertedOffset = -1;
    }
    
    private static void handleQuotationMark(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException {
        if(equalsSignInsertedOffset != -1) {
            //test whether the cursor is between completed quotations: attrname="|"
            //this situation can happen when user autocompletes ="|",
            //moves cursor somewhere else and type "
            if(dotPos == (equalsSignInsertedOffset + ("=\"".length()))) {
                //remove the quotation mark
                doc.remove(dotPos,1);
                caret.setDot(dotPos);
            }
            
        } else {
            //test whether the user typed an ending quotation in the attribute value
            doc.readLock();
            try {
                TokenHierarchy hi = TokenHierarchy.get(doc);
                TokenSequence ts = hi.tokenSequence();
                
                int diff = ts.move(dotPos);
                if(diff >= ts.token().length() || diff == Integer.MAX_VALUE) {
                    return; //no token found
                }
                
                Token token = ts.token();
                if(token.id() == HTMLTokenId.VALUE) {
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
            }finally {
                doc.readUnlock();
            }
        }
        //reset the semaphore
        equalsSignInsertedOffset = -1;
    }
    
    private static void completeQuotes(BaseDocument doc, int dotPos, Caret caret) throws BadLocationException{
        doc.readLock();
        try {
            TokenHierarchy hi = TokenHierarchy.get(doc);
            TokenSequence ts = hi.tokenSequence();
            
            int diff = ts.move(dotPos);
            if(diff >= ts.token().length() || diff == Integer.MAX_VALUE) {
                return; //no token found
            }
            
            Token token = ts.token();
            
            int dotPosAfterTypedChar = dotPos + 1;
            if(token != null &&
                    token.id() == HTMLTokenId.ARGUMENT) {
                doc.insertString( dotPosAfterTypedChar, "\"\"" , null);
                caret.setDot(dotPosAfterTypedChar + 1);
                //mark the last autocomplete position
                equalsSignInsertedOffset = dotPos;
            }
            
        }finally {
            doc.readUnlock();
        }
        
    }
    
    
}

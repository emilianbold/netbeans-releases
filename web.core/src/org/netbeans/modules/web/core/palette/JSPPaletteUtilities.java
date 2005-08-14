/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.palette;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.netbeans.modules.web.core.syntax.JspSyntaxSupport;
import org.netbeans.modules.web.core.syntax.JspTagTokenContext;


/**
 *
 * @author Libor Kotouc
 */
public final class JSPPaletteUtilities {
    
    public static int wrapTags(JspSyntaxSupport sup, int start, int end, BaseDocument doc) {
        
        try {
            TokenItem token = sup.getTokenChain(start, start + 1);

            if (token == null)
                return end;

            while (token.getOffset() < end) { // interested only in the tokens inside the body
                token = token.getNext();
                if (token == null)
                    break;
                if (token.getImage().startsWith("<") &&
                    token.getTokenID() == JspTagTokenContext.SYMBOL ||
                    token.getTokenID() == HTMLTokenContext.TAG_OPEN_SYMBOL
                   ) // it's '<' token
                {
                    int offset = token.getOffset();
                    doc.insertString(offset, "\n", null);   // insert a new-line before '<'
                    end++;  // remember new body end
                    token = sup.getTokenChain(offset + 1, offset + 2); // create new token chain reflecting changed document
                }
            }
            
        } catch (IllegalStateException ise) {
        } catch (BadLocationException ble) {
        }
        
        return end;
    }

    public static void insert(String s, JTextComponent target) 
    throws BadLocationException 
    {
        insert(s, target, true);
    }
    
    public static void insert(String s, JTextComponent target, boolean reformat) 
    throws BadLocationException 
    {
        //check whether we are not in a scriptlet 
        EditorUI eui = Utilities.getEditorUI(target);
        BaseDocument doc = eui.getDocument();
//        JspSyntaxSupport sup = (JspSyntaxSupport)(doc.getSyntaxSupport().get(JspSyntaxSupport.class));
//        int start = target.getCaret().getDot();
//        TokenItem token = sup.getTokenChain(start, start + 1);
//        if (token != null && token.getTokenContextPath().contains(JavaTokenContext.contextPath)) // we are in a scriptlet
//            return false;

        if (s == null)
            s = "";
        
        int start = insert(s, target, doc);
        
        if (reformat && start >= 0) {  // format the inserted text
            int end = start + s.length();
            Formatter f = doc.getFormatter();
            f.reformat(doc, start, end);
        }

//        if (select && start >= 0) { // select the inserted text
//            Caret caret = target.getCaret();
//            int current = caret.getDot();
//            caret.setDot(start);
//            caret.moveDot(current);
//            caret.setSelectionVisible(true);
//        }
        
    }
    
    private static int insert(String s, JTextComponent target, BaseDocument doc) 
    throws BadLocationException 
    {

        int start = -1;
        try {
            //at first, find selected text range
            Caret caret = target.getCaret();
            int p0 = Math.min(caret.getDot(), caret.getMark());
            int p1 = Math.max(caret.getDot(), caret.getMark());
            doc.remove(p0, p1 - p0);
            
            //replace selected text by the inserted one
            start = caret.getDot();
            doc.insertString(start, s, null);
        }
        catch (BadLocationException ble) {}
        
        return start;
    }

}

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

package org.netbeans.modules.web.core.palette;

import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.indent.Indent;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.jsp.lexer.JspTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Formatter;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.web.core.syntax.spi.JspContextInfo;
import org.netbeans.modules.web.jsps.parserapi.JspParserAPI;
import org.netbeans.modules.web.jsps.parserapi.PageInfo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author Libor Kotouc
 */
public final class JSPPaletteUtilities {
    
    //marekf: who calls this???? It seems the method is not used and was the only reason of having impl. dep. on web/jspsyntax, grrrr.
//    public static int wrapTags(int start, int end, BaseDocument doc) {
//        try {
//            TokenHierarchy th = TokenHierarchy.get(doc);
//            TokenSequence jspTs = th.tokenSequence();
//            if(jspTs.move(start) == Integer.MAX_VALUE) {
//                return end; //no token in sequence
//            }
//            Token token = jspTs.token();
//            //wrap JSP tags
//            while (token.offset(th) < end && jspTs.moveNext()) { // interested only in the tokens inside the body
//                token = jspTs.token();
//                if (token.text().toString().startsWith("<") && token.id() == JspTokenId.SYMBOL) {
//                    // it's '<' token
//                    int offset = token.offset(th);
//                    doc.insertString(offset, "\n", null);   // insert a new-line before '<'
//                    end++;  // remember new body end
//                }
//            }
//            //wrap HTML tags
//            TokenSequence htmlTs = th.tokenSequence(HTMLTokenId.language());
//            if(htmlTs == null || htmlTs.move(start) == Integer.MAX_VALUE) {
//                return end; //no token in sequence
//            }
//            while (token.offset(th) < end && htmlTs.moveNext()) { // interested only in the tokens inside the body
//                token = htmlTs.token();
//                if (token.text().toString().startsWith("<") && token.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
//                    // it's '<' token
//                    int offset = token.offset(th);
//                    doc.insertString(offset, "\n", null);   // insert a new-line before '<'
//                    end++;  // remember new body end
//                }
//            }
//        } catch (IllegalStateException ise) {
//            //ignore
//        } catch (BadLocationException ble) {
//            //ignore
//        }
//        
//        return end;
//    }

    public static void insert(String s, JTextComponent target) 
    throws BadLocationException 
    {
        insert(s, target, true);
    }
    
    public static void insert(String s, JTextComponent target, boolean reformat) 
    throws BadLocationException 
    {
        Document _doc = target.getDocument();
        if (_doc == null || !(_doc instanceof BaseDocument)) {
            return;
        }
        
        //check whether we are not in a scriptlet 
//        JspSyntaxSupport sup = (JspSyntaxSupport)(doc.getSyntaxSupport().get(JspSyntaxSupport.class));
//        int start = target.getCaret().getDot();
//        TokenItem token = sup.getTokenChain(start, start + 1);
//        if (token != null && token.getTokenContextPath().contains(JavaTokenContext.contextPath)) // we are in a scriptlet
//            return false;

        if (s == null)
            s = "";
        
        BaseDocument doc = (BaseDocument)_doc;
        Indent indent = Indent.get(doc);
        indent.lock();
        try {
            doc.atomicLock();
            try {
                int start = insert(s, target, _doc);
                if (reformat && start >= 0 && _doc instanceof BaseDocument) {
                    // format the inserted text
                    int end = start + s.length();
                    indent.reindent(start, end);
                }
            } finally {
                doc.atomicUnlock();
            }
        } finally {
            indent.unlock();
        }
        
    }
    
    private static int insert(String s, JTextComponent target, Document doc) 
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
    
    public static PageInfo.BeanData[] getAllBeans(JTextComponent target)
    {
        if(target!=null && target.getDocument() instanceof BaseDocument){
           BaseDocument doc =  (BaseDocument) target.getDocument();
           DataObject dobj = NbEditorUtilities.getDataObject(doc);
           FileObject fobj = (dobj != null) ? NbEditorUtilities.getDataObject(doc).getPrimaryFile() : null;
           if(fobj==null) return null;
           JspParserAPI.ParseResult result = JspContextInfo.getContextInfo (fobj).getCachedParseResult (doc, fobj, false, true);
           if(result==null) return null;
           return result.getPageInfo().getBeans();
        }
        return null;
       
    }
    public static boolean idExists(String id, PageInfo.BeanData[] beanData)
    {
        if(id!=null && beanData!=null)
        for (int i = 0; i < beanData.length; i++) {
            PageInfo.BeanData beanData1 = beanData[i];
            if(beanData1.getId().equals(id)) return true;
        }

        return false;
    }

}

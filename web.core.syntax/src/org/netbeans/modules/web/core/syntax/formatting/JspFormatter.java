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

package org.netbeans.modules.web.core.syntax.formatting;

import org.netbeans.modules.web.core.syntax.deprecated.JspMultiSyntax;
import org.netbeans.modules.web.core.syntax.deprecated.JspTagTokenContext;
import org.netbeans.modules.web.core.syntax.*;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.html.HTMLFormatter;
import org.netbeans.editor.ext.java.JavaFormatter;
import org.netbeans.editor.ext.java.JavaTokenContext;
import org.netbeans.modules.editor.java.JavaKit;

/**
 * Formatter for JSP files.
 * @author Tomasz.Slota@Sun.COM
 */

public class JspFormatter extends HTMLFormatter {
    private JavaFormatter jFormatter;
    
    /** Creates a new instance of HTMLFormater */
    public JspFormatter(Class kitClass) {
        super(kitClass);
        jFormatter = new JspJavaFormatter(JavaKit.class);
    }
    
    @Override public Writer reformat(final BaseDocument doc, final int startOffset, final int endOffset,
            final boolean indentOnly) throws BadLocationException, IOException{
        super.reformat(doc, startOffset, endOffset, indentOnly);
        
        List<ScriptletBlock> scripletBlocks = new LinkedList<ScriptletBlock>();
        ExtSyntaxSupport sup = getSyntaxSupport(doc);
        TokenItem token = sup.getTokenChain(startOffset, startOffset + 1);
        
        if (token == null){
            return null;
        }
        
        TokenItem lastNonWhiteScriptletToken = null;
        int scripletBlockStart = -1;
        int scripletBlockEnd = -1;
        
        do{
            if (token.getOffset() >= endOffset){
                break;
            }
            
            if (token.getTokenContextPath().contains(JavaTokenContext.contextPath)){
                
                if (token.getTokenID() != JavaTokenContext.WHITESPACE){
                    if (scripletBlockStart == -1){
                        scripletBlockStart = token.getOffset();
                    }
                    
                    lastNonWhiteScriptletToken = token;
                }
            } else{
                if (scripletBlockStart != -1){
                    
                    scripletBlockEnd = lastNonWhiteScriptletToken.getOffset()
                    + lastNonWhiteScriptletToken.getImage().length();
                    
                    scripletBlocks.add(new ScriptletBlock(doc, scripletBlockStart, scripletBlockEnd));
                    scripletBlockStart = -1;
                }
            }
            
            token = token.getNext();
        }
        while (token != null);
        
        for (ScriptletBlock sb : scripletBlocks){
            extFormatterReformat(doc, sb.getStart(), Math.min(sb.getEnd(), endOffset), indentOnly);
        }
        
        return null;
    }
    
    @Override protected void enterPressed(JTextComponent txtComponent, int dotPos) throws BadLocationException{
        BaseDocument doc = Utilities.getDocument(txtComponent);
        JspSyntaxSupport sup = new JspSyntaxSupport(doc);
        TokenItem token = sup.getItemAtOrBefore(dotPos);
        
        if (token.getTokenContextPath().contains(JavaTokenContext.contextPath)){
            try {
                extFormatterReformat(doc, dotPos, dotPos + 1, true);
                
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else{
            super.enterPressed(txtComponent, dotPos);
        }
    }
    
    @Override protected boolean isClosingTag(TokenItem token){
        return token != null && isJSPTag(token)
        && token.getPrevious().getImage().equals("</")
        || super.isClosingTag(token);
    }
    
    @Override protected boolean isOpeningTag(TokenItem token){
        return token != null && isJSPTag(token)
        && token.getPrevious().getImage().equals("<")
        || super.isOpeningTag(token);
    }
    
    @Override protected TokenItem getTagTokenEndingAtPosition(BaseDocument doc, int position) throws BadLocationException{
        TokenItem htmlEndingToken = super.getTagTokenEndingAtPosition(doc, position);
        
        if (htmlEndingToken != null){
            return htmlEndingToken;
        }
        
        if (position >= 0) {
            JspSyntaxSupport sup = (JspSyntaxSupport)doc.getSyntaxSupport().get(JspSyntaxSupport.class);
            TokenItem token = sup.getTokenChain(position, position + 1);
            
            if (token.getTokenID() == JspTagTokenContext.SYMBOL &&
                    token.getImage().equals(">")){ //NOI18N
                do {
                    token = token.getPrevious();
                }
                while (token != null && token.getTokenID() != JspTagTokenContext.TAG);
                
                return token;
            }
        }
        return null;
    }
    
    @Override protected int getTagEndOffset(TokenItem token){
        
        if (!isJSPTag(token)){
            return super.getTagEndOffset(token);
        }
        
        TokenItem t = token.getNext();
        
        while (t != null && !(t.getTokenID() == JspTagTokenContext.SYMBOL
                && (">".equals(t.getImage()) || "/>".equals(t.getImage())))){ //NOI18N
            t = t.getNext();
        }
        
        return t == null ? -1 : t.getOffset();
    }
    
    @Override protected int getOpeningSymbolOffset(TokenItem tknTag){
        if (!isJSPTag(tknTag)){
            return super.getOpeningSymbolOffset(tknTag);
        }
        
        TokenItem tkn = tknTag;
        
        do{
            tkn = tkn.getPrevious();
        }
        while(tkn != null && tkn.getTokenID() != JspTagTokenContext.SYMBOL);
        
        if (tkn != null){
            return tkn.getOffset();
        }
        
        return -1;
    }
    
    private boolean isJSPTag(TokenItem tagToken){
        return tagToken.getTokenID() == JspTagTokenContext.TAG;
    }
    
    @Override protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof JspMultiSyntax);
    }
    
    @Override protected void initFormatLayers() {
        addFormatLayer(new SwitchLayer());
    }
    
    @Override protected boolean isUnformattableToken(TokenItem token) {
        if (token.getTokenID() == JspTagTokenContext.COMMENT
                || token.getTokenID() == JspTagTokenContext.EOL){
            return true;
        }
        
        return super.isUnformattableToken(token);
    }
    
    private class SwitchLayer extends AbstractFormatLayer {
        
        public SwitchLayer() {
            super("Switch-line"); //NOI18N
        }
        
        public void format(FormatWriter fw) {
            int offset = fw.getOffset();
            
            JspSyntaxSupport sup = new JspSyntaxSupport( (BaseDocument)fw.getDocument() );
            try{
                TokenItem item = sup.getItemAtOrBefore(offset);
                
                if (item == null){
                    return;
                }
                
                TokenContextPath tcp = item.getTokenContextPath();
                if(tcp.contains(JavaTokenContext.contextPath)) {
                    if (JspUtils.getScriptingLanguage().equals("text/x-java")) { // NOI18N
                        jFormatter.format(fw);
                    }
                }
                
            }catch(BadLocationException e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private class ScriptletBlock{
        private Position posStart;
        private Position posEnd;
        
        public ScriptletBlock(BaseDocument doc, int start, int end) throws BadLocationException{
            posStart = doc.createPosition(start);
            posEnd = doc.createPosition(end);
        }
        
        public int getStart() {
            return posStart.getOffset();
        }
        
        public int getEnd() {
            return posEnd.getOffset();
        }
    }
}

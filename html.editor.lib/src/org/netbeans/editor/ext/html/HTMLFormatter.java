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

package org.netbeans.editor.ext.html;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.modules.editor.structure.formatting.TagBasedFormatter;

/**
 * Formatter for html files.
 * @author Tomasz.Slota@Sun.COM
 */

public class HTMLFormatter extends TagBasedFormatter {
    private static final String[] UNFORMATTABLE_TAGS = new String[]{"pre", "script", "code", "textarea"}; //NOI18N
    
    /** Creates a new instance of HTMLFormater */
    public HTMLFormatter(Class kitClass) {
	super(kitClass);
    }
    
    @Override protected boolean acceptSyntax(Syntax syntax) {
	return (syntax instanceof HTMLSyntax);
    }
    
    @Override protected ExtSyntaxSupport getSyntaxSupport(BaseDocument doc){
	return (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
    }
    
    @Override protected TokenItem getTagTokenEndingAtPosition(BaseDocument doc, int position) throws BadLocationException{
	if (position >= 0) {
	    HTMLSyntaxSupport sup = (HTMLSyntaxSupport)getSyntaxSupport(doc);
	    TokenItem token = sup.getTokenChain(position, position + 1);
	    
	    if (token.getTokenID() == HTMLTokenContext.TAG_CLOSE_SYMBOL &&
		    !token.getImage().endsWith("/>")){ //NOI18N
                
                do {
                    token = token.getPrevious();
                }
                while (token != null && !(isOpeningTag(token) || isClosingTag(token)));
                
                return token;
            }
	}
	return null;
    }
    
    @Override protected int getTagEndOffset(TokenItem token){
        TokenItem t = token.getNext();
        
        while (t != null && t.getTokenID() != HTMLTokenContext.TAG_CLOSE_SYMBOL){
            t = t.getNext();
        }
        
        return t == null ? -1 : t.getOffset();
    }
    
    @Override protected boolean isJustBeforeClosingTag(BaseDocument doc, int pos) throws BadLocationException{
        // a workaround for the difference with XML syntax support
        return super.isJustBeforeClosingTag(doc, pos + "</".length()); //NOI18N
    }
    
    @Override protected boolean isClosingTag(TokenItem token){
	return token != null && token.getTokenID() == HTMLTokenContext.TAG_CLOSE;
    }
    
    @Override protected boolean isOpeningTag(TokenItem token){
	return token != null && token.getTokenID() == HTMLTokenContext.TAG_OPEN;
    }
    
    @Override protected String extractTagName(TokenItem tknTag){
	return tknTag.getImage().trim();
    }
    
    @Override protected boolean areTagNamesEqual(String tagName1, String tagName2){
	return tagName1.equalsIgnoreCase(tagName2);
    }
    
    @Override protected int getOpeningSymbolOffset(TokenItem tknTag){
	TokenItem tkn = tknTag;
	
	do{
	    tkn = tkn.getPrevious();
	}
	while(tkn != null && tkn.getTokenID() != HTMLTokenContext.TAG_OPEN_SYMBOL);
	
	if (tkn != null){
	    return tkn.getOffset();
	}
	
	return -1;
    }
    
    @Override protected boolean isClosingTagRequired(BaseDocument doc, String tagName) {
	HTMLSyntaxSupport htmlsup = (HTMLSyntaxSupport)(doc.getSyntaxSupport().get(HTMLSyntaxSupport.class));
        DTD dtd = htmlsup.getDTD();
        
        if (dtd == null){
            // Unknown DTD, do not automatically close any tag
            return false;
        }
        
	Element elem = dtd.getElement(tagName.toUpperCase());
        
        if (elem == null){
            // automatically close unknown tags
            return true;
        }
        
	return !elem.isEmpty(); // close tag unless it is known to be empty
    }
    
    @Override protected boolean isUnformattableToken(TokenItem token) {
	
	if (token.getTokenID() == HTMLTokenContext.BLOCK_COMMENT){
	    return true;
	}
	
	return false;
    }
    
    @Override protected boolean isUnformattableTag(String tag) {
	for(int i = 0; i < UNFORMATTABLE_TAGS.length; i++) {
	    if(tag.equalsIgnoreCase(UNFORMATTABLE_TAGS[i])) {
		return true;
	    }
	}
	
	return false;
    }
}

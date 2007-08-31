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
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter;

/**
 * A lexer-based formatter for html files.
 * @author Tomasz.Slota@Sun.COM
 */

public class HTMLLexerFormatter extends TagBasedLexerFormatter {
    private static final String[] UNFORMATTABLE_TAGS = new String[]{"pre", "script", "code", "textarea"}; //NOI18N
    private final LanguagePath languagePath;
    
    /** Creates a new instance of HTMLFormater */
    public HTMLLexerFormatter(Class kitClass, LanguagePath languagePath) {
	super(kitClass);
        this.languagePath = languagePath;
    }
    
    @Override protected boolean acceptSyntax(Syntax syntax) {
	return (syntax instanceof HTMLSyntax);
    }
    
    @Override protected int getTagEndingAtPosition(TokenSequence tokenSequence, int position) throws BadLocationException{
	if (position >= 0) {
            int originalOffset = tokenSequence.offset();
            tokenSequence.move(position);
            tokenSequence.moveNext();
	    Token token = tokenSequence.token();
                    
	    if (token.id() == HTMLTokenId.TAG_CLOSE_SYMBOL &&
		    !token.text().toString().endsWith("/>")){ //NOI18N

                while (tokenSequence.movePrevious()){
                    int tokenOffset = tokenSequence.offset();
                    
                    if (isOpeningTag(tokenSequence, tokenOffset) 
                            || isClosingTag(tokenSequence, tokenOffset)){
                        int r = tokenSequence.offset();
                        tokenSequence.move(originalOffset);
                        return r;
                    }
                }
	    }
            
            tokenSequence.move(originalOffset);
	}
	return -1;
    }
    
    @Override protected int getTagEndOffset(TokenSequence tokenSequence, int tagStartOffset){
        int originalOffset = tokenSequence.offset();
        tokenSequence.move(tagStartOffset);
        tokenSequence.moveNext();
        boolean thereAreMoreTokens = true;
        
        while (thereAreMoreTokens && tokenSequence.token().id() != HTMLTokenId.TAG_CLOSE_SYMBOL){
            thereAreMoreTokens &= tokenSequence.moveNext();
        }
        
        int r = tokenSequence.offset();
        tokenSequence.move(originalOffset);
        return thereAreMoreTokens ? r : -1;
    }
    
    @Override protected boolean isJustBeforeClosingTag(TokenSequence tokenSequence, int tagTokenOffset) throws BadLocationException{
        // a workaround for the difference with XML syntax support
        return super.isJustBeforeClosingTag(tokenSequence, tagTokenOffset + "</".length()); //NOI18N
    }
    
    @Override protected boolean isClosingTag(TokenSequence tokenSequence, int tagTokenOffset){
        Token token = getTokenAtOffset(tokenSequence, tagTokenOffset);
	return token != null && token.id() == HTMLTokenId.TAG_CLOSE;
    }
    
    @Override protected boolean isOpeningTag(TokenSequence tokenSequence, int tagTokenOffset){
        Token token = getTokenAtOffset(tokenSequence, tagTokenOffset);
	return token != null && token.id() == HTMLTokenId.TAG_OPEN;
    }
    
    @Override protected String extractTagName(TokenSequence tokenSequence, int tagTokenOffset){
	return getTokenAtOffset(tokenSequence, tagTokenOffset).text().toString().trim();
    }
    
    @Override protected boolean areTagNamesEqual(String tagName1, String tagName2){
	return tagName1.equalsIgnoreCase(tagName2);
    }
    
    @Override protected int getOpeningSymbolOffset(TokenSequence tokenSequence, int tagTokenOffset){
	int originalOffset = tokenSequence.offset();
        tokenSequence.move(tagTokenOffset);
        boolean thereAreMoreTokens = true;
	
	do{
	    thereAreMoreTokens = tokenSequence.movePrevious();
	}
	while(thereAreMoreTokens && tokenSequence.token().id() != HTMLTokenId.TAG_OPEN_SYMBOL);
	
	if (thereAreMoreTokens){
            int r = tokenSequence.offset();
            tokenSequence.move(originalOffset);
	    return r;
	}
	
        tokenSequence.move(originalOffset);
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
    
    @Override protected boolean isUnformattableToken(TokenSequence tokenSequence, int tagTokenOffset) {
	Token token = getTokenAtOffset(tokenSequence, tagTokenOffset);
        
	if (token.id() == HTMLTokenId.BLOCK_COMMENT){
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

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }
}

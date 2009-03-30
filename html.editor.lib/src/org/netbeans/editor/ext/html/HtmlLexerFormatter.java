/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.editor.ext.html;

import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HtmlTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.dtd.DTD;
import org.netbeans.editor.ext.html.dtd.DTD.Element;
import org.netbeans.modules.editor.structure.formatting.TagBasedLexerFormatter;
import org.netbeans.modules.editor.structure.formatting.JoinedTokenSequence;

/**
 * A lexer-based formatter for html files.
 * @author Tomasz.Slota@Sun.COM
 */

public class HtmlLexerFormatter extends TagBasedLexerFormatter {
    /** 
     * Setting this flag in document property will make the HTML formatter act
     * as the top-level language formatter. It is useful for Ruby and PHP editors.
     * If this mechanism is used HTML formatter must be always used first.
     */
    
    public static final String HTML_FORMATTER_ACTS_ON_TOP_LEVEL = "HTML_FORMATTER_ACTS_ON_TOP_LEVEL"; //NOI18N
    private static final String[] UNFORMATTABLE_TAGS = new String[]{"pre", "script", "code", "textarea"}; //NOI18N
    private final LanguagePath languagePath;
    
    /** Creates a new instance of HTMLFormater */
    public HtmlLexerFormatter(LanguagePath languagePath) {
        this.languagePath = languagePath;
    }
    
    @Override protected int getTagEndingAtPosition(JoinedTokenSequence JoinedTokenSequence, int position) throws BadLocationException{
	if (position >= 0) {
            int originalOffset = JoinedTokenSequence.offset();
            JoinedTokenSequence.move(position);
            JoinedTokenSequence.moveNext();
	    Token token = JoinedTokenSequence.token();
                    
	    if (token.id() == HtmlTokenId.TAG_CLOSE_SYMBOL &&
		    !token.text().toString().endsWith("/>")){ //NOI18N

                while (JoinedTokenSequence.movePrevious()){
                    int tokenOffset = JoinedTokenSequence.offset();
                    
                    if (isOpeningTag(JoinedTokenSequence, tokenOffset) 
                            || isClosingTag(JoinedTokenSequence, tokenOffset)){
                        int r = JoinedTokenSequence.offset();
                        JoinedTokenSequence.move(originalOffset);
                        JoinedTokenSequence.moveNext();
                        return r;
                    }
                }
	    }
            
            JoinedTokenSequence.move(originalOffset);
            JoinedTokenSequence.moveNext();
	}
	return -1;
    }
    
    @Override protected int getTagEndOffset(JoinedTokenSequence JoinedTokenSequence, int tagStartOffset){
        int originalOffset = JoinedTokenSequence.offset();
        JoinedTokenSequence.move(tagStartOffset);
        JoinedTokenSequence.moveNext();
        boolean thereAreMoreTokens = true;
        
        while (thereAreMoreTokens && JoinedTokenSequence.token().id() != HtmlTokenId.TAG_CLOSE_SYMBOL){
            thereAreMoreTokens &= JoinedTokenSequence.moveNext();
        }
        
        int r = JoinedTokenSequence.offset() + JoinedTokenSequence.token().length();
        JoinedTokenSequence.move(originalOffset);
        JoinedTokenSequence.moveNext();
        return thereAreMoreTokens ? r : -1;
    }
    
    @Override protected boolean isJustBeforeClosingTag(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset) throws BadLocationException{
        // a workaround for the difference with XML syntax support
        return super.isJustBeforeClosingTag(JoinedTokenSequence, tagTokenOffset + "</".length()); //NOI18N
    }
    
    @Override protected boolean isClosingTag(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset){
        Token token = getTokenAtOffset(JoinedTokenSequence, tagTokenOffset);
	return token != null && token.id() == HtmlTokenId.TAG_CLOSE;
    }
    
    @Override protected boolean isOpeningTag(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset){
        Token token = getTokenAtOffset(JoinedTokenSequence, tagTokenOffset);
	return token != null && token.id() == HtmlTokenId.TAG_OPEN;
    }
    
    @Override protected String extractTagName(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset){
	return getTokenAtOffset(JoinedTokenSequence, tagTokenOffset).text().toString().trim();
    }
    
    @Override protected boolean areTagNamesEqual(String tagName1, String tagName2){
	return tagName1.equalsIgnoreCase(tagName2);
    }
    
    @Override protected int getOpeningSymbolOffset(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset){
	int originalOffset = JoinedTokenSequence.offset();
        JoinedTokenSequence.move(tagTokenOffset);
        boolean thereAreMoreTokens = true;
	
	do{
	    thereAreMoreTokens = JoinedTokenSequence.movePrevious();
	}
	while(thereAreMoreTokens && JoinedTokenSequence.token().id() != HtmlTokenId.TAG_OPEN_SYMBOL);
	
	if (thereAreMoreTokens){
            int r = JoinedTokenSequence.offset();
            JoinedTokenSequence.move(originalOffset);
            JoinedTokenSequence.moveNext();
	    return r;
	}
	
        JoinedTokenSequence.move(originalOffset);
        JoinedTokenSequence.moveNext();
	return -1;
    }
    
    @Override protected boolean isClosingTagRequired(BaseDocument doc, String tagName) {
	HtmlSyntaxSupport htmlsup = HtmlSyntaxSupport.get(doc);
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
    
    @Override protected boolean isUnformattableToken(JoinedTokenSequence JoinedTokenSequence, int tagTokenOffset) {
	Token token = getTokenAtOffset(JoinedTokenSequence, tagTokenOffset);
        
	if (token.id() == HtmlTokenId.BLOCK_COMMENT){
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
    
    @Override protected boolean isTopLevelLanguage(BaseDocument doc) {
        return super.isTopLevelLanguage(doc) || doc.getProperty(HTML_FORMATTER_ACTS_ON_TOP_LEVEL) != null;
    }

    protected LanguagePath supportedLanguagePath() {
        return languagePath;
    }
}

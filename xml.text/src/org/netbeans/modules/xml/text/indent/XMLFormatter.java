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
package org.netbeans.modules.xml.text.indent;

import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.structure.formatting.TagBasedFormatter;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;

import org.netbeans.modules.xml.text.syntax.javacc.lib.JJEditorSyntax;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class XMLFormatter extends TagBasedFormatter {
    
    private static final String TAG_OPENING_PREFIX = "<"; //NOI18N
    private static final String TAG_CLOSING_PREFIX = "</"; //NOI18N
    private static final String TAG_CLOSED_SUFFIX = "/>"; //NOI18N
    private static final String TAG_CLOSING_SUFFIX = "/>"; //NOI18N
    
    //at least one character
    private static final Pattern VALID_TAG_NAME = Pattern.compile("[\\w+|-]*"); // NOI18N
    
    private static final int WORKUNITS_MAX = 100;
    
    public XMLFormatter(Class kitClass) {
        super(kitClass);
    }
    
    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new XMLFormatSupport(fw);
    }
    
    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof JJEditorSyntax);
    }
    
    protected void initFormatLayers() {
        addFormatLayer(new StripEndWhitespaceLayer());
    }
    
    protected String extractTagName(TokenItem tknTag){
        
        String tagImage = tknTag.getImage();
        int startIndex = -1;
        
        if (isOpeningTag(tknTag)){
            startIndex = TAG_OPENING_PREFIX.length();
        } else if (isClosingTag(tknTag)){
            startIndex = TAG_CLOSING_PREFIX.length();
        }
        
        if (startIndex >= 0){
            String tagName = tagImage.substring(startIndex);
            return tagName;
        }
        
        return null;
    }
    
    @Override protected boolean isOpeningTag(TokenItem token){
        return token != null
                && token.getTokenID() == XMLTokenIDs.TAG
                && token.getImage().startsWith(TAG_OPENING_PREFIX)
                && !token.getImage().startsWith(TAG_CLOSING_PREFIX);
    }
    
    @Override protected boolean isClosingTag(TokenItem token){
        return token != null
                && token.getTokenID() == XMLTokenIDs.TAG
                && token.getImage().startsWith(TAG_CLOSING_PREFIX);
    }
    
    @Override protected int getTagEndOffset(TokenItem token){
        TokenItem t = token.getNext();
        
        while (t != null && t.getTokenID() != XMLTokenIDs.TAG){
            t = t.getNext();
        }
        
        return t == null ? -1 : t.getOffset();
    }
    
    @Override protected ExtSyntaxSupport getSyntaxSupport(BaseDocument doc){
        return (XMLSyntaxSupport)(doc.getSyntaxSupport().get(XMLSyntaxSupport.class));
    }
    
    @Override protected boolean areTagNamesEqual(String tagName1, String tagName2){
        return tagName1.equals(tagName2);
    }
    
    @Override protected boolean isClosingTagRequired(BaseDocument doc, String tagName){
        return true;
    }
    
    @Override protected int getOpeningSymbolOffset(TokenItem tknTag){
        return tknTag.getOffset();
    }
    
    @Override protected TokenItem getTagTokenEndingAtPosition(BaseDocument doc, int position) throws BadLocationException{
        if (position >= 0) {
            ExtSyntaxSupport sup = getSyntaxSupport(doc);
            TokenItem token = sup.getTokenChain(position, position + 1);
            
            if (token.getTokenID() == XMLTokenIDs.TAG &&
                    token.getImage().equals(">")){ //NOI18N
                do {
                    token = token.getPrevious();
                }
                while (token != null && !isOpeningTag(token) && !isClosingTag(token));
                
                return token;
            }
        }
        return null;
    }
    
    @Override protected boolean isUnformattableToken(TokenItem token) {
        
        if (token.getTokenID() == XMLTokenIDs.BLOCK_COMMENT
                || token.getTokenID() == XMLTokenIDs.CDATA_SECTION){
            return true;
        }
        
        return false;
    }
    
    @Override protected boolean isUnformattableTag(String tag) {
        return false;
    }
    
    public class StripEndWhitespaceLayer extends AbstractFormatLayer {
        
        public StripEndWhitespaceLayer() {
            super("xml-strip-whitespace-at-line-end-layer"); // NOI18N
        }
        
        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new XMLFormatSupport(fw);
        }
        
        public void format(FormatWriter fw) {
            XMLFormatSupport xfs = (XMLFormatSupport)createFormatSupport(fw);
            
            FormatTokenPosition pos = xfs.getFormatStartPosition();
            
            if ( (xfs.isLineStart(pos) == false) ||
                    xfs.isIndentOnly() ) { // don't do anything
                
            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    pos = xfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = xfs.getNextPosition(pos);
                    }
                }
            }
        }
    }
}

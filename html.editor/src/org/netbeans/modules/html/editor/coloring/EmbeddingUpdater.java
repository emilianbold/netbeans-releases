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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.html.editor.coloring;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxElement.TagAttribute;
import org.netbeans.editor.ext.html.parser.SyntaxParserListener;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 * Listens on HTML parser changes and updates embedding of nested languages accordingly.
 *
 * @author Marek.Fukala@Sun.com
 */
public class EmbeddingUpdater implements SyntaxParserListener {
    
    private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
    private static final String JAVASCRIPT_HREF_PREFIX = "javascript:"; //NOI18N
    
    //XXX update mimetype once Hanz fixes the mimetype in CSS editor module
    private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N
    private static final String CSS_INLINED_MIMETYPE = "text/x-css-inlined"; //NOI18N
    
    private static final String CSS_SCRIPT_TAG_NAME = "style"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(EmbeddingUpdater.class.getName());
    
    private final Document doc;
    
    private int scriptStart = -1;
    private int styleStart = -1;
    
    private boolean embeddingChanged;
    
    private LanguagePath languagePath;
    
    public EmbeddingUpdater(Document doc) {
        this.doc = doc;
        
        //hack
        String topLevelLanguageMimeType = NbEditorUtilities.getMimeType(doc);
        if(topLevelLanguageMimeType == null) {
            throw new IllegalArgumentException("Cannot determine document mimetype " + doc);
        }
        
        Language lang = Language.find(topLevelLanguageMimeType);
        if(lang == null) {
            throw new IllegalArgumentException("Cannot find language " + topLevelLanguageMimeType);
        }
        
        if("text/html".equals(topLevelLanguageMimeType)) {
            languagePath = LanguagePath.get(lang);
        } else if("text/x-jsp".equals(topLevelLanguageMimeType)) {
            languagePath = LanguagePath.get(LanguagePath.get(lang), Language.find("text/html"));
        }
        //eof hack
        
    }
    
    public void parsingFinished(List<SyntaxElement> elements) {
        embeddingChanged = false;
        for(SyntaxElement sel : elements) {
            if(sel.getType() == SyntaxElement.TYPE_TAG) {
                startTag((SyntaxElement.Tag)sel);
            } else if(sel.getType() == SyntaxElement.TYPE_ENDTAG) {
                endTag((SyntaxElement.Named)sel);
            }
        }
        if(embeddingChanged) {
            //tell Hanz the document embedding has changed
            //remove this once Hanz listens on the document token hierarchy
            //instead on the document itself
            //            ParserManager.get(doc).reparse();
        }
    }
    
    private void startTag(SyntaxElement.Tag sel) {
        if(CSS_SCRIPT_TAG_NAME.equalsIgnoreCase(sel.getName()) && declaresCSS(sel)) {
            styleStart = sel.getElementOffset() + sel.getElementLength();
        } else if("a".equalsIgnoreCase(sel.getName())) {
            //check whether the href attribute value contains the javascript: prefix
            TagAttribute hrefAttr = sel.getAttribute("href"); //NOI18N
            if(hrefAttr != null) {
                String hrefVal = unquote(hrefAttr.getValue());
                boolean quoted = isQuotationChar(hrefAttr.getValue().charAt(0));
                if(hrefVal.startsWith(JAVASCRIPT_HREF_PREFIX)) {
                    createEmbedding(JAVASCRIPT_MIMETYPE,
                            hrefAttr.getValueOffset(),
                            hrefAttr.getValueOffset() + hrefAttr.getValue().length(),
                            JAVASCRIPT_HREF_PREFIX.length() + (quoted ? 1 : 0),
                            quoted ? 1 : 0);
                }
            }
        }
        
        //various attributes values embedding
        for(SyntaxElement.TagAttribute tagattr : sel.getAttributes()) {
            if("style".equalsIgnoreCase(tagattr.getName())) { //NOI18N
                //XXX we need to look for it just in certain html tags
                createEmbedding(CSS_INLINED_MIMETYPE, tagattr);
            } else if(tagattr.getName().startsWith("on") || tagattr.getName().startsWith("ON")) {
                //XXX very simple algorithm for finding "onclick" like attributes
                //should be restricted according to the html specification
                createEmbedding(JAVASCRIPT_MIMETYPE, tagattr);
            }
        }
    }

    private boolean declaresCSS(SyntaxElement.Tag sel) {
        TagAttribute type = sel.getAttribute("type"); //NOI18N
        
        if(type == null) {
            return true; //default is css
        } else if(unquote(type.getValue()).equalsIgnoreCase("text/css")) { //NOI18N
            return true;
        }
        
        return false;
    }
    
    private String unquote(String s) {
        if(s.length() == 0) {
            //nothing to unquote
            return s;
        }
        if(isQuotationChar(s.charAt(0))) {
            s = s.substring(1);
        }
        if(s.length() == 0) {
            //nothing more to unquote
            return s;
        }
        if(isQuotationChar(s.charAt(s.length()-1))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }
    
    private boolean isQuotationChar(char ch) {
        return ch == '"' || ch == '\'';
    }
    
    private void endTag(SyntaxElement.Named sel) {
        if(CSS_SCRIPT_TAG_NAME.equalsIgnoreCase(sel.getName())) {
            if(styleStart != -1) {
                createEmbedding(CSS_MIMETYPE, styleStart, sel.getElementOffset(), 0,0);
                styleStart = -1;
            }
        }
    }
    
    //I need to specially handle the case where the javascript block contains
    //html comments.
    private void createJavascriptEmbedding(SyntaxElement.Named sel, int from, int to) {
        ((BaseDocument)doc).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = tokenSequence(th, from);
            if(ts == null) {
                //no html token sequence there - weird
                return ;
            }

            ts.move(from);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return ; //no token
            }

            int jsStart = from; int jsStartSkipLength = 0;
            int jsEnd = to; int jsEndSkipLength = 0;
            while(ts.moveNext() && (ts.token().offset(th) <= to)) {
                Token t = ts.token();
                if(t.id() == HTMLTokenId.BLOCK_COMMENT) {
                    if(t.text().toString().startsWith("<!--")) { //NOI18N
                        jsStart = t.offset(th);
                        jsStartSkipLength = "<!--".length(); //NOI18N
                    } else if(t.text().toString().endsWith("-->")) { //NOI18N
                        jsEnd = t.offset(th) + t.length();
                        jsEndSkipLength = "-->".length(); //NOI18N
                    }
                }
            }
            createEmbedding(JAVASCRIPT_MIMETYPE, jsStart, jsEnd, jsStartSkipLength, jsEndSkipLength);
        } finally {
            ((BaseDocument)doc).readUnlock();
        }
    }
    
    private void createEmbedding(String mimeType, SyntaxElement.TagAttribute tagAttr) {
        if(tagAttr.getValue().charAt(0) == '\'' || tagAttr.getValue().charAt(0) == '"') {
            //cut off the qutation marks
            createEmbedding(mimeType, tagAttr.getValueOffset(), tagAttr.getValueOffset() + tagAttr.getValue().length(), 1, 1);
        } else {
            createEmbedding(mimeType, tagAttr.getValueOffset(), tagAttr.getValueOffset() + tagAttr.getValue().length(), 0, 0);
        }
    }
    
    private void createEmbedding(String mimeType, int startOffset, int endOffset, int startSkipLength, int endSkipLength ) {
        
        //        System.out.println("* Creating embedding for " + mimeType + " (" + startOffset + " - " + endOffset + ")");
        
        if(startOffset >= endOffset) {
            LOGGER.log(Level.WARNING, "startOffset >= endOffset: "+ startOffset + " >= " + endOffset);
            return ;
        }
        
        Language lang = Language.find(mimeType);
        if(lang == null) {
            LOGGER.log(Level.WARNING, "No " + mimeType + " language found! (" + startOffset + " - " + endOffset + ")");
            return ; //no language found
        }
        
        ((BaseDocument)doc).extWriteLock(); //writeLock is required since we create embedding what is kind of document change
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            List<TokenSequence> tokenSequenceList = th.tokenSequenceList(languagePath, startOffset, endOffset);
            //TokenSequence ts = tokenSequence(th, startOffset);
            
//            if(ts == null) {
//                LOGGER.log(Level.WARNING, "Trying to create embedding for " + mimeType + " [" + startOffset + " - " + endOffset + "]: cannot get HTML token sequence!");
//                return ;
//            }
            
            for (TokenSequence ts : tokenSequenceList) {
                ts.move(startOffset);
                if(!ts.moveNext() && !ts.movePrevious()) {
                    return ; //no token
                }

                //huh, use the startSkipLength and endSkipLength only on the first and last token
                //in the sequence of tokens we create the embedding.
                boolean iAmFirstToken = true;
                boolean iAmLastToken = false;
                do {
                    Token item = ts.token();
                    //test if we are last token
                    boolean hasNextToken = ts.moveNext();
                    iAmLastToken = !(hasNextToken && ts.offset() < endOffset);
                    if(hasNextToken) {
                        //rewind the tokenSequence back so the
                        //embedding is created on a proper token
                        ts.movePrevious();
                    }
                    if(ts.embedded(lang) == null) {
                        //the embedding doesn't exist, try to create
                        if(!ts.createEmbedding(lang, iAmFirstToken ? startSkipLength : 0, iAmLastToken ? endSkipLength : 0)) {
                            LOGGER.log(Level.WARNING, "Cannot create embedding for " + mimeType + " [" + startOffset + " - "  + endOffset + "] (" + item.text().toString() + ")\n");
                        } else {
                            LOGGER.log(Level.INFO, "Embedding for " + mimeType + " created [" + startOffset + " - "  + endOffset + "] (" + printEmbeddedText(item, iAmFirstToken ? startSkipLength : 0, iAmLastToken ? endSkipLength : 0) + ")\n");
                            embeddingChanged = true;
                        }
                    }
                    iAmFirstToken = false;
                    if(!iAmLastToken) {
                        ts.moveNext();
                    }
                } while(!iAmLastToken);
            }
        }finally {
            ((BaseDocument)doc).extWriteUnlock();
        }
    }
    
    private CharSequence printEmbeddedText(Token item, int startSkipLength, int endSkipLength) {
        StringBuffer sb = new StringBuffer(item.text());
        if(startSkipLength > 0) {
            sb.insert(startSkipLength, '[');
        }
        if(endSkipLength > 0) {
            sb.insert(sb.length() - endSkipLength, ']');
        }
        return sb;
    }
    
    private static TokenSequence tokenSequence(TokenHierarchy hi, int offset) {
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        if(ts == null) {
            //HTML language is not top level one
            ts = hi.tokenSequence();
            ts.move(offset);
            if(!ts.moveNext()) {
                return null; //no token found
            } else {
                ts = ts.embedded(HTMLTokenId.language());
            }
        }
        return ts;
    }
    
    
    
}

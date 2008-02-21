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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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


package org.netbeans.modules.html.editor.coloring;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
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
import org.netbeans.modules.editor.NbEditorUtilities;

/**
 * This class creates lexer embeddings of CSS or JAVASCRIPT language in HTML code.
 * The HTML code may be either the top level language (.html file) or
 * may be embedded as FIRST level embedding language in other language like JSP, RHTML.
 *
 *
 * Note: Dynamic embedding creation for &lt;script&gt; and &lt;style&gt; tags has been removed temporarily
 * due to problems described in following issues:
 * [Issue 117450]  Provide unified LexerInput across multiple joined embedded sections
 * [Issue 118892]  Allow Schlieman lexer to continuously lex embedded language over  more tokens of its parent language
 *
 * Once Issue 117450 is fixed the old way of dynamic embeddings creation should be used
 *
 * @author Marek.Fukala@Sun.com
 */
public class EmbeddingUpdater implements SyntaxParserListener {

    private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
    private static final String JAVASCRIPT_HREF_PREFIX = "javascript:"; //NOI18N

    //XXX update mimetype once Hanz fixes the mimetype in CSS editor module
//    private static final String CSS_MIMETYPE = "text/x-css"; //NOI18N
    private static final String CSS_INLINED_MIMETYPE = "text/x-css"; //NOI18N
//    private static final String CSS_SCRIPT_TAG_NAME = "style"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(EmbeddingUpdater.class.getName());

    private final Document doc;

//    private int styleStart = -1;

    private LanguagePath languagePath;

    @SuppressWarnings("unchecked")
    public EmbeddingUpdater(Document doc) {
        this.doc = doc;

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
        } else {
            languagePath = LanguagePath.get(LanguagePath.get(lang), Language.find("text/html"));
        }

    }

    public void parsingFinished(List<SyntaxElement> elements) {
        for(SyntaxElement sel : elements) {
            if(sel.type() == SyntaxElement.TYPE_TAG) {
                startTag((SyntaxElement.Tag)sel);
            }
        }
    }

    private void startTag(SyntaxElement.Tag sel) {
//        if(CSS_SCRIPT_TAG_NAME.equalsIgnoreCase(sel.getName()) && declaresCSS(sel)) {
//            styleStart = sel.getElementOffset() + sel.getElementLength();
//        } else if("a".equalsIgnoreCase(sel.getName())) {
            if("a".equalsIgnoreCase(sel.getName())) {
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
            } else if(tagattr.getName().startsWith("on") || tagattr.getName().startsWith("ON")) { //NOI18N
                //XXX very simple algorithm for finding "onclick" like attributes
                //should be restricted according to the html specification
                createEmbedding(JAVASCRIPT_MIMETYPE, tagattr);
            }
        }
    }

//    private boolean declaresCSS(SyntaxElement.Tag sel) {
//        TagAttribute type = sel.getAttribute("type"); //NOI18N
//
//        if(type == null) {
//            return true; //default is css
//        } else if(unquote(type.getValue()).equalsIgnoreCase("text/css")) { //NOI18N
//            return true;
//        }
//
//        return false;
//    }

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

//    private void endTag(SyntaxElement.Named sel) {
//        if(CSS_SCRIPT_TAG_NAME.equalsIgnoreCase(sel.getName())) {
//            if(styleStart != -1) {
//                createEmbedding(CSS_MIMETYPE, styleStart, sel.getElementOffset(), 0,0);
//                styleStart = -1;
//            }
//        }
//    }

//    //I need to specially handle the case where the javascript block contains
//    //html comments.
//    private void createJavascriptEmbedding(SyntaxElement.Named sel, int from, int to) {
//        ((BaseDocument)doc).readLock();
//        try {
//            TokenHierarchy th = TokenHierarchy.get(doc);
//            TokenSequence ts = tokenSequence(th, from);
//            if(ts == null) {
//                //no html token sequence there - weird
//                return ;
//            }
//
//            ts.move(from);
//            if(!ts.moveNext() && !ts.movePrevious()) {
//                return ; //no token
//            }
//
//            int jsStart = from; int jsStartSkipLength = 0;
//            int jsEnd = to; int jsEndSkipLength = 0;
//            while(ts.moveNext() && (ts.token().offset(th) <= to)) {
//                Token t = ts.token();
//                if(t.id() == HTMLTokenId.BLOCK_COMMENT) {
//                    if(t.text().toString().startsWith("<!--")) { //NOI18N
//                        jsStart = t.offset(th);
//                        jsStartSkipLength = "<!--".length(); //NOI18N
//                    } else if(t.text().toString().endsWith("-->")) { //NOI18N
//                        jsEnd = t.offset(th) + t.length();
//                        jsEndSkipLength = "-->".length(); //NOI18N
//                    }
//                }
//            }
//            createEmbedding(JAVASCRIPT_MIMETYPE, jsStart, jsEnd, jsStartSkipLength, jsEndSkipLength);
//        } finally {
//            ((BaseDocument)doc).readUnlock();
//        }
//    }

    private void createEmbedding(String mimeType, SyntaxElement.TagAttribute tagAttr) {
        if(tagAttr.getValue().charAt(0) == '\'' || tagAttr.getValue().charAt(0) == '"') {
            //cut off the qutation marks
            createEmbedding(mimeType, tagAttr.getValueOffset(), tagAttr.getValueOffset() + tagAttr.getValueLength(), 1, 1);
        } else {
            createEmbedding(mimeType, tagAttr.getValueOffset(), tagAttr.getValueOffset() + tagAttr.getValueLength(), 0, 0);
        }
    }

    @SuppressWarnings("unchecked")
    private void createEmbedding(String mimeType, int startOffset, int endOffset, int startSkipLength, int endSkipLength ) {
        if(startOffset >= endOffset) {
            LOGGER.log(Level.WARNING, "startOffset >= endOffset: "+ startOffset + " >= " + endOffset); //NOI18N
            return ;
        }

        Language lang = Language.find(mimeType);
        if(lang == null) {
            LOGGER.log(Level.WARNING, "No " + mimeType + " language found! (" + startOffset + " - " + endOffset + ")"); //NOI18N
            return ; //no language found
        }

        ((BaseDocument)doc).extWriteLock(); //writeLock is required since we create embedding what is kind of document change
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            List<TokenSequence> tokenSequenceList = th.tokenSequenceList(languagePath, startOffset, endOffset);

            //use the startSkipLength and endSkipLength only on the first and last token
            //in the sequence of tokens we create the embedding.
            boolean iAmFirstToken = true;
            boolean iAmLastToken = false;

            //find all token sequences of the language in the given range
            Iterator<TokenSequence> sequences = tokenSequenceList.iterator();
            while(sequences.hasNext()) {
                TokenSequence ts = sequences.next();
                ts.move(startOffset);
                if(!ts.moveNext() && !ts.movePrevious()) {
                    return ; //no token
                }
                if(ts.offset() < startOffset || ts.offset() > endOffset) {
                    //a bug #121045 in lexer, we got a token sequence outside of the specified area
                    //just ignore...
                    continue;
                }
                do {
                    Token item = ts.token();
                    //test if the token != null, according to the API doc shouldn't happen
                    //once moveNext/Previous() has been called
                    if(item == null) {
                        LOGGER.log(Level.FINE, "Please refer to issue #126628: tokenSequence.token() returned null after tokenSequence.moveNext/Previous() called! This seems to be a bug in lexer. Please attach the info dumped into the log, the document and possibly steps to reproduce."); //NOI18N
                        LOGGER.log(Level.FINE, "TokenSequence:\n" + ts.toString()); //NOI18N
                    }

                    //test if we are last token
                    boolean hasNextToken = ts.moveNext();
                    iAmLastToken = !(hasNextToken && ts.offset() < endOffset);
                    boolean iAmLastSequence = !sequences.hasNext();
                    if(hasNextToken) {
                        //rewind the tokenSequence back so the
                        //embedding is created on a proper token
                        ts.movePrevious();
                    }
                    if(ts.embedded(lang) == null) {
                        //the embedding doesn't exist, try to create
                        if(!ts.createEmbedding(lang, iAmFirstToken ? startSkipLength : 0, iAmLastToken && iAmLastSequence ? endSkipLength : 0, true)) {
                            CharSequence text = item.text();
                            if(text == null) {
                              //according to the Token.text() javadoc this shouldn't happen =>
                              //notify user about the situation and provide some debug info.
                              LOGGER.log(Level.FINE, null, new IllegalStateException("Please refer to issue #126628: Token.text() of " + item.toString() + " == null without any previous modification of the underlying document! This seems to be a bug in lexer. Please attach the info dumped into the log, the document and possibly steps to reproduce.")); //NOI18N
                              LOGGER.log(Level.FINE, "TokenSequence:\n" + ts.toString()); //NOI18N
                            } else {
                                LOGGER.log(Level.FINE, "Cannot create embedding for " + mimeType + " [" + startOffset + " - "  + endOffset + "] (" + text + ")\n"); //NOI18N
                            }
                        } else {
                            CharSequence text = item.text();
                            if(text == null) {
                                //according to the Token.text() javadoc this shouldn't happen =>
                                //notify user about the situation and provide some debug info.
                                LOGGER.log(Level.FINE, null, new IllegalStateException("Token.text() of " + item.toString() + " == null without any previous modification of the underlying document! This seems to be a bug in lexer. Please report the issue to lexer module and attach the info dumped into the log, the document and possibly steps to reproduce.")); //NOI18N
                                LOGGER.log(Level.FINE, "TokenSequence:\n" + ts.toString()); //NOI18N
                            } else {
                                LOGGER.log(Level.FINE, "Embedding for " + mimeType + " created [" + startOffset + " - "  + endOffset + "] (" + printEmbeddedText(text, iAmFirstToken ? startSkipLength : 0, iAmLastToken && iAmLastSequence ? endSkipLength : 0) + ")\n"); //NOI18N
                            }
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

    private CharSequence printEmbeddedText(CharSequence text, int startSkipLength, int endSkipLength) {
        StringBuffer sb = new StringBuffer(text);
        if(startSkipLength >= 0) {
            sb.insert(startSkipLength, '[');
        }
        if(endSkipLength >= 0) {
            sb.insert(sb.length() - endSkipLength, ']');
        }
        return sb;
    }

}


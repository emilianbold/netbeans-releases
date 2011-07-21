/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.editor.ext.html.parser;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.api.HtmlSource;
import org.netbeans.editor.ext.html.parser.api.ProblemDescription;
import org.netbeans.editor.ext.html.parser.api.SyntaxAnalyzerResult;
import org.netbeans.editor.ext.html.parser.spi.UndeclaredContentResolver;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 * Plain HTML syntax analyzer
 *
 * @author mfukala@netbeans.org
 */
public final class SyntaxAnalyzer {

    public enum Behaviour {
        /** set as SyntaxParserContext property if you do not want to check html structure */
        DISABLE_STRUCTURE_CHECKS,
        /** set as SyntaxParserContext property if you do not want to check html attributes */
        DISABLE_ATTRIBUTES_CHECKS 
    }

    private final LanguagePath languagePath;
    private final TokenHierarchy hi;
    
    private final SyntaxElement SHARED_TEXT_ELEMENT = new SyntaxElement.SharedTextElement();
    
    private HtmlSource source;
    private CharSequence sourceCode;

    public static SyntaxAnalyzer create(HtmlSource source) {
        return new SyntaxAnalyzer(source);
    }

    private SyntaxAnalyzer(HtmlSource source) {
        this.source = source;
        this.sourceCode = source.getSourceCode();
        this.languagePath = LanguagePath.get(HTMLTokenId.language());
        this.hi = TokenHierarchy.create(sourceCode, HTMLTokenId.language());
        
        init();
    }

    public SyntaxAnalyzerResult analyze() {
        return new SyntaxAnalyzerResult(this);
    }

    public SyntaxAnalyzerResult analyze(UndeclaredContentResolver resolver) {
        return new SyntaxAnalyzerResult(this, resolver);
    }

    public HtmlSource source() {
        return source;
    }
    
    /**
     * Iterating over the SyntaxElements of this iterator causes the input source
     * being incrementally parsed if it hasn't been parsed before.
     * 
     * !!! Not thread-safe and MOREOVER No more than one iterator 
     * can be iterated in one moment, resp. always stop using one iterator 
     * before creating another one. If this happens the iterated elements 
     * may not properly reflect the real elements in the parsed source !!!
     * 
     * @return Iterator of SyntaxElement-s
     */
    public synchronized Iterator<SyntaxElement> elementsIterator() {
        final Iterator<SyntaxElement> alreadyParsedElementsIterator = new ArrayList<SyntaxElement>(elements).iterator();
        return new Iterator<SyntaxElement>() {
            
            boolean iteratingCache = true;

            @Override
            public boolean hasNext() {
                iteratingCache = alreadyParsedElementsIterator.hasNext();
                if(iteratingCache) {
                    return iteratingCache;
                } else {
                    return SyntaxAnalyzer.this.hasNext();
                }
            }

            @Override
            public SyntaxElement next() {
                return iteratingCache ? alreadyParsedElementsIterator.next() : SyntaxAnalyzer.this.next();
            }

            @Override
            public void remove() {
                //no-op
            }
            
        };
    }

    public synchronized SyntaxAnalyzerElements elements() {
        List<SyntaxElement> result = new ArrayList<SyntaxElement>();
        Iterator<SyntaxElement> elementsIterator = elementsIterator();
        while(elementsIterator.hasNext()) {
            result.add(elementsIterator.next());
        }
        return new SyntaxAnalyzerElements(result);
    }
    
    //---------------------------- private methods -----------------------------
 
    private void error() {
        current = new SyntaxElement.Error(sourceCode,
                start,
                ts.offset() + ts.token().length() - start);
    }

    private void text() {
        //Memory consumption optimalization: Since noone seems to use the text elements
        //there's no need to create a separate instance for each piece of text in the source.
        //Instead a shared instance is used, but of course none of the information
        //provided are valid
        current = SHARED_TEXT_ELEMENT;
    }

    private void entityReference() {
        current = new SyntaxElement.EntityReference(sourceCode,
                start, 
                ts.offset() + ts.token().length() - start);
        
    }
    
    private void comment() {
        current = new SyntaxElement.Comment(sourceCode,
                start, 
                ts.offset() + ts.token().length() - start);
    }
    
    private void declaration() {
        current = new SyntaxElement.Declaration(sourceCode,
                start, 
                ts.offset() + ts.token().length() - start,
                root_element,
                doctype_public_id,
                doctype_file,
                doctype_name);
    }
    
    private void tag(boolean emptyTag) {
        tag(emptyTag, null);
    }

    private void tag(boolean emptyTag, ProblemDescription problem) {
        List<SyntaxElement.TagAttribute> attributes = new ArrayList<SyntaxElement.TagAttribute>();
            for(int i = 0; i < attr_keys.size(); i++) {
                TokenInfo key = attr_keys.get(i);
                List<TokenInfo> values = attr_values.get(i);
                StringBuilder joinedValue = new StringBuilder();
                
                if(values == null) {
                    //attribute has no value
                    SyntaxElement.TagAttribute ta = new SyntaxElement.TagAttribute(
                            key.token.text().toString().intern(),
                            joinedValue.toString().intern(), 
                            key.offset,
                            key.offset + key.token.length(),
                            0);
                    attributes.add(ta);
                } else {
                    for(TokenInfo t: values) {
                        joinedValue.append(t.token.text());
                    }

                    TokenInfo firstValuePart = values.get(0);
                    TokenInfo lastValuePart = values.get(values.size() - 1);

                    SyntaxElement.TagAttribute ta = new SyntaxElement.TagAttribute(
                            key.token.text().toString().intern(),
                            joinedValue.toString().intern(), 
                            key.offset, 
                            firstValuePart.offset,
                            lastValuePart.offset + lastValuePart.token.length() - firstValuePart.offset);
                    attributes.add(ta);
                }
            }

        current = new SyntaxElement.Tag(sourceCode,
                start,
                ts.offset() + ts.token().length() - start,
                tagName.intern(),
                attributes.isEmpty() ? null : attributes,
                openTag,
                emptyTag);

        if(problem != null) {
            current.addProblem(problem);
        }

        tagName = null;
        attrib = null;
        attr_keys = new ArrayList<TokenInfo>();
        attr_values = new ArrayList<List<TokenInfo>>();
    }

    //an error inside a tag, at least the tag name is known
    private void tag_with_error(ProblemDescription problem) {
        //lets put back the errorneous symbol first
        backup(1);
        //make the tag, we do not know if empty or not
        tag(false, problem);
        
        state = S_INIT;
        start = -1;
    }

    //recover from error
    private void reset() {
        backup(1);
        //create error element excluding the last token caused the error
        error();
        state = S_INIT;
        start = -1;
    }
    
    private void backup(int tokens) {
        for(int i = 0; i < tokens; i++) {
            ts.movePrevious();
            token = ts.token();
        }
    }
    
    private static final int S_INIT = 0;
    private static final int S_TAG_OPEN_SYMBOL = 1;
    private static final int S_TAG = 2;
    private static final int S_TAG_ATTR = 3;
    private static final int S_TAG_VALUE = 4;
    private static final int S_COMMENT = 5;
    private static final int S_DECLARATION = 6;
    private static final int S_DOCTYPE_DECLARATION = 7;
    private static final int S_DOCTYPE_AFTER_ROOT_ELEMENT = 8;
    private static final int S_DOCTYPE_PUBLIC_ID = 9;
    private static final int S_DOCTYPE_FILE = 10;
    private static final int S_TEXT = 11;
    private static final int S_TAG_AFTER_NAME = 12;
    
    private int state;
    private int start;
    private TokenSequence ts;
    private Token<HTMLTokenId> token;
    private List<SyntaxElement> elements;
    
    private boolean openTag = true;
    private String tagName = null;
    private TokenInfo attrib = null;
    private ArrayList<TokenInfo> attr_keys = null;
    private ArrayList<List<TokenInfo>> attr_values = null;
    private Iterator<TokenSequence<HTMLTokenId>> sequences;
    private SyntaxElement current;
    private boolean eof;
    private AtomicReference<SyntaxElement> lastFoundElement;
    
    private String root_element, doctype_public_id, doctype_file, doctype_name;
    
    private void init() {
        elements = new ArrayList<SyntaxElement>();
        sequences = hi.tokenSequenceList(languagePath, 0, Integer.MAX_VALUE).iterator();
        if(!sequences.hasNext()) {
            eof = true;
            return;
        }
        ts = sequences.next();
        ts.moveStart();
        state = S_INIT;
        start = -1;
        attr_keys = new ArrayList<TokenInfo>();
        attr_values = new ArrayList<List<TokenInfo>>();
        eof = false;
        
    }
    
    private boolean hasNext() {
        if(lastFoundElement == null) {
            lastFoundElement = new AtomicReference<SyntaxElement>(findNextElement());
        }
        return lastFoundElement.get() != null;
    }
    
    private SyntaxElement next() {
        if(!hasNext()) {
            throw new IllegalStateException("No such element");
        }
        SyntaxElement element = lastFoundElement.get();
        lastFoundElement = null;
        elements.add(element);
        return element;
    }
    
    
    private SyntaxElement findNextElement() {
        SyntaxElement element = null;
        //parse tokens until a syntaxelement is found
        while(!eof && (element = processNextToken()) == null);
        return element;
    }
    
    private SyntaxElement processNextToken() {
        current = null;

        if (!ts.moveNext()) {
            //no more tokens in this token sequence
            if (sequences.hasNext()) {
                //there's another tokensequence
                ts = sequences.next();
                ts.moveStart();
            } else {
                //eof
                handleEOF(); //may possibly set current element
                eof = true; //finish the parsing cycle
                return current;
            }
        }

        int offset = ts.offset();
        token = ts.token();
        HTMLTokenId id = token.id();

        switch (state) {
            case S_INIT:
                switch (id) {
                    case CHARACTER:
                        start = ts.offset();
                        entityReference();
                        state = S_INIT;
                        start = -1;
                        break;
                    case TAG_OPEN_SYMBOL:
                        start = ts.offset();
                        state = S_TAG_OPEN_SYMBOL;
                        break;
                    case BLOCK_COMMENT:
                        start = ts.offset();
                        state = S_COMMENT;
                        break;
                    case DECLARATION:
                        start = ts.offset();
                        if (LexerUtils.equals("<!doctype", token.text(), true, true)) { //NOI18N
                            root_element = null;
                            doctype_public_id = null;
                            doctype_file = null;
                            state = S_DOCTYPE_DECLARATION;
                        } else {
                            state = S_DECLARATION;
                        }
                        doctype_name = token.text().subSequence(2, token.text().length()).toString(); //strip off the <! chars
                        break;
                    default:
                        //everything else is just a text
                        start = ts.offset();
                        state = S_TEXT;
                        break;
                }
                break;

            case S_TEXT:
                switch (id) {
                    case TEXT:
                        break;
                    default:
                        backup(1);
                        text();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_TAG_OPEN_SYMBOL:
                switch (id) {
                    case TAG_OPEN:
                        state = S_TAG_AFTER_NAME;
                        openTag = true;
                        tagName = token.text().toString();
                        break;
                    case TAG_CLOSE:
                        state = S_TAG_AFTER_NAME;
                        openTag = false;
                        tagName = token.text().toString();
                        break;
                    default:
                        reset(); //error
                        break;
                }
                break;

            case S_TAG_AFTER_NAME:
                //just switch to 'in tag state'
                backup(1);
                state = S_TAG;
                break;

            case S_TAG:
                switch (id) {
                    case WS:
                    case EOL:
                    case ERROR:
                        break;
                    case ARGUMENT:
                        state = S_TAG_ATTR;
                        attrib = tokenInfo();
                        break;
                    case TAG_CLOSE_SYMBOL:
                        boolean emptyTag = "/>".equals(token.text().toString());
                        tag(emptyTag);
                        state = S_INIT;
                        start = -1;
                        break;
                    default:
                        tag_with_error(
                                ProblemDescription.create(SyntaxTreeBuilder.UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                }
                break;


            case S_TAG_ATTR:
                switch (id) {
                    case OPERATOR:
                    case WS:
                        break;
                    case VALUE:
                    case VALUE_JAVASCRIPT:
                    case VALUE_CSS:
                        backup(1); //backup the value
                        state = S_TAG_VALUE;
                        break;
                    case ARGUMENT:
                    case TAG_CLOSE_SYMBOL:
                        //attribute without value
                        attr_keys.add(attrib);
                        attr_values.add(null);
                        state = S_TAG;
                        backup(1);
                        break;
                    default:
                        tag_with_error(
                                ProblemDescription.create(SyntaxTreeBuilder.UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                }
                break;

            case S_TAG_VALUE:
                switch (id) {
                    case VALUE:
                    case VALUE_JAVASCRIPT:
                    case VALUE_CSS:
                        int index = attr_keys.indexOf(attrib);
                        if (index == -1) {
                            List<TokenInfo> values = new ArrayList<TokenInfo>();
                            values.add(tokenInfo());
                            attr_keys.add(attrib);
                            attr_values.add(values);
                        } else {
                            attr_values.get(index).add(tokenInfo());
                        }

                        break;
                    case ERROR:
                        tag_with_error(
                                ProblemDescription.create(SyntaxTreeBuilder.UNEXPECTED_SYMBOL_IN_OPEN_TAG,
                                String.format("Unexpected symbol '%s' found in the open tag", token.text()),
                                ProblemDescription.ERROR,
                                offset,
                                offset + token.length()));
                        break;
                    default:
                        backup(1);
                        state = S_TAG;
                        break;
                }
                break;

            case S_COMMENT:
                switch (id) {
                    case BLOCK_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        comment();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DECLARATION:
                switch (id) {
                    case DECLARATION:
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_DECLARATION:
                switch (id) {
                    case DECLARATION:
                        root_element = token.text().toString();
                        state = S_DOCTYPE_AFTER_ROOT_ELEMENT;
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_AFTER_ROOT_ELEMENT:
                switch (id) {
                    case DECLARATION:
                        if (LexerUtils.equals("public", token.text(), true, true)) { //NOI18N
                            doctype_public_id = new String();
                            state = S_DOCTYPE_PUBLIC_ID;
                            break;
                        } else if (LexerUtils.equals("system", token.text(), true, true)) { //NOI18N
                            state = S_DOCTYPE_FILE;
                            doctype_file = new String();
                            break;
                        } else if (token.text().charAt(0) == '>') {
                            declaration();
                            state = S_INIT;
                            start = -1;
                        }
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_PUBLIC_ID:
                switch (id) {
                    case WS:
                    case DECLARATION:
                        String tokenText = token.text().toString();
                        if (tokenText.startsWith("\"")) {
                            //first token
                            tokenText = tokenText.substring(1); //cut off the quotation mark
                        }
                        if (tokenText.endsWith("\"")) {
                            //last token
                            tokenText = tokenText.substring(0, tokenText.length() - 1); //cut off the quotation mark
                            doctype_public_id += tokenText; //short and rare strings, no perf problem
                            doctype_public_id = doctype_public_id.trim();
                            state = S_DOCTYPE_FILE;
                            break;
                        }
                        doctype_public_id += tokenText; //short and rare strings, no perf problem

                        break;
                    case SGML_COMMENT:
                    case EOL:

                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

            case S_DOCTYPE_FILE:
                switch (id) {
                    case DECLARATION:
                        doctype_file = token.text().toString();
                        //jump to simple sgml declaration so potentially 
                        //other declaration tokens are inluded
                        state = S_DECLARATION;
                        break;
                    case SGML_COMMENT:
                    case EOL:
                    case WS:
                        break;
                    default:
                        backup(1);
                        declaration();
                        state = S_INIT;
                        start = -1;
                        break;
                }
                break;

        } //switch end

        return current;

    }
    
    private void handleEOF() {
        if(state != S_INIT) {
            //an incomplete syntax element at the end of the file
            switch(state) {
                case S_COMMENT:
                    comment();
                    break;
                case S_DECLARATION:
                case S_DOCTYPE_AFTER_ROOT_ELEMENT:
                case S_DOCTYPE_DECLARATION:
                case S_DOCTYPE_FILE:
                case S_DOCTYPE_PUBLIC_ID:
                    declaration();
                    break;
                case S_TEXT:
                    text();
                    break;
                case S_TAG:
                case S_TAG_ATTR:
                case S_TAG_VALUE:
                    tag(false);
                    break;
                case S_TAG_AFTER_NAME:
                    tag(false);
                    break;
                default:
                    error();
                    break;
            }
            
        }
    }

    private TokenInfo tokenInfo() {
        return new TokenInfo(ts.offset(), token);
    }

    private static final class TokenInfo {
        public int offset;
        public Token token;
        public TokenInfo(int offset, Token token) {
            this.offset = offset;
            this.token = token;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TokenInfo other = (TokenInfo) obj;
            if (this.offset != other.offset) {
                return false;
            }
            if (this.token != other.token && (this.token == null || !this.token.equals(other.token))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 37 * hash + this.offset;
            hash = 37 * hash + (this.token != null ? this.token.hashCode() : 0);
            return hash;
        }

    }

}

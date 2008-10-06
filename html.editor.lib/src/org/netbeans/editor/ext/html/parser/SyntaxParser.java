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
package org.netbeans.editor.ext.html.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.openide.util.RequestProcessor;

/**
 * Simple HTML syntax analyzer
 *
 * @author Marek.Fukala@Sun.com
 */
public final class SyntaxParser {

    private static final Logger LOGGER = Logger.getLogger(SyntaxParser.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    
    private static final int PARSER_DELAY = 1000; //ms (=1second)
    
    private final Document doc;
    private final LanguagePath languagePath;
    private final TokenHierarchy hi;
    private final RequestProcessor.Task parserTask;
    private final ArrayList<SyntaxParserListener> listeners = new ArrayList<SyntaxParserListener>();
    private final TokenHierarchyListener tokenHierarchyListener = new TokenHierarchyListener() {

        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            if (evt.type() == TokenHierarchyEventType.MODIFICATION) {
                restartParser();
            }
        }
    };
    
    private List<SyntaxElement> EMPTY_ELEMENTS_LIST = Collections.emptyList();
    private List<SyntaxElement> parsedElements;
    private boolean isSuccessfulyParsed = false;

    protected ParserSource parserSource;
    
    /** Returns an instance of SyntaxParser for given document.
     *  The client is supposed to add a SyntaxParserListener to the obtained instance
     *  to get notification whenever the document changes and is reparsed.
     */
    //XXX We cannot create multiple SyntaxParser-s for various languagePaths on one document.
    public static synchronized SyntaxParser get(Document doc, LanguagePath languagePath) {
        SyntaxParser parser = (SyntaxParser) doc.getProperty(SyntaxParser.class);
        if (parser == null) {
            parser = new SyntaxParser(doc, languagePath);
            doc.putProperty(SyntaxParser.class, parser);
        }
        return parser;
    }

    /** Creates a new instance of SyntaxParser parsing the immutable source. 
     * 
     * @param source A non null sequence of characters - parser input
     */
    public static List<SyntaxElement> parseImmutableSource(final CharSequence source) {
        if(source == null) {
            throw new NullPointerException("Parser source cannot be null!");
        }
        
        SyntaxParser parser = new SyntaxParser(source);

        parser.parserSource = new ParserSource() {

            public CharSequence getText(int offset, int length) throws BadLocationException {
                return source.subSequence(offset, offset + length);
            }
        };

        try {
            return parser.parseDocument();
        } catch (BadLocationException ex) {
            LOGGER.log(Level.WARNING, "Error during parsing html content", ex);
            return null;

        } finally {
            //help GC - the SyntaxElements returned by the parser
            //holds references to the parser source 
            parser.parserSource = null; 
        }

    }
    
    private SyntaxParser(final CharSequence source) {
        this.parserTask = null;
        this.doc = null;
        this.parsedElements = EMPTY_ELEMENTS_LIST;
        this.languagePath = LanguagePath.get(HTMLTokenId.language());
        this.hi = TokenHierarchy.create(source, HTMLTokenId.language());
    }
    
    private SyntaxParser(Document document, LanguagePath languagePath) {
        this.doc = document;
        this.languagePath = languagePath;
        this.hi = TokenHierarchy.get(doc);

        if (hi == null) {
            String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
            if (mimeType == null) {
                mimeType = "unknown";
            }
            throw new IllegalStateException("Cannot obtain TokenHierarchy instance for document " + document + " with " + mimeType + " mimetype."); //NOI18N
        }

        this.parserSource = new ParserSource() {
            public String getText(int offset, int length) throws BadLocationException {
                return doc.getText(offset, length);
            }
        };
        
        parsedElements = EMPTY_ELEMENTS_LIST;

        parserTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                parse();
            }
        });

        //add itself as token hierarchy listener
        hi.addTokenHierarchyListener(tokenHierarchyListener);

        //ensure the document is parsed
        restartParser();

    }

    //---------------------------- public methods ------------------------------
    public void addSyntaxParserListener(SyntaxParserListener spl) {
        listeners.add(spl);
    }

    /** Removes the SyntaxParserListener from the listeners list.*/
    public void removeSyntaxParserListener(SyntaxParserListener spl) {
        listeners.remove(spl);
    }
    
    //----------------------- package private methods---------------------------
    /** used by unit tests */
    void forceParse() {
        parserTask.cancel();
        parse();
    }
    
    public List<SyntaxElement> elements() {
        return parsedElements;
    }
    
    //---------------------------- private methods -----------------------------
    private void restartParser() {
        if (!parserTask.isFinished()) {
            parserTask.cancel(); //removes the task from the queue AND INTERRUPTS the thread!
        }
        parserTask.schedule(PARSER_DELAY);
    }

    private synchronized void parse() {
        BaseDocument bdoc = (BaseDocument) doc;
        bdoc.readLock();
        try {
            List<SyntaxElement> newElements = parseDocument();
            parsedElements = newElements;
            isSuccessfulyParsed = true;
        } catch (BadLocationException ble) {
            isSuccessfulyParsed = false;
            LOGGER.log(Level.WARNING, "Error during parsing html content", ble);
        } finally {
            bdoc.readUnlock();
        }

        if (isSuccessfulyParsed) {
            notifyParsingFinished();
        }
    }

    private void notifyParsingFinished() {
        if (!parsedElements.isEmpty()) {
            
            //debug messages
            if(LOG) {
                for (SyntaxElement se : parsedElements) {
                    LOGGER.log(Level.FINE, se.toString());
                    System.out.println(se.toString());
                }
            }
            
            for (SyntaxParserListener spl : listeners) {
                spl.parsingFinished(parsedElements);
            }
        }
    }

    private void entityReference() {
        elements.add(new SyntaxElement(parserSource, 
                start, 
                token.offset(hi) + token.length() - start, 
                SyntaxElement.TYPE_ENTITY_REFERENCE));
        
    }
    
    private void comment() {
        elements.add(new SyntaxElement(parserSource, 
                start, 
                token.offset(hi) + token.length() - start, 
                SyntaxElement.TYPE_COMMENT));
    }
    
    private void declaration() {
        elements.add(new SyntaxElement.Declaration(parserSource, 
                start, 
                token.offset(hi) + token.length() - start,
                root_element,
                doctype_public_id,
                doctype_file));
    }
    
    
    private void tag(boolean emptyTag) {
        List<SyntaxElement.TagAttribute> attributes = new ArrayList<SyntaxElement.TagAttribute>();
            for(int i = 0; i < attr_keys.size(); i++) {
                Token key = attr_keys.get(i);
                List<Token> values = attr_values.get(i);
                StringBuffer joinedValue = new StringBuffer();
                
                if(values == null) {
                    //attribute has no value
                    SyntaxElement.TagAttribute ta = new SyntaxElement.TagAttribute(
                            key.text().toString().intern(), 
                            joinedValue.toString().intern(), 
                            key.offset(hi), 
                            key.offset(hi) + key.length(),
                            0);
                    attributes.add(ta);
                } else {
                    for(Token t: values) {
                        joinedValue.append(t.text());
                    }

                    Token firstValuePart = values.get(0);
                    Token lastValuePart = values.get(values.size() - 1);

                    SyntaxElement.TagAttribute ta = new SyntaxElement.TagAttribute(
                            key.text().toString().intern(), 
                            joinedValue.toString().intern(), 
                            key.offset(hi), 
                            firstValuePart.offset(hi),
                            lastValuePart.offset(hi) + lastValuePart.length() - firstValuePart.offset(hi));
                    attributes.add(ta);
                }
            }
        
        elements.add(new SyntaxElement.Tag(parserSource, 
                start, 
                token.offset(hi) + token.length() - start, 
                tagName.intern(), 
                attributes.isEmpty() ? null : attributes,
                openTag,
                emptyTag));
        
        tagName = null;
        attrib = null;
        attr_keys = new ArrayList<Token>();
        attr_values = new ArrayList<List<Token>>();
    }
    
    private void reset() {
        state = S_INIT;
        start = -1;
        backup(1);
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
    
    private int state;
    private int start;
    private TokenSequence ts;
    private Token<HTMLTokenId> token;
    private List<SyntaxElement> elements;
    
    private boolean openTag = true;
    private String tagName = null;
    private Token attrib = null;
    private ArrayList<Token> attr_keys = null;
    private ArrayList<List<Token>> attr_values = null;
    
    private String root_element, doctype_public_id, doctype_file;
    
    //PENDING: we do not handle incomplete tokens yet - should be added
    private List<SyntaxElement> parseDocument() throws BadLocationException {
        elements = new ArrayList<SyntaxElement>();
        List<TokenSequence<HTMLTokenId>> sequences = hi.tokenSequenceList(languagePath, 0, Integer.MAX_VALUE);
        state = S_INIT;
        start = -1;
        attr_keys = new ArrayList<Token>();
        attr_values = new ArrayList<List<Token>>();
        
        for (TokenSequence _ts : sequences) {
            ts = _ts;
            while (ts.moveNext()) {
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
                                if(token.text().toString().equals("<!DOCTYPE")) {
                                    root_element = null;
                                    doctype_public_id = null;
                                    doctype_file = null;
                                    state = S_DOCTYPE_DECLARATION;
                                } else {
                                    state = S_DECLARATION;
                                }
                                break;
                        }
                        break;
                        
                    case S_TAG_OPEN_SYMBOL:
                        switch (id) {
                            case TAG_OPEN:
                                state = S_TAG;
                                openTag = true;
                                tagName = token.text().toString();
                                break;
                            case TAG_CLOSE:
                                state = S_TAG;
                                openTag = false;
                                tagName = token.text().toString();
                                break;
                            default:
                                reset(); //error
                                break;
                        }
                        break;
                        
                    case S_TAG:
                        switch (id) {
                            case WS:
                            case EOL:
                            case ERROR:
                                break;
                            case ARGUMENT:
                                state = S_TAG_ATTR;
                                attrib = token;
                                break;
                            case TAG_CLOSE_SYMBOL:
                                boolean emptyTag = "/>".equals(token.text().toString());
                                tag(emptyTag);
                                state = S_INIT;
                                start = -1;
                                break;
                            default:
                                reset(); //error
                                break;
                        }
                        break;
                          
                        
                    case S_TAG_ATTR:
                        switch(id) {
                            case OPERATOR:
                            case WS:
                                break;
                            case VALUE:
                            case VALUE_JAVASCRIPT:
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
                                reset(); //error
                                break;
                        }
                        break;
                        
                    case S_TAG_VALUE:
                        switch(id) {
                            case VALUE:
                            case VALUE_JAVASCRIPT:
                                int index = attr_keys.indexOf(attrib);
                                if(index == -1) {
                                    List<Token> values = new ArrayList<Token>();
                                    values.add(token);
                                    attr_keys.add(attrib);
                                    attr_values.add(values);
                                } else {
                                    attr_values.get(index).add(token);
                                }
                                
                                break;
                            default:
                                backup(1);
                                state = S_TAG;
                                break;
                        }
                        break;    
                        
                    case S_COMMENT:
                        switch(id) {
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
                        switch(id) {
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
                        switch(id) {
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
                       switch(id) {
                            case DECLARATION:
                                if(token.text().toString().equals("PUBLIC")) {
                                    doctype_public_id = new String();
                                    state = S_DOCTYPE_PUBLIC_ID;
                                    break;
                                } else if(token.text().toString().equals("SYSTEM")) {
                                    state = S_DOCTYPE_FILE;
                                    doctype_file = new String();
                                    break;
                                }
                                //not of the expected
                                backup(1);
                                declaration();
                                state = S_INIT;
                                start = -1;
                                
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
                        switch(id) {
                            case WS:
                            case DECLARATION:
                                String tokenText = token.text().toString();
                                if(tokenText.startsWith("\"")) {
                                    //first token
                                    tokenText = tokenText.substring(1); //cut off the quotation mark
                                }
                                if(tokenText.endsWith("\"")) {
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
                        switch(id) {
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
                        
                }
            }
        }
        
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
            }
            
        }

        return elements;

    }

}

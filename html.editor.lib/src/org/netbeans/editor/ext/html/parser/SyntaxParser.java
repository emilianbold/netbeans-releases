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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //---------------------------- public methods -------------------------------
    public void addSyntaxParserListener(SyntaxParserListener spl) {
        listeners.add(spl);
    }

    /** Removes the SyntaxParserListener from the listeners list.*/
    public void removeSyntaxParserListener(SyntaxParserListener spl) {
        listeners.remove(spl);
    }
    
    //---------------------------- private methods -------------------------------
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
                }
            }
            
            for (SyntaxParserListener spl : listeners) {
                spl.parsingFinished(parsedElements);
            }
        }
    }

    private void entityReference() {
        elements.add(new SyntaxElement(doc, 
                start, 
                token.offset(hi) + token.length() - start, 
                SyntaxElement.TYPE_TEXT));
        
    }
    
    private void tag(boolean emptyTag) {
        List<SyntaxElement.TagAttribute> attributes = new ArrayList<SyntaxElement.TagAttribute>();
        if(attribs != null) {
            for(Token key : attribs.keySet()) {
                List<Token> values = attribs.get(key);
                StringBuffer joinedValue = new StringBuffer();
                for(Token t: values) {
                    joinedValue.append(t.text());
                }
                
                
                Token firstValuePart = values.get(0);
                Token lastValuePart = values.get(values.size() - 1);
                
                SyntaxElement.TagAttribute ta = new SyntaxElement.TagAttribute(
                        key.text().toString(), 
                        joinedValue.toString(), 
                        key.offset(hi), 
                        firstValuePart.offset(hi),
                        lastValuePart.offset(hi) + lastValuePart.length());
                attributes.add(ta);
            }
        }
        elements.add(new SyntaxElement.Tag(doc, 
                start, 
                token.offset(hi) + token.length() - start, 
                tagName, 
                attributes,
                openTag,
                emptyTag));
        
        tagName = null;
        attrib = null;
        attribs = new HashMap<Token, List<Token>>();
    }
    
    private void reset() {
        state = S_INIT;
        start = -1;
        ts.movePrevious(); //backup this token
    }
    
    private static final int S_INIT = 0;
    private static final int S_CHARACTER = 1;
    private static final int S_TAG_OPEN_SYMBOL = 2;
    private static final int S_TAG = 3;
    private static final int S_TAG_ATTR = 4;
    private static final int S_TAG_VALUE = 5;
    
    private int state;
    private int start;
    private TokenSequence ts;
    private Token<HTMLTokenId> token;
    private List<SyntaxElement> elements;
    
    private boolean openTag = true;
    private String tagName = null;
    private Token attrib = null;
    private Map<Token, List<Token>> attribs = null;

    //PENDING: we do not handle incomplete tokens yet - should be added
    public List<SyntaxElement> parseDocument() throws BadLocationException {
        elements = new ArrayList<SyntaxElement>();
        List<TokenSequence<HTMLTokenId>> sequences = hi.tokenSequenceList(languagePath, 0, Integer.MAX_VALUE);
        state = S_INIT;
        start = -1;
        attribs = new HashMap<Token, List<Token>>();
        
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
                                state = S_CHARACTER;
                                break;
                            case TAG_OPEN_SYMBOL:
                                start = ts.offset();
                                state = S_TAG_OPEN_SYMBOL;
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
                                ts.movePrevious(); //backup the value
                                state = S_TAG_VALUE;
                                break;
                            default:
                                reset(); //error
                                break;
                        }
                        break;
                        
                    case S_TAG_VALUE:
                        switch(id) {
                            case VALUE:
                                List<Token> values = attribs.get(attrib);
                                if(values == null) {
                                    values = new ArrayList<Token>();
                                    attribs.put(attrib, values);
                                }
                                values.add(token);
                                break;
                            default:
                                ts.movePrevious();
                                state = S_TAG;
                                break;
                        }
                        break;    
                        
                    case S_CHARACTER:
                        switch (id) {
                            case CHARACTER:
                                break;
                            default:
                                ts.movePrevious(); //backup the foreign token
                                entityReference();
                                state = S_INIT;
                                start = -1;
                                break;
                        }
                }
            }
        }

        return elements;

    }

}

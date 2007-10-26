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
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.openide.util.RequestProcessor;

/**
 * Simple HTML syntax parser.
 *
 * @author Marek.Fukala@Sun.com
 */
public final class SyntaxParser {
    
    private static final int PARSER_DELAY = 1000; //1 second
    
    private final Document doc;
    private final TokenHierarchy hi;
    private final RequestProcessor.Task parserTask;
    private final ArrayList<SyntaxParserListener> listeners = new ArrayList<SyntaxParserListener>();
    private final Object parsingState = new Object();
    
    private final TokenHierarchyListener tokenHierarchyListener = new TokenHierarchyListener() {
        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            if(evt.type() == TokenHierarchyEventType.MODIFICATION) {
                synchronized (parsingState) {
                    restartParser();
                }
            }
        }
    };
    
    private ArrayList<SyntaxElement> parsedElements;
    
    private boolean isParsing = false;
    private boolean isScheduled = false;
    
    /** Returns an instance of SyntaxParser for given document.
     *  The client is supposed to add a SyntaxParserListener to the obtained instance
     *  to get notification whenever the document changes and is reparsed.
     */
    public static synchronized SyntaxParser get(Document doc) {
        SyntaxParser parser = (SyntaxParser)doc.getProperty(SyntaxParser.class);
        if(parser == null) {
            parser = new SyntaxParser(doc);
            doc.putProperty(SyntaxParser.class, parser);
        }
        return parser;
    }
    
    private SyntaxParser(Document doc) {
        this.doc = doc;
        this.hi = TokenHierarchy.get(doc);
        
        if(hi == null) {
            String mimeType = (String)doc.getProperty("mimeType"); //NOI18N
            if(mimeType == null) {
                mimeType = "unknown";
            }
            throw new IllegalStateException("Cannot obtain TokenHierarchy instance for document " + doc + " with " + mimeType + " mimetype."); //NOI18N
        }
        
        parsedElements = null; //null states the data are not available yet
        
        parserTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                parse();
            }
        });
        
        //add itself as token hierarchy listener
        hi.addTokenHierarchyListener(tokenHierarchyListener);
        
    }
    
    //---------------------------- public methods -------------------------------
    
    /** Adds a new SyntaxParserListener and starts parsing if fresh data not available, otherwise synchronously
     * notifies the added SyntaxParserListener that parsed data are available.*/
    public void addSyntaxParserListener(SyntaxParserListener spl) {
        listeners.add(spl);
        
        synchronized (parsingState) {
            if(isParsing || isScheduled) return ; //we are either parsing or waiting for parser to start - will parse and fire event then
            
            if(parsedElements == null) {
                //we need to run the parser
                restartParser();
            } else {
                //data actual no need to reparse - just synchronously return parsed data
                spl.parsingFinished(createParseResult());
            }
        }
    }
    
    /** Removes the SyntaxParserListener from the listeners list.*/
    public void removeSyntaxParserListener(SyntaxParserListener spl) {
        listeners.remove(spl);
    }
    
    //---------------------------- private methods -------------------------------
    
    private void restartParser() {
        if(!parserTask.isFinished()) {
            parserTask.cancel(); //removes the task from the queue AND INTERRUPTS the thread!
            isParsing = false;
        }
        parserTask.schedule(PARSER_DELAY);
        isScheduled = true;
    }
    
    private void parse() {
        synchronized (parsingState) {
            if(isParsing) {
               return ;
            }
            isParsing = true;
            isScheduled = false;
        }
        
        reallyParse();
        
        notifyParsingFinished();
        
        synchronized (parsingState) {
            isParsing = false;
        }
        
    }
    
    private void reallyParse() {
        parsedElements = new ArrayList<SyntaxElement>();
        try {
            SyntaxElement sel = getElementChain(0);
            while (sel != null) {
                parsedElements.add(sel);
                SyntaxElement next_sel = sel.getNext();
                
                if(next_sel != null && (next_sel == sel || next_sel.getElementOffset() <= sel.getElementOffset())) {
                    Logger.getLogger(SyntaxParser.class.getName()).warning(
                            "The SyntaxParser.getElementChain() cycles. Please report this and attach the document content."); //NOI18N
                    return ;
                }
                
                sel = next_sel;
            }
            
        }catch(BadLocationException ble) {
            ble.printStackTrace();
        }
    }
    
    private void notifyParsingFinished() {
        if(!parsedElements.isEmpty()) {
            List<SyntaxElement> results = createParseResult();
            for(SyntaxParserListener spl : listeners) {
                spl.parsingFinished(results);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<SyntaxElement> createParseResult() {
        return Collections.unmodifiableList((List<SyntaxElement>)parsedElements.clone());
    }
    
    Document getDocument() {
        return doc;
    }
    
    /** Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just after the offset.
     *
     * @param offset offset in document where to search for SyntaxElement
     * @return SyntaxElement surrounding or laying after the offset
     * or <CODE>null</CODE> if there is no element there (end of document)
     */
    public SyntaxElement getElementChain( int offset ) throws BadLocationException {
        ((BaseDocument)doc).readLock();
        try {
            TokenSequence ts = tokenSequence(hi, offset);
            if(ts == null) {
                //we are out of html - go back and try to find an html element
                TokenSequence tseq = hi.tokenSequence();
                tseq.move(offset);
                if(!tseq.movePrevious() && !tseq.moveNext()) {
                    //no token on the position
                    return null;
                }
                int nonHtmlBlockStart = 0;
                //go back until we find an html code
                while(tseq.movePrevious()) {
                    //XXX - just one level embedding
                    TokenSequence htmlTS = tseq.embedded(HTMLTokenId.language());
                    if(htmlTS != null) {
                        if(htmlTS.moveNext() || htmlTS.movePrevious()) {
                            //found html piece
                            nonHtmlBlockStart = htmlTS.offset() + htmlTS.token().length();
                            break;
                        }
                    }
                }
                return getNextElement( nonHtmlBlockStart );
            }
            
            //html token found
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) return null; //no token found
            
            Token item = ts.token();
            
            int beginning = ts.offset();
            
            if( item.id() == HTMLTokenId.CHARACTER ) {
                do {
                    item = ts.token();
                    beginning = ts.offset();
                } while(item.id() == HTMLTokenId.CHARACTER && ts.movePrevious());
                
                // now item is either HTMLSyntax.VALUE or we're in text, or at BOF
                if( item.id() != HTMLTokenId.VALUE && item.id() != HTMLTokenId.TEXT ) {
                    return getNextElement(  beginning );
                } // else ( for VALUE or TEXT ) fall through
            }
            
            if( item.id() == HTMLTokenId.WS || item.id() == HTMLTokenId.ARGUMENT ||     // these are possible only in Tags
                    item.id() == HTMLTokenId.OPERATOR || item.id() == HTMLTokenId.VALUE ) { // so find boundary
                while(ts.movePrevious() && !isTag(item = ts.token()));
                return getNextElement(  item.offset(hi) );       // TAGC
            }
            
            if( item.id() == HTMLTokenId.TEXT ) {
                do {
                    beginning = ts.offset();
                } while ( ts.movePrevious() && (ts.token().id() == HTMLTokenId.TEXT || ts.token().id() == HTMLTokenId.CHARACTER));
                
                return getNextElement(  beginning ); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.SCRIPT) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            if( item.id() == HTMLTokenId.STYLE) {
                //we have just one big token for script
                return getNextElement(  ts.token().offset(hi));
            }
            
            
            if( isTag(item)) {
                if( item.id() == HTMLTokenId.TAG_OPEN ||
                        item.id() == HTMLTokenId.TAG_OPEN_SYMBOL)  return getNextElement(  item.offset(hi) );  // TAGO/ETAGO // NOI18N
                else {
                    do {
                        if(!ts.movePrevious()) {
                            return getNextElement( item.offset(hi));
                        }
                        item = ts.token();
                    } while( item.id() != HTMLTokenId.TAG_OPEN_SYMBOL);
                    
                    return getNextElement(  item.offset(hi) );       // TAGC
                }
            }
            
            if( item.id() == HTMLTokenId.ERROR )
                return new SyntaxElement( this, item.offset(hi), getTokenEnd( hi, item ), SyntaxElement.TYPE_ERROR );
            
            if( item.id() == HTMLTokenId.BLOCK_COMMENT ) {
                while( item.id() == HTMLTokenId.BLOCK_COMMENT && !item.text().toString().startsWith( "<!--" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi)); // from start of Commment
            }
            
            if( item.id() == HTMLTokenId.DECLARATION || item.id() == HTMLTokenId.SGML_COMMENT ) {
                while( item.id() != HTMLTokenId.DECLARATION || !item.text().toString().startsWith( "<!" ) && ts.movePrevious()) { // NOI18N
                    item = ts.token();
                }
                return getNextElement(  item.offset(hi) ); // from start of Commment
            }
        } finally {
            ((BaseDocument)doc).readUnlock();
        }
        return null;
    }
    
    
    SyntaxElement getPreviousElement(int offset) throws javax.swing.text.BadLocationException {
        return offset == 0 ? null
                : getElementChain(offset - 1);
    }
    
    SyntaxElement getNextElement(int offset) throws javax.swing.text.BadLocationException {
        ((BaseDocument)doc).readLock();
        try {
            TokenSequence ts = tokenSequence(hi, offset);
            if(ts == null) {
                //we are out of html - go back and try to find an html element
                TokenSequence tseq = hi.tokenSequence();
                tseq.move(offset);
                if(!tseq.movePrevious() && !tseq.moveNext()) {
                    //no token on the position
                    return  null;
                }
                int nonHtmlBlockEnd = doc.getLength();
                //find end of the non-html block
                while(tseq.moveNext()) {
                    //XXX - just one level embedding
                    TokenSequence htmlTS = tseq.embedded(HTMLTokenId.language());
                    if(htmlTS != null) {
                        //found html piece
                        if(!htmlTS.moveNext()) {
                            return null; //no token in the TS!?!?!
                        }
                        nonHtmlBlockEnd = htmlTS.offset();
                        break;
                    }
                }
                if(offset == nonHtmlBlockEnd) {
                    return null;
                }
                
                return new SyntaxElement(this, offset, nonHtmlBlockEnd, SyntaxElement.TYPE_UNKNOWN);
            }
            
            ts.move(offset);
            if (!ts.moveNext())
                return null;
            org.netbeans.api.lexer.Token item = ts.token();
            int lastOffset = getTokenEnd(hi, item);
            
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.BLOCK_COMMENT) {
                do {
                    lastOffset = getTokenEnd(hi, ts.token());
                } while (ts.token().id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.BLOCK_COMMENT &&
                        ts.moveNext());
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_COMMENT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.DECLARATION) {
                java.lang.StringBuffer sb = new java.lang.StringBuffer(item.text());
                
                while (item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.DECLARATION ||
                        item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.SGML_COMMENT) {
                    lastOffset = getTokenEnd(hi, item);
                    if (!ts.moveNext()) {
                        break;
                    }
                    item = ts.token();
                    if (item.id() ==
                            org.netbeans.api.html.lexer.HTMLTokenId.DECLARATION)
                        sb.append(item.text().toString());
                }
                java.lang.String image = sb.toString();
                
                if (!image.startsWith("<!DOCTYPE"))
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                image = image.substring(9).trim();
                int index = image.indexOf(' ');
                
                if (index < 0)
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration(this,
                            offset,
                            lastOffset,
                            null,
                            null,
                            null);
                java.lang.String rootElem = image.substring(0, index);
                
                image = image.substring(index).trim();
                if (image.startsWith("PUBLIC")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String pi = getQuotedString(sb);
                    
                    if (pi != null) {
                        java.lang.String si = getQuotedString(sb);
                        
                        return new org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                pi,
                                si);
                    }
                } else if (image.startsWith("SYSTEM")) {
                    image = image.substring(6).trim();
                    sb = new java.lang.StringBuffer(image);
                    java.lang.String si = getQuotedString(sb);
                    
                    if (si != null) {
                        return new org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration(this,
                                offset,
                                lastOffset,
                                rootElem,
                                null,
                                si);
                    }
                }
                return new org.netbeans.editor.ext.html.parser.SyntaxElement.Declaration(this,
                        offset,
                        lastOffset,
                        null,
                        null,
                        null);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.ERROR)
                return new SyntaxElement(this, item.offset(hi), lastOffset,
                        SyntaxElement.TYPE_ERROR);
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TEXT ||
                    item.id() == org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER) {
                do {
                    lastOffset = getTokenEnd(hi, item);
                    item = ts.token();
                } while (ts.moveNext() &&
                        (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TEXT ||
                        item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER));
                return new SyntaxElement(this, offset, lastOffset,
                        SyntaxElement.TYPE_TEXT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.SCRIPT) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_SCRIPT);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.STYLE) {
                return new SyntaxElement(this, offset, getTokenEnd(hi, item),
                        SyntaxElement.TYPE_STYLE);
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE || (item.id() ==
                    org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL &&
                    item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                
                if (item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                do {
                    item = ts.token();
                    lastOffset = getTokenEnd(hi, item);
                } while (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.WS &&
                        ts.moveNext());
                if (item.id() ==
                        org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Named(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                } else {
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Named(this,
                            offset,
                            lastOffset,
                            SyntaxElement.TYPE_ENDTAG,
                            name);
                }
            }
            if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN ||
                    (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL &&
                    !item.text().toString().equals("</"))) {
                java.lang.String name = item.text().toString();
                ArrayList<SyntaxElement.TagAttribute> attrs = new ArrayList<SyntaxElement.TagAttribute>();
                
                if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_OPEN_SYMBOL) {
                    ts.moveNext();
                    name = ts.token().text().toString();
                }
                ts.moveNext();
                item = ts.token();
                
                //find tag attributes
                Token attrNameToken = null;
                do {
                    item = ts.token();
                    if (item.id() == HTMLTokenId.ARGUMENT) {
                        //attribute name
                        attrNameToken = item;
                    } else if (item.id() == HTMLTokenId.VALUE && attrNameToken != null) {
                        //found attribute value after attribute name
                        
                        //there may be entity reference inside the attribute value
                        //e.g. onclick="alert('hello&nbsp;world');" which divides the value
                        //into more html tokens
                        StringBuffer value = new StringBuffer();
                        Token t = null;
                        do {
                            t = ts.token();
                            value.append(t.text().toString());
                        } while (ts.moveNext() && (ts.token().id() == HTMLTokenId.VALUE || ts.token().id() == HTMLTokenId.CHARACTER));
                        
                        SyntaxElement.TagAttribute tagAttr =
                                new SyntaxElement.TagAttribute(attrNameToken.text().toString(),
                                value.toString(),
                                attrNameToken.offset(hi),
                                item.offset(hi));
                        attrs.add(tagAttr);
                        attrNameToken = null;
                    }
                    lastOffset = getTokenEnd(hi, item);
                } while ((item.id() == org.netbeans.api.html.lexer.HTMLTokenId.WS ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.ARGUMENT ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.OPERATOR ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.VALUE ||
                        item.id() == org.netbeans.api.html.lexer.HTMLTokenId.CHARACTER) &&
                        ts.moveNext());
                
                if (item.id() == org.netbeans.api.html.lexer.HTMLTokenId.TAG_CLOSE_SYMBOL) {
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Tag(this,
                            offset,
                            getTokenEnd(hi,
                            item),
                            name,
                            attrs,
                            item.text().toString().equals("/>"));
                } else {
                    return new org.netbeans.editor.ext.html.parser.SyntaxElement.Tag(this,
                            offset,
                            lastOffset,
                            name,
                            attrs);
                }
            }
            
        } finally {
            ((BaseDocument)doc).readUnlock();
        }
        return null;
    }
    
    
    public static boolean isTag(Token t) {
        return (( t.id() == HTMLTokenId.TAG_OPEN ) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE ) ||
                ( t.id() == HTMLTokenId.TAG_OPEN_SYMBOL) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL));
    }
    
    public static boolean isTagButNotSymbol(Token t) {
        return (( t.id() == HTMLTokenId.TAG_OPEN) ||
                ( t.id() == HTMLTokenId.TAG_CLOSE));
    }
    
    
    private static int getTokenEnd( TokenHierarchy thi, Token item ) {
        return item.offset(thi) + item.text().length();
    }
    
    /**
     * Beware, changes data
     */
    private static String getQuotedString( StringBuffer data ) {
        int startIndex = 0;
        if (data == null || data.length() == 0) return null;
        while( data.charAt( startIndex ) == ' ' ) startIndex++;
        
        char stopMark = data.charAt( startIndex++ );
        if( stopMark == '"' || stopMark == '\'' ) {
            for( int index = startIndex; index < data.length(); index++ )
                if( data.charAt( index ) == stopMark ) {
                    String quoted = data.substring( startIndex, index );
                    data.delete( 0, index + 1 );
                    return quoted;
                }
        }
        
        return null;
    }
    
    private static TokenSequence tokenSequence(TokenHierarchy hi, int offset) {
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        if(ts == null) {
            //HTML language is not top level one
            ts = hi.tokenSequence();
            ts.move(offset);
            if(!ts.moveNext() && !ts.movePrevious()) {
                return null; //no token found
            } else {
                ts = ts.embedded(HTMLTokenId.language());
            }
        }
        return ts;
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.editor.completion.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.print.attribute.SetOfIntegerSyntax;
import javax.xml.namespace.QName;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Parses the XML using the lexer.
 *
 * Error recovery strategies:
 * 
 * Unclosed tag: pretend the tag is closed and is self-closed, so it ends 
 * immediately. 
 * 
 * Unclosed end tag:
 * emit subsequent child tags (going to level N+1 and deeper). 
 * 
 * The Parser will, in addition to normal instructions, emit also "error" tokens, 
 * which are portions of erroneous content, i.e. tokens after last attribute assignment
 * and before unexpected opening tag.
 * 
 * @author sdedic
 */
public class XmlLexerParser implements ContentLocator {
    public static final String XMLNS_PREFIX = "xmlns";
    public static final String NO_NAMESPACE_PREFIX = "";
    public static final char PREFIX_DELIMITER_CHAR = ':';
    public static final String TAG_CLOSE = ">";
    public static final String OPEN_TAG = "<";
    public static final String OPEN_CLOSING_TAG = "</";
    public static final String SELF_CLOSING_TAG_CLOSE = "/>";
    public static final char TAG_START_CHAR = '<';
    public static final char TAG_END_CHAR = '>';
    public static final char CLOSE_TAG_CHAR = '/';
    
    private LexicalHandler  lexicalHandler;
    private ContentHandler  contentHandler;
    private TokenHierarchy  hierarchy;
    private TokenSequence<XMLTokenId>   seq;
    
    private LinkedList<Level>    levelStack = new LinkedList<Level>();
    
    private int error;
    
    private int errorCount;
    
    private static class Level {
        private String tagQName;
        private Map<String, String> savedPrefixes;
        private Collection<String>  newPrefixes = new LinkedList<String>();
    }
    
    /**
     * Error marker attached to an Element
     */
    public static class ErrorMark {
        /**
         * Offset of the error mark
         */
        private int offset;
        
        /**
         * Length of the offending text
         */
        private int len;
        
        /**
         * Type / classification of the error
         */
        private ErrorType errorType;
        
        /**
         * Custom message, if any
         */
        private String  message;

        public ErrorMark(int offset, int len, ErrorType errorType, String message) {
            this.offset = offset;
            this.len = len;
            this.errorType = errorType;
            this.message = message;
        }
    }

    private enum ErrorType {
        UnexpectedToken,
        DuplicateAttribute,
        UnclosedValue,
        /**
         * Tag content is invalid, e.g. attributes in closing tag
         */
        InvalidTagContent,
        /**
         * Reported on tags, which are not finished, instead a new tag starts
         */
        TagNotFinished,
        /**
         * Reported on artifical END TAGS, which do not correspond to anything in the source.
         * Actually the error is on their counterpart start tags.
         */
        MissingEndTag,
        /**
         * Closing tag which could not be mapped onto a start tag.
         */
        UnexpectedTag,
    }
    
    private List<ErrorMark>     errors = Collections.emptyList();
    
    private Level currentLevel;
    
    private void markError() {
        addError(ErrorType.UnexpectedToken, null);
    }
    
    private void addError(ErrorType type, String msg) {
        if (errors.isEmpty()) {
            errors = new LinkedList<ErrorMark>();
        }
        Token t = seq.token();
        
        ErrorMark mark = new ErrorMark(seq.offset(), t == null ? 1 : t.length(),
                type, msg);
        errors.add(mark);
        errorCount++;
    }
    
    private void resetOffsets() {
        elementOffset = -1;
        if (attrOffsets != null && attrOffsets.isEmpty()) {
            attrOffsets = null;
        }
        flushErrors();
    }
    
    private void flushErrors() {
        errors = Collections.emptyList();
    }

    public XmlLexerParser(TokenHierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setLexicalHandler(LexicalHandler lexicalHandler) {
        this.lexicalHandler = lexicalHandler;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }
    
    private void parseProcessingInstruction() throws SAXException {
        StringBuilder content = new StringBuilder();
        String target = null;

        skipWhitespace();
        
        Token<XMLTokenId> t = nextToken();
        if (t == null || t.id() != XMLTokenId.PI_TARGET) {
            markError();
            return;
        }
        target = t.text().toString();
        consume();
        
        skipWhitespace();
        while ((t = nextToken()) != null) {
            switch (t.id()) {
                case WS:
                    skipWhitespace();
                    break;

                case PI_CONTENT:
                    content.append(t.text().toString());
                    consume();
                    break;
                case PI_END:
                    contentHandler.processingInstruction(target, content.toString().trim());
                    flushErrors();
                    consume();
                    resetOffsets();
                    return;
                    
                default:
                    // bail out, the current processing instruction ends
                    contentHandler.processingInstruction(target, content.toString().trim());
                    // FIXME - add error info
                    resetOffsets();
                    flushErrors();
                    return;
            }
        }
    }
    
    private XMLTokenId  forcedId;
    
    private void handleErrorAs(XMLTokenId id) {
        markError();
        forcedId = id;
    }
    
    private XMLTokenId getTokenId() {
        if (forcedId != null) {
            XMLTokenId id = this.forcedId;
            forcedId = null;
            return id;
        }
        return nextToken().id();
    }
    
    public void parse() throws SAXException {
        seq = hierarchy.tokenSequence();
        seq.move(0);
        parse2();
    }

    public void parse2() throws SAXException {
        boolean whitespacePossible = true;
        
        Token<XMLTokenId> t;
        
        while ((t = nextToken()) != null) {
            XMLTokenId id = getTokenId();
            elementOffset = seq.offset();
            switch (t.id()) {
                case PI_START:
                    consume();
                    parseProcessingInstruction();
                    whitespacePossible = true;
                    break;
                case BLOCK_COMMENT:
                    consume();
                    lexicalHandler.comment(t.text().toString().toCharArray(), 0, t.length());
                    break;
                case CDATA_SECTION:
                    consume();
                    whitespacePossible = false;
                    lexicalHandler.startCDATA();
                    contentHandler.characters(t.text().toString().toCharArray(), 0, t.length());
                    lexicalHandler.endCDATA();
                    break;
                case TAG: {
                    CharSequence cs = t.text();
                    
                    if (cs.charAt(0) == TAG_START_CHAR && cs.charAt(1) == CLOSE_TAG_CHAR) {
                        consume();
                        parseClosingTag(cs.subSequence(2, cs.length()).toString());
                    } else if (cs.charAt(0) != TAG_START_CHAR) {
                        // some error, mark it
                        handleErrorAs(XMLTokenId.TEXT);
                        break;
                    } else {
                        parseTag(t);
                    }
                    
                    whitespacePossible = true;
                    break;
                }
                case CHARACTER:
                    // character entity - will be reported as usual characters data
                case TEXT: {
                    consume();
                    String s = t.text().toString();
                    if (whitespacePossible && s.trim().isEmpty()) {
                        contentHandler.ignorableWhitespace(t.text().toString().toCharArray(), 0, t.length());
                    } else {
                        contentHandler.characters(t.text().toString().toCharArray(), 0, t.length());
                        whitespacePossible = false;
                    }
                    break;
                }
            }
            resetOffsets();
        }
    }
    
    private Map<String, String> prefix2Uri = new HashMap<String, String>();
    
    private void processPrefixMappings() throws SAXException {
        Map<String, String> savedPrefixMap = prefix2Uri;
        Map<String, String> prefixMap = null;
        
        for (String s : attrNames) {
            if (!s.startsWith(XMLNS_PREFIX)) {
                continue;
            }
            
            String prefix = NO_NAMESPACE_PREFIX; // NOI18N
            
            if (s.length() > 5 && s.charAt(5) == PREFIX_DELIMITER_CHAR) {
                prefix = s.substring(6);
            }
            
            if (prefixMap == null) {
                prefixMap = new HashMap<String, String>(savedPrefixMap);
            }
            currentLevel.newPrefixes.add(s);
            prefixMap.put(prefix, attrs.get(s));
            contentHandler.startPrefixMapping(prefix, attrs.get(s));
        }
        if (prefixMap != null) {
            this.prefix2Uri = prefixMap;
        }
        currentLevel.savedPrefixes = savedPrefixMap;
    }
    
    /**
     * Starts element's level. initializes attribute lists etc. Saves the current
     * level on the stack, and defines a new level.
     * 
     */
    private void startLevel() {
        this.attrNames = new ArrayList<String>();
        this.attrOffsets = new HashMap<String, Integer>();
        this.attrs = new HashMap<String, String>();

        this.currentLevel = new Level();
        currentLevel.tagQName = qName;
        currentLevel.savedPrefixes = prefix2Uri;
        levelStack.push(currentLevel);
    }
    
    /**
     * Finishes the element's level. 
     * @throws SAXException 
     */
    private void terminateLevel() throws SAXException {
        if (currentLevel == null) {
            return;
        }
        for (String s : currentLevel.newPrefixes) {
            contentHandler.endPrefixMapping(s);
        }
        prefix2Uri = currentLevel.savedPrefixes;
        levelStack.pop();

        if (levelStack.size() <= levelBound) {
            currentLevel = null;
            return;
        }
        currentLevel = levelStack.peek();
        
        if (currentLevel != null) {
            qName = currentLevel.tagQName;
            String[] qn = parseQName(qName);
            this.tagUri = prefix2Uri.get(qn[0]);
            this.tagName = qn[1];
        } else {
            qName = null;
            tagName = null;
            tagUri = null;
        }
    }
    
    private String[] parseQName(String qn) {
        int pos = qn.indexOf(PREFIX_DELIMITER_CHAR);
        if (pos == -1) {
            return new String[] {"", qn}; // NOI18N
        } else {
            return new String[] { qn.substring(0, pos), qn.substring(pos + 1) };
        }
    }
    
    private int elementOffset;
    
    private String tagName;
    private String tagUri;
    private String qName;
    
    private Map<String, String>   attrs;
    private List<String>          attrNames;
    private Map<String, Integer>  attrOffsets;
    
    private void parseClosingTag(String tagName) throws SAXException {
        this.elementOffset = seq.offset();
        
        Token<XMLTokenId> token;

        skipWhitespace();
        
        qName = tagName;
        
        out:
        while ((token = nextToken()) != null) {
            XMLTokenId id = token.id();
            
            switch (id) {
                case WS:
                    skipWhitespace();
                    break;
                    
                case TAG:
                    String s = token.text().toString();
                    if (s.equals(TAG_CLOSE)) {
                        consume();
                    } else {
                       addError(ErrorType.TagNotFinished, null);
                    }
                    break out;
                    
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                    // just mark error, but consume and ignore
                    addError(ErrorType.InvalidTagContent, null);
                    consume();
                    break;
                    
                default:
                    markError();
                    break out;
            }
        }

        processTagName(tagName);
        if (levelStack.size() <= levelBound || this.qName.equals(currentLevel.tagQName)) {
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            return;
        }

        // case 1: the tagName may equal to a tag on the stack. This would indicate
        // one or more unclosed tags, which should be reported as "closed - error"
        if (findUpperOpening(tagName)) {

            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            return;
        }
        
        // ignore the tag completely; the content handler does not have a callback for this,
        // just ignore the end tag completely, and go on. If this tag was a mismatch for an existing open tag,
        // the next close tag will be hopefully found and the incorrectly opened open tag will be closed.
        addError(ErrorType.UnexpectedTag, tagName);
    }
    
    /**
     * Attempts to find a place in the stack, which actually matches the text.
     * Matches the closing tags in the text to the stack
     * 
     * @param tagName
     * @return
     * @throws SAXException 
     */
    private boolean findUpperOpening(String tagName) throws SAXException {
        int depth = 0;
        Level found = null;
        
        for (Iterator<Level> it = levelStack.iterator(); it.hasNext(); ) {
            Level l = it.next();
            if (l.tagQName.equals(tagName)) {
                found = l;
                break;
            }
            depth++;
        }
        if (found == null) {
            return false;
        }
        
        if (depth == 1) {
            // simple case, the immediate parent matches the closing tagname
            // mark error, close this level etc
            addError(ErrorType.MissingEndTag, null);
            String[] nsName = parseQName(currentLevel.tagQName);
            contentHandler.endElement(prefix2Uri.get(nsName[0]), nsName[1], currentLevel.tagQName);
            terminateLevel();
            flushErrors();
            return true;
        }
        
        // some additional assuarance is needed before popping more levels:
        // parse ahead and match 2* popped levels or root
        
        int markSequence = seq.offset();
        
        XmlLexerParser parser = duplicate();
        parser.consume();
        
        ParentCollector pm = new ParentCollector((depth - 1) * 2);
        parser.setContentHandler(pm);
        
        boolean success = false;
        try {
            parser.parse2();
        } catch (StopParseException ex) {
            // expected
        } finally {
            seq.move(markSequence);
            seq.moveNext();
        }
        
        Deque<String>    followingClosingTags = pm.closingTags;
        followingClosingTags.addLast(tagName);
        
        // try to match / find match on the stack:
        int count = 0;
        
        for (int stackPtr = 0; stackPtr < levelStack.size() - followingClosingTags.size(); stackPtr++) {
            Iterator<String> it = followingClosingTags.descendingIterator();
            boolean ok = true;
            for (int i = stackPtr; i >= 0 && it.hasNext(); i++) {
                String s = it.next();
                Level l = levelStack.get(i);
                if (!l.tagQName.equals(s)) {
                    // break the for cycle, move next
                    ok = false;
                    break;
                }
            }
            
            if (ok) {
                // stackPtr is the place 
                success = true;
                count = stackPtr;
                break;
            }
        }

        if (!success) {
            // no match in upper hierarchy
            return false;
        }

        for (int i = 0; i <= count; i++) {
            markError();
            contentHandler.endElement(tagUri, this.tagName, qName);
            terminateLevel();
            processTagName(qName = currentLevel.tagQName);
            flushErrors();
        }
        
        return true;
    }
    
    static class StopParseException extends SAXException {
        private boolean success;

        public StopParseException(boolean success) {
            this.success = success;
        }
        
    }
    
    /**
     * Scan forward the token stream, and collect the closing tags up to 
     * 'matchLevels' count. 
     */
    private static class ParentCollector implements ContentHandler {
        int matchLevels;
        Deque<String> closingTags = new LinkedList<String>();
        int levelsBelow;
        
        ParentCollector(int matchLevels) {
            this.matchLevels = matchLevels;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            levelsBelow++;
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (levelsBelow > 0) {
                levelsBelow--;
                return;
            }
            closingTags.push(qName);
            if (closingTags.size() == matchLevels) {
                throw new StopParseException(true);
            }
        }

        @Override
        public void setDocumentLocator(Locator locator) {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void endDocument() throws SAXException {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException {
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException {
        }

        @Override
        public void skippedEntity(String name) throws SAXException {
        }
    }
    
    private XmlLexerParser duplicate() {
        XmlLexerParser parser = new XmlLexerParser(hierarchy);
        parser.seq = seq;
        parser.levelStack = new LinkedList<Level>(levelStack);
        parser.levelBound = levelStack.size();
        
        return parser;
    }
    
    private int levelBound;
    
    private Token<XMLTokenId> currentToken;
    
    private Token<XMLTokenId> nextToken() {
        if (currentToken == null) {
            if (seq.moveNext()) {
                return currentToken = seq.token();
            } else {
                return null;
            }
        } else {
            return currentToken;
        }
    }
    
    private void consume() {
        currentToken = null;
    }
    
    private void skipWhitespace() {
        Token<XMLTokenId> t;
        
        while ((t = nextToken()) != null && t.id() == XMLTokenId.WS) {
            consume();
        }
    }
    
    private boolean parseAttribute(Token<XMLTokenId> t) {
        String argName = t.text().toString();
        boolean ignore;
        
        if (attrs.containsKey(argName)) {
            addError(ErrorType.DuplicateAttribute, null);
            ignore = true;
        } else {
            attrOffsets.put(argName, seq.offset());
            attrNames.add(argName);
            attrs.put(argName, null);
            ignore = false;
        }

        consume();
        skipWhitespace();
        t = nextToken();
        if (t == null || t.id() != XMLTokenId.OPERATOR) {
            markError();
            return false;
        }
        consume();
        skipWhitespace();
        t = nextToken();
        if (t == null ||t.id() != XMLTokenId.VALUE) {
            markError();
            return false;
        }
        
        CharSequence s = t.text();
        char quote = s.charAt(0);
        if (quote == '\'' || quote == '"') { // NOI18N
            if (s.charAt(t.length() - 1) == quote) {
                s = s.subSequence(1, s.length() - 1);
            } else {
                s = s.subSequence(1, s.length() - 1);
            }
        }
        if (!ignore) {
            attrs.put(argName, s.toString());
        }
        consume();
        return true;
    }
    
    private void processTagName(String tagName) {
        String [] qn = parseQName(tagName);
        this.tagUri = prefix2Uri.get(qn[0]);
        this.tagName = qn[1];
    }
    
    /**
     * Handles stat tag that appears in a tagname. Suppose the user did not yet type
     * the closing brace (>). Report tag open with attributes.
     */
    private void errorStartTagInTagName() throws SAXException {
        markError();
    }
    
    /**
     * Makes only sense within tag processing
     */
    private boolean selfClosed;
    
    private void markUnexpectedToken() {
        markError();
    }
    
    private void parseTag(Token<XMLTokenId> t) throws SAXException {
        String tagMark = t.text().toString();
        
        selfClosed = false;
        
        // shorten the tagName so it does not contain the <
        tagMark = tagMark.substring(1);
        if (tagMark.endsWith(SELF_CLOSING_TAG_CLOSE)) {
            selfClosed = true;
            tagMark = tagMark.substring(0, tagMark.length() - 2);
        }
        consume();
        qName = tagMark;

        startLevel();
        
        boolean inError = false;

        out:
        while ((t = nextToken()) != null) {
            XMLTokenId id = t.id();
            
            switch (id) {
                case WS:
                    consume();
                    break;
                case ARGUMENT:
                    if (parseAttribute(t)) {
                        inError = false;
                    }
                    break;
                    
                case TAG: {
                    // expect end tag
                    CharSequence cs = t.text();
                    if (cs.charAt(0) == TAG_START_CHAR) {
                        // some error - bail out
                        errorStartTagInTagName();
                        // report tag start as usual
                        break out;
                    }
                    if (cs.charAt(cs.length() - 1) != TAG_END_CHAR) {
                        // does not begin with tag start, does not end with tag end - how is it
                        // possible it is a tag ?
                        throw new IllegalStateException("Invalid tag text: " + cs);
                    } else if (cs.charAt(0) == CLOSE_TAG_CHAR) {
                        selfClosed = true;
                    }
                    consume();
                    break out;
                }
                    
                case ERROR:
                    markUnexpectedToken();
                    consume();
                    // try to recover
                    inError = true;
                    break;
                    
                default:
                    break out;
            }
        }
        
        handleStartElement(selfClosed);
    }
    
    private void handleStartElement(boolean selfClosed) throws SAXException {
        processPrefixMappings();
        processTagName(qName);
        
        contentHandler.startElement(tagUri, tagName, qName, 
                new Attrs(attrNames, attrs, prefix2Uri));
        flushErrors();
        if (selfClosed) {
            contentHandler.endElement(tagUri, tagName, qName);
            terminateLevel();
        }
    }
    
    @Override
    public int getElementOffset() {
        return elementOffset;
    }

    @Override
    public int getAttributeOffset(String attribute) {
        if (attrOffsets == null) {
            return -1;
        }
        Integer i = attrOffsets.get(attribute);
        return i == null ? -1 : i;
    }

    @Override
    public int hasError() {
        return error;
    }

    private static final class Attrs implements Attributes {
        private List<String>            order;
        private Map<String, String>     attributes;
        private Map<String, String>     prefi2URI;

        public Attrs(List<String> order, Map<String, String> attributes, Map<String, String> prefi2URI) {
            assert order.size() == attributes.size();
            this.order = order;
            this.attributes = attributes;
            this.prefi2URI = prefi2URI;
        }
        
        
        
        @Override
        public int getLength() {
            return order.size();
        }
        
        private String n(int index) {
            return order.get(index);
        }

        @Override
        public String getURI(int index) {
            String n = n(index);
            int i = n.indexOf(PREFIX_DELIMITER_CHAR);
            if (i == -1) {
                return null;
            }
            return prefi2URI.get(n.substring(0, i));
        }

        @Override
        public String getLocalName(int index) {
            String n = n(index);
            int i = n.indexOf(PREFIX_DELIMITER_CHAR);
            if (i == -1) {
                return n;
            }
            return n.substring(i + 1);
        }

        @Override
        public String getQName(int index) {
            return n(index);
        }

        @Override
        public String getType(int index) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getValue(int index) {
            return attributes.get(n(index));
        }

        @Override
        public int getIndex(String uri, String localName) {
            return order.indexOf(qn(uri, localName));
        }

        @Override
        public int getIndex(String qName) {
            return order.indexOf(qName);
        }

        @Override
        public String getType(String uri, String localName) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getType(String qName) {
            return "CDATA"; // NOI18N
        }

        @Override
        public String getValue(String uri, String localName) {
            return attributes.get(qn(uri, localName));
        }

        @Override
        public String getValue(String qName) {
            return attributes.get(qName);
        }
        
        private String qn(String uri, String localName) {
            String qn = localName;
            String prefix = uri2prefix(uri);
            if (prefix != null) {
                qn = prefix + PREFIX_DELIMITER_CHAR + localName;
            }
            return qn;
        }
        
        private String uri2prefix(String uri) {
            for (Map.Entry<String, String> en : prefi2URI.entrySet()) {
                if (uri.equals(en.getValue())) {
                    return en.getKey();
                }
            }
            return null;
        }
    
    }
}

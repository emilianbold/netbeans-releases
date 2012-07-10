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

import com.sun.istack.internal.NotNull;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.api.whitelist.WhiteListQuery.Result;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.text.syntax.dom.SyntaxNode;
import org.netbeans.spi.editor.completion.CompletionResultSet;

/**
 * Provides information about the soruce, caret position etc to the individual
 * Completers. It is created for each invocation of the CC and shared between
 * Completers. The infrastructure can alter state between Completer invocations,
 * e.g. to provide custom state flags.
 *
 * @author sdedic
 */
public final class CompletionContext {
    /**
     * Caret offset where the completion was invoked
     */
    private int caretOffset;
    
    /**
     * The completion prefix, typed in the current token
     */
    private String prefix;
    
    /**
     * Offset at which the replacement is supposed to happen
     */
    private int startOffset = -1;
    
    /**
     * Length of the tail of the token after the caret, possibly replaced
     */
    private int tokenTail;
    
    /**
     * For processing instructions, this is the target of the instruction
     */
    private String piTarget;
    
    /**
     * The data / content for processing instructions
     */
    private String piData;
    
    private String tagName;
    
    private int tagStartOffset;
    
    private int rootTagStartOffset;
    
    private Type type;
    
    private ClasspathInfo   cpInfo;
    
    private CompilationInfo compilationInfo;

    private Document doc;
    
    private WhiteListQuery.WhiteList    typeWhiteList;
    
    /**
     * Insertion offset for additional root element attributes
     */
    private int rootAttrInsertOffset;
    
    /**
     * True, if the current tag is the root one
     */
    private boolean currentIsRoot;
    
    public enum Type {

        /**
         * The context is not known, probably empty completion
         */
        UNKNOWN,
        /**
         * The user is trying to type in a root element, processing instruction
         * or a bean
         */
        ROOT,
        /**
         * The user is trying to type a processing instruction target
         */
        INSTRUCTION_TARGET,
        /**
         * Data for processing instruction, the behavour depends on the
         * instruction target
         */
        INSTRUCTION_DATA,
        /**
         * Unspecified child element: can be either a Bean or a property element,
         * depending on further analysis
         */
        CHILD_ELEMENT,
         
        /**
         * Bean element
         */
        BEAN,
        
        /**
         * Property element
         */
        PROPERTY_ELEMENT,
        
        /**
         * Property attribute
         */
        PROPERTY,
        
        /**
         * Property value contents
         */
        PROPERTY_VALUE,
        
        /**
         * Variable completion inside property elements or attributes
         */
        VARIABLE,
        
        /**
         * Variable binding inside property values
         */
        BINDING,
        
        /**
         * Reference to bundles inside property values
         */
        BUNDLE_REF,
        
        /**
         * Reference to resources inside property values
         */
        RESOURCE_REF,
    }

    public CompletionContext(CompletionResultSet set, Document doc, int offset) {
        this.doc = doc;
        this.caretOffset = offset;
    }
    
    CompletionContext(TokenHierarchy h, int offset) {
        this.caretOffset = offset;
        processTokens(h);
    }
    
    public void init(TokenHierarchy h, CompilationInfo info) {
        this.compilationInfo = info;
        this.cpInfo = info.getClasspathInfo();
        processTokens(h);
    }
    
    private void processTokens(TokenHierarchy h) {
        TokenSequence ts = h.tokenSequence();
        processType(ts);
        processValueType();
        
        readRootElement(ts);
        readCurrentContent(ts);

        processNamespaces();
    }
 
    /**
     * Returns the text/x-java compilation info.
     * The CompilationInfo can be used to execute java queries.
     * 
     * @return CompilationInfo instance
     */
    @NotNull
    public CompilationInfo getCompilationInfo() {
        return compilationInfo;
    }
    
    /**
     * Returns the classpath info for the project.
     * 
     * @return classpath info
     */
    @NotNull
    public ClasspathInfo getClasspathInfo() {
        return cpInfo;
    }

    void setClasspathInfo(ClasspathInfo cpInfo) {
        this.cpInfo = cpInfo;
    }
    
    /**
     * Provides completion type, see {@link Type}.
     * @return completion type
     */
    public Type getType() {
        return type;
    }

    /**
     * Offset of the caret in the editor
     * @return caret offset
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    /**
     * The prefix the user has typed. The prefix can be used to narrow searches
     * for classes and/or fields. The prefix contains the text from the start of the
     * token till the caret offset. Note: the prefix starts at the TOKEN start, so for
     * e.g. BEAN type, the prefix ALSO contains "<".
     * @return 
     */
    public String getPrefix() {
        return prefix;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getTokenTail() {
        return tokenTail;
    }

    public String getPiTarget() {
        return piTarget;
    }

    public String getPiData() {
        return piData;
    }

    public String getTagName() {
        return tagName;
    }

    public int getTagStartOffset() {
        return tagStartOffset;
    }
    
    public int getEndOffset() {
        return getCaretOffset() + getTokenTail();
    }
    
    public int getReplaceLength() {
        return getEndOffset() - getStartOffset();
    }
    
    private int tagEndOffset = -1;
    
    private boolean tagClosed;
    
    private boolean selfClosed;

    public Document getDoc() {
        return doc;
    }

    public int getTagEndOffset() {
        return tagEndOffset;
    }

    public boolean isTagClosed() {
        return tagClosed;
    }

    public boolean isSelfClosed() {
        return selfClosed;
    }
    
    private void markUnclosed(int offendingContent) {
        this.tagClosed = false;
        this.selfClosed = false;
        this.tagEndOffset = offendingContent;
    }
    
    public boolean isProcessingInstruction() {
        switch (type) {
            case INSTRUCTION_DATA:
            case INSTRUCTION_TARGET:
//            case ROOT:
                return true;
        }
        return false;
    }
    
    public boolean isAttribute() {
        switch (type) {
            case BINDING:
            case BUNDLE_REF:
            case PROPERTY:
            case PROPERTY_VALUE:
            case RESOURCE_REF:
            case VARIABLE:
                return true;
        }
        return false;
    }
    
    /**
     * Determines if the thing being completed can be a tag name
     * @return true, if tag name is being completed
     */
    public boolean isTag() {
        switch (type) {
            case BEAN:
            case CHILD_ELEMENT:
            case PROPERTY_ELEMENT:
//            case ROOT:
                return true;
        }
        return false;
    }

    /**
     * Scans the processing instruction, reads target and data if it present
     * 
     * @param seq 
     */
    private void readPIContent(TokenSequence seq) {
        boolean cont = true;
        Token t;
        
        while (cont && seq.moveNext()) {
            t = seq.token();
            XMLTokenId id = (XMLTokenId)t.id();
            switch (id) {
                case TAG:
                    markUnclosed(seq.offset());
                    return;

                // OK for tag content, not OK for PI
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                case CHARACTER:
                case BLOCK_COMMENT:
                case CDATA_SECTION:
                case DECLARATION:
                case TEXT:
                case PI_START:
                case ERROR:
                    markUnclosed(seq.offset());
                    return;

                // not OK for tag
                case PI_TARGET:
                    this.piTarget = t.text().toString();
                    break;
                    
                case PI_CONTENT:
                    this.piData = t.text().toString();
                    break;

                case PI_END:
                    selfClosed = true;
                    tagClosed = true;
                    tagEndOffset = seq.offset() + t.length();
                    return;
                    
                // this is OK for all
                case WS:
                    break;
                default:
                    markUnclosed(seq.offset());
                    return;
            }
        }
    }
    
    public boolean isRootElement() {
        return currentIsRoot;
    }

    public int getRootAttrInsertOffset() {
        return rootAttrInsertOffset;
    }
    
    /**
     * Finds a prefix for the nsURi
     * @return prefix for the URI or {@code null} if URI was not declared
     */
    public String findNsPrefix(String nsUri) {
        return rootNamespacePrefixes.get(nsUri);
    }
    
    /**
     * Finds a suitable non conflicting prefix string for the namespace URI.
     * If the URI is already declared, returns the existing prefix. It will return
     * either the value of 'suggested' parameter, or 'suggested' with some unique
     * suffix or an existing prefix for the URI
     * 
     * @param nsURI namespace URI
     * @param suggested suggested prefix
     * @return suitable prefix
     */
    public String findPrefixString(@NotNull String nsURI, @NotNull String suggested) {
        String existing = findNsPrefix(nsURI);
        if (existing != null) {
            return existing;
        }
        boolean repeat = false;
        int counter = 1;
        String pref = suggested;
        
        while (repeat) {
            repeat = false;
            for (String s : rootNamespacePrefixes.values()) {
                if (s.equals(pref)) {
                    pref = suggested + counter;
                    counter++;
                    repeat = true;
                    break;
                }
            }
        }
        
        return pref;
    }
    
    private Map<String, String> rootNamespacePrefixes = new HashMap<String, String>();
    
    private static final String NAMESPACE_PREFIX = "xmlns:"; // NOI18N
    
    private void processNamespaces() {
        for (String s : rootAttributes.keySet()) {
            if (s.startsWith(NAMESPACE_PREFIX)) {
                String nsPrefix = s.substring(NAMESPACE_PREFIX.length());
                String uri = rootAttributes.get(s).valueContent;
                
                rootNamespacePrefixes.put(uri, nsPrefix);
            }
        }
    }
    
    private void copyToRoot() {
        rootTagStartOffset = tagStartOffset;
        rootAttributes = attributes;
        
        attributes = Collections.emptyMap();
        tagStartOffset = -1;
        tagClosed = false;
        selfClosed = false;
    }
    
    /**
     * Skips to and reads the root Element. Reads root element's attributes,
     * so we can detect whether required namespace(s) are present.
     * 
     * This MUST be called prior to readCurrentContent().
     * @param s 
     */
    private void readRootElement(TokenSequence seq) {
        seq.move(0);
        while (seq.moveNext()) {
            Token t = seq.token();
            XMLTokenId id = (XMLTokenId)t.id();
            if (id == XMLTokenId.TAG) {
                int startOffset = seq.offset();
                readTagContent(seq);
                // reassign stuff:
                copyToRoot();
                rootAttrInsertOffset = startOffset + t.length();
                if (t.text().charAt(t.length() - 1) == '>') {
                    rootAttrInsertOffset--;
                    if (t.length() > 2 && t.text().charAt(t.length() - 2) == '/') {
                        rootAttrInsertOffset--;
                    }
                }
                findRootInsertionPoint();
                return;
            }
        }
    }
    
    private void findRootInsertionPoint() {
        if (!rootAttributes.isEmpty()) {
            int min = Integer.MAX_VALUE;

            for (ArgumentInfo ai : rootAttributes.values()) {
                min = Math.min(min, ai.argStart);
            }
            rootAttrInsertOffset = min;
        }
    }
    
    static class ArgumentInfo {
        private int argStart;
        private int valueStart;
        private int valueEnd;
        private String valueContent;

        public ArgumentInfo(int argStart, int valueStart, int valueEnd, String valContent) {
            this.argStart = argStart;
            this.valueStart = valueStart;
            this.valueEnd = valueEnd;
            this.valueContent = valContent;
        }
    }
    
    private static final ArgumentInfo NO_ARGUMENT = new ArgumentInfo(-1, -1, -1, null);
    
    private Map<String, ArgumentInfo> attributes = Collections.emptyMap();
    private Map<String, ArgumentInfo> rootAttributes = Collections.emptyMap();
    
    ArgumentInfo attr(String aName) {
        ArgumentInfo ai = attributes.get(aName);
        return ai != null ? ai : NO_ARGUMENT;
    }
    
    public Enumeration<String> attributeNames() {
        return Collections.enumeration(attributes.keySet());
    }
    
    public String value(String arg) {
        return attr(arg).valueContent;
    }
    
    public String createNSName(String prefix, String name) {
        if (prefix == null) {
            return name;
        } else {
            return prefix + ":" + name; // NOI18N
        }
    }
    
    public String attributeName(String nsURI, String name) {
        if (nsURI == null) {
            return name;
        }
        String prefix = findNsPrefix(nsURI);
        if (prefix == null) {
            return null;
        }
        return prefix + ":" + name; // NOI18N
    }
    
    public boolean contains(String a) {
        return attr(a).argStart != -1;
    }
    
    public int attrStart(String a) {
        return attr(a).argStart;
    }
    
    public int valueStart(String a) {
        return attr(a).valueStart;
    }
    
    public int valueEnd(String a) {
        return attr(a).valueEnd;
    }
    
    private void readCurrentContent(TokenSequence seq) {
        if (tagStartOffset == -1) {
            return;
        }
        int diff = seq.move(tagStartOffset);
        if (diff > 0) {
            throw new IllegalStateException();
        }
        boolean cont = true;
        
        if (!seq.moveNext()) {
            return;
        }
        Token t = seq.token();
                
        if (t.id() == XMLTokenId.TAG) {
            // the tag can be self-closed, without any arguments:
            if (t.text().toString().endsWith("/>")) {
                tagClosed = true;
                tagEndOffset = seq.offset() + t.length();
                selfClosed = true;
                return;
            }
            if (rootTagStartOffset == seq.offset()) {
                currentIsRoot = true;
            }
            readTagContent(seq);
        } else if (t.id() == XMLTokenId.PI_START) {
            readPIContent(seq);
        }
        
    }
    
    /**
     * Reads the TokenSequence forward, and reach the end of the tag, or 
     * the processing instruction.
     * If the tag is not terminated, ie some TEXT will appear, or another tag token,
     * the search terminates, and the tag is recorded as unterminated
     */
    private void readTagContent(TokenSequence seq) {
        attributes = new HashMap<String, ArgumentInfo>();
        String argName = null;
        int argStart = -1;
        Token t;
        while (seq.moveNext()) {
            t = seq.token();
            XMLTokenId id = (XMLTokenId)t.id();
            switch (id) {
                case TAG:
                    CharSequence s = t.text();
                    if (s.charAt(0) == '<') {
                        // unterminated tag
                        markUnclosed(seq.offset());
                        return;
                    } else if (s.charAt(s.length() - 1) == '>') {
                        // end tag marker
                        tagClosed = true;
                        tagEndOffset = seq.offset() + s.length();
                        selfClosed = s.length() >= 2 && s.charAt(s.length() - 2) == '/';
                        return;
                    }

                // OK for tag content, not OK for PI
                case ARGUMENT:
                    argName = t.text().toString();
                    argStart = seq.offset();
                    break;
                case VALUE:
                    if (argName != null) {
                        attributes.put(argName, new ArgumentInfo(argStart, 
                                seq.offset(), seq.offset() + t.length(), t.text().toString()));
                    }
                    break;
                case OPERATOR:
                    break;

                // these are neither OK for tag or PI
                case CHARACTER:
                case BLOCK_COMMENT:
                case CDATA_SECTION:
                case DECLARATION:
                case TEXT:
                case ERROR:
                    markUnclosed(seq.offset());
                    return;
 
                // not OK for tag
                case PI_TARGET:
                case PI_CONTENT:
                case PI_END:
                    markUnclosed(seq.offset());
                    return;
                    
                // this is OK for all
                case WS:
                    break;
                    
                case PI_START:
            }
        }
    }

    private void processValueType() {
        if (type != Type.PROPERTY_VALUE) {
            return;
        }
        
        if (!prefix.isEmpty()) {
            char c = prefix.charAt(0);
            if (c == '\'' || c == '"') {
                prefix = prefix.substring(1);
            }
        }
        
        if (prefix.startsWith("@")) {
            type = Type.RESOURCE_REF;
        } else if (prefix.startsWith("%")) {
            type = Type.BUNDLE_REF;
        } else if (prefix.startsWith("${")) {
            type = Type.BINDING;
        } else if (prefix.startsWith("$")) {
            type = Type.VARIABLE;
        }
    }
    
    private boolean isTextContent(XMLTokenId id) {
        return id == XMLTokenId.TEXT || id == XMLTokenId.CDATA_SECTION || id == XMLTokenId.CHARACTER;
    }
    
    private void setTextContentBoundaries(TokenSequence ts) {
        while (ts.movePrevious()) {
            Token t = ts.token();
            
            XMLTokenId id = (XMLTokenId)t.id();
            if (isTextContent(id)) {
                break;
            }
        }
        int start = ts.offset() + ts.token().length();
        
        while (ts.moveNext()) {
            Token t = ts.token();
            
            XMLTokenId id = (XMLTokenId)t.id();
            if (isTextContent(id)) {
                break;
            }
        }
        int end = ts.offset();
        
        this.startOffset = start;
        this.tokenTail = end - caretOffset;
    }

    private void processType(TokenSequence ts) {
        int diff = ts.move(caretOffset);
        boolean hasToken;
        boolean middle = diff > 0;

        if (middle) {
            // we are in the middle of some token, scan that token first
            hasToken = ts.moveNext();
        } else {
            hasToken = ts.movePrevious();
        }

        boolean wsFound = false;
        Token t = null;

        while (type == null && hasToken) {
            t = ts.token();
            if (middle) {
                tokenTail = t.length() - diff;
            }
            XMLTokenId id = (XMLTokenId) t.id();

            switch (id) {
                case PI_END:
                    // was after PI, PI or Bean is allowed; no completion 
                    type = middle ? Type.UNKNOWN : Type.ROOT;
                    break;

                case PI_CONTENT:
                    type = Type.INSTRUCTION_DATA;
                    break;

                case PI_TARGET:
                    type = Type.INSTRUCTION_TARGET;
                    piTarget = t.text().toString();
                    break;

                case PI_START:
                    type = Type.INSTRUCTION_TARGET;
                    break;
                    
                case VALUE:
                    type = Type.PROPERTY_VALUE;
                    break;

                case OPERATOR:
                    type = Type.PROPERTY_VALUE;
                    startOffset = caretOffset;
                    prefix = ""; // NOI18N
                    break;

                case CHARACTER:
                case CDATA_SECTION:
                case TEXT:
                    // text in between tags; end tag should be suggested by XML completion itself, 
                    // check if all the text is whitespace and if not, do not suggest anything,
                    // as the content is likely a value
                    String nonWh = t.text().toString().trim();
                    if (!nonWh.isEmpty()) {
                        if (nonWh.startsWith("<")) {
                            type = Type.BEAN;
                            break;
                        }
                        // some content; assume it is a property value
                        type = Type.PROPERTY_VALUE;
                        
                        // traverse back to the 1st nonWhite character, record start position and length of the token
                        setTextContentBoundaries(ts);
                        break;
                    }
                    // fall through to whitespace
                case WS:
                    wsFound = true;
                    middle = false;
                    // replacement will start at the caret offset, whitespaces preserved
                    startOffset = caretOffset;
                    prefix = ""; // NOI18N
                    break;

                case TAG:
                    CharSequence s = t.text();
                    if (s.length() == 1 && s.charAt(0) == '>') {
                        // after the ending > of a tag
                        type = Type.CHILD_ELEMENT;
                        break;
                    }
                    if (s.length() < 2) {
                        throw new IllegalArgumentException();
                    }
                    if (s.charAt(1) == '/') {
                        // end tag, no completion:
                        type = Type.UNKNOWN;
                        break;
                    }
                    s = s.subSequence(1, s.length());
                    type = isClassTagName(s) ? Type.BEAN : Type.PROPERTY_ELEMENT;
                    
                    int l = s.length();
                    if (s.length() > 1 && s.charAt(l - 2) == '/') {
                        l--;
                    }
                    tagName = s.subSequence(0, l).toString();
                    break;

                case ARGUMENT:
                    type = Type.PROPERTY;
            }
            if (type == null) {
                hasToken = ts.movePrevious();
            }
        }

        if (!wsFound && prefix == null) {
            if (diff > 0) {
                prefix = t.text().subSequence(0, diff).toString();
            } else if (ts.offset() < caretOffset) {
                // assume preceding token
                
                prefix = t.toString();
            }
        }
        if (startOffset == -1) {
            startOffset = ts.offset();
        }
        if (wsFound) {
            switch (type) {
                case INSTRUCTION_TARGET: type = Type.INSTRUCTION_DATA; break;
                    
                case BEAN:
                case PROPERTY_ELEMENT: type = Type.PROPERTY; break;
                    
                case PROPERTY_VALUE: type = Type.PROPERTY; break;
            }
        }
        
        // traverse back to reach the opening tag or processing instruction:
        boolean cont = true;
        while (ts.movePrevious() && cont) {
            t = ts.token();
            XMLTokenId id = (XMLTokenId)t.id();
            switch (id) {
                case TAG:
                    CharSequence s = t.text();
                    if (s.length() == 1 && s.charAt(0) == '>') {
                        // closing >, go on
                        break;
                    }
                    if (s.charAt(1) != '/' && s.charAt(t.length() - 1) != '>') {
                        tagName = s.subSequence(1, s.length() - 1).toString();
                        tagStartOffset = ts.offset();
                    }
                    cont = false;
                    break;
                
                case PI_END:
                    cont = false;
                    break;
                    
                case PI_START:
                    tagStartOffset = ts.offset();
                    cont = false;
                    break;
                            
                case TEXT:
                case BLOCK_COMMENT:
                    cont = false;
                    break;
                    
                case PI_TARGET:
                    piTarget = t.text().toString();
                    // fall through
                case PI_CONTENT:
                case ARGUMENT:
                case OPERATOR:
                case VALUE:
                case WS:
                    break;
            }
        }
    }
    
    private static boolean isClassTagName(CharSequence s) {
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '.') {
                if (i < s.length() - 1) {
                    char c = s.charAt(i + 1);
                    return Character.isUpperCase(c);
                }
            }
        }
        return Character.isUpperCase(s.charAt(0));
    }
    
    public boolean isBlackListed(Element elem) {
        if (getCompilationInfo().getElements().isDeprecated(elem)) {
            return true;
        }
        if (typeWhiteList == null) {
            return false;
        }
        Result r = typeWhiteList.check(ElementHandle.create(elem), WhiteListQuery.Operation.USAGE);
        return r != null && !r.isAllowed();
    }
}

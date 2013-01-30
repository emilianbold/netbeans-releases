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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.Element;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext.CompletionType;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRoot;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil.DocRootAttribute;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.openide.util.Lookup;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helps in populating the completion list.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionContextImpl extends CompletionContext {
    public static final String PREFIX                   = "ns"; //NOI18N
    public static final String XSI_SCHEMALOCATION       = "schemaLocation"; //NOI18N
    public static final String XSI_NONS_SCHEMALOCATION  = "noNamespaceSchemaLocation"; //NOI18N
    private static final String XSD_TARGET_NAMESPACE = "targetNamespace"; // NOI18N

    private static final Logger _logger = Logger.getLogger(CompletionContextImpl.class.getName());


    private int completionAtOffset = -1;
    private FileObject primaryFile;
    private String typedChars;
    private TokenItem token;
    private SyntaxElement element;
    private String attribute;
    private DocRoot docRoot;
    private char lastTypedChar;
    private CompletionType completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
    private List<QName> pathFromRoot;
    /**
     * Tags on the path from root to the context element (the one the CC tries to fill)
     */
    private List<Tag> elementsFromRoot;
    private Map<String, String>  schemaLocationMap = new HashMap<String, String>();
    private String schemaLocation;
    private String noNamespaceSchemaLocation;
    private String defaultNamespace;
    private BaseDocument document;
    private HashMap<String, CompletionModel> nsModelMap =
            new HashMap<String, CompletionModel>();
    private List<CompletionModel> noNSModels =
            new ArrayList<CompletionModel>();
    private HashMap<String, String> declaredNamespaces =
            new HashMap<String, String>();
    private HashMap<String, String> suggestedNamespaces =
            new HashMap<String, String>();
    private HashMap<String, String> specialNamespaceMap =
            new HashMap<String, String>();
    private CompletionModel noNamespaceModel;
    private transient List<String> existingAttributes;
    private boolean specialCompletion;
    /**
     * HACK: target namespace for XML schemas. See defect #212972
     */
    private String targetNamespace;

    /**
     * Creates a new instance of CompletionQueryHelper
     */
    public CompletionContextImpl(FileObject primaryFile, XMLSyntaxSupport support,
        int offset) {
        try {
            this.completionAtOffset = offset;
            this.primaryFile = primaryFile;
            this.document = support.getDocument();
            this.element = support.getElementChain(offset);
            this.token = support.getPreviousToken(offset);
            this.docRoot = CompletionUtil.getDocRoot(document);
            this.lastTypedChar = support.lastTypedChar();
            populateNamespaces();            
        } catch(Exception ex) {
            //in the worst case, there won't be any code completion help
            _logger.log(Level.SEVERE, ex.getMessage());
        }
    }
    
    ////////////////START CompletionContext Implementations////////////////
    @Override
    public CompletionType getCompletionType() {
        return completionType;
    }
        
    @Override
    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    @Override
    public List<QName> getPathFromRoot() {
        return pathFromRoot;
    }
    
    @Override
    public FileObject getPrimaryFile() {
        return primaryFile;
    }
            
    @Override
    public BaseDocument getBaseDocument() {
        return document;
    }
    
    @Override
    public HashMap<String, String> getDeclaredNamespaces() {
        return declaredNamespaces;
    }
    
    @Override
    public String getTypedChars() {
        return typedChars;
    }    
    ////////////////END CompletionContext Implementations////////////////
    
    public boolean isSchemaAwareCompletion() {
        return (schemaLocation != null) || (noNamespaceSchemaLocation != null);
    }
    
    /**
     * Namespaces suggested by the context, but NOT DECLARED yet in the document
     * 
     * @return suggested namespaces
     */
    public Map<String, String> getSuggestedNamespace() {
        return Collections.unmodifiableMap(suggestedNamespaces);
    }

    public List<URI> getSchemas() {
        List<URI> uris = new ArrayList<URI>();
        if(schemaLocation != null) {
            CompletionUtil.loadSchemaURIs(schemaLocation, uris, schemaLocationMap);
            return uris;
        }
        if(noNamespaceSchemaLocation != null) {
            CompletionUtil.loadSchemaURIs(noNamespaceSchemaLocation, uris, null);
            return uris;
        }                        
        return uris;
    }
    
    /**
     * Extracts prefix from the tagname, returns "" or {@code null} if there's no
     * prefix
     * 
     * @param tagName tag name string, incl. possible prefix (DOM Level 1)
     * @param empty if true, "" is returned for no prefix.
     * @return namespace prefix, "" or {@code null) for no prefix
     */
    private String getPrefix(String tagName, boolean empty) {
        int index = tagName.indexOf(':');
        if (index == -1) {
            return empty ? "" : null;
        }
        return tagName.substring(index + 1);
    }
    
    /**
     * Adds namespaces from the tag to this context, possibly overriding namespaces
     * from previously added tags. Tags should be added starting from the root down
     * to the context position.
     */
    private void addNamespacesFrom(Tag e) {
        NamedNodeMap attrs = e.getAttributes();
        String nodePrefix = getPrefix(e.getTagName(), false);
        String version = null;
        String xsltAttrName = null;
        
        for (int i = attrs.getLength() - 1; i >= 0; i--) {
            Node n = attrs.item(i);
            if (n.getNodeType() == Node.ATTRIBUTE_NODE) {
                Attr a = (Attr)n;
                String attrName = a.getName();
                String value = a.getValue();
                addNamespace(attrName, value, nodePrefix);

            
                if(value.trim().equals("http://www.w3.org/1999/XSL/Transform")) { //NOI18N
                    xsltAttrName = attrName;
                }
                if(CompletionUtil.getLocalNameFromTag(attrName).
                        equals("version")) { //NOI18N
                    version = value.trim();
                }
            } 
        }
        
        if (xsltAttrName != null && "2.0".equals(version)) {
            String prefix = getPrefix(xsltAttrName, false);
            if (prefix == null) {
                // override nonNS location because nonNS schema is XSLT 2.0
                noNamespaceSchemaLocation = "http://www.w3.org/2007/schema-for-xslt20.xsd";
            } else {
                addSchemaLocation(prefix + " http://www.w3.org/2007/schema-for-xslt20.xsd"); //NOI18N
            }
        }
    }

    /**
     * Initializes the declared namespaces from the context path.
     * Adds all declared namespaces from context element and its parents - these are
     * in scope and will form {@link #declaredNamespaces} map.
     * 
     * @param stack path from the context element (index 0) to the root (index N-1).
     */
    private void addContextNamespaces(List<Tag> stack) {
        // must iterate from root down to the context element, to properly override
        // namespaces and replace default/noNamespace information.
        for (int i = stack.size() - 1; i >= 0; i--) {
            Tag t = stack.get(i);
            addNamespacesFrom(t);
        }
    }

    /**
     * Processes an attribute for namespace-related stuff. Detects and sets
     * {@link #schemaLocation}, {@link #noNamespaceSchemaLocation}, {@link #defaultNamespace} URIs.
     * Should be called for elements starting from root down to the context element for
     * proper overriding
     */
    private void addNamespace(String attrName, String value, String nodePrefix) {
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;

        if(CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSI_SCHEMALOCATION)) {
            schemaLocation = value.trim();
            return;
        } 
        if(CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSI_NONS_SCHEMALOCATION)) {
            noNamespaceSchemaLocation = value.trim();
            return;
        }  
        if (CompletionUtil.getLocalNameFromTag(attrName).
                equals(XSD_TARGET_NAMESPACE)) {
            targetNamespace = value.trim();
            return;
        }

        if(! attrName.startsWith(XMLConstants.XMLNS_ATTRIBUTE))
            return;            

        if(attrName.equals(defNS)) {
            this.defaultNamespace = value;
        }
        declaredNamespaces.put(attrName, value);
    }
    
    private void addSchemaLocation(String s) {
        if (schemaLocation == null) {
            schemaLocation = s;
        } else {
            schemaLocation = schemaLocation + " " + s; // NO18N
        }
    }
    
    /**
     * Keeps all namespaces along with their prefixes in a HashMap.
     * This is obtained from the root element's attributes, with
     * the attribute value(namespace) as the key and name with prefix
     * as the value.
     * For example the hashmap may look like this
     *  KEY                                    VALUE
     *  http://www.camera.com                  xmlns:c
     *  http://www.nikon.com                   xmlns:n
     */
    private void populateNamespaces() {
        if(docRoot == null)
            return;
        //Check if the tag has any prefix. If yes, the defaultNamespace
        //is the one with this prefix.
        String tagName = docRoot.getName();
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;
        String temp = CompletionUtil.getPrefixFromTag(tagName);
        if(temp != null) defNS = defNS+":"+temp; //NOI18N
        List<DocRootAttribute> attributes = docRoot.getAttributes();
        
        String version = null;
        boolean xsltDeclared = false;
        
        for(int index=0; index<attributes.size(); index++) {
            DocRootAttribute attr = attributes.get(index);
            String attrName = attr.getName();
            String value = attr.getValue();
            addNamespace(attrName, value, temp);

            //resolve xsl stylesheets w/o the schema location specification.
            //In such case the root element only contains the xmlns:xsl=""http://www.w3.org/1999/XSL/Transform"
            //along with the version attribute.
            //If such ns is declared and the version is 2.0 then use the 
            //http://www.w3.org/2007/schema-for-xslt20.xsd schema for the completion model
            
            if(attr.getValue().trim().equals("http://www.w3.org/1999/XSL/Transform")) { //NOI18N
                xsltDeclared = true;
            }
            if(CompletionUtil.getLocalNameFromTag(attrName).
                    equals("version")) { //NOI18N
                version = attr.getValue().trim();
            }
        }
        
        if(schemaLocation == null && xsltDeclared && "2.0".equals(version)) {
            //only the second "token" from the schemaLocation is considered as the schema
            schemaLocation = "schema http://www.w3.org/2007/schema-for-xslt20.xsd"; //NOI18N
        }
        
    }

    private TokenSequence getTokenSequence() {
        TokenSequence tokenSequence = null;
        try {
            ((AbstractDocument) document).readLock();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get(document);
            tokenSequence = tokenHierarchy.tokenSequence();
        } catch(Exception e) {
            _logger.log(Level.WARNING,
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
        } finally {
            ((AbstractDocument) document).readUnlock();
        }
        return tokenSequence;
    }

    private boolean isTagAttributeRequired(TokenSequence tokenSequence) {
        int caretPos = completionAtOffset;

        int diff = tokenSequence.move(caretPos);
        tokenSequence.moveNext();

        Token tok = tokenSequence.token();
        if (tok == null) return false;
        
        TokenId tokID = tok.id();
        if (tokID.equals(XMLTokenId.TAG) && CompletionUtil.isEndTagSuffix(tok) &&
           (tokenSequence.offset() + 1 == caretPos)) { // <... /|>, | - a caret position
            return false;
        }
        boolean 
            isAttributeOrSpace = tokID.equals(XMLTokenId.ARGUMENT) ||
                                 tokID.equals(XMLTokenId.WS),
            isTagLastCharFound = tokID.equals(XMLTokenId.TAG) &&
                                 (CompletionUtil.isTagLastChar(tok) ||
                                  CompletionUtil.isEndTagSuffix(tok)),
            //this may happen when there's a lexical error inside the tag itself,
            //for example in this valid case: <tag att|> or in something errorneous
            //like: <tag att|#$#$#>
            isJustBeforeTagErrorToken 
                                = tokID.equals(XMLTokenId.ERROR) && diff == 0;
        
        while (true) {
            if (tokID.equals(XMLTokenId.TAG)) {
                if (CompletionUtil.isEndTagPrefix(tok)) break;
                else {
                    String tagName = CompletionUtil.getTokenTagName(tok);
                    if (tagName != null) {
                        int tokOffset = tokenSequence.offset(),
                            tagNameEndPos = tokOffset + CompletionUtil.TAG_FIRST_CHAR.length() +
                                            tagName.length();
                        if ((tagNameEndPos < caretPos) && 
                            (isAttributeOrSpace || isTagLastCharFound || isJustBeforeTagErrorToken)) {
                            return true;
                        }
                    }
                }
            }
            if (! tokenSequence.movePrevious()) break;
            
            tok = tokenSequence.token();
            tokID = tok.id();
            if (CompletionUtil.isEndTagSuffix(tok) || CompletionUtil.isTagLastChar(tok)) break;
        }
        return false;
    }

    /**
     * At a given context, that is, at the current cursor location
     * in the document, finds the type of query that needs to be
     * carried out and finds the path from root.
     */
    public boolean initContext() {
        boolean res = doInitContext();
        if (pathFromRoot != null) {
            addContextNamespaces(elementsFromRoot);
        } else {
            populateNamespaces();
        }
        return res;
    }
    
    private boolean doInitContext() {
        TokenSequence tokenSequence = getTokenSequence();
        try {
            if (isTagAttributeRequired(tokenSequence)) {
                completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                typedChars = token.getTokenID().equals(XMLDefaultTokenContext.WS) ? null : token.getImage();
                createPathFromRoot(element);
                return true;
            }
            
            int id = token.getTokenID().getNumericID();
            switch (id) {
                //user enters < character
                case XMLDefaultTokenContext.TEXT_ID:
                    String chars = token.getImage().trim();
                    String previousTokenText = token.getPrevious() == null ? 
                            "" :token.getPrevious().getImage().trim();
                    if(chars != null && chars.startsWith("&")) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }                    
                    if (chars != null && chars.equals("") && //previousTokenText.equals("/>")) {
                        previousTokenText.endsWith(">")) {
                        //completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        createPathFromRoot(element);
                        break;
                    }                    
                    if(chars != null && chars.startsWith("<")) {
                        typedChars = chars.substring(1);
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        createPathFromRoot(element);
                        break;
                    }
                    if (chars != null && previousTokenText.equals(">")) {
                        if(!chars.equals("") && !chars.equals(">"))
                            typedChars = chars;
                        createPathFromRoot(element);
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT_VALUE;
                        break;
                    }
                    if (chars != null && !chars.equals("<") && previousTokenText.equals(">")) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }
                    break;

                case XMLDefaultTokenContext.BLOCK_COMMENT_ID:
                    completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                    createPathFromRoot(element);
                    break;

                //start tag of an element
                case XMLDefaultTokenContext.TAG_ID:
                    if(element instanceof EndTag) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }
                    if (element instanceof EmptyTag) {
                        /*
                        if (token != null &&
                            token.getImage().trim().equals("/>")) {
                            completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                            break;
                        }
                        */
                        EmptyTag tag = (EmptyTag) element;
                        if ((element.getElementOffset() + 1 == completionAtOffset) ||
                            (token.getOffset() + token.getImage().length() == completionAtOffset)) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            createPathFromRoot(element.getPrevious());
                            break;
                        }
                        if (completionAtOffset > element.getElementOffset() + 1 &&
                            completionAtOffset <= (element.getElementOffset() + 1 +
                                                  tag.getTagName().length())) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            int index = completionAtOffset - element.getElementOffset() - 1;
                            typedChars = index < 0 ? tag.getTagName() :
                                tag.getTagName().substring(0, index);
                            createPathFromRoot(element.getPrevious());
                            break;
                        }                        
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???pathFromRoot = getPathFromRoot(element);
                        break;
                    }
                    
                    if(element instanceof StartTag) {
                        if(token != null &&
                           token.getImage().trim().equals(">")) {
                            createPathFromRoot(element);
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT_VALUE;
                            break;
                        }
                        if(element.getElementOffset() + 1 == this.completionAtOffset) {
                            typedChars = null;
                        } else {
                            StartTag tag = (StartTag)element;
                            int index = completionAtOffset-element.getElementOffset()-1;
                            typedChars = index<0?tag.getTagName() :
                                tag.getTagName().substring(0, index);
                        }
                    }
                    completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                    createPathFromRoot(element.getPrevious());
                    break;

                //user enters an attribute name
                case XMLDefaultTokenContext.ARGUMENT_ID:
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???typedChars = token.getImage();
//***???pathFromRoot = getPathFromRoot(element);
                    break;

                //some random character
                case XMLDefaultTokenContext.CHARACTER_ID:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
                    
                //user enters = character, we should ignore all other operators
                case XMLDefaultTokenContext.OPERATOR_ID:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
                    
                //user enters either ' or "
                case XMLDefaultTokenContext.VALUE_ID: {
                    //user enters start quote and no end quote exists
                    if(token.getNext() == null) {
                        if(lastTypedChar == '\'' || lastTypedChar == '\"')
                            typedChars = null;
                        else 
                            typedChars = token.getImage().substring(1,
                                token.getImage().indexOf(">"));
                    }                    
                    
                    //user is inside start/end quotes
                    if(lastTypedChar != '\'' && lastTypedChar != '\"') {
                        String str = token.getImage();
                        if( str != null && !str.equals("\"\"") && !str.equals("\'\'") &&
                            (str.startsWith("\"") || str.startsWith("\'")) &&
                            (str.endsWith("\"") || str.endsWith("\'")) ) {
                            typedChars = str.substring(1, str.length()-1);
                            if(completionAtOffset == token.getOffset()+1)
                                typedChars = "";
                        }
                    }
                    attribute = findAttributeName();
                    completionType = attribute == null ?
                            CompletionType.COMPLETION_TYPE_UNKNOWN : 
                            CompletionType.COMPLETION_TYPE_ATTRIBUTE_VALUE;
                    createPathFromRoot(element);
                    break;
                }

                //user enters white-space character
                case XMLDefaultTokenContext.WS_ID:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    TokenItem prev = token.getPrevious();
                    while( prev != null &&
                           (prev.getTokenID().getNumericID() == XMLDefaultTokenContext.WS_ID) ) {
                            prev = prev.getPrevious();
                    }
                    if( (prev.getTokenID().getNumericID() == XMLDefaultTokenContext.VALUE_ID) ||
                        (prev.getTokenID().getNumericID() == XMLDefaultTokenContext.TAG_ID) ) {
                        //no attr completion for end tags
                        if (prev.getImage().startsWith("</")) break;
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???pathFromRoot = getPathFromRoot(element);
                    }
                    break;

                default:
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
            }
        } catch (Exception e) {
            _logger.log(Level.INFO,
                e.getMessage() == null ? e.getClass().getName() : e.getMessage(), e);
            return false;
        }
        // TODO - complete namespaces based on 'pathToRoot'
        
        return true;        
    }
    
    /**
     * Assumes that the current token is at the attribute name, operator or
     * somewhere in the attribute value. In all cases, attempt to return the
     * context attribute name.
     * 
     * @return attribute name, or <code>null</code> in unexpected situations
     */
    private String findAttributeName() {
        TokenItem item = token;
        while (item != null) {
            int tid = item.getTokenID().getNumericID();
            switch (tid) {
                case XMLDefaultTokenContext.VALUE_ID:
                case XMLDefaultTokenContext.OPERATOR_ID:
                case XMLDefaultTokenContext.WS_ID:
                case XMLDefaultTokenContext.TEXT_ID:
                    item = item.getPrevious();
                    break;
                case XMLDefaultTokenContext.ARGUMENT_ID:
                    return item.getImage();
                default:
                    return null;
            }
        }
        return null;
    }

    public List<DocRootAttribute> getDocRootAttributes() {
        return docRoot.getAttributes();
    }
    
    public DocRoot getDocRoot() {
        return docRoot;
    }
    
    public String getAttribute() {
        return attribute;
    }
        
    /**
     * This is a 2-pass algorithm. In the 1st pass, it pushes all the relevant
     * start tags all the way to the root into the stack, root being at the top.
     * In the 2nd pass, it gets items from the stack in reverse order and checks
     * if any intermediate item can be treated as root. This is possible when dealing
     * with items from multiple namespaces.
     */
    private void createPathFromRoot(SyntaxElement se) {
        //1st pass
        if(se == null)
            return;
        Stack<Tag> stack = new Stack<Tag>();
        if(se instanceof EmptyTag)
            stack.push((Tag)se);
        while( se != null) {
            if( (se instanceof EndTag) ||
                (se instanceof StartTag && stack.isEmpty()) ) {
                stack.push((Tag)se);
                se = se.getPrevious();
                continue;
            }
            if(se instanceof StartTag) {
                StartTag start = (StartTag)se;
                if(stack.peek() instanceof EndTag) {
                    EndTag end = (EndTag)stack.peek();
                    if(end.getTagName().equals(start.getTagName())) {
                        stack.pop();
                    }
                } else {
                    stack.push((Tag)se);
                }
            }
            se = se.getPrevious();
        }
        
        this.elementsFromRoot = stack;
        //2nd pass
        this.pathFromRoot = createPath(stack);
    }    

    /**
     * Peeks items from the stack in reverse order and checks if that tag is a
     * root. If yes, it returns the path. If no, continues to the next tag.
     * While creating the path it always adds items to the start of the list so
     * that the returned path starts from root, all the way to the current tag.
     */
    private ArrayList<QName> createPath(Stack<Tag> stack) {
        ArrayList<QName> path = new ArrayList<QName>();
        ListIterator<Tag> tags = stack.listIterator();
        while(tags.hasNext()) {
            Tag tag = tags.next();
            //add to the start of the list
            path.add(0, createQName(tag));
            if(isRoot(tag, tags.hasNext()?tags.next():null)) {
                return path;
            }
            tags.previous();//since we moved twice.
        }
        //CompletionUtil.printPath(path);
        return path;
    }       
    
    /**
     * If namespace differs from previous, then this one is considered a root.
     * However, there are exceptions to this and may not work well for cases when
     * you combine itmes from schemas with/without namespace.
     */
    private boolean isRoot(Tag thisTag, Tag previousTag) {
        //no previous => this has to be the root
        if(previousTag == null)
            return true;
        
        //if the tag declares a namespace and is diff from default, then it is root
        String prefix = CompletionUtil.getPrefixFromTag(thisTag.getTagName());
        Attr namespaceAttr = null;
        if(prefix==null) {
            namespaceAttr = thisTag.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE);
        } else {
            namespaceAttr = thisTag.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
        }
        if(namespaceAttr != null) {
            String namespace = namespaceAttr.getValue();
            if(!namespace.equals(defaultNamespace)) {
                //see if it declares a schemaLocation or noNamespaceSchemaLocation
                String sl = getAttributeValue(thisTag, XSI_SCHEMALOCATION);
                if(sl != null)
                    this.schemaLocation = sl;
                String nnsl = getAttributeValue(thisTag, XSI_NONS_SCHEMALOCATION);
                if(nnsl != null)
                    this.noNamespaceSchemaLocation = nnsl;
                if(prefix != null)
                    declaredNamespaces.put(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix, namespace);
                return true;
            }
        }
        
        return !fromSameNamespace(thisTag, previousTag);
    }
    
    private String getAttributeValue(Tag tag, String attrName) {
        NamedNodeMap attrs = tag.getAttributes();
        for(int i=0; i<attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            String name = attr.getNodeName();
            if(name!= null && name.contains(attrName)) {
                return attr.getNodeValue();
            }
        }
        return null;
    }
    
    /**
     * This is onlyused from createPath() for the path from the current element to the root.
     * It's possible to use elementsFromRoot so that content need not to be lexed (so much)
     * as with navigation using tag.getParent().
     * @param tag
     * @return 
     */
    private QName createQName(Tag tag) {
        String tagName = tag.getTagName();
        String prefix = CompletionUtil.getPrefixFromTag(tagName);
        String lName = CompletionUtil.getLocalNameFromTag(tagName);     
        
        int index = elementsFromRoot.indexOf(tag);
        if (index == -1) {
            throw new IllegalStateException();
        }
        for (int i = index; i < elementsFromRoot.size(); i++) {
            Tag t = elementsFromRoot.get(i);
            if (prefix == null) {
                Attr attrNode = t.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE);
                if (attrNode != null) {
                    return new QName(attrNode.getValue(), lName);
                }
            } else {
                Attr attrNode = t.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
                if(attrNode != null) {
                    return new QName(attrNode.getValue(), lName, prefix); //NOI18N
                }
            }
        }
        if (prefix == null) {
            return new QName(defaultNamespace, lName);
        } else {
            return new QName(null, lName, prefix);
        }
    }
    
    /**
     * Determines if this and previous tags are from same namespaces.
     */
    private boolean fromSameNamespace(Tag current, Tag previous) {
        String prevPrefix = CompletionUtil.getPrefixFromTag(previous.getTagName());
        String thisPrefix = CompletionUtil.getPrefixFromTag(current.getTagName());
        String thisNS = (thisPrefix == null) ? declaredNamespaces.get(
            XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+thisPrefix);
        String prevNS = (prevPrefix == null) ? declaredNamespaces.get(
            XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prevPrefix);
        
        return (thisNS == null && prevNS == null) ||
               (thisNS != null && thisNS.equals(prevNS)) ||
               (prevNS != null && prevNS.equals(thisNS));
    }
                        
    /**
     * Returns the active no namespace model.
     */
    public CompletionModel getActiveNoNSModel() {
        return noNamespaceModel;
    }
    
    /**
     * Returns the CompletionModel map.
     * Maps target namespaces to the CompletionModels
     */
    public HashMap<String, CompletionModel> getCompletionModelMap() {
        return nsModelMap;
    }
    
    /**
     * Returns the list of no namespace CompletionModels.
     */
    public List<CompletionModel> getNoNamespaceModels() {
        return noNSModels;
    }
    
    /**
     * Returns the combined list of CompletionModels.
     */
    public List<CompletionModel> getCompletionModels() {
        List<CompletionModel> models = new ArrayList<CompletionModel>();
        models.addAll(nsModelMap.values());
        models.addAll(noNSModels);
        return models;
    }

    public void addCompletionModel(CompletionModel cm) {
        String tns = cm.getTargetNamespace();
        if(tns == null && !noNSModels.contains(cm)) {
            noNSModels.add(cm);
            return;
        }
        if(nsModelMap.get(tns) == null)
            nsModelMap.put(tns, cm);
    }
    
    /**
     * Finds all CompletionModelProviders and builds a model map for schemas having TNS
     * and builds a list for all no namespace models.
     */
    public boolean initModels() {
        Lookup.Template templ = new Lookup.Template(CompletionModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        if(impls == null || impls.size() == 0)
            return false;
        //first try all providers
        for(Object obj: impls) {
            CompletionModelProvider modelProvider = (CompletionModelProvider)obj;
            List<CompletionModel> models = modelProvider.getModels(this);
            if(models == null || models.size() == 0)
                continue;
            for(CompletionModel m: models) {
                populateModelMap(m);
            }
        }
        
        if(noNamespaceSchemaLocation != null && noNSModels.size() == 1) {
            noNamespaceModel = noNSModels.get(0);
        }
        
        //last resort: try special completion
        specialCompletion();
        
        return !(nsModelMap.size() == 0 && noNSModels.size() == 0);
    }
    
    private void populateModelMap(CompletionModel m) {
        if (m != null) {
            SchemaModel sm = m.getSchemaModel();
            if (sm != null) {
                Schema schema = sm.getSchema();
                if (schema != null) {
                    String tns = schema.getTargetNamespace();
                    if (tns != null) {
                        //models with namespaces
                        nsModelMap.put(tns, m);
                        return;
                    }
                }
            }
        }
        //
        noNSModels.add(m); //no namespace models
    }
    
    /**
     *  Special completion comes into play when all register providers fail.
     *  And we try to provide completion for docs like project.xml.
     */
    private void specialCompletion() {
        //instance documents with neither schemaLocation nor
        //noNamespaceSchemaLocation attribute, e.g. project.xml
        if(primaryFile == null)
            return;
        
//        specialCompletion = true;
        for(String prefix : declaredNamespaces.keySet()) {
            String temp = declaredNamespaces.get(prefix);
            try {
                if (nsModelMap.containsKey(temp)) {
                    // ignore, was added from specific location
                    continue;
                }
                if (schemaLocationMap.get(temp) != null) {
                    // ignore; already processed by default provider from schemaLocation attribute
                    continue;
                }
                if (temp.equals(targetNamespace)) {
                    // ignore: the NS is a targetNamespace defined by this schema
                    continue;
                }
                CompletionModel cm = DefaultModelProvider.getCompletionModel(new java.net.URI(temp), true, this);
                if (cm != null) {
                    populateModelMap(cm);
                    continue;
                }
            } catch (Exception ex) {
                _logger.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }
    
    /**
     * Lets first try with "xmlns:ns1". If not used, use it. If used, we
     * keep trying with ns2, ns3 etc.
     */
    String suggestPrefix(String tns) {
        if(tns == null)
            return null;

        if (specialNamespaceMap.containsKey(tns)) {
            return specialNamespaceMap.get(tns);
        }

        //if the tns is already present in declared namespaces,
        //return the prefix
        for(String key : getDeclaredNamespaces().keySet()) {
            String ns = getDeclaredNamespaces().get(key);
            if(ns.equals(tns))
                return key;
        }

        //then try to look that up in the suggested namespace
        for(String key : suggestedNamespaces.keySet()) {
            String ns = suggestedNamespaces.get(key);
            if(ns.equals(tns))
                return key;
        }
        
        int index = suggestedNamespaces.size() + 1;
        String prefix = PREFIX + index;
        String nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+prefix;
        while(getDeclaredNamespaces().get(nsDecl) != null) {
            prefix = PREFIX + index++;
            nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":" + prefix;
        }        
        suggestedNamespaces.put(prefix, tns);        
        return prefix;
    }
    
    public boolean isPrefixBeingUsed(String prefix) {
        return getDeclaredNamespaces().
                get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix) != null;
    }
    
    public boolean isSpecialCompletion() {
        return specialCompletion;
    }
    
    public boolean canReplace(String text) {
        if(completionType == CompletionType.COMPLETION_TYPE_ELEMENT && element instanceof Tag) {
            String name = ((Tag)element).getTagName();
            if(name != null && name.equals(typedChars) && text.equals(name))
                return false;
        }
        if(completionType == CompletionType.COMPLETION_TYPE_ATTRIBUTE) {
            Element e = CompletionUtil.findAXIElementAtContext(this);
            for(AbstractAttribute a : e.getAttributes()) {
                if(a.getName().equals(typedChars))
                    return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns target namespace for a given prefix.
     */
    public String getTargetNamespaceByPrefix(String prefix) {
        for(CompletionModel cm : getCompletionModelMap().values()) {
            if(prefix.equals(cm.getSuggestedPrefix()))
                return cm.getTargetNamespace();
        }
        
        return null;
    }
        
    /**
     * Issue 108636 : Eliminate existing attibutes.
     */
    List<String> getExistingAttributes() {
        if(existingAttributes != null)
            return existingAttributes;
        existingAttributes = new ArrayList<String>();
        TokenItem item = token.getPrevious();
        while(item != null) {
            if(item.getTokenID().getNumericID() ==
                    XMLDefaultTokenContext.TAG_ID)
                break;
            if(item.getTokenID().getNumericID() ==
                    XMLDefaultTokenContext.ARGUMENT_ID) {
                existingAttributes.add(item.getImage());
            }
            item = item.getPrevious();
        }
        return existingAttributes;
    }
}
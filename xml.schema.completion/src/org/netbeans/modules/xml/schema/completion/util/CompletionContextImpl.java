/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
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

    public List<URI> getSchemas() {
        List<URI> uris = new ArrayList<URI>();
        if(schemaLocation != null) {
            CompletionUtil.loadSchemaURIs(schemaLocation, uris, false);
            return uris;
        }
        if(noNamespaceSchemaLocation != null) {
            CompletionUtil.loadSchemaURIs(noNamespaceSchemaLocation, uris, true);
            return uris;
        }                        
        return uris;
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
        for(int index=0; index<attributes.size(); index++) {
            DocRootAttribute attr = attributes.get(index);
            String attrName = attr.getName();
            if(CompletionUtil.getLocalNameFromTag(attrName).
                    equals(XSI_SCHEMALOCATION)) {
                schemaLocation = attr.getValue().trim();
                continue;
            }
            if(CompletionUtil.getLocalNameFromTag(attrName).
                    equals(XSI_NONS_SCHEMALOCATION)) {
                noNamespaceSchemaLocation = attr.getValue().trim();
                continue;
            }            
            if(! attrName.startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                continue;            
            if(attrName.equals(defNS))
                this.defaultNamespace = attr.getValue();
            declaredNamespaces.put(attrName, attr.getValue());
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

        tokenSequence.move(caretPos);
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
                                  CompletionUtil.isEndTagSuffix(tok));
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
                            (isAttributeOrSpace || isTagLastCharFound)) {
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
        TokenSequence tokenSequence = getTokenSequence();
        try {
            if (isTagAttributeRequired(tokenSequence)) {
                completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                pathFromRoot = getPathFromRoot(element);
                return true;
            }
            
            int id = token.getTokenID().getNumericID();
            switch (id) {
                //user enters < character
                case XMLDefaultTokenContext.TEXT_ID:
                    String chars = token.getImage().trim(),
                           previousTokenText = token.getPrevious().getImage().trim();
                    if(chars != null && chars.startsWith("&")) {
                        completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        break;
                    }                    
                    if (chars != null && chars.equals("") && //previousTokenText.equals("/>")) {
                        previousTokenText.endsWith(">")) {
                        //completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        pathFromRoot = getPathFromRoot(element);
                        break;
                    }                    
                    if(chars != null && chars.startsWith("<")) {
                        typedChars = chars.substring(1);
                        completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                        pathFromRoot = getPathFromRoot(element);
                        break;
                    }
                    if (chars != null && previousTokenText.equals(">")) {
                        if(!chars.equals("") && !chars.equals(">"))
                            typedChars = chars;
                        pathFromRoot = getPathFromRoot(element);
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
                    pathFromRoot = getPathFromRoot(element);
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
                        if (CompletionUtil.isCaretInsideTag(completionAtOffset, tokenSequence)) {
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???pathFromRoot = getPathFromRoot(element);
                            break;
                        }
                        if ((element.getElementOffset() + 1 == completionAtOffset) ||
                            (token.getOffset() + token.getImage().length() == completionAtOffset)) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            pathFromRoot = getPathFromRoot(element.getPrevious());
                            break;
                        }
                        if (completionAtOffset > element.getElementOffset() + 1 &&
                            completionAtOffset <= (element.getElementOffset() + 1 +
                                                  tag.getTagName().length())) {
                            completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                            int index = completionAtOffset - element.getElementOffset() - 1;
                            typedChars = index < 0 ? tag.getTagName() :
                                tag.getTagName().substring(0, index);
                            pathFromRoot = getPathFromRoot(element.getPrevious());
                            break;
                        }                        
//***???completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
//***???pathFromRoot = getPathFromRoot(element);
                        break;
                    }
                    
                    if(element instanceof StartTag) {
                        if(token != null &&
                           token.getImage().trim().equals(">")) {
                            pathFromRoot = getPathFromRoot(element);
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
                    pathFromRoot = getPathFromRoot(element.getPrevious());
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
                    attribute = element.getPrevious().toString();                    
                    completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE_VALUE;
                    pathFromRoot = getPathFromRoot(element);
                    TokenItem t = token;
                    while(t != null) {
                        int nId = t.getTokenID().getNumericID();
                        if(nId == XMLDefaultTokenContext.ARGUMENT_ID) {
                            attribute = t.getImage();
                            break;
                        }
                        t = t.getPrevious();
                    }
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
        return true;        
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
    private List<QName> getPathFromRoot(SyntaxElement se) {
        //1st pass
        if(se == null)
            return null;
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
        
        //2nd pass
        return createPath(stack);
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
        
        //handle no namespace
        if(defaultNamespace == null)
            return false;
        
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
    
    private QName createQName(Tag tag) {
        QName qname = null;
        String tagName = tag.getTagName();
        String prefix = CompletionUtil.getPrefixFromTag(tagName);
        String lName = CompletionUtil.getLocalNameFromTag(tagName);        
        if(prefix == null) {
            Attr attrNode = tag.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE);
            if(attrNode == null) {
                qname = new QName(defaultNamespace, lName);
            } else {
                String ns = attrNode.getValue();
                qname = new QName(ns, lName);
            }
        } else {
            //first try ns declaration in the tag
            Attr attrNode = tag.getAttributeNode(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
            if(attrNode != null) {
                String ns = attrNode.getValue();
                qname = new QName(ns, lName, prefix); //NOI18N
            } else {
                qname = new QName(declaredNamespaces.
                        get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix), lName, prefix); //NOI18N
            }
        }
        return qname;
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
        if(nsModelMap.size() == 0 && noNSModels.size() == 0)
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
        
        specialCompletion = true;
        specialNamespaceMap = CompletionUtil.getNamespacesFromStartTags(document);
        for(String temp : specialNamespaceMap.keySet()) {
            try {
                DefaultModelProvider provider = new DefaultModelProvider(this);
                CompletionModel cm = provider.getCompletionModel(new java.net.URI(temp), false);
                populateModelMap(cm);
            } catch (Exception ex) {
                _logger.log(Level.INFO, null, ex);
                continue; //continue with the next one
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
        
        if(isSpecialCompletion()) {
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
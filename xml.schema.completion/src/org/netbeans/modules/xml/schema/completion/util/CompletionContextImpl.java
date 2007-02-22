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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext.CompletionType;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.openide.util.Lookup;

/**
 * Helps in populating the completion list.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionContextImpl extends CompletionContext {
        
    /**
     * Creates a new instance of CompletionQueryHelper
     */
    public CompletionContextImpl(FileObject primaryFile,
            XMLSyntaxSupport support, int offset) {
        try {
            this.primaryFile = primaryFile;
            this.document = support.getDocument();
            this.element = support.getElementChain(offset);
            this.token = support.getPreviousToken(offset);
            this.docRoot = CompletionUtil.getRoot(element);
            this.lastTypedChar = support.lastTypedChar();
            populateNamespaces();            
        } catch(Exception ex) {
            //in the worst case, there will not be
            //any code completion help.
        }
    }
    
    ////////////////START CompletionContext Implementations////////////////
    public CompletionType getCompletionType() {
        return completionType;
    }
        
    public String getDefaultNamespace() {
        return defaultNamespace;
    }
    
    public List<QName> getPathFromRoot() {
        return pathFromRoot;
    }
    
    public FileObject getPrimaryFile() {
        return primaryFile;
    }
            
    public BaseDocument getBaseDocument() {
        return document;
    }
    
    public HashMap<String, String> getDeclaredNamespaces() {
        return declaredNamespaces;
    }
    
    public String getTypedChars() {
        return typedChars;
    }    
    
    public boolean isSchemaAwareCompletion() {
        return schemaLocation != null;
    }

    public List<URI> getSchemas() {
        List<URI> uris = new ArrayList<URI>();
        if(schemaLocation != null)
            CompletionUtil.loadSchemaURIs(schemaLocation, uris, false);
        if(noNamespaceSchemaLocation != null)
            CompletionUtil.loadSchemaURIs(noNamespaceSchemaLocation, uris, true);        
        return uris;
    }
    ////////////////END CompletionContext Implementations////////////////
        
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
        String tagName = docRoot.getTagName();
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;
        String temp = CompletionUtil.getPrefixFromTag(tagName);
        if(temp != null) defNS = defNS+":"+temp; //NOI18N
        NamedNodeMap attributes = docRoot.getAttributes();
        for(int index=0; index<attributes.getLength(); index++) {
            Attr attr = (Attr)attributes.item(index);
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
            if(!attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                continue;            
            if(attr.getName().equals(defNS))
                this.defaultNamespace = attr.getValue();
            declaredNamespaces.put(attr.getName(), attr.getValue());
        }
    }
            
    /**
     * At a given context, that is, at the current cursor location
     * in the document, finds the type of query that needs to be
     * carried out and finds the path from root.
     */
    public void initContext() {
        fromNoNamespace = false;
        noNamespaceModel = null;
        int id = token.getTokenID().getNumericID();
        switch ( id) {
            //user enters < character
            case XMLDefaultTokenContext.TEXT_ID:
                String chars = token.getImage().trim();
                if(chars.equals("") &&
                   token.getPrevious().getImage().trim().equals(">")) {
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
                }
                if(!chars.equals("<") &&
                   token.getPrevious().getImage().trim().equals(">")) {
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    break;
                }
                completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                pathFromRoot = getPathFromRoot(element);
                break;
                
            //start tag of an element
            case XMLDefaultTokenContext.TAG_ID:
                if(lastTypedChar == '>') {
                    completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                    //pathFromRoot = getPathFromRoot(element);
                    break;
                }
                if(element instanceof StartTag) {
                    StartTag tag = (StartTag)element;
                    typedChars = tag.getTagName();
                }
                completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                pathFromRoot = getPathFromRoot(element.getPrevious());
                break;
                
            //user enters an attribute name
            case XMLDefaultTokenContext.ARGUMENT_ID:
                completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                typedChars = token.getImage();
                pathFromRoot = getPathFromRoot(element);
                break;
                
            //not sure
            case XMLDefaultTokenContext.CHARACTER_ID:
                break;
                
            //user enters = character, we should ignore all other operators
            case XMLDefaultTokenContext.OPERATOR_ID:
            //user enters either ' or "
            case XMLDefaultTokenContext.VALUE_ID:
                completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                break;
                
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
                    completionType = CompletionType.COMPLETION_TYPE_ATTRIBUTE;
                    pathFromRoot = getPathFromRoot(element);
                }
                break;
                
            default:
                completionType = CompletionType.COMPLETION_TYPE_UNKNOWN;
                pathFromRoot = getPathFromRoot(element);
                break;
        }
    }
       
    public NamedNodeMap getDocRootAttributes() {
        return docRoot.getAttributes();
    }
    
    public StartTag getDocRoot() {
        return docRoot;
    }
    
    private List<QName> getPathFromRoot(SyntaxElement se) {
        assert(se != null);
        Stack stack = new Stack();
        while( se != null) {            
            if( (se instanceof EndTag) ||
                (se instanceof StartTag && stack.isEmpty()) ) {
                stack.push(se);
                if(defaultNamespace == null && (se instanceof StartTag) &&
                   isRootInNoNSModels(((StartTag)se).getTagName())) {
                    break;
                }
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
                    StartTag current = (StartTag)stack.peek();
                    if(isRoot(current.getTagName()))
                        break;
                    stack.push(se);
                }
            }
            se = se.getPrevious();
        }
        
        return createPath(stack);
    }    
        
    private boolean fromSameNamespace(StartTag current, StartTag previous) {
        String prevPrefix = CompletionUtil.getPrefixFromTag(previous.getTagName());
        String thisPrefix = CompletionUtil.getPrefixFromTag(current.getTagName());
        String thisNS = (thisPrefix == null) ? declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+thisPrefix);
        String prevNS = (prevPrefix == null) ? declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE) :
            declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prevPrefix);
        
        return (thisNS == null && prevNS == null) ||
               (thisNS != null && thisNS.equals(prevNS)) ||
               (prevNS != null && prevNS.equals(thisNS));
    }

    private ArrayList<QName> createPath(Stack stack) {
        ArrayList<QName> path = new ArrayList<QName>();
        while(!stack.isEmpty()) {
            StartTag tag = (StartTag)stack.pop();
            String prefix = CompletionUtil.getPrefixFromTag(tag.getTagName());
            String lName = CompletionUtil.getLocalNameFromTag(tag.getTagName());
            if(fromNoNamespace) {
                path.add(new QName(lName));
                continue;
            }
            
            QName qname = (prefix == null)?
                new QName(declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE), lName) :
                new QName(declaredNamespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix), lName, prefix); //NOI18N            
            path.add(qname);
        }
        //CompletionUtil.printPath(path);
        return path;
    }
    
    private boolean isRoot(String tag) {
        //if no default namespace found, try the no namespace models
        if(defaultNamespace == null) {
            if(isRootInNoNSModels(tag))
                return true;
        }
        //now try all models, including no NS models
        String prefix = CompletionUtil.getPrefixFromTag(tag);
        if(prefix == null) {
            //try default namespace first
            CompletionModel cm = getCompletionModelMap().get(getDefaultNamespace());
            if(CompletionUtil.isRoot(tag, cm))
                return true;
            if(isRootInNoNSModels(tag))
                return true;
            return false;
        }
        String tns = getDeclaredNamespaces().
                get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix);
        CompletionModel cm = getCompletionModelMap().get(tns);
        return CompletionUtil.isRoot(tag, cm);
    }
    
    private boolean isRootInNoNSModels(String tag) {
        for(CompletionModel m : noNSModels) {
            if(CompletionUtil.isRoot(tag, m)) {
                fromNoNamespace = true;
                noNamespaceModel = m;
                return true;
            }
        }
        return false;
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
    
    /**
     * Finds all CompletionModelProviders and builds a model map for schemas having TNS
     * and builds a list for all no namespace models.
     */
    public boolean initModels() {
        Lookup.Template templ = new Lookup.Template(CompletionModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        CompletionModel primaryCompletionModel = null;
        for(Object obj: impls) {
            CompletionModelProvider modelProvider = (CompletionModelProvider)obj;
            List<CompletionModel> models = modelProvider.getModels(this);
            if(models == null || models.size() == 0)
                continue;
            for(CompletionModel m: models) {
                String tns = m.getSchemaModel().getSchema().getTargetNamespace();
                if(tns == null) {
                    noNSModels.add(m); //no namespace models
                    continue;
                }
                //models with namespaces
                nsModelMap.put(tns, m);
            }
        }
        
        return true;
    }
    
    /**
     * Lets first try with "xmlns:ns1". If not used, use it. If used, we
     * keep trying with ns2, ns3 etc.
     */
    String suggestPrefix(String tns) {
        if(tns == null)
            return null;
        //if the tns is already present in declared namespaces,
        //return the prefix
        for(String key : getDeclaredNamespaces().keySet()) {
            String ns = getDeclaredNamespaces().get(key);
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
        
    private FileObject primaryFile;
    private String typedChars;
    private TokenItem token;
    private SyntaxElement element;
    private StartTag docRoot;
    private char lastTypedChar;
    private CompletionType completionType;
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
    private boolean fromNoNamespace = false;
    private CompletionModel noNamespaceModel;
    
    public static final String PREFIX                   = "ns"; //NOI18N
    public static final String XSI_SCHEMALOCATION       = "schemaLocation"; //NOI18N
    public static final String XSI_NONS_SCHEMALOCATION  = "noNamespaceSchemaLocation"; //NOI18N
}

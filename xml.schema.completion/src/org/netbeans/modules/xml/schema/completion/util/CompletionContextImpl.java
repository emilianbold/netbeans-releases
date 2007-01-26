/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
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
            this.xmlFileLocation = getFileLocation();
            this.document = support.getDocument();
            this.element = support.getElementChain(offset);
            this.token = support.getPreviousToken(offset);
            this.docRoot = getRoot(element);
            populateNamespaces();
            initContext();
        } catch(Exception ex) {
            //in the worst case, there will not be
            //any code completion help.
        }
    }
    
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
        return namespaces;
    }
    
    public String getTypedChars() {
        return typedChars;
    }    
    
    public boolean isSchemaAwareCompletion() {
        return schemaLocation != null;
    }

    public List<URI> getSchemas() {
        if(schemaLocation == null)
            return null;
        List<URI> schemas = new ArrayList<URI>();
        StringTokenizer st = new StringTokenizer(
                schemaLocation.replaceAll("\n", " "), " "); //NOI18N
        while(st.hasMoreTokens()) {
            String namespace = st.nextToken().trim();
            if(st.hasMoreTokens()) {
                String schema = st.nextToken().trim();
                try {
                    URI uri = URI.create(schema);
                    if(uri != null)
                        schemas.add(uri);
                } catch (Exception ex) {
                    //just catch
                }
            }
        }
        
        return schemas;
    }
    
    private String getFileLocation() {
        String path = primaryFile.getPath();
        String fileLocation = path.substring(0, path.indexOf(primaryFile.getName()));
        if("/".equals(System.getProperty("file.separator")) && //NOI18N
                !fileLocation.startsWith("/")) {  //NOI18N
            fileLocation = "/" + fileLocation;  //NOI18N
        }
        return fileLocation;
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
        String tagName = docRoot.getTagName();
        String defNS = XMLConstants.XMLNS_ATTRIBUTE;
        String temp = CompletionUtil.getPrefixFromTag(tagName);
        if(temp != null) defNS = defNS+":"+temp; //NOI18N
        NamedNodeMap attributes = docRoot.getAttributes();
        for(int index=0; index<attributes.getLength(); index++) {
            Attr attr = (Attr)attributes.item(index);
            if(attr.getName().endsWith(XSI_SCHEMALOCATION)) {
                schemaLocation = attr.getValue().trim();
                continue;
            }
            if(!attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                continue;
            if(attr.getName().equals(defNS)) {
                this.defaultNamespace = attr.getValue();
            }
            namespaces.put(attr.getName(), attr.getValue());
        }
    }
            
    /**
     * At a given context, that is, at the current cursor location
     * in the document, finds the type of query that needs to be
     * carried out and finds the path from root.
     */
    private void initContext() {
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
                if(element instanceof StartTag) {
                    StartTag tag = (StartTag)element;
                    typedChars = tag.getTagName();
                }
                completionType = CompletionType.COMPLETION_TYPE_ELEMENT;
                pathFromRoot = getPathFromRoot(element);
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
                    //if current element and previous element are from
                    //diff namespaces or current element is same as docroot
                    //the current element is treated as the root
                    StartTag current = (StartTag)stack.peek();
                    if( !fromSameNamespace(current, start) ||
                            CompletionUtil.getLocalNameFromTag(current.getTagName()).
                            equals(CompletionUtil.getLocalNameFromTag(docRoot.getTagName())) )
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
        String thisNS = (thisPrefix == null) ? namespaces.get(XMLConstants.XMLNS_ATTRIBUTE) :
            namespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+thisPrefix);
        String prevNS = (prevPrefix == null) ? namespaces.get(XMLConstants.XMLNS_ATTRIBUTE) :
            namespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prevPrefix);
        
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
            QName qname = (prefix == null)?
                new QName(namespaces.get(XMLConstants.XMLNS_ATTRIBUTE), lName) :
                new QName(namespaces.get(XMLConstants.XMLNS_ATTRIBUTE+":"+prefix), lName, prefix); //NOI18N            
            path.add(qname);
        }
        //printPath(path);
        return path;
    }

    private void printPath(List<QName> path) {
        StringBuffer buffer = new StringBuffer();
        for(QName item: path) {
            if(buffer.toString().equals(""))
                buffer.append(item);
            else
                buffer.append("/" + item);
        }
        //System.out.println(buffer);
    }
    
    /**
     * Returns the StartTag corresponding to the root element.
     */
    private StartTag getRoot(SyntaxElement se) {
        StartTag root = null;
        while( se != null) {
            if(se instanceof StartTag) {
                root = (StartTag)se;
            }
            se = se.getPrevious();
        }
        
        return root;
    }
        
    /**
     * Returns the completion model map.
     */
    public HashMap<String, CompletionModel> getCompletionModelMap() {
        return completionModelMap;
    }
    
    public boolean initModels() {
        HashMap<String, CompletionModel> modelMap = new HashMap<String, CompletionModel>();
        Lookup.Template templ = new Lookup.Template(CompletionModelProvider.class);
        Lookup.Result result = Lookup.getDefault().lookup(templ);
        Collection impls = result.allInstances();
        for(Object obj: impls) {
            CompletionModelProvider modelProvider = (CompletionModelProvider)obj;
            List<CompletionModel> models = modelProvider.getModels(this);
            if(models == null || models.size() == 0)
                continue;
            for(CompletionModel m: models) {
                String tns = m.getSchemaModel().getSchema().getTargetNamespace();
                modelMap.put(tns, m);
                if(getDefaultNamespace() != null &&
                   getDefaultNamespace().equals(tns))
                    this.primaryCompletionModel = m;
            }
        }
        
        if(primaryCompletionModel == null)
            return false;
        
        this.primaryAXIModel = AXIModelFactory.getDefault().
                getModel(primaryCompletionModel.getSchemaModel());
        this.completionModelMap = modelMap;
        return true;
    }
    
    private FileObject primaryFile;
    private String xmlFileLocation;
    private String typedChars;
    private TokenItem token;
    private SyntaxElement element;
    private StartTag docRoot;
    private HashMap<String, String> namespaces = new HashMap<String, String>();
    private CompletionType completionType;
    private List<QName> pathFromRoot;
    private String schemaLocation;
    private String defaultNamespace;
    private BaseDocument document;
    private HashMap<String, CompletionModel> completionModelMap;
    private AXIModel primaryAXIModel;
    private CompletionModel primaryCompletionModel;
    
    public static final String XSI_SCHEMALOCATION   = "schemaLocation"; //NOI18N
}

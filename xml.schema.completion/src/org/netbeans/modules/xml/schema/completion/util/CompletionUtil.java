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
package org.netbeans.modules.xml.schema.completion.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.completion.*;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.Form;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionUtil {
    
    private static final Logger logger = Logger.getLogger(CompletionUtil.class.getName());
    
    /**
     * No instantiation.
     */
    private CompletionUtil() {
    }
        
    /**
     * For debugging purposes only.
     */
    public static void printPath(List<QName> path) {
        StringBuffer buffer = new StringBuffer();
        for(QName item: path) {
            if(buffer.toString().equals(""))
                buffer.append(item);
            else
                buffer.append("/" + item);
        }
        //System.out.println(buffer);
    }
    
    public static boolean isRoot(String tag, CompletionModel cm) {
        if(cm == null)
            return false;
        AXIModel model = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        for(AbstractElement element : model.getRoot().getChildElements()) {
            if(tag.endsWith(element.getName()))
                return true;
        }
        return false;
    }        
    
    /**
     * Returns the prefix from the element's tag.
     */
    public static String getPrefixFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? null :
            tagName.substring(0, tagName.indexOf(":"));
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? tagName :
            tagName.substring(tagName.indexOf(":")+1, tagName.length());
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromXMLNS(String namespace) {
        if(namespace == null) return null;
        return (namespace.indexOf(":") == -1) ?
            null : namespace.substring(namespace.indexOf(":")+1);
    }
    
    /**
     * Returns the list of prefixes declared against the specified
     * namespace. For example a document may declare namespaces as follows:
     * xmlns="tns" xmlns:a="tns" xmlns:b="tns"
     * The returned list in this case will be [a, b, null]
     */
    public static List<String> getPrefixesAgainstNamespace(
            CompletionContextImpl context, String namespace) {
        List<String> list = new ArrayList<String>();
        
        for(String key : context.getDeclaredNamespaces().keySet()) {
            String ns = context.getDeclaredNamespaces().get(key);
            if(ns.equals(namespace))
                list.add(getPrefixFromXMLNS(key));
        }
        
        return list;
    }
            
    /**
     * Populates schema URIs from schemaLocation and noNamespaceSchemaLocation
     * attributes of the doc-root. For schemaLocation, uses the 2nd token, where as
     * for the later, uses every token.
     */
    public static void loadSchemaURIs(String schemaLocation, List<URI> uris, boolean noNS) {
        StringTokenizer st = new StringTokenizer(
                schemaLocation.replaceAll("\n", " "), " "); //NOI18N
        while(st.hasMoreTokens()) {
            URI uri = null;
            try {
                String token1 = st.nextToken().trim();
                if(noNS) {
                    uri = URI.create(token1); //every token is a schema
                    if(uri != null)
                        uris.add(uri);
                    continue;
                }
                if(st.hasMoreTokens()) {
                    String token2 = st.nextToken().trim();
                        uri = URI.create(token2); //every 2nd token is a schema
                        if(uri != null)
                            uris.add(uri);
                }
            } catch (Exception ex) {
                continue;
            }
        }
    }
    
    /**
     * Returns the list of attributes for a given element.
     */
    public static List<CompletionResultItem> getAttributes(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);        
        if(element == null)
            return null;
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractAttribute aa: element.getAttributes()) {
            AXIComponent original = aa.getOriginal();
            if(original.getTargetNamespace() == null) {  //no namespace
                CompletionResultItem item = createResultItem(original, null, context);
                if(item != null)
                    results.add(item);
                continue;
            }            
            if(original instanceof AnyAttribute) {
                results.addAll(substituteAny((AnyAttribute)original, context));
                continue;
            }
            addNSAwareCompletionItems(original,context,null,results);
        }
        return results;
    }
    
    /**
     * Returns the list of child-elements for a given element.
     */
    public static List<CompletionResultItem> getElements(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        if(element == null)
            return null;
        
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractElement ae: element.getChildElements()) {
            AXIComponent original = ae.getOriginal();
            if(original.getTargetNamespace() == null) {  //no namespace
                CompletionResultItem item = createResultItem(original, null, context);
                if(item != null)
                    results.add(item);
                continue;
            }            
            if(original instanceof AnyElement) {
                results.addAll(substituteAny((AnyElement)original, context));
                continue;
            }
            addNSAwareCompletionItems(original,context,null,results);
        }
        return results;
    }
    
    public static List<CompletionResultItem> getElementValues(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        List<CompletionResultItem> result = new ArrayList<CompletionResultItem>();
        if(element == null)
            return null;
        AXIType type = element.getType();
        if( type == null || !(type instanceof Datatype) ||
            ((Datatype)type).getEnumerations() == null)
            return null;
        for(Object value: ((Datatype)type).getEnumerations()) {
            if(context.getTypedChars() == null || context.getTypedChars().equals("")) {
                ValueResultItem item = new ValueResultItem(element, (String)value, context);
                result.add(item);
                continue;
            }
            String str = (String)value;
            if(str.startsWith(context.getTypedChars())) {
                ValueResultItem item = new ValueResultItem(element, (String)value, context);
                result.add(item);
            }
        }
        return result;
    }    
    
    public static List<CompletionResultItem> getAttributeValues(
            CompletionContextImpl context) {
        Element element = findAXIElementAtContext(context);
        if(element == null)
            return null;        
        List<CompletionResultItem> result = new ArrayList<CompletionResultItem>();
        Attribute attr = null;
        for(AbstractAttribute a: element.getAttributes()) {
            if(a instanceof AnyAttribute)
                continue;
            if(a.getName().equals(context.getAttribute())) {
                attr = (Attribute)a;
                break;
            }
        }
        if(attr == null)
            return null;
        AXIType type = attr.getType();
        if(type == null || !(type instanceof Datatype) ||
           ((Datatype)type).getEnumerations() == null)
            return null;                
        for(Object value: ((Datatype)type).getEnumerations()) {
            if(context.getTypedChars() == null || context.getTypedChars().equals("")) {
                ValueResultItem item = new ValueResultItem(attr, (String)value, context);
                result.add(item);
                continue;
            }
            String str = (String)value;
            if(str.startsWith(context.getTypedChars())) {
                ValueResultItem item = new ValueResultItem(attr, (String)value, context);
                result.add(item);
            }
        }
        return result;
    }    
    
    private static void addNSAwareCompletionItems(AXIComponent axi, CompletionContextImpl context,
            CompletionModel cm, List<CompletionResultItem> results) {
        String typedChars = context.getTypedChars();
        CompletionResultItem item = null;
        if(!isFormQualified(axi)) {
            item = createResultItem(axi, null, context);
            if(item == null)
                return;
            if(typedChars == null) {
                results.add(item);
            } else if(item.getReplacementText().startsWith(typedChars)) {
                results.add(item);
            }
            return;
        }
        //namespace aware items
        List<String> prefixes = getPrefixes(context, axi, cm);
        if(prefixes.size() == 0) {
           prefixes.add(null);
        }
        for(String prefix: prefixes) {
            item = createResultItem(axi, prefix, context);
            if(item == null)
                continue;
            if(typedChars == null) {
                results.add(item);
            } else if(item.getReplacementText().startsWith(typedChars)) {
                results.add(item);
            }
        }
    }
    
    private static CompletionResultItem createResultItem(AXIComponent axi,
            String prefix, CompletionContextImpl context) {
        CompletionResultItem item = null;
        if(axi instanceof AbstractElement) {
            if(prefix == null)
                item = new ElementResultItem((AbstractElement)axi, context);
            else
                item = new ElementResultItem((AbstractElement)axi, prefix, context);
        }
        
        if(axi instanceof AbstractAttribute) {
            Attribute a = (Attribute)axi;
            if(prefix == null) {
                if(!context.getExistingAttributes().contains(a.getName()))
                    item = new AttributeResultItem((AbstractAttribute)axi, context);
            } else {
                if(!context.getExistingAttributes().contains(prefix+":"+a.getName()))
                    item = new AttributeResultItem((AbstractAttribute)axi, prefix, context);
            }
        }
        
        return item;
    }
    
    private static List<String> getPrefixes(CompletionContextImpl context, AXIComponent ae, CompletionModel cm) {
        List<String> prefixes = new ArrayList<String>();
        if(cm == null) {
            if(context.getDefaultNamespace() != null &&
               !context.getDefaultNamespace().equals(ae.getTargetNamespace())) {
                prefixes = getPrefixesAgainstNamespace(context, ae.getTargetNamespace());
                if(prefixes.size() != 0)
                    return prefixes;
                String prefix = context.suggestPrefix("ns1");
                CompletionModel m = new CompletionModelEx(context, prefix, ae.getModel().getSchemaModel());
                context.addCompletionModel(m);
                prefixes.add(prefix); //NOI18N
                return prefixes;
            }
            return getPrefixesAgainstNamespace(context, ae.getTargetNamespace());
        }
        
        prefixes = getPrefixesAgainstNamespace(context, cm.getTargetNamespace());
        if(prefixes.size() == 0)
            prefixes.add(cm.getSuggestedPrefix());
        
        return prefixes;
    }
    
    private static boolean isFormQualified(AXIComponent component) {
        if(component instanceof Attribute) {
            AXIComponent original = component.getOriginal();
            if( ((Attribute)original).isReference() ||
                (original.getParent() instanceof AXIDocument) )
                return true;
            
            Attribute a = (Attribute)component;            
            return (a.getForm() == Form.QUALIFIED);
        }
        
        if(component instanceof Element) {
            AXIComponent original = component.getOriginal();
            if( ((Element)original).isReference() ||
                (original.getParent() instanceof AXIDocument) )
                return true;
            Element e = (Element)component;
            return (e.getForm() == Form.QUALIFIED);
        }
        
        return false;
    }
    
    /**
     * Returns the appropriate AXIOM element for a given context.
     */
    public static Element findAXIElementAtContext(
            CompletionContextImpl context) {
        List<QName> path = context.getPathFromRoot();
        if(path == null || path.size() == 0)
            return null;
        
        CompletionModel cm = null;
        QName tag = context.getPathFromRoot().get(0);
        String tns = tag.getNamespaceURI();
        if(tns != null && tns.equals(XMLConstants.NULL_NS_URI)) {
            cm = context.getActiveNoNSModel();
        } else {
            cm = context.getCompletionModelMap().get(tns);
        }
        if(cm == null)
            return null;
        
        AXIModel am = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        AXIComponent parent = am.getRoot();
        if(parent == null)
            return null;
        
        AXIComponent child = null;
        for(QName qname : path) {
            child = findChildElement(parent, qname);
            parent = child;
        }
        
        if(child != null && (child instanceof Element))
            return (Element)child;
        
        return null;
    }
    
    private static AXIComponent findChildElement(AXIComponent parent,
            QName qname) {
        if(parent == null)
            return null;
        for(AbstractElement element : parent.getChildElements()) {
            if(!(element instanceof Element))
                continue;
            Element e = (Element)element;
            if(qname.getLocalPart().equals(e.getName()))
                return element;
        }
        
        return null;
    }
        
    /**
     * Substitue any or anyAttribute with a valid list of items.
     */
    private static List<CompletionResultItem> substituteAny(AXIComponent any,
            CompletionContextImpl context) {
        List<CompletionResultItem> items = new ArrayList<CompletionResultItem>();
        String anyNamespace = any.getTargetNamespace();
        String tns = any.getModel().getRoot().getTargetNamespace();        
        for (CompletionModel cm : context.getCompletionModels()) {
            if (cm == null) continue;

            //##other => items from other namespaces
            if (anyNamespace.equals("##other")) { //NOI18N
                if ((tns != null) && (! tns.equals(cm.getTargetNamespace()))) {
                    populateItemsForAny(cm, any, context, items);
                }
            }

            //##targetNamespace => items from target namespace
            if(anyNamespace.equals("##targetNamespace")) { //NOI18N
               if(tns != null && tns.equals(cm.getTargetNamespace()))
                populateItemsForAny(cm,any,context,items);
            }
            
            //##local => unqualified items
            if(anyNamespace.equals("##local") && //NOI18N
               cm.getTargetNamespace() == null) {
                populateItemsForAny(cm,any,context,items);
            }
            
            //only specfied namespaces
            if(!anyNamespace.startsWith("##") &&  //NOI18N
               cm.getTargetNamespace() != null &&
               anyNamespace.indexOf(cm.getTargetNamespace()) != -1) {
                populateItemsForAny(cm,any,context,items);
            }
            
            //##any => unconditional
            if(anyNamespace.equals("##any")) { //NOI18N
                populateItemsForAny(cm,any,context,items);
            }
        }
        
        return items;
    }
    
    private static void populateItemsForAny(CompletionModel cm, AXIComponent any,
            CompletionContextImpl context, List<CompletionResultItem> items) {
        if (cm == null) return;

        AXIModel am = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        if (any instanceof AnyElement) {
            for(Element e : am.getRoot().getElements()) {
                addNSAwareCompletionItems(e, context, cm, items);
            }
        }
        if (any instanceof AnyAttribute) {
            for(Attribute a : am.getRoot().getAttributes()) {
                addNSAwareCompletionItems(a, context, cm, items);
            }
        }        
    }
    
    /**
     * Finds namespaces declared in all start tags in the document and keeps a map
     * of namespaces to their prefixes.
     * @param document
     * @return
     */
    public static HashMap<String, String> getNamespacesFromStartTags(Document document) {
        HashMap<String, String> map = new HashMap<String, String>();
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            String lastNS = null;
            while(ts.moveNext()) {
                Token t = ts.token();
                if(t.id() == XMLTokenId.ARGUMENT &&
                   t.text().toString().startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    lastNS = t.text().toString();
                }
                if(t.id() == XMLTokenId.VALUE && lastNS != null) {
                    String value = t.text().toString();
                    if(value.length() >= 2 && (value.startsWith("'") || value.startsWith("\"")))
                        value = value.substring(1, value.length()-1);
                    if(XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(value)) {
                        lastNS = null;
                        continue;
                    }
                    map.put(value, CompletionUtil.getPrefixFromXMLNS(lastNS));
                    lastNS = null;
                }
            } //while loop            
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        return map;
    }
    
//    private static NsHandler getNamespaces(InputSource is) {
//        NsHandler handler = new NsHandler();
//        try {
//            XMLReader xmlReader = org.openide.xml.XMLUtil.createXMLReader(false, true);
//            xmlReader.setContentHandler(handler);
//            xmlReader.parse(is);
//        } catch (Exception ex) {
//            logger.log(Level.INFO, ex.getMessage());
//        }
//        return handler;
//    }
    
//    private static class NsHandler extends org.xml.sax.helpers.DefaultHandler {
//
//        //key=tns, value=prefix
//        private HashMap<String, String> nsMap;
//
//        NsHandler() {
//            nsMap = new HashMap<String, String>();
//        }
//
//        @Override
//        public void startElement(String uri, String localName, String rawName, Attributes atts) throws SAXException {
//            if (atts.getLength() > 0) {
//                String locations = atts.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"); // NOI18N
//                if (locations == null)
//                    return;                
//                StringTokenizer tokenizer = new StringTokenizer(locations);
//                if ((tokenizer.countTokens() % 2) == 0) {
//                    while (tokenizer.hasMoreElements()) {
//                        String nsURI = tokenizer.nextToken();
//                        String nsLocation = tokenizer.nextToken();
//                        //mapping.put(nsURI, nsLocation);
//                    }
//                }
//            }
//        }
//
//        @Override
//        public void startPrefixMapping(String prefix, String uri) throws SAXException {
//            if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(uri)) {
//                return;
//            }
//            nsMap.put(uri, prefix);
//        }
//
//        HashMap<String, String> getNamespaceMap() {
//            return nsMap;
//        }
//    }
    
    /**
     * Checks to see if this document declares any DOCTYPE or not?
     * If exists, it must appear before the first xml tag.
     * @return true if found, else false.
     */
    public static boolean isDTDBasedDocument(Document document) {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            while(ts.moveNext()) {
                Token token = ts.token();
                //if an xml tag is found, we have come too far.
                if(token.id() == XMLTokenId.TAG)
                    return false;
                if(token.id() == XMLTokenId.DECLARATION)
                    return true;
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        return false;
    }
    
    /**
     * Finds the namespace insertion offset in the xml document.
     */
    public static int getNamespaceInsertionOffset(Document document) {
        int offset = 0;
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            while(ts.moveNext()) {
                Token nextToken = ts.token();
                if(nextToken.id() == XMLTokenId.TAG && nextToken.text().toString().equals(">")) {
                   offset = nextToken.offset(th);
                   break;
                }
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
        
        return offset;
    }
    
    
    /**
     * Finds the root element of the xml document. It also populates the
     * attributes for the root element.
     * 
     * See DocRoot.
     */
    public static DocRoot getDocRoot(Document document) {
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            List<DocRootAttribute> attributes = new ArrayList<DocRootAttribute>();
            String name = null;
            while(ts.moveNext()) {
                Token nextToken = ts.token();
                if(nextToken.id() == XMLTokenId.TAG) {
                    String tagName = nextToken.text().toString();
                    if(name == null && tagName.startsWith("<"))
                        name = tagName.substring(1, tagName.length());
                    String lastAttrName = null;
                    while(ts.moveNext() ) {
                        Token t = ts.token();
                        if(t.id() == XMLTokenId.TAG && t.text().toString().equals(">"))
                            break;
                        if(t.id() == XMLTokenId.ARGUMENT) {
                            lastAttrName = t.text().toString();
                        }
                        if(t.id() == XMLTokenId.VALUE && lastAttrName != null) {
                            String value = t.text().toString();
                            if(value == null || value.length() == 1)
                                value = null;
                            else if(value.startsWith("'") || value.startsWith("\""))
                                value = value.substring(1, value.length()-1);
                            attributes.add(new DocRootAttribute(lastAttrName, value));
                            lastAttrName = null;
                        }
                    } //while loop
                    
                    //first start tag with a valid name is the root
                    if(name != null)
                        break;
                }
            } //while loop
            
            return new DocRoot(name, attributes);
        } finally {
            ((AbstractDocument)document).readUnlock();
        }        
    }
    
    /**
     * Can't provide completion for XML documents based on DTD
     * and for those who do not declare namespaces in root tag.
     */
    public static boolean canProvideCompletion(BaseDocument doc) {
        FileObject file = getPrimaryFile();
        if(file == null)
            return false;
        
        //for .xml documents        
        if("xml".equals(file.getExt())) { //NOI18N
            //if DTD based, no completion
            if(CompletionUtil.isDTDBasedDocument(doc)) {
                return false;
            }
            //if docroot doesn't declare ns, no completion
            DocRoot root = CompletionUtil.getDocRoot(doc);
            if(root != null && !root.declaresNamespace()) {
                return false;
            }
        }
        
        return true;
    }
    
    public static FileObject getPrimaryFile() {
        TopComponent activatedTC = TopComponent .getRegistry().getActivated();
        if(activatedTC == null)
            return null;
        DataObject activeFile = activatedTC.getLookup().lookup(DataObject.class);
        if(activeFile == null)
            return null;
        
        return activeFile.getPrimaryFile();
    }
    
    public static class DocRoot {
        //name of the root along with prefix, e.g. po:purchaseOrder
        private String name;
        private List<DocRootAttribute> attributes;
        
        DocRoot(String name, List<DocRootAttribute> attributes) {
            this.name = name;
            this.attributes = new ArrayList(attributes);
        }
        
        public String getName() {
            return name;
        }
        
        public String getPrefix() {
            return CompletionUtil.getPrefixFromTag(name);
        }
        
        public List<DocRootAttribute> getAttributes() {
            return attributes;
        }
        
        public boolean declaresNamespace() {
            for(DocRootAttribute attr: getAttributes()) {
                if(attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                    return true;
            }            
            return false;
        }
    }
    
    public static class DocRootAttribute {
        private String name;
        private String value;
        
        DocRootAttribute(String name, String value) {
            this.name = name;            
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return name+"="+value;
        }
    }    
    
}

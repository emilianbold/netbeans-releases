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
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.completion.*;
import org.netbeans.modules.xml.schema.completion.spi.CompletionContext;
import org.netbeans.modules.xml.schema.completion.spi.CompletionModelProvider.CompletionModel;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionUtil {
    
    /**
     * No instantiation.
     */
    private CompletionUtil() {
    }
    
    /**
     * Returns the StartTag corresponding to the root element.
     */
    public static StartTag getRoot(SyntaxElement se) {
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
    public static String getPrefixFromNamespaceDeclaration(String namespace) {
        if(namespace == null) return null;
        return (namespace.indexOf(":") == -1) ?
            null : namespace.substring(namespace.indexOf(":")+1);
    }
    
    /**
     * Returns the list of prefixes declared against the specified
     * target namespace. For example a document may declare namespaces as follows:
     * xmlns="tns" xmlns:a="tns" xmlns:b="tns"
     * The returned list in this case will be [a, b, null]
     */
    public static List<String> getPrefixesAgainstTargetNamespace(
            CompletionContextImpl context, String namespace) {
        List<String> list = new ArrayList<String>();
        NamedNodeMap attributes = context.getDocRootAttributes();
        for(int index=0; index<attributes.getLength(); index++) {
            Attr attr = (Attr)attributes.item(index);
            if(!attr.getName().startsWith(XMLConstants.XMLNS_ATTRIBUTE))
                continue;
            if(attr.getValue().equals(namespace)) {
                String prefix = getPrefixFromNamespaceDeclaration(attr.getName());
                list.add(prefix);
            }
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
        String typedChars = context.getTypedChars();
        for(AbstractAttribute aa: element.getAttributes()) {
            if(aa instanceof AnyAttribute) {
                results.addAll(substituteAny((AnyAttribute)aa, context));
                continue;
            }
            CompletionResultItem item = null;
            if(aa.getTargetNamespace() == null) {  //no namespace
                results.add(new AttributeResultItem(aa, context));
                continue;
            }            
            if(!isFormQualified(aa)) {
                item = new AttributeResultItem(aa, context);
                if(typedChars == null) {
                    results.add(item);
                } else if(item.getReplacementText().startsWith(typedChars)) {
                    results.add(item);
                } else
                    continue;
            } else {
                List<String> prefixes = getPrefixesAgainstTargetNamespace(
                        context, aa.getTargetNamespace());
                for(String prefix: prefixes) {
                    if(prefix == null) prefix = aa.getTargetNamespace();
                    item = new AttributeResultItem(aa, prefix, context);
                    if(typedChars == null) {
                        results.add(item);
                    } else if(item.getReplacementText().startsWith(typedChars)) {
                        results.add(item);
                    } else
                        continue;
                }
            }
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
        String typedChars = context.getTypedChars();
        for(AbstractElement ae: element.getChildElements()) {
            if(ae instanceof AnyElement) {
                results.addAll(substituteAny((AnyElement)ae, context));
                continue;
            }
            CompletionResultItem item = null;
            if(ae.getTargetNamespace() == null) {  //no namespace
                results.add(new ElementResultItem(ae, context));
                continue;
            }
            if(!isFormQualified(ae)) {
                item = new ElementResultItem(ae, context);
                if(typedChars == null) {
                    results.add(item);
                } else if(item.getReplacementText().startsWith(typedChars)) {
                    results.add(item);
                } else
                    continue;                
            } else {                
                List<String> prefixes = getPrefixesAgainstTargetNamespace(
                        context, ae.getTargetNamespace());
                for(String prefix: prefixes) {
                    if(prefix == null)
                        item = new ElementResultItem(ae, context);
                    else
                        item = new ElementResultItem(ae, prefix, context);

                    if(typedChars == null) {
                        results.add(item);
                    } else if(item.getReplacementText().startsWith(typedChars)) {
                        results.add(item);
                    } else
                        continue;
                }
            }
        }
        return results;
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
            Element e = (Element)component;
            return (e.getForm() == Form.QUALIFIED);
        }
        
        return false;
        
    }
    
    /**
     * Returns the appropriate AXIOM element for a given context.
     */
    private static Element findAXIElementAtContext(
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
        for(AbstractElement element : parent.getChildElements()) {
            if(!(element instanceof Element))
                continue;
            Element e = (Element)element;
            if(qname.getLocalPart().endsWith(e.getName()))
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
        for(CompletionModel cm : context.getCompletionModels()) {            
            //##other => items from other namespaces
            if(anyNamespace.equals("##other")) { //NOI18N
               if(tns != null && !tns.equals(cm.getTargetNamespace()))
                populateItemsForAny(cm,any,context,items);
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
    
    private static void populateItemsForAny(CompletionModel cm,
            AXIComponent any, CompletionContext context, List<CompletionResultItem> items) {
        AXIModel am = AXIModelFactory.getDefault().getModel(cm.getSchemaModel());
        if(any instanceof AnyElement) {
            for(Element e : am.getRoot().getElements()) {
                CompletionResultItem item = null;
                if(cm.getSuggestedPrefix() == null)
                    item = new ElementResultItem(e, context);
                else
                    item = new ElementResultItem(e, cm.getSuggestedPrefix(), context);
                items.add(item);
            }
        }
        if(any instanceof AnyAttribute) {
            for(Attribute a : am.getRoot().getAttributes()) {
                CompletionResultItem item = null;
                if(cm.getSuggestedPrefix() == null)
                    item = new AttributeResultItem(a, context);
                else
                    item = new AttributeResultItem(a, cm.getSuggestedPrefix(), context);
                items.add(item);
            }
        }        
    }
        
}

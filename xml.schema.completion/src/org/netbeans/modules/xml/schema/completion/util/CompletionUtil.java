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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
     * Suggests a new prefix if the prefix specified is already in use.
     */
    public static String getSuggestedPrefix(CompletionContextImpl context,
            String prefix, String tns) {
        String newPrefix = prefix;
        String nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+newPrefix;
        int i = 0;
        while(context.getDeclaredNamespaces().get(nsDecl) != null) {
            String ns = context.getDeclaredNamespaces().get(nsDecl);
            if(ns.equals(tns))
                return null;
            newPrefix = newPrefix + 1;  //NOI18N
            nsDecl = XMLConstants.XMLNS_ATTRIBUTE+":"+newPrefix;
        }        
        return newPrefix;
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
     * Returns target namespace for a given prefix.
     */
    public static String getTargetNamespaceByPrefix(
            CompletionContextImpl context, String prefix) {
        for(CompletionModel cm : context.getCompletionModelMap().values()) {
            if(cm.getSuggestedPrefix().equals(prefix))
                return cm.getTargetNamespace();
        }
        
        return null;
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
        
        String tns = context.getPathFromRoot().get(0).getNamespaceURI();
        CompletionModel cm = context.getCompletionModelMap().get(tns);
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
        HashMap<String, CompletionModel> modelMap = context.getCompletionModelMap();
        if(modelMap == null || modelMap.size() == 0)
            return items;
        
        for(CompletionModel cm : modelMap.values()) {
            //all other models except default namespace
            if(anyNamespace.equals("##other") &&
               cm.getTargetNamespace().equals(tns))
                continue;

            //only from default namespace
            if(anyNamespace.equals("##targetNamespace") &&
               !cm.getTargetNamespace().equals(tns))
                continue;
            
            //only specfied namespaces
            if(!anyNamespace.startsWith("##") &&
               anyNamespace.indexOf(cm.getTargetNamespace()) == -1)
                continue;
            
            populateItemsForAny(cm,any,context,items);
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

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

/*
 * CompletionUtil.java
 *
 * Created on June 6, 2006, 10:31 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.schema.completion.*;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class CompletionUtil {
    
    /**
     * Creates a new instance of CompletionUtil
     */
    private CompletionUtil() {
    }
                
    static List<StartTag> getPathFromRoot(SyntaxElement element) {
        assert(element != null);
        Stack stack = new Stack();
        while( element != null) {
            if(element instanceof EndTag) {
                stack.push(element);
            }
            if(element instanceof StartTag) {
                if(stack.isEmpty()) {
                    stack.push(element);
                } else {
                    StartTag start = (StartTag)element;
                    Object last = stack.peek();
                    if(last instanceof EndTag) {
                        EndTag end = (EndTag)last;
                        if(end.getTagName().equals(start.getTagName())) {
                            stack.pop();
                        }
                    } else {
                        stack.push(element);
                    }
                }
            }
            element = element.getPrevious();
        } //while
        ArrayList<StartTag> path = new ArrayList<StartTag>();
        while(!stack.isEmpty()) {
            StartTag tag = (StartTag)stack.pop();
            path.add(tag);
        }
        CompletionUtil.printPath(path);
        return path;
    }
        
    private static void printPath(List<StartTag> path) {
        StringBuffer buffer = new StringBuffer();
        for(StartTag tag: path) {
            if(buffer.toString().equals(""))
                buffer.append(tag.getTagName());
            else
                buffer.append("/" + tag.getTagName());
        }
//System.out.println(buffer);
    }
    
    /**
     * Returns the StartTag corresponding to the root element.
     */
    static StartTag getRoot(SyntaxElement element) {
        StartTag root = null;
        while( element != null) {
            if(element instanceof StartTag) {
                root = (StartTag)element;
            }
            element = element.getPrevious();
        }
        
        return root;
    }
    
    private static String getAttributeValue(StartTag tag, String name) {
        NamedNodeMap attributes = tag.getAttributes();
        for(int index=0; index<attributes.getLength(); index++) {
            Attr attr = (Attr)attributes.item(index);            
            if(attr.getName().endsWith(name)) {
                return attr.getValue();
            }
        }        
        return null;
    }
    
    static List<CompletionResultItem> getAttributes(Element element,
            String typedChars, HashMap<String, List<String>> namespaces) {
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractAttribute aa: element.getAttributes()) {
            CompletionResultItem item = null;
            if(!isFormQualified(aa)) {
                item = new AttributeResultItem(aa, typedChars);
                if(typedChars == null) {
                    results.add(item);
                } else if(item.getReplacementText().startsWith(typedChars)) {
                    results.add(item);
                } else
                    continue;                
            } else {
                List<String> prefixes = namespaces.get(aa.getTargetNamespace());
                for(String p: prefixes) {
                    String prefix = getPrefixFromNamespaceDeclaration(p);
                    if(prefix == null) prefix = aa.getTargetNamespace();
                    item = new AttributeResultItem(aa, prefix, typedChars);
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
    
    static List<CompletionResultItem> getElements(Element element,
            String typedChars, HashMap<String, List<String>> namespaces) {
        if(element == null)
            return null;
        List<CompletionResultItem> results = new ArrayList<CompletionResultItem>();
        for(AbstractElement ae: element.getChildElements()) {
            CompletionResultItem item = null;
            if(!isFormQualified(ae)) {
                item = new ElementResultItem(ae, typedChars);
                if(typedChars == null) {
                    results.add(item);
                } else if(item.getReplacementText().startsWith(typedChars)) {
                    results.add(item);
                } else
                    continue;                
            } else {
                List<String> prefixes = namespaces.get(ae.getTargetNamespace());
                for(String p: prefixes) {
                    String prefix = getPrefixFromNamespaceDeclaration(p);
                    if(prefix == null)
                        item = new ElementResultItem(ae, typedChars);
                    else
                        item = new ElementResultItem(ae, prefix, typedChars);

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
    
    static Element findElementAtContext(AXIDocument document,
            List<StartTag> path) {
        AXIComponent parent = document;
        AXIComponent child = null;
        for(StartTag tag : path) {
            child = findChildElement(parent, tag);
            parent = child;
        }
        
        if(child != null && (child instanceof Element))
            return (Element)child;
        
        return null;
    }
    
    private static AXIComponent findChildElement(AXIComponent parent,
            StartTag tag) {
        for(AbstractElement element : parent.getChildElements()) {
            if(!(element instanceof Element))
                continue;
            Element e = (Element)element;
            if(tag.getTagName().endsWith(e.getName()))
                return element;
        }
        
        return null;
    }

    static String getPrefix(StartTag tag) {
        String tagName = tag.getTagName();
        int index = tagName.indexOf(":");
        if(index == -1)
            return tagName;
        
        return tagName.substring(0, index);
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromTagName(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? null :
            tagName.substring(0, tagName.indexOf(":"));
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

}

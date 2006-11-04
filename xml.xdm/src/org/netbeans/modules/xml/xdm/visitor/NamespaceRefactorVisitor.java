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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author Nam Nguyen
 */
public class NamespaceRefactorVisitor extends ChildVisitor {
    private String namespace;
    private String prefix;
    private List<Node> path = new ArrayList<Node>();
    private Set<String> prefixesUsedByAttributes = new HashSet<String>();
    
    public void refactor(NodeImpl tree, String namespace, String newPrefix) {
        assert namespace != null : "Cannot refactor null namespace";
        this.namespace = namespace;
        prefix = newPrefix;
        tree.accept(this);
    }
    
    public void visit(Element e) {
        path.add(0, e);
        NamespaceCheck redec = new NamespaceCheck(prefix, namespace, e);
        if (redec.getPrefixRedeclaration() == null) {
            visitNode(e);
            
            if (namespace.equals(NodeImpl.lookupNamespace(e.getPrefix(), path))) {
                e.setPrefix(prefix);
            }

            for (Attribute sameNamespace : redec.getNamespaceRedeclaration()) {
                String prefixToRemove = sameNamespace.getLocalName();
                if (prefixesUsedByAttributes.contains(prefixToRemove)) {
                    // spared the declaration, so no needs to be remembered.
                    prefixesUsedByAttributes.remove(prefixToRemove);
                } else {
                    e.removeAttributeNode(sameNamespace);
                }
            }
            
            if (redec.getDuplicateDeclaration() != null) {
                e.removeAttributeNode(redec.getDuplicateDeclaration());
            } 
        }
        path.remove(e);
    }
    
    public void visit(Attribute attr) {
        if (attr.isXmlnsAttribute()) return;
        String attrPrefix = attr.getPrefix();
        if (attrPrefix == null || attrPrefix.length() == 0) {
            // default namespace is not applicable for attribute, just have no namespaces.
            return; 
        }
        
        if (namespace.equals(NodeImpl.lookupNamespace(attrPrefix, path))) {
            if (prefix == null || prefix.length() == 0) {
                prefixesUsedByAttributes.add(attrPrefix);
            } else {
                attr.setPrefix(prefix);
            }
        }
    }
    
    public static class NamespaceCheck {
        Attribute duplicate;
        Attribute prefixRedeclaration;
        List<Attribute> namespaceRedeclaredAttributes = new ArrayList<Attribute>();
        public NamespaceCheck(String existingPrefix, String existingNamespace, Element e) {
            init(existingPrefix, existingNamespace, e);
        }
        public Attribute getPrefixRedeclaration() {
            return prefixRedeclaration;
        }
        public List<Attribute> getNamespaceRedeclaration() {
            return namespaceRedeclaredAttributes;
        }
        public Attribute getDuplicateDeclaration() {
            return duplicate;
        }
        private void init(String existingPrefix, String existingNamespace, Element e) {
            NamedNodeMap nnm = e.getAttributes();
            for (int i=0; i<nnm.getLength(); i++) {
                if (! (nnm.item(i) instanceof Attribute))  continue;
                Attribute attr = (Attribute) nnm.item(i);
                if (attr.isXmlnsAttribute()) {
                    Attribute samePrefix = null;
                    Attribute sameNamespace = null;
                    String prefix = attr.getLocalName();
                    if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                        prefix = XMLConstants.DEFAULT_NS_PREFIX;
                    }
                    if (prefix.equals(existingPrefix)) {
                        samePrefix = attr;
                    }
                    if (existingNamespace.equals(attr.getValue())) {
                        sameNamespace = attr;
                    }
                    if (samePrefix != null && sameNamespace != null) {
                        duplicate = attr;
                    } else if (samePrefix != null) {
                        prefixRedeclaration = attr;
                    } else if (sameNamespace != null) {
                        namespaceRedeclaredAttributes.add(attr);
                    }
                }
            }
        }
    }
}

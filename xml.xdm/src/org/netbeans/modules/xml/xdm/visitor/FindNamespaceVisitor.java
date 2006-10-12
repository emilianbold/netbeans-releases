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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.w3c.dom.NamedNodeMap;

/**
 *
 * @author ajit
 */
public class FindNamespaceVisitor extends ChildVisitor {
    
    /** Creates a new instance of FindNamespaceVisitor */
    public FindNamespaceVisitor(Document root) {
        this.root = root;
    }
    
    public String findNamespace(Node target) {
        if(!(target instanceof Element) && !(target instanceof Attribute)) return null;
        return getNamespaceMap().get(target.getId());
    }
    
    public Map<Integer,String> getNamespaceMap() {
        if(namespaceMap.isEmpty()) {
            nodeCtr = 0;
            visit(root);
        }
        return namespaceMap;
    }
    
    protected void visitNode(Node node) {
        Map<String,String> namespaces = new HashMap<String,String>();
        if((node instanceof Element) || (node instanceof Attribute)) {
            nodeCtr++;
            boolean found = false;
            String prefix = node.getPrefix();
            if(prefix == null) {
                if(node instanceof Attribute) return;
                prefix = "";
            }
            if(node instanceof Element && node.hasAttributes()) {
                NamedNodeMap attrMap = node.getAttributes();
                for (int i=0;i<attrMap.getLength();i++) {
                    Attribute attribute = (Attribute)attrMap.item(i);
                    if(Element.XMLNS.equals(attribute.getPrefix()) || Element.XMLNS.equals(attribute.getName())) {
                        String key = attribute.getPrefix()==null?"":attribute.getLocalName();
                        String value = attribute.getValue();
                        namespaces.put(key,value);
                        if(key.equals(prefix)) {
                            namespaceMap.put(node.getId(),value);
                            found = true;
                        }
                    }
                }
            }
            if(!found) {
                for(Map<String,String> map:ancestorNamespaceMaps) {
                    if(map.containsKey(prefix)) {
                        namespaceMap.put(node.getId(),map.get(prefix));
                        break;
                    }
                }
            }
        }
        if(!namespaces.isEmpty())
            ancestorNamespaceMaps.add(0,namespaces);
        super.visitNode(node);
        ancestorNamespaceMaps.remove(namespaces);
    }

    private Map<Integer,String> namespaceMap = new HashMap<Integer,String>();
    private Document root = null;
    private List<Map<String,String>> ancestorNamespaceMaps = new ArrayList<Map<String,String>>();
    int nodeCtr;
}

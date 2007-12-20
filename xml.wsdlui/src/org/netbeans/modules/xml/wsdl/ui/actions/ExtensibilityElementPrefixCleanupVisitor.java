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

package org.netbeans.modules.xml.wsdl.ui.actions;


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.ChildVisitor;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

public class ExtensibilityElementPrefixCleanupVisitor extends ChildVisitor
        implements WSDLVisitor {
    
    Map<String, String> prefixesMapAfterTraversal;

    public ExtensibilityElementPrefixCleanupVisitor() {
        prefixesMapAfterTraversal = new HashMap<String, String>();
    }
    
    public void visit(Definitions definition) {
        Map<String, String> prefixes = ((AbstractDocumentComponent)definition).getPrefixes();
        boolean foundXSDPrefix = false;
        //Do not allow schema namespace mapping to be removed automatically.
        if (prefixes.containsKey("xsd")) {
            String xsdNS = prefixes.get("xsd");
            if (xsdNS != null && xsdNS.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                prefixesMapAfterTraversal.put("xsd", xsdNS);
                foundXSDPrefix = true;
            }
        }
        if (!foundXSDPrefix) {
            for (Entry<String, String> entry : prefixes.entrySet() ) {
                String ns = entry.getValue();
                if (ns != null && ns.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
                    prefixesMapAfterTraversal.put(entry.getKey(), ns);
                }
            }
        }
        super.visit(definition);
    };
    
    @Override
    public void visit(ExtensibilityElement ee) {
        QName qname = ee.getQName();
        String prefix = qname.getPrefix();
        String namespace = qname.getNamespaceURI();
        addIfNotAvailable(prefix, namespace, qname);
        super.visit(ee);
    }
    
    private void addIfNotAvailable(String prefix, String namespace, QName qname) {
        if (prefix != null && namespace != null && 
                !prefixesMapAfterTraversal.containsKey(prefix)) {
            prefixesMapAfterTraversal.put(prefix, namespace);
        } 
    }

    public boolean containsPrefix(String prefix) {
        return prefixesMapAfterTraversal.containsKey(prefix);
    }
}

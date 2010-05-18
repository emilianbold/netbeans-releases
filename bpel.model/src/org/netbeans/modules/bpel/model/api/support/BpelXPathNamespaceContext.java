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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.model.api.support;

import java.util.Iterator;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;

/**
 * This namespace context is a wrapper. 
 * It delegates almost all execution to the BPEL Namespace context. 
 * The only case which it processes self is the call of the getNamespaceURI() 
 * with the empty prefix. The BPEL namespace context returns a default 
 * namespace in such case. But this class returns NULL namespace. 
 * The XPath expressions in the BPEL don't use the default namespace!
 * 
 * @author nk160297
 */
public class BpelXPathNamespaceContext implements ExNamespaceContext {

    private ExNamespaceContext myParentNsContext;
    
    public BpelXPathNamespaceContext(ExNamespaceContext parentNsContext) {
        myParentNsContext = parentNsContext;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return XMLConstants.NULL_NS_URI;
        } else {
            return myParentNsContext.getNamespaceURI(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        return myParentNsContext.getPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) {
        return myParentNsContext.getPrefixes(namespaceURI);
    }

    public Iterator<String> getPrefixes() {
        return myParentNsContext.getPrefixes();
    }

    public String addNamespace(String uri) throws InvalidNamespaceException {
        return myParentNsContext.addNamespace(uri);
    }

    public void addNamespace(String prefix, String uri) throws InvalidNamespaceException {
        myParentNsContext.addNamespace(prefix, uri);
    }

}

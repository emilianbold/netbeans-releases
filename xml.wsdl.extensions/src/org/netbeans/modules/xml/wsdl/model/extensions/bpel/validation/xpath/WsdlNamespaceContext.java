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

package org.netbeans.modules.xml.wsdl.model.extensions.bpel.validation.xpath;

import java.util.Collections;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author supernikita
 */
public class WsdlNamespaceContext implements NamespaceContext {

    private WSDLComponent mXPathOwner;
    
    public WsdlNamespaceContext(WSDLComponent xPathOwner) {
        mXPathOwner = xPathOwner;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            // The default namespace isn't supported by BPEL XPath
            // So empty prefix corresponds to empty namespace.
            return XMLConstants.NULL_NS_URI;
        }
        //
        assert mXPathOwner instanceof AbstractDocumentComponent;
        String nsUri = ((AbstractDocumentComponent)mXPathOwner).
                lookupNamespaceURI(prefix, true);
        //
        return nsUri;
    }

    public String getPrefix(String namespaceURI) {
        assert mXPathOwner instanceof AbstractDocumentComponent;
        String nsPrefix = ((AbstractDocumentComponent)mXPathOwner).
                lookupPrefix(namespaceURI);
        //
        return nsPrefix;
    }

    public Iterator getPrefixes(String namespaceURI) {
        String single = getPrefix(namespaceURI);
        return Collections.singletonList(single).iterator();
    }
    
}

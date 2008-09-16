/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.xpath.ext.schema;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.xml.XMLConstants;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 * The default implementation of the ExNamespaceContext.
 * It can be used with any XAM based XML model (Schema, WSDL, ...). 
 * 
 * @author nk160297
 */
public class XmlExNamespaceContext implements ExNamespaceContext {
    
    public static String DEFAULT_PREFIX_NAME = "ns";
    
    private AbstractDocumentComponent mXPathOwner;
    
    public XmlExNamespaceContext(AbstractDocumentComponent xPathOwner) {
        assert xPathOwner instanceof AbstractDocumentComponent;
        mXPathOwner = (AbstractDocumentComponent)xPathOwner;
    }

    public String getNamespaceURI(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            // the default namespace isn't supported by BPEL XPath
            // so, empty prefix corresponds to empty namespace.
            return XMLConstants.NULL_NS_URI;
        }
        assert (mXPathOwner instanceof AbstractDocumentComponent);
        String nsUri = ((AbstractDocumentComponent) mXPathOwner).lookupNamespaceURI(
            prefix, true);
        return nsUri;
    }

    public String getPrefix(String namespaceURI) {
        assert mXPathOwner instanceof AbstractDocumentComponent;
        String nsPrefix = ((AbstractDocumentComponent) mXPathOwner).lookupPrefix(
            namespaceURI);
        return nsPrefix;
    }

    public Iterator getPrefixes(String namespaceURI) {
        String single = getPrefix(namespaceURI);
        return Collections.singletonList(single).iterator();
    }

    public Iterator<String> getPrefixes() {
        return getPrefixesSet().iterator();
    }

    private HashSet<String> getPrefixesSet() {
        HashSet<String> prefixSet = new HashSet<String>();
        //
        AbstractDocumentComponent entity = mXPathOwner;
        while (entity != null) {
            Map<String, String> map = entity.getPrefixes();
            for(String prefix : map.keySet()) {
                if ((prefix == null) || (prefix.length() < 1)) continue;
                
                prefixSet.add(prefix);
            }
            entity = (AbstractDocumentComponent)entity.getParent();
        }
        //
        return prefixSet;
    }

    public String addNamespace(String uri) throws InvalidNamespaceException {
        AbstractDocumentComponent root = (AbstractDocumentComponent)mXPathOwner.
                getModel().getRootComponent();
        if (root != null) {
            String prefix = calculateUniqueNsPrefix();
            root.addPrefix(prefix, uri);
            return prefix;
        }
        return null;
    }
    
    private String calculateUniqueNsPrefix() {
        HashSet<String> prefixes = getPrefixesSet();
        int counter = -1;
        String prefixCandidate = null;
        while (true) {
            prefixCandidate = (counter < 0 ? DEFAULT_PREFIX_NAME : 
                DEFAULT_PREFIX_NAME + counter);
            //
            if (! prefixes.contains(prefixCandidate)) break;

            ++counter;
        }
        //
        return prefixCandidate;
    }

    public void addNamespace(String prefix, String uri) throws InvalidNamespaceException {
        AbstractDocumentComponent root = (AbstractDocumentComponent)mXPathOwner.
                getModel().getRootComponent();
        if (root != null) {
            root.addPrefix(prefix, uri);
        }
    }
}

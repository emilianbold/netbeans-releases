/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.xpath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import javax.xml.XMLConstants;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.impl.PreferredNsPrefixes;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xpath.ext.schema.ExNamespaceContext;
import org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException;
import org.netbeans.modules.xml.xpath.ext.schema.XmlExNamespaceContext;

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
public class WlmXPathNamespaceContext implements ExNamespaceContext {

    private ExNamespaceContext myParentNsContext;

    public WlmXPathNamespaceContext(WLMComponent wlmComponent) {
        myParentNsContext = new XmlExNamespaceContext(
                AbstractDocumentComponent.class.cast(wlmComponent));
    }

    public WlmXPathNamespaceContext(ExNamespaceContext parentNsContext) {
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

    /**
     * The prefix will not created if a prefix already defined for the specified
     * namespace URI.
     * 
     * @param uri
     * @return
     * @throws org.netbeans.modules.xml.xpath.ext.schema.InvalidNamespaceException
     */
    public String addNamespace(String uri) throws InvalidNamespaceException {
        try {
            new URI(uri);
        } catch (URISyntaxException e) {
            InvalidNamespaceException exc = new InvalidNamespaceException(e.getMessage());
            throw exc;
        }
        //
        String prefix = getPrefix(uri);
        if (prefix != null) {
            return prefix;
        }
        //
        prefix = PreferredNsPrefixes.getPreferredPrefix(uri);
        if (prefix == null || prefix.length() == 0) {
            return myParentNsContext.addNamespace(uri);
        } else {
            addNamespace(prefix, uri);
            return prefix;
        }
    }

    public void addNamespace(String prefix, String uri) throws InvalidNamespaceException {
        myParentNsContext.addNamespace(prefix, uri);
    }

}

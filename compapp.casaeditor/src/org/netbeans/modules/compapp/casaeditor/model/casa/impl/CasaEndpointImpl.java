/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Map;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponentVisitor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public class CasaEndpointImpl extends CasaComponentImpl 
        implements CasaEndpoint {

    public CasaEndpointImpl(CasaModel model, Element element) {
        super(model, element);
    }
    
    public CasaEndpointImpl(CasaModel model) {
        this(model, createElementNS(model, CasaQName.ENDPOINT));
    }
    
    public void accept(CasaComponentVisitor visitor) {
        visitor.visit(this);
    }

//    public boolean isConsumes() {
//        return Boolean.valueOf(getAttribute(CasaAttribute.IS_CONSUME));
//    }
    
    public String getName() {
        return getAttribute(CasaAttribute.NAME);
    }
    
    public void setName(String name) {
        setAttribute(NAME_PROPERTY, CasaAttribute.NAME, name);
    }
    
    public String getEndpointName() {
        return getAttribute(CasaAttribute.ENDPOINT_NAME);
    }

    public void setEndpointName(String endpointName) {
        setAttribute(ENDPOINT_NAME_PROPERTY, CasaAttribute.ENDPOINT_NAME, endpointName);        
    }
    
    public QName getInterfaceQName() {
        String attrValue = getAttribute(CasaAttribute.INTERFACE_NAME);
        return getQName(attrValue);
    }

    public void setInterfaceQName(QName qname) { // REFACTOR ME
        if (qname == null) {
            qname = new QName(""); // NOI18N
        }
        
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();

        if (namespace != null && !namespace.equals(Constants.EMPTY_STRING)) {  
            String existingPrefix = lookupPrefix(namespace);
            if (existingPrefix == null) {
                AbstractDocumentComponent root =
                        (AbstractDocumentComponent) getModel().getRootComponent();
                existingPrefix = root.lookupPrefix(namespace);
                if (existingPrefix == null) {
                    if (prefix == null || prefix.equals(Constants.EMPTY_STRING)) {      
                        prefix = "ns";                             // NOI18N
                    }
                    // prefix = ensureUnique(prefix, namespace);
                    // IZ#129816, 129810, incorrect namespace prefix generated
                    Map pfx = root.getPrefixes();
                    int count = 0;
                    while (pfx.get(prefix) != null) {
                        prefix = "ns" + count;       // NOI18N
                        count++;
                    }
                    root.addPrefix(prefix, namespace);
                } else {
                    prefix = existingPrefix;
                }
            } else {
                prefix = existingPrefix;
            }
        }

        String qName;
        if (//(prefix == null || prefix.trim().length() == 0) &&
            (localPart == null || localPart.trim().length() == 0)) {
            qName = Constants.EMPTY_STRING;                                         
        } else {
            qName = prefix + Constants.COLON_STRING + localPart;                   
        }
        setAttribute(INTERFACE_NAME_PROPERTY, CasaAttribute.INTERFACE_NAME, qName);     
    }

    public QName getServiceQName() {
        String attrValue = getAttribute(CasaAttribute.SERVICE_NAME);
        return getQName(attrValue);
    }
        
    public void setServiceQName(QName qname) {     
        if (qname == null) {
            qname = new QName(""); // NOI18N
        }
        
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();

        if (namespace != null && !namespace.equals(Constants.EMPTY_STRING)) {           
            String existingPrefix = lookupPrefix(namespace);
            if (existingPrefix == null) {
                AbstractDocumentComponent root =
                        (AbstractDocumentComponent) getModel().getRootComponent();
                existingPrefix = root.lookupPrefix(namespace);
                if (existingPrefix == null) {
                    if (prefix == null || prefix.equals(Constants.EMPTY_STRING)) {      
                        prefix = "ns";                             // NOI18N
                    }
                    // prefix = ensureUnique(prefix, namespace);
                    // IZ#129816, 129810, incorrect namespace prefix generated
                    Map pfx = root.getPrefixes();
                    int count = 0;
                    while (pfx.get(prefix) != null) {
                        prefix = "ns" + count;       // NOI18N
                        count++;
                    }
                    root.addPrefix(prefix, namespace);
                } else {
                    prefix = existingPrefix;
                }
            } else {
                prefix = existingPrefix;
            }
        }

        String qName;
        if (//(prefix == null || prefix.trim().length() == 0) &&
            (localPart == null || localPart.trim().length() == 0)) {
            qName = Constants.EMPTY_STRING;                                            
        } else {
            qName = prefix + Constants.COLON_STRING + localPart;                       
        }
        setAttribute(SERVICE_NAME_PROPERTY, CasaAttribute.SERVICE_NAME, qName);  
    }
    
    public String getDisplayName() {
        String displayName = getAttribute(CasaAttribute.DISPLAY_NAME);
        if (displayName == null || displayName.length() == 0) {
            displayName = getEndpointName();
        } 
        
        return displayName;
    }
    
    public String getProcessName() {
        return getAttribute(CasaAttribute.PROCESS_NAME);
    }
    
    public String getFilePath() {
        return getAttribute(CasaAttribute.FILE_PATH);
    }
        
    private QName getQName(String prefixedName) {
        assert prefixedName != null;
        
        String localPart;
        String namespaceURI;
        String prefix;
        
        int colonIndex = prefixedName.indexOf(Constants.COLON_STRING);             
        if (colonIndex != -1) {
            prefix = prefixedName.substring(0, colonIndex);
            localPart = prefixedName.substring(colonIndex + 1);
            namespaceURI = getPeer().lookupNamespaceURI(prefix);
        } else {
            prefix = Constants.EMPTY_STRING;                                
            localPart = prefixedName;
            namespaceURI = Constants.EMPTY_STRING;
        }
        
        return new QName(namespaceURI, localPart, prefix);
    }   
    
    @Override
    public String toString() {
        return getServiceQName() + "." + getEndpointName();
    }

}
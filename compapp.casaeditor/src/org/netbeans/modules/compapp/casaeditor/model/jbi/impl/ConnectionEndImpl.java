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
package org.netbeans.modules.compapp.casaeditor.model.jbi.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.Constants;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ConnectionEnd;
import org.netbeans.modules.compapp.casaeditor.model.jbi.JBIModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public abstract class ConnectionEndImpl extends JBIComponentImpl implements ConnectionEnd {
    
    /** Creates a new instance of ConnectionEndImpl */
    public ConnectionEndImpl(JBIModel model, Element element) {
        super(model, element);
    }
    
    public /*NamedComponentReference<Service>*/ String getServiceName() {
        return getAttribute(JBIAttributes.SERVICE_NAME);
        //return resolveGlobalReference(Service.class, JBIAttributes.SERVICE_NAME);
    }
    
    public void setServiceName(String /*NamedComponentReference<Service>*/ serviceName) {
        setAttribute(SERVICE_NAME_PROPERTY, JBIAttributes.SERVICE_NAME, serviceName);
    }
    
    public String getEndpointName() {
        return getAttribute(JBIAttributes.ENDPOINT_NAME);
    }
    
    public void setEndpointName(String endpointName) {
        setAttribute(ENDPOINT_NAME_PROPERTY, JBIAttributes.ENDPOINT_NAME, endpointName);
    }
    
    // Convenience method
    public QName getServiceQName() {
        String serviceName = getServiceName();
        return getQName(serviceName);
    }
    
    public void setServiceQName(QName qname) {
        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localPart = qname.getLocalPart();
//        setAnyAttribute(qname, localPart);  // FIXME: what about event firing???
        
        String existingPrefix = lookupPrefix(namespace);
        if (existingPrefix == null) {
            AbstractDocumentComponent root = (AbstractDocumentComponent) getModel().getRootComponent();
            if (prefix == null) {
                prefix = "ns"; // NOI18N
            }
            prefix = ensureUnique(prefix, namespace); 
            root.addPrefix(prefix, namespace);
        } else {
            prefix = existingPrefix;
        }
        
        setServiceName(prefix + Constants.COLON_STRING + localPart);
    }
}

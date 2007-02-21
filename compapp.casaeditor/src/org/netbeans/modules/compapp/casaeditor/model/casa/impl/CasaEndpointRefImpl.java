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
package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpointRef;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaEndpoint;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author jqian
 */
public abstract class CasaEndpointRefImpl extends CasaComponentImpl 
        implements CasaEndpointRef {

    public CasaEndpointRefImpl(CasaModel model, Element element) {
        super(model, element);
    }
        
    public NamedComponentReference<CasaEndpoint> getEndpoint() {
        NamedComponentReference<CasaEndpoint> ret =
                resolveGlobalReference(CasaEndpoint.class, CasaAttribute.ENDPOINT);
        return ret;
    }

    public void setEndpoint(NamedComponentReference<CasaEndpoint> endpoint) {
        setAttribute(ENDPOINT_PROPERTY, CasaAttribute.ENDPOINT, endpoint);
    }
      
//    public String getEndpoint() { // REFACTOR ME: use NamedReference from XAM model
//        return getAttribute(CasaAttribute.ENDPOINT);        
//    }
//    
//    private CasaEndpoint getCasaEndpoint() { // TMP
//        String endpoint = getEndpoint();
//        
//        CasaWrapperModel model = (CasaWrapperModel) getModel();
//        for (CasaEndpoint casaEndpoint : model.getEndpoints().getEndpoints()) {
//            if (casaEndpoint.getName().equals(endpoint)) {
//                return casaEndpoint;
//            }
//        }
//    }
//    
//    public String getName() {
//        return getCasaEndpoint().getName(); 
//    }
//    
//    public void setName(String name) {
//        setAttribute(NAME_PROPERTY, CasaAttribute.NAME, name);
//    }
//    
//    public String getEndpointName() {
//        return getCasaEndpoint().getEndpointName(); 
//    }
//
//    public void setEndpointName(String endpointName) {
//        getCasaEndpoint().setEndpointName(endpointName);          
//    }
//    
//    public QName getInterfaceQName() {
//        return getCasaEndpoint().getInterfaceQName(); 
//    }
//    
//    public void setInterfaceQName(QName qname) { // REFACTOR ME
//        getCasaEndpoint().setInterfaceQName(qname);    
//    }
//
//    public QName getServiceQName() {
//        return getCasaEndpoint().getServiceQName(); 
//    }
//        
//    public void setServiceQName(QName qname) {
//        getCasaEndpoint().setServiceQName(qname);
//    }
}
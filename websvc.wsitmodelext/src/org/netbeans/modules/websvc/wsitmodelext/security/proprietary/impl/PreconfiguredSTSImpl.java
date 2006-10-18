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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.PreconfiguredSTS;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryTrustClientQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class PreconfiguredSTSImpl extends ProprietaryTrustComponentClientImpl implements PreconfiguredSTS {
    
    /**
     * Creates a new instance of PreconfiguredSTSImpl
     */
    public PreconfiguredSTSImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PreconfiguredSTSImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietaryTrustClientQName.PRECONFIGUREDSTS.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }
    
    public void setEndpoint(String url) {
        setAttribute(ENDPOINT, ProprietarySecurityPolicyAttribute.ENDPOINT, url);
    }

    public String getEndpoint() {
        return getAttribute(ProprietarySecurityPolicyAttribute.ENDPOINT);
    }

    public void setMetadata(String url) {
        setAttribute(METADATA, ProprietarySecurityPolicyAttribute.METADATA, url);
    }

    public String getMetadata() {
        return getAttribute(ProprietarySecurityPolicyAttribute.METADATA);
    }
    
    public void setWsdlLocation(String url) {
        setAttribute(WSDLLOCATION, ProprietarySecurityPolicyAttribute.WSDLLOCATION, url);
    }

    public String getWsdlLocation() {
        return getAttribute(ProprietarySecurityPolicyAttribute.WSDLLOCATION);
    }

    public void setServiceName(String sname) {
        setAttribute(SERVICENAME, ProprietarySecurityPolicyAttribute.SERVICENAME, sname);
    }

    public String getServiceName() {
        return getAttribute(ProprietarySecurityPolicyAttribute.SERVICENAME);
    }

    public void setPortName(String pname) {
        setAttribute(PORTNAME, ProprietarySecurityPolicyAttribute.PORTNAME, pname);
    }

    public String getPortName() {
        return getAttribute(ProprietarySecurityPolicyAttribute.PORTNAME);
    }

    public void setNamespace(String ns) {
        setAttribute(NAMESPACE, ProprietarySecurityPolicyAttribute.NAMESPACE, ns);
    }

    public String getNamespace() {
        return getAttribute(ProprietarySecurityPolicyAttribute.NAMESPACE);
    }
}

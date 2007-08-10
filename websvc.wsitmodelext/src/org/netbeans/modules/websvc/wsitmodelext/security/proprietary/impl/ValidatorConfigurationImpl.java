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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ValidatorConfiguration;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class ValidatorConfigurationImpl extends ProprietarySecurityPolicyComponentImpl implements ValidatorConfiguration {
    
    /**
     * Creates a new instance of ValidatorConfigurationImpl
     */
    public ValidatorConfigurationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ValidatorConfigurationImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySecurityPolicyQName.VALIDATORCONFIGURATION.getQName(), model));
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setVisibility(String vis) {
        setAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName(), vis);
    }

    public String getVisibility() {
        return getAnyAttribute(ProprietaryPolicyQName.VISIBILITY.getQName());
    }
    
//    public void setMaxClockSkew(String maxClockSkew) {
//        setAnyAttribute(ProprietarySecurityPolicyQName.MAXCLOCKSKEW.getQName(), maxClockSkew);
//        setAttribute(MAXCLOCKSKEW, ProprietarySecurityPolicyAttribute.MAXCLOCKSKEW, maxClockSkew);        
//    }

//    public String getMaxClockSkew() {
//        return getAnyAttribute(ProprietarySecurityPolicyQName.MAXCLOCKSKEW.getQName());
//        return getAttribute(ProprietarySecurityPolicyAttribute.MAXCLOCKSKEW);
//    }
//
//    public void setTimestampFreshnessLimit(String limit) {
//        setAnyAttribute(ProprietarySecurityPolicyQName.TIMESTAMPFRESHNESSLIMIT.getQName(), limit);
//        setAttribute(TIMESTAMPFRESHNESS, ProprietarySecurityPolicyAttribute.TIMESTAMPFRESHNESSLIMIT, limit);
//    }
//
//    public String getTimestampFreshnessLimit() {
//        return getAnyAttribute(ProprietarySecurityPolicyQName.TIMESTAMPFRESHNESSLIMIT.getQName());
//        return getAttribute(ProprietarySecurityPolicyAttribute.TIMESTAMPFRESHNESSLIMIT);
//    }
//
//    public void setMaxNonceAge(String maxNonceAge) {
//        setAttribute(MAXNONCEAGE, ProprietarySecurityPolicyAttribute.MAXNONCEAGE, maxNonceAge);
//    }
//
//    public String getMaxNonceAge() {
//        return getAttribute(ProprietarySecurityPolicyAttribute.MAXNONCEAGE);
//    }
    
}

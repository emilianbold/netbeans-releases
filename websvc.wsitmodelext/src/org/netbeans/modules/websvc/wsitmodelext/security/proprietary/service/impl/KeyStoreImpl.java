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

package org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.impl;

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.KeyStore;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietaryPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.ProprietarySecurityPolicyServiceQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class KeyStoreImpl extends ProprietarySecurityPolicyComponentImpl implements KeyStore {
    
    /**
     * Creates a new instance of KeyStoreImpl
     */
    public KeyStoreImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public KeyStoreImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySecurityPolicyServiceQName.KEYSTORE.getQName(), model));
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

    public void setLocation(String location) {
        setAttribute(LOCATION, ProprietarySecurityPolicyAttribute.LOCATION, location);
    }

    public String getLocation() {
        return getAttribute(ProprietarySecurityPolicyAttribute.LOCATION);
    }

    public void setAlias(String alias) {
        setAttribute(ALIAS, ProprietarySecurityPolicyAttribute.ALIAS, alias);        
    }

    public String getAlias() {
        return getAttribute(ProprietarySecurityPolicyAttribute.ALIAS);
    }

    public void setType(String type) {
        setAttribute(TYPE, ProprietarySecurityPolicyAttribute.TYPE, type);        
    }

    public String getType() {
        return getAttribute(ProprietarySecurityPolicyAttribute.TYPE);
    }

    public void setStorePassword(String storepass) {
        setAttribute(PASSWORD, ProprietarySecurityPolicyAttribute.STOREPASS, storepass);        
    }

    public String getStorePassword() {
        return getAttribute(ProprietarySecurityPolicyAttribute.STOREPASS);
    }

    public void setKeyPassword(String keypass) {
        setAttribute(KEYPASSWORD, ProprietarySecurityPolicyAttribute.KEYPASS, keypass);
    }

    public String getKeyPassword() {
        return getAttribute(ProprietarySecurityPolicyAttribute.KEYPASS);
    }
    
}

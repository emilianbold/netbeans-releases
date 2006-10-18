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

package org.netbeans.modules.websvc.wsitmodelext.security.impl;

import java.util.Collections;
import org.netbeans.modules.websvc.wsitmodelext.security.RequestSecurityTokenTemplate;
import org.netbeans.modules.websvc.wsitmodelext.security.SecurityPolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeySize;
import org.netbeans.modules.websvc.wsitmodelext.trust.KeyType;
import org.netbeans.modules.websvc.wsitmodelext.trust.TokenType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class RequestSecurityTokenTemplateImpl extends SecurityPolicyComponentImpl implements RequestSecurityTokenTemplate {
    
    /**
     * Creates a new instance of RequestSecurityTokenTemplateImpl
     */
    public RequestSecurityTokenTemplateImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public RequestSecurityTokenTemplateImpl(WSDLModel model){
        this(model, createPrefixedElement(SecurityPolicyQName.REQUESTSECURITYTOKENTEMPLATE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public KeyType getKeyType() {
        return getChild(KeyType.class);
    }

    public void setKeyType(KeyType keyType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(KeyType.class, KEYTYPE_PROPERTY, keyType, classes);
    }

    public void removeKeyType(KeyType keyType) {
        removeChild(KEYTYPE_PROPERTY, keyType);
    }

    public KeySize getKeySize() {
        return getChild(KeySize.class);
    }

    public void setKeySize(KeySize keySize) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(KeySize.class, KEYSIZE_PROPERTY, keySize, classes);
    }

    public void removeKeySize(KeyType keySize) {
        removeChild(KEYSIZE_PROPERTY, keySize);
    }

    public TokenType getTokenType() {
        return getChild(TokenType.class);
    }

    public void setTokenType(TokenType tokenType) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(TokenType.class, TOKENTYPE_PROPERTY, tokenType, classes);
    }

    public void removeTokenType(TokenType tokenType) {
        removeChild(TOKENTYPE_PROPERTY, tokenType);
    }

}

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

package org.netbeans.modules.websvc.wsitmodelext.policy.impl;

import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class PolicyReferenceImpl extends PolicyComponentImpl implements PolicyReference {
    
    /**
     * Creates a new instance of PolicyReferenceImpl
     */
    public PolicyReferenceImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PolicyReferenceImpl(WSDLModel model){
        this(model, createPrefixedElement(PolicyQName.POLICYREFERENCE.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        setAttribute(DIGESTALGORITHM_PROPERTY, PolicyAttribute.DIGESTALGORITHM, digestAlgorithm);        
    }

    public void setDigest(String digest) {
        setAttribute(DIGEST_PROPERTY, PolicyAttribute.DIGEST, digest);        
    }

    public void setPolicyURI(String policyUri) {
        setAttribute(POLICY_URI_PROPERTY, PolicyAttribute.URI, policyUri);        
    }

    public String getPolicyURI() {
        return getAttribute(PolicyAttribute.URI);
    }

    public String getDigestAlgorithm() {
        return getAttribute(PolicyAttribute.DIGESTALGORITHM);
    }

    public String getDigest() {
        return getAttribute(PolicyAttribute.DIGEST);
    }
    
}

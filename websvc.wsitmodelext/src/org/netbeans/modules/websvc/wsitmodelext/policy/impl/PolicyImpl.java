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

import org.netbeans.modules.websvc.wsitmodelext.security.SecurityQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyReference;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

import java.util.Collections;

/**
 *
 * @author Martin Grebac
 */
public class PolicyImpl extends PolicyComponentImpl implements Policy {
    
    /**
     * Creates a new instance of PolicyImpl
     */
    public PolicyImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PolicyImpl(WSDLModel model){
        this(model, createPrefixedElement(PolicyQName.POLICY.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setID(String id) {
        setAnyAttribute(SecurityQName.SECPOLID.getQName(), id);
    }

    public String getID() {
        return getAnyAttribute(SecurityQName.SECPOLID.getQName());
    }

    public void setAll(All all) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(All.class, ALL_PROPERTY, all, classes);
    }

    public All getAll() {
        return getChild(All.class);
    }

    public void removeAll(All all) {
        removeChild(ALL_PROPERTY, all);
    }

    public void setExactlyOne(ExactlyOne exactlyOne) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(ExactlyOne.class, EXACTLYONE_PROPERTY, exactlyOne, classes);
    }

    public ExactlyOne getExactlyOne() {
        return getChild(ExactlyOne.class);
    }

    public void removeExactlyOne(ExactlyOne exactlyOne) {
        removeChild(EXACTLYONE_PROPERTY, exactlyOne);
    }

    public void setPolicy(Policy policy) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Policy.class, POLICY_PROPERTY, policy, classes);
    }

    public Policy getPolicy() {
        return getChild(Policy.class);
    }

    public void removePolicy(Policy policy) {
        removeChild(POLICY_PROPERTY, policy);
    }

    public void setPolicyReference(PolicyReference policyReference) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(PolicyReference.class, POLICYREFERENCE_PROPERTY, policyReference, classes);
    }

    public PolicyReference getPolicyReference() {
        return getChild(PolicyReference.class);
    }

    public void removePolicyReference(PolicyReference policyReference) {
        removeChild(POLICYREFERENCE_PROPERTY, policyReference);
    }    
}

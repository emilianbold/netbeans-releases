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

package org.netbeans.modules.websvc.wsitmodelext.tx.impl;

import org.netbeans.modules.websvc.wsitmodelext.policy.PolicyQName;
import org.netbeans.modules.websvc.wsitmodelext.tx.ATAssertion;
import org.netbeans.modules.websvc.wsitmodelext.tx.TxQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class ATAssertionImpl extends TxComponentImpl implements ATAssertion {
    
    /**
     * Creates a new instance of ATAssertionImpl
     */
    public ATAssertionImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public ATAssertionImpl(WSDLModel model){
        this(model, createPrefixedElement(TxQName.ATASSERTION.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setOptional(boolean optional) {
        setAnyAttribute(PolicyQName.OPTIONAL.getQName(), Boolean.toString(optional));
    }

    public boolean isOptional() {
        return Boolean.parseBoolean(getAnyAttribute(PolicyQName.OPTIONAL.getQName()));
    }

}

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

import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyAttribute;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.ProprietarySecurityPolicyQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Martin Grebac
 */
public class CallbackHandlerImpl extends ProprietarySecurityPolicyComponentImpl implements CallbackHandler {
    
    /**
     * Creates a new instance of CallbackHandlerImpl
     */
    public CallbackHandlerImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public CallbackHandlerImpl(WSDLModel model){
        this(model, createPrefixedElement(ProprietarySecurityPolicyQName.CALLBACKHANDLER.getQName(), model));
    }

    @Override
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }

    public void setDefault(String def) {
        setAttribute(DEFAULT, ProprietarySecurityPolicyAttribute.DEFAULT, def);        
    }

    public String getDefault() {
        return getAttribute(ProprietarySecurityPolicyAttribute.DEFAULT);
    }

    public void setName(String name) {
        setAttribute(NAME, ProprietarySecurityPolicyAttribute.NAME, name);        
    }

    public String getName() {
        return getAttribute(ProprietarySecurityPolicyAttribute.NAME);
    }

    public void setClassname(String classname) {
        setAttribute(CLASSNAME, ProprietarySecurityPolicyAttribute.CLASSNAME, classname);        
    }

    public String getClassname() {
        return getAttribute(ProprietarySecurityPolicyAttribute.CLASSNAME);
    }
}

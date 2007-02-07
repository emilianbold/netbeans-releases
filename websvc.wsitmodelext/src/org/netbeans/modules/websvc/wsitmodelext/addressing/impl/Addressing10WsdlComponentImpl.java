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

package org.netbeans.modules.websvc.wsitmodelext.addressing.impl;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author MartinGrebac
 */
public abstract class Addressing10WsdlComponentImpl extends GenericExtensibilityElement {
    
    /**
     * Creates a new instance of Addressing10WsdlComponentImpl
     */
    public Addressing10WsdlComponentImpl(WSDLModel model, Element e) {
        super(model, e);
    }

    @Override
    public abstract void accept(WSDLVisitor visitor);
    
     @Override
     protected String getNamespaceURI() {
        return Addressing10WsdlQName.ADDRESSING10_WSDL_NS_URI;
    }
    
    @Override
    public String getAttribute(String attribute) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void setAttribute(String attribute, String value) {
        throw new UnsupportedOperationException();
    }
}

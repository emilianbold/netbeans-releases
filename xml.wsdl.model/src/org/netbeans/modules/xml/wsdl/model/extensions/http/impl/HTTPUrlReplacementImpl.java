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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.xml.wsdl.model.extensions.http.impl;

import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent.Visitor;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPUrlEncoded;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

public class HTTPUrlReplacementImpl extends HTTPComponentImpl implements HTTPUrlEncoded {

    public HTTPUrlReplacementImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HTTPUrlReplacementImpl(WSDLModel model){
        this(model, createPrefixedElement(HTTPQName.URLENCODED.getQName(), model));
    }
    
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        return (target instanceof BindingInput || target instanceof BindingOutput);
    }
}

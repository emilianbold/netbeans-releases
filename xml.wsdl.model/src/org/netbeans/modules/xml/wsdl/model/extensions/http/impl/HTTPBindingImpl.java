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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.model.extensions.http.impl;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPBinding.Verb;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.http.HTTPQName;
import org.netbeans.modules.xml.xam.Component;
import org.w3c.dom.Element;

public class HTTPBindingImpl extends HTTPComponentImpl implements HTTPBinding {
    
    public HTTPBindingImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public HTTPBindingImpl(WSDLModel model){
        this(model, createPrefixedElement(HTTPQName.BINDING.getQName(), model));
    }
    
    public void accept(HTTPComponent.Visitor visitor) {
        visitor.visit(this);
    }
    
    public void setVerb(Verb style) {
        setAttribute(VERB_PROPERTY, HTTPAttribute.VERB, style);
    }
    
    public Verb getVerb() {
        String s = getAttribute(HTTPAttribute.VERB);
        return s == null ? null : Verb.valueOf(s.toUpperCase());
    }

    private Verb getVerbValueOf(String s) {
        return s == null ? null : Verb.valueOf(s.toUpperCase());
    }
    
    protected Object getAttributeValueOf(HTTPAttribute attr, String s) {
        if (attr == HTTPAttribute.VERB) {
            return getVerbValueOf(s);
        } else {
            return super.getAttributeValueOf(attr, s);
        }
    }

    @Override
    public boolean canBeAddedTo(Component target) {
        return (target instanceof Binding);
    }
}

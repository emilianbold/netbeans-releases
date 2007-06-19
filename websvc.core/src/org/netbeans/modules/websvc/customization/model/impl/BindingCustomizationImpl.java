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
/*
 * BindingCustomizationImpl.java
 *
 * Created on February 4, 2006, 4:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.customization.model.BindingCustomization;
import org.netbeans.modules.websvc.customization.model.EnableMIMEContent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.spi.WSDLComponentBase;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class BindingCustomizationImpl extends CustomizationComponentImpl
   implements BindingCustomization{
    
    /** Creates a new instance of BindingCustomizationImpl */
    public BindingCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public BindingCustomizationImpl(WSDLModel model) {
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }

    public void setEnableMIMEContent(EnableMIMEContent mime) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(EnableMIMEContent.class, ENABLE_MIME_CONTENT_PROPERTY, mime,
                 classes);
    }

    public EnableMIMEContent getEnableMIMEContent() {
        return getChild(EnableMIMEContent.class);
    }

    public void removeEnableMIMEContent(EnableMIMEContent mime) {
        removeChild(ENABLE_MIME_CONTENT_PROPERTY, mime);
    }
    
   
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}

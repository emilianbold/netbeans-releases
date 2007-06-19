
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
 * PortCustomizationImpl.java
 *
 * Created on February 4, 2006, 4:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.customization.model.JAXWSQName;
import java.util.Collections;
import org.netbeans.modules.websvc.customization.model.JavaMethod;
import org.netbeans.modules.websvc.customization.model.PortCustomization;
import org.netbeans.modules.websvc.customization.model.Provider;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class PortCustomizationImpl extends CustomizationComponentImpl
        implements PortCustomization{
    
    /** Creates a new instance of PortCustomizationImpl */
    public PortCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PortCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }
    
    public void setJavaMethod(JavaMethod method) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(JavaMethod.class, JAVA_METHOD_PROPERTY, method,
                classes);
    }
    
    public JavaMethod getJavaMethod() {
        return getChild(JavaMethod.class);
    }
    
    public void removeJavaMethod(JavaMethod method) {
        removeChild(JAVA_METHOD_PROPERTY, method);
    }
    
    public void setProvider(Provider provider) {
        java.util.List<Class<? extends WSDLComponent>> classes = Collections.emptyList();
        setChild(Provider.class, PROVIDER_PROPERTY, provider,
                classes);
    }
    
    public void removeProvider(Provider provider) {
        removeChild(PROVIDER_PROPERTY, provider);
    }
    
    public Provider getProvider() {
        return getChild(Provider.class);
    }
    
    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}

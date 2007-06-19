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
 * PortTypeOperationCustomizationImpl.java
 *
 * Created on February 4, 2006, 2:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.model.impl;

import org.netbeans.modules.websvc.customization.model.JAXWSQName;
import java.util.Collection;
import org.netbeans.modules.websvc.customization.model.EnableAsyncMapping;
import org.netbeans.modules.websvc.customization.model.EnableWrapperStyle;
import org.netbeans.modules.websvc.customization.model.JavaMethod;
import org.netbeans.modules.websvc.customization.model.JavaParameter;
import org.netbeans.modules.websvc.customization.model.PortTypeOperationCustomization;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.visitor.WSDLVisitor;

import org.w3c.dom.Element;

/**
 *
 * @author Roderico Cruz
 */
public class PortTypeOperationCustomizationImpl extends CustomizationComponentImpl
    implements PortTypeOperationCustomization{
    
    /**
     * Creates a new instance of PortTypeOperationCustomizationImpl
     */
    public PortTypeOperationCustomizationImpl(WSDLModel model, Element e) {
        super(model, e);
    }
    
    public PortTypeOperationCustomizationImpl(WSDLModel model){
        this(model, createPrefixedElement(JAXWSQName.BINDINGS.getQName(), model));
    }

    public void setEnableAsyncMapping(EnableAsyncMapping async) {
        appendChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removeJavaParameter(JavaParameter parameter) {
        removeChild(JAVA_PARAMETER_PROPERTY, parameter);
    }

    public void addJavaParameter(JavaParameter parameter) {
        appendChild(JAVA_PARAMETER_PROPERTY, parameter);
    }

    public void setJavaMethod(JavaMethod method) {
        appendChild(JAVA_METHOD_PROPERTY, method);
    }

    public void setEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        appendChild(ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public Collection<JavaParameter> getJavaParameters() {
        return getChildren(JavaParameter.class);
    }

    public JavaMethod getJavaMethod() {
        return getChild(JavaMethod.class);
    }

    public EnableWrapperStyle getEnableWrapperStyle() {
        return getChild(EnableWrapperStyle.class);
    }

    public EnableAsyncMapping getEnableAsyncMapping() {
       return getChild(EnableAsyncMapping.class);
    }

    public void removeEnableAsyncMapping(EnableAsyncMapping async) {
        removeChild(ENABLE_ASYNC_MAPPING_PROPERTY, async);
    }

    public void removeJavaMethod(JavaMethod method) {
        removeChild(JAVA_METHOD_PROPERTY, method);
    }

    public void removeEnableWrapperStyle(EnableWrapperStyle wrapperStyle) {
        removeChild(this.ENABLE_WRAPPER_STYLE_PROPERTY, wrapperStyle);
    }

    public void accept(WSDLVisitor visitor) {
        visitor.visit(this);
    }
    
}

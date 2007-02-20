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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.Converter;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl
 */
public class ConverterImpl extends JSFConfigComponentImpl.ComponentInfoImpl implements Converter{
    
    /** Creates a new instance of CondverterImpl */
    public ConverterImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public ConverterImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.CONVERTER));
    }
    
    public String getConverterClass() {
        return getChildElementText(JSFConfigQNames.CONVERTER_CLASS.getQName(getModel().getVersion()));
    }
    
    public void setConverterClass(String value) {
        setChildElementText(CONVERTER_CLASS, value, JSFConfigQNames.CONVERTER_CLASS.getQName(getModel().getVersion()));
    }
    
    public String getConverterForClass() {
        return getChildElementText(JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getModel().getVersion()));
    }
    
    public void setConverterForClass(String value) {
        setChildElementText(CONVERTER_FOR_CLASS, value, JSFConfigQNames.CONVERTER_FOR_CLASS.getQName(getModel().getVersion()));
    }
    
    public String getConverterId() {
        return getChildElementText(JSFConfigQNames.CONVERTER_ID.getQName(getModel().getVersion()));
    }
    
    public void setConverterId(String value) {
        setChildElementText(CONVERTER_ID, value, JSFConfigQNames.CONVERTER_ID.getQName(getModel().getVersion()));
    }
        
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }
}

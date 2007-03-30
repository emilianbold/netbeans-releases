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

package org.netbeans.modules.web.jsf.api.facesmodel;

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "converter" element represents a concrete Converter
 * implementation class that should be registered under the
 * specified converter identifier.  Converter identifiers must
 * be unique within the entire web application.
 * 
 * Nested "attribute" elements identify generic attributes that
 * may be configured on the corresponding UIComponent in order
 * to affect the operation of the Converter.  Nested "property"
 * elements identify JavaBeans properties of the Converter
 * implementation class that may be configured to affect the
 * operation of the Converter.  "attribute" and "property"
 * elements are intended to allow component developers to
 * more completely describe their components to tools and users.
 * These elements have no required runtime semantics.
 * @author Petr Pisl
 */
public interface Converter  extends JSFConfigComponent, DescriptionGroup {
    /**
     *
     */
    public static final String CONVERTER_CLASS = JSFConfigQNames.CONVERTER_CLASS.getLocalName();
    /**
     *
     */
    public static final String CONVERTER_FOR_CLASS = JSFConfigQNames.CONVERTER_FOR_CLASS.getLocalName();
    /**
     *
     */
    public static final String CONVERTER_ID = JSFConfigQNames.CONVERTER_ID.getLocalName();
    
    
    String getConverterClass();
    void setConverterClass(String value);
    
    String getConverterForClass();
    void setConverterForClass(String value);
    
    String getConverterId();
    void setConverterId(String value);
    
}

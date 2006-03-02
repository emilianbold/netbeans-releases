/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.netbeans.modules.xml.xam.Named;

/**
 * Represents a message part in the WSDL document
 * @author rico
 * @author Nam Nguyen
 */
public interface Part extends Named<WSDLComponent>, WSDLComponent {
    public static final String ELEMENT_PROPERTY = "element";
    public static final String TYPE_PROPERTY = "type";

    /**
     * Sets the element attribute value to a GlobalReference to a schema component 
     * @param elementRef GlobalReference to a schema component
     */
    void setElement(GlobalReference<GlobalElement> elementRef);
    
    /**
     * Retrieves the element attribute value. The attribute value is a GlobalReference to
     * a schema component.
     */
    GlobalReference<GlobalElement> getElement();
    
    /**
     * Sets the type attribute value to a GlobalReference to a schema component 
     * @param typeRef GlobalReference to a schema component
     */
    void setType(GlobalReference<GlobalType> typeRef);
    
    /**
     * Retrieves the type attribute value. The attribute value is a GlobalReference to
     * a schema component.
     */
    GlobalReference<GlobalType> getType();
}

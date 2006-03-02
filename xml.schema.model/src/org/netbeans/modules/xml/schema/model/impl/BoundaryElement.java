/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;
import org.netbeans.modules.xml.schema.model.BoundaryFacet;
import org.w3c.dom.Element;

/**
 * Common class for element class representing bounding value.
 *
 * @author nn136682
 */
public abstract class BoundaryElement extends SchemaComponentImpl implements BoundaryFacet {
    
    /** Creates a new instance of BoundaryElement */
    public BoundaryElement(SchemaModelImpl model, Element e) {
        super(model, e);
    }
    
    public abstract String getComponentName(); 
    
    public void setValue(String v) {
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, v);
    }
    
    public String getValue() {
        String v = super.getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' got null value.");
        }
        return v;
    }
    
    public Boolean isFixed() {
        String s = getAttribute(SchemaAttributes.FIXED);
        return s == null ? null : Boolean.valueOf(s);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }

    public boolean getFixedDefault() {
        return false;
    }
	
    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }
}
    
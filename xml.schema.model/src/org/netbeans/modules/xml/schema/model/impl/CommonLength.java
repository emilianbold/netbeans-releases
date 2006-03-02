/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.LengthFacet;
import org.w3c.dom.Element;

/**
 * Common implementation for length-related components.
 *
 * @author nn136682
 */

public abstract class CommonLength extends SchemaComponentImpl implements LengthFacet {
    
    /** Creates a new instance of CommonLength */
    public CommonLength(SchemaModelImpl model, Element e) {
        super(model, e);
    }
    
    public abstract String getComponentName(); 
    
    public void setValue(int v) {
        if (v < 0) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' can only have positive integer value.");
        }
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, String.valueOf(v));
    }
    
    public int getValue() {
        String v = super.getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            return 1;
        }
        int i = Integer.valueOf(v);
        if (i < 0) {
            throw new IllegalArgumentException("Element '" + getComponentName() + "' can only has positive integer value.");
        }
        return i;
    }
    
    public Boolean isFixed() {
        String v = super.getAttribute(SchemaAttributes.FIXED);
        return v == null ? null : Boolean.valueOf(v);
    }
    
    public void setFixed(Boolean isFixed) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, isFixed);
    }
    
    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case FIXED:
                return String.class;
            default:
                return super.getAttributeType(attr);
        }
    }

    public boolean getFixedEffective() {
        Boolean v = isFixed();
        return v == null ? getFixedDefault() : v;
    }

    public boolean getFixedDefault() {
        return false;
    }
}

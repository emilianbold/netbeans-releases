/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

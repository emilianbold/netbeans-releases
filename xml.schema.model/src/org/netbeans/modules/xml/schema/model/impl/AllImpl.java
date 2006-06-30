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


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AllElement;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Occur;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This class implements the xml schema all type. The all 
 * type describes an unordered group of elements. 
 *
 * @author nn136682
 */
public class AllImpl extends GroupAllImpl implements All {
    
     public AllImpl(SchemaModelImpl model) {
         this(model,createNewComponent(SchemaElements.ALL, model));
     }
    
    /** Creates a new instance of AllImpl */
    public AllImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return All.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case MIN_OCCURS:
                return Occur.ZeroOne.class;
            default:
                return super.getAttributeType(attr);
        }
    }
    
    
    /**
     * @return minimum occurrences, must be 0 <= x <= 1
     */
    public Occur.ZeroOne getMinOccurs() {
        String s = super.getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Util.parse(Occur.ZeroOne.class, s);
    }
    
    /**
     * set the minimum number of occurs.
     * @param occurs must satisfy 0 <= occurs <= 1
     */
    public void setMinOccurs(Occur.ZeroOne occurs) {
        setAttribute(MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, occurs);
    }
    
    public Occur.ZeroOne getMinOccursDefault() {
        return Occur.ZeroOne.ONE;
    }
    
    public Occur.ZeroOne getMinOccursEffective() {
        Occur.ZeroOne v = getMinOccurs();
        return v == null ? getMinOccursDefault() : v;
    }
}

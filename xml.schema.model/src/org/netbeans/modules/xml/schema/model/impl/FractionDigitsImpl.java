/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.FractionDigits;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class FractionDigitsImpl extends CommonLength implements FractionDigits {
    
    public FractionDigitsImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.FRACTION_DIGITS,model));
    }
    
    /** Creates a new instance of FractionDigitsImpl */
    public FractionDigitsImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return FractionDigits.class;
	}

    public String getComponentName() {
        return SchemaElements.FRACTION_DIGITS.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
}

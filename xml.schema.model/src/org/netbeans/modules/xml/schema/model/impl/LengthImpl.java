/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author nn136682
 */
public class LengthImpl extends CommonLength implements Length {
    
    public LengthImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.LENGTH,model));
    }
    
    /** Creates a new instance of LengthImpl */
    public LengthImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Length.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getComponentName() {
        return SchemaElements.LENGTH.getName();
    }
}

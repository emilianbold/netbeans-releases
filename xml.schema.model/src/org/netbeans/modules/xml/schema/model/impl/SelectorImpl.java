/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * Provide implementation for selector.
 * @author Chris Webster
 */
public class SelectorImpl extends SchemaComponentImpl 
implements Selector {
    public SelectorImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.SELECTOR, model));
    }
    
    public SelectorImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return Selector.class;
	}

    public void setXPath(String xPath) {
        setAttribute(XPATH_PROPERTY, SchemaAttributes.XPATH, xPath);
    }

    public void accept(SchemaVisitor v) {
        v.visit(this);
    }

    public String getXPath() {
        return getAttribute(SchemaAttributes.XPATH);
    }
}

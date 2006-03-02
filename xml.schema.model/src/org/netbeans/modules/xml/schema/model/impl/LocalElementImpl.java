/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * LocalElementImpl.java
 *
 * Created on October 6, 2005, 10:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class LocalElementImpl extends LocalElementBaseImpl
implements LocalElement {
    
    public LocalElementImpl(SchemaModelImpl model) {
	this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of LocalElementImpl
     */
    public LocalElementImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
	return LocalElement.class;
    }
    
    public void accept(SchemaVisitor v) {
	v.visit(this);
    }
    
}

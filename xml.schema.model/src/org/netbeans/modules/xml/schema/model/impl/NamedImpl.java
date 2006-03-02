/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * NamedImpl.java
 *
 * Created on October 28, 2005, 8:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.Named;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class NamedImpl extends SchemaComponentImpl implements Named<SchemaComponent>{
    
    public NamedImpl(SchemaModelImpl model, Element e) {
	super(model,e);
    }
    
    public void setName(String name) {
	RenamingVisitor renameVisitor = new RenamingVisitor();
	renameVisitor.rename(this, name);
    }

    public String getName() {
	return getAttribute(SchemaAttributes.NAME);
    }
}

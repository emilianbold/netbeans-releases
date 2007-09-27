/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPQNames;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.w3c.dom.Element;

/**
 *
 * 
 */
public class PropertyImpl extends IEPComponentBase implements Property {

	private TcgPropertyType mType;
	
    public PropertyImpl(IEPModel model) {
        this(model, createNewElement(IEPQNames.PROPERTY.getQName(), model));
    }
    
    public PropertyImpl(IEPModel model, Element e) {
        super(model, e);
    }

    public void accept(IEPVisitor visitor) {
        visitor.visitProperty(this);
    }

    public IEPComponent createChild(Element childEl) {
        return null;
    }

    public String getName() {
        return getAttribute(ATTR_NAME);
    }

    public void setName(String name) {
        setAttribute(NAME_PROPERTY, ATTR_NAME, name);
    }

    public String getValue() {
        return getAttribute(ATTR_VALUE);
    }

    public void setValue(String value) {
        setAttribute(VALUE_PROPERTY, ATTR_VALUE, value);
    }

    public Component getParentComponent() {
    	return (Component) getParent();
    }
    
    public String toString() {
    	return getName();
    }
    
    public TcgPropertyType getPropertyType() {
    	
    	if(mType == null) {
	    	Component parent = getParentComponent();
	    	if(parent != null) {
	    		mType = parent.getComponentType().getPropertyType(getName());
	    	}
    	}
    	
    	return mType;
    }
}

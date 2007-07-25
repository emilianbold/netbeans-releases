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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.iep.model.impl;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPQNames;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.Property;
import org.w3c.dom.Element;

/**
 *
 * 
 */
public class PropertyImpl extends IEPComponentBase implements Property {

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

}

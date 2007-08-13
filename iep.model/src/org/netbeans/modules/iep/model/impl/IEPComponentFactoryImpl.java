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

package org.netbeans.modules.iep.model.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.LinkComponent;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;

import org.w3c.dom.Element;

/**
 * 
 * 
 */
public class IEPComponentFactoryImpl implements IEPComponentFactory {

	private IEPModel model;

	/** Creates a new instance of IEPComponentFactoryImpl */
	public IEPComponentFactoryImpl(IEPModel model) {
		this.model = model;
	}

	public IEPComponent create(Element element, IEPComponent context) {

		return context.createChild(element);
	}

	public IEPComponent create(IEPComponent parent, QName qName) {
		String q = qName.getPrefix();
		if (q == null || q.length() == 0) {
			q = qName.getLocalPart();
		} else {
			q = q + ":" + qName.getLocalPart();
		}
		Element element = model.getDocument().createElementNS(
				qName.getNamespaceURI(), q);
		return parent.createChild(element);
	}

    public Component createComponent(IEPModel model) {
        return new ComponentImpl(model);
    }

    public OperatorComponent createOperator(IEPModel model) {
    	return new OperatorComponentImpl(model);
    }
    
    public LinkComponent createLink(IEPModel model) {
    	return new LinkComponentImpl(model);
    }
    
    public Property createProperty(IEPModel model) {
        return new PropertyImpl(model);
    }

	public SchemaComponent createSchema(IEPModel model) {
		return new SchemaComponentImpl(model);
	}

	public SchemaAttribute createSchemaAttribute(IEPModel model) {
		return new SchemaAttributeImpl(model);
	}

	
}

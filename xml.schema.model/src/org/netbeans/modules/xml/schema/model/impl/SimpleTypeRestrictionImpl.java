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

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class SimpleTypeRestrictionImpl extends CommonSimpleRestrictionImpl implements SimpleTypeRestriction{

    /** Creates a new instance of SimpleTypeRestrictionImpl */
    protected SimpleTypeRestrictionImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.RESTRICTION, model));
    }
    
    public SimpleTypeRestrictionImpl(SchemaModelImpl model, Element el){
        super(model,el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return SimpleTypeRestriction.class;
	}

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public void setBase(NamedComponentReference<GlobalSimpleType> type) {
        setAttribute(BASE_PROPERTY, SchemaAttributes.BASE, type);
    }
    
    public NamedComponentReference<GlobalSimpleType> getBase() {
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.BASE);
    }
    
}

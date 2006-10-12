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

import org.netbeans.modules.xml.schema.model.MinLength;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * This implents interface represents the xs:minLength facet.
 * @author nn136682
 */
public class MinLengthImpl extends CommonLength implements MinLength {

    public MinLengthImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.MIN_LENGTH, model));
    }
    
    /** Creates a new instance of MinLengthImpl */
    public MinLengthImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return MinLength.class;
	}
    
    public String getComponentName() {
        return SchemaElements.MIN_LENGTH.getName();
    }
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
}

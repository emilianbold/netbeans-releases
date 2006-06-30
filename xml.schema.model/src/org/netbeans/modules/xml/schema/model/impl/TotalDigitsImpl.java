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


package org.netbeans.modules.xml.schema.model.impl;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TotalDigits;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * <totalDigits
 *  fixed = boolean : false
 *  id = ID
 *  value = positiveInteger
 *  {any attributes with non-schema namespace . . .}>
 *  Content: (annotation?)
 * </totalDigits>
 *
 * @author Nam Nguyen
 */
public class TotalDigitsImpl extends CommonLength implements TotalDigits {
    
    public TotalDigitsImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.TOTAL_DIGITS,model));
    }
    
    /** Creates a new instance of TotalDigitsImpl */
    public TotalDigitsImpl(SchemaModelImpl model, Element e) {
        super(model, e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return TotalDigits.class;
	}
    
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }

    public String getComponentName() {
        return SchemaElements.TOTAL_DIGITS.toString();
    }
    
    @Override
    public void setValue(int v) {
        if (v < 1) {
            throw new IllegalArgumentException("Element 'totalDigits' can only have positive integer value.");
        }
        setAttribute(VALUE_PROPERTY, SchemaAttributes.VALUE, Integer.valueOf(v));
    }
    
    @Override
    public int getValue() {
        String v = getAttribute(SchemaAttributes.VALUE);
        if (v == null) {
            return 1;
        }
        int i = Integer.valueOf(v);
        if (i < 1) {
            throw new IllegalArgumentException("Element '" + SchemaElements.TOTAL_DIGITS + "' got non-positive integer value.");
        }
        return i;
    }
    
}

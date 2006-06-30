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

/*
 * GlobalSimpleTypeImpl.java
 *
 * Created on October 5, 2005, 4:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType.Final;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 *
 * @author rico
 */
public class GlobalSimpleTypeImpl extends CommonSimpleTypeImpl implements GlobalSimpleType{
    
    /** Creates a new instance of GlobalSimpleTypeImpl */
    public GlobalSimpleTypeImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.SIMPLE_TYPE,model));
    }
    
    public GlobalSimpleTypeImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalSimpleType.class;
	}
    
    protected Class getAttributeMemberType(SchemaAttributes attribute) {
        switch(attribute) {
            case FINAL:
                return Final.class;
            default:
                return super.getAttributeType(attribute);
        }
    }
    
    //setters/getters of attributes
    public void setName(String name) {
        RenamingVisitor renameVisitor = new RenamingVisitor();
	renameVisitor.rename(this, name);
    }
    
    
    public String getName() {
        return getAttribute(SchemaAttributes.NAME);
    }
    
    public void setFinal(Set<Final> finalValue) {
        setAttribute(FINAL_PROPERTY, SchemaAttributes.FINAL, 
                finalValue == null ? null : 
                    Util.convertEnumSet(Final.class, finalValue));
    }
    
    public Set<Final> getFinal() {
        String s = getAttribute(SchemaAttributes.FINAL);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
 
    public Set<Final> getFinalEffective() {
        Set<Final> v = getFinal();
        return v == null ? getFinalDefault() : v;
    }

    public Set<Final> getFinalDefault() {
        return Util.convertEnumSet(Final.class, getSchemaModel().getSchema().getFinalDefaultEffective());
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
}

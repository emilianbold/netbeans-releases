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

import java.util.Set;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalElement.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class GlobalElementImpl extends ElementImpl implements GlobalElement {

    public GlobalElementImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of GlobalElementImpl
     */
    public GlobalElementImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

	/**
	 *
	 *
	 */
	public Class<? extends SchemaComponent> getComponentType() {
		return GlobalElement.class;
	}
    
    /**
     *
     */
    public void setSubstitutionGroup(NamedComponentReference<GlobalElement> element) {
	setAttribute(SUBSTITUTION_GROUP_PROPERTY, SchemaAttributes.SUBSTITUTION_GROUP, element);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalElement> getSubstitutionGroup() {
        return resolveGlobalReference(GlobalElement.class, SchemaAttributes.SUBSTITUTION_GROUP);
    }
    
    /**
     *
     */
    public void setAbstract(Boolean abstr) {
        setAttribute(ABSTRACT_PROPERTY, SchemaAttributes.ABSTRACT, abstr);
    }
    
    /**
     *
     */
    public Boolean isAbstract() {
        String s = getAttribute(SchemaAttributes.ABSTRACT);
        return s == null ? null : Boolean.valueOf(s);
    }
    

    public boolean getAbstractEffective() {
        Boolean v = isAbstract();
        return v == null ? getAbstractDefault() : v;
    }

    public boolean getAbstractDefault() {
        return false;
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
        return Util.convertEnumSet(Final.class, getModel().getSchema().getFinalDefaultDefault());
    }

    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
 
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case FINAL:
                return Final.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }
}

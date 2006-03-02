/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * GlobalElementImpl.java
 *
 * Created on October 6, 2005, 10:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Set;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalElement.Final;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;
/**
 *
 * @author Vidhya Narayanan
 */
public class GlobalElementImpl extends CommonElementImpl implements GlobalElement {
    
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
    public void setSubstitutionGroup(GlobalReference<GlobalElement> element) {
	setGlobalReference(SUBSTITUTION_GROUP_PROPERTY, SchemaAttributes.SUBSTITUTION_GROUP, element);
    }
    
    /**
     *
     */
    public GlobalReference<GlobalElement> getSubstitutionGroup() {
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
        return Util.convertEnumSet(Final.class, getSchemaModel().getSchema().getFinalDefaultDefault());
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

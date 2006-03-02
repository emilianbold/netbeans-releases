/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * LocalElementImpl.java
 *
 * Created on October 6, 2005, 10:54 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class LocalElementBaseImpl extends CommonElementImpl {
    
    public LocalElementBaseImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.ELEMENT,model));
    }
    
    /**
     * Creates a new instance of LocalElementImpl
     */
    public LocalElementBaseImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }

    /**
     *
     */
    public void setForm(Form f) {
        setAttribute(LocalElement.FORM_PROPERTY, SchemaAttributes.FORM, f);
    }
    
    /**
     *
     */
    public void setRef(GlobalReference<GlobalElement> ref) {
        setGlobalReference(LocalElement.REF_PROPERTY, SchemaAttributes.REF, ref);
    }
    
    /**
     *
     */
    public void setMinOccurs(Integer min) {
        setAttribute(LocalElement.MIN_OCCURS_PROPERTY, SchemaAttributes.MIN_OCCURS, min);
    }
    
    /**
     *
     */
    public void setMaxOccurs(String max) {
        setAttribute(LocalElement.MAX_OCCURS_PROPERTY, SchemaAttributes.MAX_OCCURS, max);
    }
    
    /**
     *
     */
    public GlobalReference<GlobalElement> getRef() {
        return resolveGlobalReference(GlobalElement.class, SchemaAttributes.REF);
    }
    
    /**
     *
     */
    public Integer getMinOccurs() {
        String s = getAttribute(SchemaAttributes.MIN_OCCURS);
        return s == null ? null : Integer.valueOf(s);
    }
    
    /**
     *
     */
    public String getMaxOccurs() {
        return getAttribute(SchemaAttributes.MAX_OCCURS);
    }
    
    /**
     *
     */
    public Form getForm() {
        String s = super.getAttribute(SchemaAttributes.FORM);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public int getMinOccursEffective() {
        Integer v = getMinOccurs();
        return v == null ? getMinOccursDefault() : v;
    }

    public int getMinOccursDefault() {
        return 1;
    }

    public String getMaxOccursEffective() {
        String v = getMaxOccurs();
        return v == null ? getMaxOccursDefault() : v;
    }

    public String getMaxOccursDefault() {
        return String.valueOf(1);
    }

    public Form getFormEffective() {
        Form v = getForm();
        return v == null ? getFormDefault() : v;
    }

    public Form getFormDefault() {
        return getSchemaModel().getSchema().getElementFormDefaultEffective();
    }
}

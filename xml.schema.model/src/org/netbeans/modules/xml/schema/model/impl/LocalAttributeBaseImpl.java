/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model.impl;

import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.Attribute.Use;
import org.netbeans.modules.xml.xam.GlobalReference;
import org.w3c.dom.Element;

/**
 * @author Chris Webster
 */
public abstract class LocalAttributeBaseImpl extends CommonAttributeImpl {
    
    /**
     *
     */
    public LocalAttributeBaseImpl(SchemaModelImpl model) {
        super(model,createNewComponent(SchemaElements.ATTRIBUTE, model));
    }
    
    /**
     *
     */
    public LocalAttributeBaseImpl(SchemaModelImpl model, Element e) {
        super(model,e);
    }

    /**
     *
     */
    public LocalAttribute.Use getUse() {
        String s = getAttribute(SchemaAttributes.USE);
        return s == null ? null : Util.parse(Use.class, s);
    }
    
    public Use getUseEffective() {
        Use v = getUse();
        return v == null ? getUseDefault() : v;
    }

    public Use getUseDefault() {
        return Use.OPTIONAL;
    }

    /**
     *
     */
    public void setUse(LocalAttribute.Use use) {
        setAttribute(LocalAttribute.USE_PROPERTY, SchemaAttributes.USE, use);
    }

    /**
     *
     */
    public GlobalReference<GlobalAttribute> getRef() {
        return resolveGlobalReference(GlobalAttribute.class, SchemaAttributes.REF);
    }
    
    /**
     *
     */
    public void setRef(GlobalReference<GlobalAttribute> attribute) {
        setGlobalReference(LocalAttribute.REF_PROPERTY, SchemaAttributes.REF, attribute);
    }

    /**
     *
     */
    public Form getForm() {
        String s = getAttribute(SchemaAttributes.FORM);
        return s == null ? null : Util.parse(Form.class, s);
    }

    public Form getFormEffective() {
        Form v = getForm();
        return v == null ? getFormDefault() : v;
    }

    public Form getFormDefault() {
        return getSchemaModel().getSchema().getAttributeFormDefaultEffective();
    }

    /**
     *
     */
    public void setForm(Form form) {
        setAttribute(LocalAttribute.FORM_PROPERTY, SchemaAttributes.FORM, form);
    }
    
}

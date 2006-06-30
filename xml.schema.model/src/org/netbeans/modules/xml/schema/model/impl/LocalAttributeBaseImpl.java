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

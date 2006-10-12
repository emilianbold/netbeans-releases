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

import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class LocalElementBaseImpl extends ElementImpl {
    
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
    public void setRef(NamedComponentReference<GlobalElement> ref) {
        setAttribute(LocalElement.REF_PROPERTY, SchemaAttributes.REF, ref);
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
    public NamedComponentReference<GlobalElement> getRef() {
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
        return getModel().getSchema().getElementFormDefaultEffective();
    }
}

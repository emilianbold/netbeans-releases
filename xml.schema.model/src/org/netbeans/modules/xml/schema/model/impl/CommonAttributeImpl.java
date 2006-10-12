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

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public abstract class CommonAttributeImpl extends NamedImpl
    implements Attribute {
    
    /** 
     * Creates a new instance of CommonAttributeImpl 
     */
    public CommonAttributeImpl(SchemaModelImpl model, Element el) {
	super(model, el);
    }
 
    protected Class getAttributeType(SchemaAttributes attr) {
        switch(attr) {
            case FIXED:
                return String.class;
            default:
                return super.getAttributeType(attr);
        }
    }
    
    /**
     *
     */
    public String getFixed() {
        return getAttribute(SchemaAttributes.FIXED);
    }
    
    /**
     *
     */
    public void setFixed(String fixedValue) {
        setAttribute(FIXED_PROPERTY, SchemaAttributes.FIXED, fixedValue);
    }

    /**
     *
     */
    public String getDefault() {
        return getAttribute(SchemaAttributes.DEFAULT);
    }
    
    /**
     *
     */
    public void setDefault(String defaultValue) {
        setAttribute(DEFAULT_PROPERTY, SchemaAttributes.DEFAULT, defaultValue);
    }
    
    /**
     *
     */
    public NamedComponentReference<GlobalSimpleType> getType() { 
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public void setType(NamedComponentReference<GlobalSimpleType> type) {
        setAttribute(TYPE_PROPERTY, SchemaAttributes.TYPE, type);
    }

    /**
     *
     */
    public LocalSimpleType getInlineType() {
        java.util.Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);        
        if(!types.isEmpty()){
            return types.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public void setInlineType(LocalSimpleType type) {
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, type, classes);
    }
 
}

/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

/*
 * CommonAttributeImpl.java
 *
 * Created on October 18, 2005, 1:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.xml.schema.model.Attribute;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.GlobalReference;
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
    public GlobalReference<GlobalSimpleType> getType() { 
        return resolveGlobalReference(GlobalSimpleType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public void setType(GlobalReference<GlobalSimpleType> type) {
        setGlobalReference(TYPE_PROPERTY, SchemaAttributes.TYPE, type);
    }

    /**
     *
     */
    public LocalSimpleType getInlineType() {
        java.util.Collection<LocalSimpleType> types = getChildren(LocalSimpleType.class);        
        return (LocalSimpleType)types.toArray()[0];
    }
    
    /**
     *
     */
    public void setInlineType(LocalSimpleType type) {
        List<Class<? extends SchemaComponent>> classes = Collections.emptyList();
        setChild(LocalSimpleType.class, INLINE_TYPE_PROPERTY, type, classes);
    }
 
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.Element.Block;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
/**
 *
 * @author Vidhya Narayanan
 */
public abstract class ElementImpl extends NamedImpl implements Element {
    
    /**
     * Creates a new instance of CommonElementImpl
     */
    public ElementImpl(SchemaModelImpl model, org.w3c.dom.Element el) {
        super(model, el);
    }
    
    protected Class getAttributeMemberType(SchemaAttributes attr) {
        switch(attr) {
            case BLOCK:
                return Block.class;
            default:
                return super.getAttributeMemberType(attr);
        }
    }
    
    public void setDefault(String defaultValue) {
        setAttribute(DEFAULT_PROPERTY ,SchemaAttributes.DEFAULT, defaultValue);
    }
    
    public void setFixed(String fixed) {
        setAttribute(FIXED_PROPERTY ,SchemaAttributes.FIXED, fixed);
    }
    
    public void setType(NamedComponentReference<? extends GlobalType> t) {
        setAttribute(LocalElement.TYPE_PROPERTY, SchemaAttributes.TYPE, t);
    }
    
    public void setNillable(Boolean nillable) {
        setAttribute(NILLABLE_PROPERTY, SchemaAttributes.NILLABLE, nillable);
    }
    
    /**
     *
     */
    public void addConstraint(Constraint c) {
        Collection<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<java.lang.Class<? extends SchemaComponent>>();
	list.add(Annotation.class);
	list.add(LocalType.class);
        addAfter(CONSTRAINT_PROPERTY, (SchemaComponent) c, list);
    }
    
    /**
     *
     */
    public void removeConstraint(Constraint c) {
        removeChild(CONSTRAINT_PROPERTY , (SchemaComponent) c);
    }
    
    /**
     *
     */
    public void setInlineType(LocalType t) {
        Collection<Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        setChild(LocalType.class, LocalElement.INLINE_TYPE_PROPERTY, t, list);
    }
    
    
    public void setBlock(Set<Block> block) {
        setAttribute(BLOCK_PROPERTY, SchemaAttributes.BLOCK,
                block == null ? null : 
                    Util.convertEnumSet(Block.class, block));
    }
    
    public Set<Block> getBlock() {
        String s = getAttribute(SchemaAttributes.BLOCK);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }

    public Set<Block> getBlockEffective() {
        Set<Block> v = getBlock();
        return v == null ? getBlockDefault() : v;
    }

    public Set<Block> getBlockDefault() {
        Set<Schema.Block> v = getModel().getSchema().getBlockDefaultEffective();
        return Util.convertEnumSet(Block.class, v);
    }

    /**
     *
     */
    public Collection<Constraint> getConstraints() {
        return getChildren(Constraint.class);
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
    public String getFixed() {
        return getAttribute(SchemaAttributes.FIXED);
    }
    
    /**
     *
     */
    public LocalType getInlineType() {
        Collection<LocalType> elements = getChildren(LocalType.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public  NamedComponentReference<? extends GlobalType> getType() {
       return resolveGlobalReference(GlobalType.class, SchemaAttributes.TYPE);
    }
    
    /**
     *
     */
    public Boolean isNillable() {
        String s = getAttribute(SchemaAttributes.NILLABLE);
        return s == null ? null : Boolean.parseBoolean(s);
    }

    public boolean getNillableDefault() {
        return false;
    }

    public boolean getNillableEffective() {
        Boolean v = isNillable();
        return v == null ? getNillableDefault() : v;
    }
    
}

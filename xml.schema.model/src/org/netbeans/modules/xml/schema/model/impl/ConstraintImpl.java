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
import java.util.List;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.Constraint;
import org.netbeans.modules.xml.schema.model.Field;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Selector;
import org.w3c.dom.Element;/**
 *
 * @author Vidhya Narayanan
 */
public abstract class ConstraintImpl extends NamedImpl
	implements Constraint {
    
    /**
     * Creates a new instance of ConstraintImpl 
     */
    public ConstraintImpl(SchemaModelImpl model) {
        this(model,createNewComponent(SchemaElements.KEY,model));
    }
    
    /**
     * Creates a new instance of ConstraintImpl
     */
    public ConstraintImpl(SchemaModelImpl model, Element el) {
        super(model, el);
    }
    
    /**
     *
     */
    public void setSelector(Selector s) {
        List<Class<? extends SchemaComponent>> classes = new ArrayList<Class<? extends SchemaComponent>>();
        classes.add(Annotation.class);
        setChild(Selector.class, SELECTOR_PROPERTY, s, classes);
    }
    
    /**
     *
     */
    public Selector getSelector() {
        Collection<Selector> elements = getChildren(Selector.class);
        if(!elements.isEmpty()){
            return elements.iterator().next();
        }
        return null;
    }
    
    /**
     *
     */
    public Collection<Field> getFields() {
        return getChildren(Field.class);
    }
    
    /**
     *
     */
    public void deleteField(Field field) {
        removeChild(FIELD_PROPERTY, field);
    }
    
    /**
     *
     */
    public void addField(Field field) {
        List<java.lang.Class<? extends SchemaComponent>> list = new ArrayList<Class<? extends SchemaComponent>>();
        list.add(Annotation.class);
        list.add(Selector.class);
        addAfter(FIELD_PROPERTY, field, list);
    }
}

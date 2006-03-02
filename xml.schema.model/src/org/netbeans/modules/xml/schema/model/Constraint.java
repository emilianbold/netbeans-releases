/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import org.netbeans.modules.xml.xam.Named;

/**
 * This is the super interface for Key and Unique.
 * @author Chris Webster
 */
public interface Constraint extends SchemaComponent, Named<SchemaComponent> {
    
    public static final String SELECTOR_PROPERTY = "selector";
    public static final String FIELD_PROPERTY = "field";
    
    Selector getSelector();
    void setSelector(Selector s);
    
    Collection<Field> getFields();
    void addField(Field field);
    void deleteField(Field field);
}

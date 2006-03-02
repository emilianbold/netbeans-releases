/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents a reference to a group.
 * @author Chris Webster
 */
public interface GroupReference extends ComplexExtensionDefinition,
        SequenceDefinition, ComplexTypeDefinition, SchemaComponent  {
    public static final String REF_PROPERTY         = "ref";
    public static final String MAX_OCCURS_PROPERTY  = "maxOccurs";
    public static final String MIN_OCCURS_PROPERTY   = "minOccurs";
    
    String getMaxOccurs();
    void setMaxOccurs(String max);
    String getMaxOccursDefault();
    String getMaxOccursEffective();
    
    Integer getMinOccurs();
    void setMinOccurs(Integer min);
    int getMinOccursDefault();
    int getMinOccursEffective();
    
    GlobalReference<GlobalGroup> getRef();
    void setRef(GlobalReference<GlobalGroup> def);
}

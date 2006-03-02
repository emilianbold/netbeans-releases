/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents a complex content model.
 * @author Chris Webster
 */
public interface ComplexContent extends ComplexTypeDefinition, SchemaComponent {
    public static final String MIXED_PROPERTY = "mixed";
    public static final String LOCAL_DEFINITION_PROPERTY = "localDefinition";
    
    Boolean isMixed();
    void setMixed(Boolean mixed);
    boolean getMixedDefault();
    boolean getMixedEffective();
    
    ComplexContentDefinition getLocalDefinition();
    void setLocalDefinition(ComplexContentDefinition definition);
}

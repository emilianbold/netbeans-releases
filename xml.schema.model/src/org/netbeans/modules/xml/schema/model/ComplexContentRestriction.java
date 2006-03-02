/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents a restriction within a complex content element.
 * @author Chris Webster
 */
public interface ComplexContentRestriction extends ComplexContentDefinition,
        LocalAttributeContainer, SchemaComponent {
    public static final String BASE_PROPERTY = "base";
    public static final String DEFINITION_CHANGED_PROPERTY = "definition";
    
    GlobalReference<GlobalComplexType> getBase();
    void setBase(GlobalReference<GlobalComplexType> type);
    
    ComplexTypeDefinition getDefinition();
    void setDefinition(ComplexTypeDefinition definition);
}

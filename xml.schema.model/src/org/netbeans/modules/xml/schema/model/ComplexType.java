/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

import java.util.Collection;

/**
 * This interface represents the common capabilities between global complex
 * types and locally defined complex types.
 * @author Chris Webster
 */
public interface ComplexType extends LocalAttributeContainer {
    
    public static final String MIXED_PROPERTY = "mixed";
    public static final String DEFINITION_PROPERTY = "definition"; 
    
    Boolean isMixed();
    void setMixed(Boolean mixed);
    boolean getMixedDefault();
    boolean getMixedEffective();
    
    ComplexTypeDefinition getDefinition();
    void setDefinition(ComplexTypeDefinition content);
}

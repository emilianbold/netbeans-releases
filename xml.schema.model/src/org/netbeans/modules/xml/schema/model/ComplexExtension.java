/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents an extension of a complex (or mixed) content
 * complex type.
 * @author Chris Webster
 */
public interface ComplexExtension extends Extension,
        ComplexContentDefinition, SchemaComponent {
    public static final String LOCAL_DEFINITION_PROPERTY = "localDefinition"; //NOI18N
    
    void setLocalDefinition(ComplexExtensionDefinition content);
    ComplexExtensionDefinition getLocalDefinition();
}

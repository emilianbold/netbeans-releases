/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */


package org.netbeans.modules.xml.schema.model;

/**
 * This interface represents the simple content element. 
 * @author Chris Webster
 */
public interface SimpleContent extends ComplexTypeDefinition, SchemaComponent  {
        public static final String LOCAL_DEFINITION_PROPERTY = "restriction"; //NOI18N
        
        SimpleContentDefinition getLocalDefinition();
        void setLocalDefinition(SimpleContentDefinition definition);
}

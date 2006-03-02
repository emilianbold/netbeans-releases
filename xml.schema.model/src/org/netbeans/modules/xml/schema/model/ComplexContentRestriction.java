/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
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

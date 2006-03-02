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

import java.util.Collection;
import org.netbeans.modules.xml.xam.GlobalReference;

/**
 * This interface represents
 * @author Chris Webster
 */
public interface Union extends SimpleTypeDefinition, SchemaComponent  {
    
    public static final String INLINE_TYPE_PROPERTY = "inline_type";
    public static final String MEMBER_TYPES_PROPERTY = "memberTypes";
    // this is a list type
    Collection<GlobalReference<GlobalSimpleType>> getMemberTypes();
    void addMemberType(GlobalReference<GlobalSimpleType> gst);
    void removeMemberType(GlobalReference<GlobalSimpleType> gst);
    
    Collection<LocalSimpleType> getInlineTypes();
    void addInlineType(LocalSimpleType type);
    void removeInlineType(LocalSimpleType type);
}

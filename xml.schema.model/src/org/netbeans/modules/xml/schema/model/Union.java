/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
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

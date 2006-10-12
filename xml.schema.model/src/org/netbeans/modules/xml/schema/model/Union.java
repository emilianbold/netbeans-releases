/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.schema.model;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 * This interface represents
 * @author Chris Webster
 */
public interface Union extends SimpleTypeDefinition, SchemaComponent  {

    public static final String INLINE_TYPE_PROPERTY = "inline_type";
    public static final String MEMBER_TYPES_PROPERTY = "memberTypes";
    // this is a list type
    List<NamedComponentReference<GlobalSimpleType>> getMemberTypes();
    void addMemberType(NamedComponentReference<GlobalSimpleType> gst);
    void removeMemberType(NamedComponentReference<GlobalSimpleType> gst);
    void setMemberTypes(List<NamedComponentReference<GlobalSimpleType>> types);
    
    Collection<LocalSimpleType> getInlineTypes();
    void addInlineType(LocalSimpleType type);
    void removeInlineType(LocalSimpleType type);
}

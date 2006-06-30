/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.schema2beans;

import java.util.*;
import java.io.*;


/**
 *  The BaseBean introspection methods return BaseProperty and BaseAttribute
 *  objects. This interface is the attribute equivalent to the BaseProperty
 *  interface.
 *
 *  This interface is the public access to the schema2beans internal attribute
 *  information (see AttrProp class for the implementation of this interface).
 */
public interface BaseAttribute {
    /**
     *	Values returned by getType()
     **/
    static public final int TYPE_CDATA 		= AttrProp.CDATA;
    static public final int TYPE_ENUM 		= AttrProp.ENUM;
    static public final int TYPE_NMTOKEN 	= AttrProp.NMTOKEN;
    static public final int TYPE_ID 		= AttrProp.ID;
    static public final int TYPE_IDREF 		= AttrProp.IDREF;
    static public final int TYPE_IDREFS 	= AttrProp.IDREFS;
    static public final int TYPE_ENTITY 	= AttrProp.ENTITY;
    static public final int TYPE_ENTITIES 	= AttrProp.ENTITIES;
    static public final int TYPE_NOTATION 	= AttrProp.NOTATION;
    
    /**
     *	Values returned by getOption()
     */
    static public final int OPTION_REQUIRED 	= AttrProp.REQUIRED;
    static public final int OPTION_IMPLIED 	= AttrProp.IMPLIED;
    static public final int OPTION_FIXED 	= AttrProp.FIXED;
    
    
    /**
     *	Return the name of the attribute as it is used in the bean class.
     */
    public String 	getName();
    
    /**
     *	Return the dtd name of the attribute, as it appears in the DTD file.
     */
    public String 	getDtdName();
    
    /**
     *	Return true if the name is either equals to getName() or getDtdName()
     */
    public boolean	hasName(String name);
    
    /**
     *	If the attribute is Enum, returns the list of possible values
     */
    public String[] getValues();
    
    /**
     *	Default value used when creating this attribute
     */
    public String getDefaultValue();
    
    /**
     *	True if the attribute is Enum
     */
    public boolean isEnum();
    
    /**
     *	True if the attribute has a fixed value
     */
    public boolean isFixed();
    
    /**
     *	Returns one of the following constants:
     *
     *		OPTION_REQUIRED
     *		OPTION_IMPLIED
     *		OPTION_FIXED
     */
    public int getOption();
    
    /**
     *	Returns one of the following constants:
     *
     *		TYPE_CDATA
     *		TYPE_ENUM
     *		TYPE_NMTOKEN
     *		TYPE_ID
     *		TYPE_IDREF
     *		TYPE_IDREFS
     *		TYPE_ENTITY
     *		TYPE_ENTITIES
     *		TYPE_NOTATION
     */
    public int getType();
    
    /**
     *	In general the attributes are defined in the DTD file,
     *	like the properties, and are therefore part of the bean structure,
     *	because part of the generated beans. This method returns false
     *	for such attributes.
     *	If an attribute is not declared in the DTD file but used in the XML
     *	document, the schema2beans consider such attributes as transient and
     *	add them, on the fly, into the bean structures. This method returns
     *	true for such attributes.
     */
    public boolean isTransient();
}

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
import java.beans.*;


/**
 *  The BaseProperty interface is the public (user) view of the schema2beans
 *  property objects BeanProp. See also BaseAttribute and the BaseBean
 *  introspection methods.
 */
public interface BaseProperty {
    public class VetoException extends RuntimeException {
	PropertyVetoException pce;
	
	public VetoException(PropertyVetoException pce, String str) {
	    super(str);
	    this.pce = pce;
	}
	
	public PropertyVetoException getPropertyVetoException() {
	    return this.pce;
	}
    }
    
    
    /**
     *	Values returned by getInstanceType()
     */
    public static final int INSTANCE_OPTIONAL_ELT	=	Common.TYPE_0_1;
    public static final int INSTANCE_MANDATORY_ELT 	=	Common.TYPE_1;
    public static final int INSTANCE_OPTIONAL_ARRAY	=	Common.TYPE_0_N;
    public static final int INSTANCE_MANDATORY_ARRAY	=	Common.TYPE_1_N;
    

    /**
     *	Return true if this is the root property
     */
    public boolean 	isRoot();

    /**
     *  Return the BaseBean for this property if there is one
     */
    public BaseBean	getParent();

    /**
     *	Return the name of the property as it is used in the bean class.
     */
    public String 	getName();
    
    /**
     *	Return the dtd name of the property, as it appears in the DTD file.
     */
    public String 	getDtdName();
    
    /**
     *	Return the class type of the property. If a wrapper is defined for
     *	the class, the class of the wrapper is returned instead of the
     *	java.lang.String class.
     */
    public Class	getPropertyClass();
    
    /**
     *	Return true if the property is an indexed property.
     */
    public boolean 	isIndexed();
    
    /**
     *	Return true if the property is a bean (a node in the graph). Any
     *	object of the graph which is a bean is a subclass of BaseBean.
     */
    public boolean	isBean();
    
    /**
     *	If the property is an indexed property, return the number of element
     *	(might contain null elements).
     */
    public int 		size();
    
    /**
     *	If the property has an attribute, return all the attribute names.
     *	Return an empty array of there is no attribute.
     */
    public String[] getAttributeNames();
    
    /**
     *	If the property has an attribute, return all the attributes
     *	definitions, as a list of BaseAttribute interfaces.
     *	The array is empty if there is no attribute.
     */
    public BaseAttribute[] getAttributes();
    
    /**
     *	Returns the full path name of the property (unique String name
     *	identifying the property for the lifetime of the graph).
     */
    public String getFullName(int index);
    public String getFullName();
    
    /**
     *	Returns the instanciation type of the property. This might be one of
     *	the following:
     *
     *  	INSTANCE_OPTIONAL_ELT
     *  	INSTANCE_MANDATORY_ELT
     *  	INSTANCE_OPTIONAL_ARRAY
     *  	INSTANCE_MANDATORY_ARRAY
     */
    public int getInstanceType();
    
    /**
     *	Returns true if this property is amoung a set of properties choice
     *	(DTD element such as (a | b | ...)
     */
    public boolean isChoiceProperty();
    
    
    /**
     *	If this property is a choice property, returns all other choice
     *	properties associated to this one (including this one).
     *	If the current property is not a choice property, returns null.
     */
    public BaseProperty[] getChoiceProperties();
    
    /**
     *	Return true if the name is either equals to getName() or getDtdName()
     */
    public boolean  hasName(String name);

    /**
     *	Return true if this property matters when schema2beans compare graphs
     */
    public boolean isKey();
}

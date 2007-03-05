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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package com.sun.rave.web.ui.component.util.event;

import java.util.HashMap;
import java.util.Map;


/**
 *  This class describes an input or output parameter.
 *
 *  @author Ken Paulsen	(ken.paulsen@sun.com)
 */
public class IODescriptor implements java.io.Serializable {

    /**
     *	Constructor.
     *
     *	@param	name	The name of the input/output field
     *	@param	type	The type of the input/output field
     */
    public IODescriptor(String name, String type) {
	setName(name);
	setType(type);
    }


    /**
     *	This method returns the name for this handler definition.
     */
    public String getName() {
	if (_name == null) {
	    throw new NullPointerException("Name cannot be null!");
	}
	return _name;
    }


    /**
     *	This method sets the handler definitions name (used by the contsrutor).
     */
    protected void setName(String name) {
	_name = name;
    }


    /**
     *	For future tool support
     */
    public String getDescription() {
	return _description;
    }


    /**
     *	For future tool support
     */
    public void setDescription(String desc) {
	_description = desc;
    }


    /**
     *	This method returns the type for this parameter
     */
    public Class getType() {
	return _type;
    }


    /**
     *	This method sets the type for this parameter
     */
    public void setType(Class type) {
	_type = type;
    }


    /**
     *	This method sets the type for this parameter
     */
    public void setType(String type) {
	if ((type == null) || (type.trim().length() == 0)) {
	    return;
	}
	Class cls = (Class)_typeMap.get(type);
	if (cls == null) {
	    try {
		cls = Class.forName(type);
	    } catch (Exception ex) {
		throw new RuntimeException(
		    "Unable to determine parameter type '"+type+
		    "' for parameter named '"+getName()+"'.", ex);
	    }
	}
	_type = cls;
    }


    /**
     *	This method returns the default for this parameter (valid for input
     *	only)
     */
    public Object getDefault() {
	return _default;
    }


    /**
     *	This method sets the default for this parameter (valid for input only)
     */
    public void setDefault(Object def) {
	_default = def;
    }

    /**
     *	This method returns the default for this parameter (valid for input
     *	only)
     */
    public boolean isRequired() {
	return _required;
    }

    /**
     *	<P> This method specifies whether this Input field is required.</P>
     */
    public void setRequired(boolean required) {
	_required = required;
    }

    //	The following provides some basic pre-defined types
    private static Map		_typeMap		= new HashMap();
    static {
	_typeMap.put("boolean", Boolean.class);
	_typeMap.put("Boolean", Boolean.class);
	_typeMap.put("byte", Byte.class);
	_typeMap.put("Byte", Byte.class);
	_typeMap.put("char", Character.class);
	_typeMap.put("Character", Character.class);
	_typeMap.put("double", Double.class);
	_typeMap.put("Double", Double.class);
	_typeMap.put("float", Float.class);
	_typeMap.put("Float", Float.class);
	_typeMap.put("int", Integer.class);
	_typeMap.put("Integer", Integer.class);
	_typeMap.put("long", Long.class);
	_typeMap.put("Long", Long.class);
	_typeMap.put("short", Short.class);
	_typeMap.put("Short", Short.class);
	_typeMap.put("char[]", String.class);
	_typeMap.put("String", String.class);
	_typeMap.put("Object", Object.class);
    }

    private String  _name	    = null;
    private String  _description    = null;
    private Object  _default	    = null; // Input only
    private Class   _type	    = Object.class;
    private boolean _required	    = false; // Input only

    private static final long serialVersionUID = 0xA9B8C7D6E5F40312L; 
}

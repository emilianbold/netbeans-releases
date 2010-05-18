/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

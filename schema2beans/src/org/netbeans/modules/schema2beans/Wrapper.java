/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.schema2beans;

/**
 *  The Wrapper interface is one of the two ways a user can initialize a
 *  Wrapper object.
 *
 *  By default, when schema2beans generates the java classes, a #PCDATA DTD type 
 *  is mapped to a java String. Sometime, the user might want to map an
 *  element to either a scalar or a specialized type (user defined class).
 *
 *  For example, a price DTD element specified as #PCDATA could be mapped
 *  to a float or integer data type. Or a date DTD element also specified
 *  as #PCDATA could be mapped to a specialized Date object, that the user might
 *  provide. schema2beans calls these specialized object 'wrappers'.
 *
 *  If the user specifies a wrapper object in the mdd file (see user
 *  documentation for mdd explanations), schema2beans uses the wrapper class
 *  instead of the String type. In this case, schema2beans needs to initialize
 *  the wrapper object using the String value from the XML document, and
 *  also needs to be able to get the String value from the wrapper 
 *  object (in order to write back the XML document).
 *
 *  This what this Wrapper interface provides. A wrapper class has
 *  has either to have a String constructor and toString() method,
 *  or implements the Wrapper interface. This is how schema2beans can set/get
 *  the String values of user wrapper/customized object.
 *
 */
public interface Wrapper {
    /**
     *	Method called by the schema2beans runtime to get the String value of the
     *	wrapper object. This String value is the value that has to appear 
     *	in the XML document.
     */
    public String 	getWrapperValue();
    
    /**
     *	Method called by the schema2beans runtime to set the value of the
     *	wrapper object. The String value is the value read in the XML document.
     */
    public void 	setWrapperValue(String value);
}

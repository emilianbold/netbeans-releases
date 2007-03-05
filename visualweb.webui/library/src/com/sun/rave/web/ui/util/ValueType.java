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
package com.sun.rave.web.ui.util;

/**
 * This class provides a typesafe enumeration of value types (see also
 * ValueTypeEvaluator). The ValueTypeEvaluator and the
 * ValueTypes are helper classes for UIComponents which accept
 * value bindings that can be either single objects or a collection of
 * objects (for example, an array). Typically, these components have
 * to process input differently depending on the type of the value
 * object.
 *@see com.sun.rave.web.ui.util.ValueTypeEvaluator
 *
 */
public class ValueType {

    private String type;

    /** Indicates that the value binding is an array (of primitives
     *  or of objects). */
    public static final ValueType ARRAY = new ValueType("array") ;
    /** Indicates that the value binding is assigneable to a
     * java.util.List. */
    public static final ValueType LIST = new ValueType("list");
    /** Indicates that the value binding is neither an array, nor
     * does it implement java.util.List. */
    public static final ValueType OBJECT = new ValueType("object"); 
   /** Indicates that the value binding is invalid. This is a place
    * holder, currently the ValueTypeEvaluator does not return this.
    * It should be used to help page authors identify what the valid
    * types are (e.g. java.util.List works, but java.util.Collection
    *does not). */
    public static final ValueType INVALID = new ValueType("invalid"); 
    /** Indicates that no value was specified for the component. */
    public static final ValueType NONE = new ValueType("none"); 

    private ValueType(String s) { 
	type = s; 
    } 
       
    /**
     * Get a String representation of the action
     * @return A String representation of the value type.
     */
    public String toString() {
	return type;
    }
}

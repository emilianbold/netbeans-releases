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

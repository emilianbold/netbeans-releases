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

package org.netbeans.modules.uml.core.reverseengineering.reframework;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

public interface IREParameter extends IParserData {
    public static final int PDK_IN = 0;
    public static final int PDK_INOUT = 1;
    public static final int PDK_OUT = 2;
    public static final int PDK_RESULT = 3;
    
    /**
     * Specifies an expression whose evaluation yields a value to be used when no argument is supplied for the Parameter.
     */
    public String getDefaultValue();
    
    /**
     * Specifies what kind of a Parameter is required.  A parameter can be an in parameter,  out parameter, or in/out parameter.
     */
    public int getKind();
    
    /**
     * Designates a type to which an argument value must conform.
     */
    public String getType();
    
    /**
     * Retrieve the name of the parameter.
     */
    public String getName();
    
    /**
     * Specifies if the parameter is a primitive attribute or an object instance.
     */
    public boolean getIsPrimitive();
    
    public ETList<IREMultiplicityRange> getMultiplicity() ;
}

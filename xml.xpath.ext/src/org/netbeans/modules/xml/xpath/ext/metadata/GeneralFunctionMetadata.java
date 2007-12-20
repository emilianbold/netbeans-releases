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

package org.netbeans.modules.xml.xpath.ext.metadata;

import java.util.List;
import javax.swing.Icon;

/**
 * Provides information about a function which is helpfull at design time
 * like following: 
 *  +-- function name
 *  +-- short description
 *  +-- long description
 *  +-- is operation or function
 *  +-- supported types of arguments
 *  +-- type of result
 *  +-- quantity of arguments
 *  +-- priority level (for operations only). 
 *     It can be used for avoiding redundant brackets 
 *     when an expression is converted to a string
 * 
 * @author nk160297
 */
public interface GeneralFunctionMetadata<NameType> {

    NameType getName();
    
    Icon getIcon();
    
    String getDisplayName();
    
    String getShortDescription();

    String getLongDescription();
    
    FunctionType getFunctionType();
    
    /**
     * Null or empty list means that the function doesn't have arguments.
     */  
    List<AbstractArgument> getArguments();
    
    XPathType getResultType();
    
    ResultTypeCalculator getResultTypeCalculator();
    
    enum FunctionType {
        CORE_FUNCTION,
        EXT_FUNCTION, 
        OPERATION;
    }
    
}

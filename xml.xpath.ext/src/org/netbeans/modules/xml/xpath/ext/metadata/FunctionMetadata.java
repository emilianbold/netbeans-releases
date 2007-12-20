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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.xpath.ext.metadata;

import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;

/**
 * 
 * @author nk160297
 */
public interface FunctionMetadata<NameType> extends GeneralFunctionMetadata<NameType> {

    /**
     * Indicates if the specified instance of function requires a context item. 
     * If true then it usually means that the function uses the context item as 
     * an implicit argument.
     * @see http://www.w3.org/TR/xpath20/#dt-context-item
     * 
     * The parameter is required because of some functions can have 
     * different forms: with or without an arguments. So such functions
     * can have different behaviour depends on the used form. 
     * 
     */ 
    boolean isContextItemRequired(XPathOperationOrFuntion func);
    
}

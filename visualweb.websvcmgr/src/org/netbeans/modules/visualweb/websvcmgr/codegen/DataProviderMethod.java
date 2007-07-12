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
package org.netbeans.modules.visualweb.websvcmgr.codegen;

import java.util.List;

/**
 * Interface that provides access to java method information.  Used by the
 * DataProvider information storage structures.
 *
 * @author quynguyen
 */
public interface DataProviderMethod {

    /**
     * Gets the method name.
     * @return the name of the method
     */
    public String getMethodName();

    /**
     * Gets the fully quantified method return type
     * @return the return type
     */
    public String getMethodReturnType();

    /**
     * Gets the parameter list.  Returns an empty list if there are no parameters
     * @return the parameter list
     */
    public List<DataProviderParameter> getParameters();
    
    /**
     * @return a list of the exception types
     */
    public List<String> getExceptions();
}

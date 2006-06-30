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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jmx.test.helpers;

/**
 *
 * @author an156382
 */
public class Parameter {

    private String paramName = "";
    private String paramType = "";
    private String paramComment = "";

    /** Creates a new instance of Parameter */
    public Parameter(String paramName, String paramType, String paramComment) {

        this.paramName = paramName;
        this.paramType = paramType;
        this.paramComment = paramComment;
    }

    /**
     * Method which returns the name of the parameter
     * @return paramName the name of the parameter
     *
     */
    public String getParamName() {
        return paramName;
    }
    
    /**
     * Method which returns the type of the parameter
     * @return paramType the type of the parameter
     *
     */
    public String getParamType() {
        return paramType;
    }
    
    /**
     * Method which returns the comment of the parameter
     * @return paramComment the comment of the parameter
     *
     */
    public String getParamComment() {
        return paramComment;
    }
    
}

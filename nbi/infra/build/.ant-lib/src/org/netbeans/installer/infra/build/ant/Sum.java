/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.infra.build.ant;

import org.apache.tools.ant.Task;

/**
 * This class is an ant task which is capable of summing two integer values and 
 * storing the result as the value of a property.
 * 
 * @author Kirill Sorokin
 */
public class Sum extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the first item.
     */
    private String arg1;
    
    /**
     * Name of the second item.
     */
    private String arg2;
    
    /**
     * Name of the property shich should hold the result.
     */
    private String property;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'arg1' property.
     * 
     * @param arg1 New value for the 'arg1' property.
     */
    public void setArg1(final String arg1) {
        this.arg1 = arg1;
    }
    
    /**
     * Setter for the 'arg2' property.
     * 
     * @param arg2 New value for the 'arg2' property.
     */
    public void setArg2(final String arg2) {
        this.arg2 = arg2;
    }
    
    /**
     * Setter for the 'property' property.
     * 
     * @param property New value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task.
     */
    public void execute() {
        getProject().setProperty(
                property, 
                Long.toString(Long.parseLong(arg1) + Long.parseLong(arg2)));
    }
}

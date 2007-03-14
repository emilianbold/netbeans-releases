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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This class is an ant task which is capable of setting a property value basing on 
 * either a supplied value or a value of another property.
 * 
 * @author Kirill Sorokin
 */
public class SetProperty extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the target property whose value should be set.
     */
    private String property;
    
    /**
     * Name of the source property whose value should be evaluated and set as the 
     * value of the target property.
     */
    private String source;
    
    /**
     * String which should be set as the value for the target property.
     */
    private String value;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     * 
     * @param property New value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    /**
     * Setter for the 'source' property.
     * 
     * @param source New value for the 'source' property.
     */
    public void setSource(final String source) {
        this.source = source;
    }
    
    /**
     * Setter for the 'value' property.
     * 
     * @param value New value for the 'value' property.
     */
    public void setValue(final String value) {
        this.value = value;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. If the source property was specified, its value is 
     * evaluated and set as the value of the target property. Otherwise the literal 
     * string value is used.
     */
    public void execute() {
        final Project project = getProject();
        
        if (source != null) {
            log("setting " + property + " to " + 
                    project.replaceProperties(project.getProperty(source)));
            project.setProperty(
                    property, 
                    project.replaceProperties(project.getProperty(source)));
        } else {
            log("setting " + property + " to " + project.replaceProperties(value));
            project.setProperty(
                    property, 
                    project.replaceProperties(value));
        }
    }
}

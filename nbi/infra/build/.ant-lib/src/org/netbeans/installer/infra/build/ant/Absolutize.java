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

import java.io.File;
import org.apache.tools.ant.Task;

/**
 * This class is an ant task which absolutizes the path contained in a given 
 * property with regard to the ant project's basedir.
 * 
 * @author Kirill Sorokin
 */
public class Absolutize extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the property whose value should be corrected.
     */
    private String property  = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     * 
     * @param property The new value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. A <code>File</code> object is constructed from the 
     * property value and is then compared to its absolute variant. If they differ 
     * the absolute path is put back to the property.
     */
    public void execute() {
        final String value = getProject().getProperty(property);
        final File file = new File(value);
        
        if (!file.equals(file.getAbsoluteFile())) {
            getProject().setProperty(
                    property, 
                    new File(getProject().getBaseDir(), value).getAbsolutePath());
        }
    }
}

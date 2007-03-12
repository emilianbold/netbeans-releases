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
import org.netbeans.installer.infra.build.ant.utils.Utils;

/**
 * This class is an ant task which is capable of calculating the size of a file (or
 * a directory) and storing it as the value of a given property.
 * 
 * @author Kirill Sorokin
 */
public class SizeOf extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * File for which the size should be calculated.
     */
    private File file;
    
    /**
     * Name of the property whose value should contain the size.
     */
    private String property;
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'file' property.
     * 
     * @param path New value for the 'path' property.
     */
    public void setFile(final String path) {
        file = new File(path);
        if (!file.equals(file.getAbsoluteFile())) {
            file = new File(getProject().getBaseDir(), path);
        }
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
        Utils.setProject(getProject());
        
        getProject().setProperty(property, Long.toString(Utils.size(file)));
    }
}

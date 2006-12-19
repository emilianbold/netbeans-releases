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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.installer.infra.build.ant;

import java.net.URLEncoder;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

public class SetProperty extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String property = null;
    
    private String source   = null;
    private String value    = null;
            
    private boolean uri     = false;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setProperty(final String property) {
        this.property = property;
    }
    
    public void setSource(final String source) {
        this.source = source;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        Project project = getProject();
        
        String newValue = null;
        
        if (source != null) {
            newValue = project.replaceProperties(project.getProperty(source));
        } else {
            newValue = project.replaceProperties(value);
        }
        
        project.setProperty(property, newValue);
    }
}

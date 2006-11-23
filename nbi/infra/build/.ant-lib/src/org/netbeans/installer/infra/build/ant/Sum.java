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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

public class Sum extends Task {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String arg1     = null;
    private String arg2     = null;
    private String property = null;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setArg1(final String arg1) {
        this.arg1 = arg1;
    }
    
    public void setArg2(final String arg2) {
        this.arg2 = arg2;
    }
    
    public void setProperty(final String property) {
        this.property = property;
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        getProject().setProperty(property, "" + (Long.parseLong(arg1) + Long.parseLong(arg2)));
    }
}

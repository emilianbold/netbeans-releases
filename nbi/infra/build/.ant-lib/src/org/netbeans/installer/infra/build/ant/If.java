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

public class If extends Task implements TaskContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private String       property  = null;
    private String       value     = null;
    
    private List<Task>   children  = new LinkedList<Task>();
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setProperty(final String property) {
        this.property = property;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }
    
    // task container ///////////////////////////////////////////////////////////////
    public void addTask(final Task task) {
        children.add(task);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        if (value != null) {
            if (getProject().getProperty(property).equals(value)) {
                executeChildren();
            }
        } else {
            if (getProject().getProperty(property) != null) {
                executeChildren();
            }
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void executeChildren() throws BuildException {
        for (Task task: this.children) {
            task.perform();
        }
    }
}

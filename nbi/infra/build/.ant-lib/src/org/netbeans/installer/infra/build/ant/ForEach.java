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

public class ForEach extends Task implements TaskContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<String> list      = null;
    
    private int          from      = 0;
    private int          to        = 0;
    private int          increment = 1;
    
    private String       property  = null;
    
    private List<Task>   children  = new LinkedList<Task>();
    
    private boolean      wrongArgs = false;
    
    // setters //////////////////////////////////////////////////////////////////////
    public void setList(final String list) {
        this.list = Arrays.asList(list.trim().split(" "));
    }
    
    public void setProperty(final String property) {
        this.property = property;
    }
    
    public void setFrom(final String from) {
        try {
            this.from = Integer.parseInt(from);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'from'.");
            wrongArgs = true;
        }
    }
    
    public void setTo(final String to) {
        try {
            this.to = Integer.parseInt(to);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'to'.");
            wrongArgs = true;
        }
    }
    
    public void setIncrement(final String increment) {
        try {
            this.increment = Integer.parseInt(increment);
        } catch (NumberFormatException e) {
            log("Wrong value for parameter 'increment'.");
            wrongArgs = true;
        }
    }
    
    // task container ///////////////////////////////////////////////////////////////
    public void addTask(final Task task) {
        children.add(task);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    public void execute() throws BuildException {
        if (wrongArgs) {
            throw new BuildException("Correct parameters were not supplied.");
        }
        
        if (list != null) {
            for (String value: this.list) {
                executeChildren(value);
            }
            return;
        } else {
            for (int i = from; i <= to; i += increment) {
                executeChildren(Integer.toString(i));
            }
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void executeChildren(String value) throws BuildException {
        getProject().setProperty(this.property, value);
        
        for (Task task: this.children) {
            task.perform();
        }
    }
}

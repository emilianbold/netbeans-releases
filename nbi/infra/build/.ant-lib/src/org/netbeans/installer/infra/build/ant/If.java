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

import java.util.LinkedList;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;

/**
 * This class is an ant task, which adds conditional execution capabilities. It 
 * examines the value of the given property and executed the nested tasks only if 
 * the the property's value equals to the given string.
 * 
 * @author Kirill Sorokin
 */
public class If extends Task implements TaskContainer {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    /**
     * Name of the property whose value should be checked.
     */
    private String property;
    
    /**
     * String which should be equal to the property's value in order for the nested 
     * tasks to execute.
     */
    private String value;
    
    /**
     * List of child tasks which should be executed if the condition is satisfied.
     */
    private List<Task> children;
    
    // constructor //////////////////////////////////////////////////////////////////
    /**
     * Constructs a new instance of the {@link If} task. It simply sets the
     * default values for the attributes.
     */
    public If() {
        children = new LinkedList<Task>();
    }
    
    // setters //////////////////////////////////////////////////////////////////////
    /**
     * Setter for the 'property' property.
     * 
     * @param property The new value for the 'property' property.
     */
    public void setProperty(final String property) {
        this.property = property;
    }
    
    /**
     * Setter for the 'value' property.
     * 
     * @param value The new value for the 'value' property.
     */
    public void setValue(final String value) {
        this.value = value;
    }
    
    /**
     * Registers a child task. The supplied <code>Task</code> object will be added 
     * to the list of child tasks and executed if the condition is satisfied.
     * 
     * @param task The <code>Task</code> object to register.
     */
    public void addTask(final Task task) {
        children.add(task);
    }
    
    // execution ////////////////////////////////////////////////////////////////////
    /**
     * Executes the task. If the required value is set, then the property's value is 
     * compared to it and the child tasks are executes if they are equal. If the 
     * required value is not set, then the child tasks are executed if the property
     * was set, without regard to its value.
     * 
     * @throws org.apache.tools.ant.BuildException if a child task fails to execute.
     */
    public void execute() throws BuildException {
        if (getProject().getProperty(property) != null) {
            if (value != null) {
                if (getProject().getProperty(property).equals(value)) {
                    executeChildren();
                }
            } else {
                executeChildren();
            }
        }
    }
    
    // private //////////////////////////////////////////////////////////////////////
    /**
     * Executes the child tasks.
     */
    private void executeChildren() throws BuildException {
        for (Task task: this.children) {
            task.perform();
        }
    }
}

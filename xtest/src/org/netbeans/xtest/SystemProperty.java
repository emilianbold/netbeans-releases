/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * SystemProperty.java
 *
 * Created on February 8, 2001, 8:59 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import java.lang.String;

/**
 *
 * @author  vstejskal
 * @version 0.1
 */
public class SystemProperty extends Task {

    private String name = null;
    private String value = null;
    
    /** Creates new SystemProperty */
    public SystemProperty() {
    }

    public void init() throws BuildException {
    }

    public void execute() throws BuildException {
        if (null == name)
            throw new BuildException("The 'name' attribut must be set.");

        if (null == value)
            value = new String();
        
        System.setProperty(name, value);
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
}

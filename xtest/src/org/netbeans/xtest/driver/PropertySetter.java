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
 * PropertySetter.java
 *
 * Created on January 3, 2002, 6:25 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 *
 * @author  lm97939
 */
public class PropertySetter extends Task {

    private String out_property;
    private String in_property;

    public void setInput(String p) {
        in_property = p;
    }
    
    public void setOutput(String p) {
        out_property = p;
    }
    
    public void execute() throws BuildException {
        if (in_property == null) throw new BuildException("Attribute 'input' is empty.");   
        if (out_property == null) throw new BuildException("Attribute 'output' is empty.");   
        if (getProject().getProperty(in_property) == null) return;
        getProject().setProperty(out_property,getProject().getProperty(in_property));
    }

}

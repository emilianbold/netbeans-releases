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

package org.netbeans.nbbuild;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** Task to check value of any property. If value match than it will set
 * another property to specified value
 * @author Michal Zlamal
 */
public class CheckValue extends Task {
    String property = null;
    String value = null;
    String set = null;
    String toValue = null;
    
    /** Property to chech */
    public void setProperty(String property) {
        this.property = property;
    }
    
    /** property will be checked against this value */
    public void setIs(String value) {
        this.value = value;
    }
    
    /** Which property to set */
    public void setSet(String set) {
        this.set = set;
    }
    
    /** To which value set the property */
    public void setTovalue(String toValue) {
        this.toValue = toValue;
    }
    
    public void execute () throws BuildException {
        if (property == null)
            throw new BuildException( "Attribute \"property\" is mandantory" );
        if (value == null)
            throw new BuildException( "Attribute \"is\" is mandantory" );
        if (set == null)
            throw new BuildException( "Attribute \"set\" is mandantory" );
        if (toValue == null)
            throw new BuildException( "Attribute \"toValue\" is mandantory" );

        String propValue = this.getProject().getUserProperty( property );
        if (propValue != null) {
            if (propValue.equals(value)) {
                this.getProject().setUserProperty( set, toValue );
            }
        }
    }
}

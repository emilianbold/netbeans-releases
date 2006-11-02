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

package org.netbeans.nbbuild;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/** Task to check value of any property. If value match than it will set
 * another property to specified value
 * @author Michal Zlamal
 * @deprecated unused
 */
@Deprecated
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

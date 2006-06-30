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

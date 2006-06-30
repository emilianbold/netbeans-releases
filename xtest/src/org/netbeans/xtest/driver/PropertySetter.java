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

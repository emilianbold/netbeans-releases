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
 * FileNameGetter.java
 *
 * Created on March 13, 2002, 4:21 PM
 */

package org.netbeans.xtest.driver;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;

/**
 * @author lm97939
 */
public class FileName extends Task {

    private File file;
    private String name_prop, nameext_prop;

    public void setFile(File f) {
        file = f;
    }

    public void setNameProperty(String p) {
        name_prop = p;
    }

    public void setNameExtProperty(String p) {
        nameext_prop = p;
    }

    public void execute() throws BuildException {
        if (file == null) throw new BuildException("Attribute 'file' is empty.");
        if (name_prop == null) throw new BuildException("Attribute 'nameProperty' is empty.");
        if (nameext_prop == null) throw new BuildException("Attribute 'nameExtProperty' is empty.");
        
        String name = file.getName();
        getProject().setProperty(nameext_prop,name);
        int i = name.lastIndexOf(".");
        if (i > 0) 
            name = name.substring(0,i);
        getProject().setProperty(name_prop,name);
    }

}

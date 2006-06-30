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

import java.io.BufferedReader;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author lm97939
 */
public class LookForTimestamp extends Task {

    private File file;
    private String prop,name;

    public void setInstallPath(File f) {
        file = f;
    }

    public void setTimestampProperty(String p) {
        prop = p;
    }
    
    public void setTimestampFileName(String p) {
        name = p;
    }
    
    public void execute() throws BuildException {
        if (file == null) throw new BuildException("Attribute 'installPath' is empty.");
        if (prop == null) throw new BuildException("Attribute 'timestampProperty' is empty.");
        if (name == null) throw new BuildException("Attribute 'timestampFileName' is empty.");
        
        String dir = file.getParent();
        File timestampfile = new File(file.getParentFile(),name);
        if (timestampfile.exists()) {
          try {
            BufferedReader reader = new BufferedReader(new FileReader(timestampfile));
            String timestamp = reader.readLine();
            reader.close();
            getProject().setUserProperty(prop,timestamp);
          }
          catch (IOException e) {
              throw new BuildException(e);
          }
        }
    }

}

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
            project.setUserProperty(prop,timestamp);
          }
          catch (IOException e) {
              throw new BuildException(e);
          }
        }
    }

}

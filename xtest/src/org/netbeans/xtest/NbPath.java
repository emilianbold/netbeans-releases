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
 * NbPath.java
 *
 * Created on February 14, 2001, 6:26 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.*;

/**
 *
 * @author  vstejskal
 * @version 1.0
 */
public class NbPath extends Task {

    
    public void setJdkHomePropertyName(String propertyName) {
        this.jdkHomePropertyName = propertyName;
    }        
    

    public void execute () throws BuildException {
        
        if (null == jdkHomePropertyName) {
            throw new BuildException("You need to set jdkHomeProperty attribute to get jdk home");
            
        }        
        
        // prepare jdkhome property
        if (null == getProject().getProperty(jdkHomePropertyName)) {
            String jdkhome = lookupJdk();
            if (jdkhome != null)
                getProject().setProperty(jdkHomePropertyName, jdkhome);
        }        

    }


    private String lookupJdk() {
        File jdk = new File(getProject().getProperty("java.home"));
        if (jdk != null && jdk.exists ()) {
            if (!System.getProperty("os.name").startsWith("Mac OS X")) {
                File tmp = new File(jdk, "lib/tools.jar");
                if (!tmp.exists ()) {
                    jdk = jdk.getParentFile ();
                
                    tmp = new File(jdk, "lib/tools.jar");
                    if (!tmp.exists ())
                        return null;
                }
            }
            
            return jdk.getAbsolutePath ().replace ('\\', '/');
        }
        return null;
    }
    

    private String jdkHomePropertyName = null;
    

}

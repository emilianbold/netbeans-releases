/*
 * GetSystemProperty.java
 *
 * Created on January 9, 2002, 8:14 PM
 */

package org.netbeans.xtest;

// IMPORTANT! You may need to mount ant.jar before this class will
// compile. So mount the JAR modules/ext/ant.jar (NOT modules/ant.jar)
// from your IDE installation directory in your Filesystems before
// continuing to ensure that it is in your classpath.

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author lm97939
 */
public class GetSystemProperty extends Task {

    private String system_prop = null;
    private String ant_prop = null;
    
    public void setSystemProperty(String s) {
        system_prop = s;
    }

    public void setAntProperty(String s) {
        ant_prop = s;
    }
    
    public void execute() throws BuildException {
        if (system_prop == null) throw new BuildException("Attribute 'systemProperty' isn't set.");
        if (ant_prop == null) ant_prop = system_prop;
        
        String value = System.getProperty(system_prop);
        if (value != null) project.setProperty(ant_prop,value);
    }

}

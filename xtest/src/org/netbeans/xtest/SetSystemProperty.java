/*
 * SetSystemProperty.java
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
public class SetSystemProperty extends Task {

    private String system_prop = null;
    private String value = null;
    
    public void setSystemProperty(String s) {
        system_prop = s;
    }

    public void setValue(String v) {
        value = v;
    }
    
    public void execute() throws BuildException {
        if (system_prop == null) throw new BuildException("Attribute 'systemProperty' isn't set.");
        if (value == null) throw new BuildException("Attribute 'value' isn't set.");
        
        System.setProperty(system_prop,value);
    }

}

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
 * IdeArguments.java
 *
 * Created on February 25, 2002, 3:26 PM
 */

package org.netbeans.xtest.plugin.ide;


import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.*;

/**
 * @author lm97939
 */
public class IdeJvmArgs extends Task {

    private String jvm_args;
    private String property;
    
    public void setJvmargs(String ja) {
        jvm_args = ja;
    }
    
    public void setProperty(String p) {
        property = p;
    }

    public void execute() throws BuildException {
        if (property == null) throw new BuildException("Property 'property' is empty.");
        if (jvm_args == null || jvm_args.equals("")) 
            project.setProperty(property, "");
        
        StringBuffer buff = new StringBuffer();
        StringTokenizer str = new StringTokenizer(jvm_args," ");
        while (str.hasMoreTokens()) {
            String token = str.nextToken();
            if (buff.length() != 0) 
                buff.append(" ");
            buff.append("-J");
            buff.append(token);
        }
        project.setProperty(property, buff.toString());
    }

}

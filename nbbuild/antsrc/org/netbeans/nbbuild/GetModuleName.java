/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Michal Zlamal
 */
public class GetModuleName extends Task {
    String name = null;
    File root = null;
    
    public void setName (String name) {
        this.name = name;
    }
    
    /** Root directory of the whole project - ${nb_all} */
    public void setRoot( File root ) {
        this.root = root;
    }
     
    public void execute() throws BuildException {
        if (name == null) 
            throw new BuildException("You must set the property name, where to store the module name", this.getLocation());
        if (root == null)
            throw new BuildException("You must set the root dir", this.getLocation());
        String basedir = this.getProject().getBaseDir().getAbsolutePath();
        String rootdir = root.getAbsolutePath();
        if (!basedir.startsWith(rootdir)) throw new BuildException( "This module in on different path than the root dir",this.getLocation());
        System.out.println("Basedir: " + basedir + " rootdir: " + rootdir);
        String modulename = basedir.substring(rootdir.length() + 1).replace(File.separatorChar,'/');
        this.getProject().setProperty( name, modulename);
    }
    
}

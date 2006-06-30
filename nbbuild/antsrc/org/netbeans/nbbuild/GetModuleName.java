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

package org.netbeans.nbbuild;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;

/**
 * @author Michal Zlamal
 */
public class GetModuleName extends Task {
    String name = null;
    File root = null;

    // XXX this is a lousy attr name; conventional for such attrs to
    // end in 'property' so you realize they refer to a property name
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
        try {
            File dir = this.getProject ().getBaseDir ();
            if (dir.toString ().endsWith (java.io.File.separatorChar + "test")) {
                // when looking for base dir for tests
                dir = dir.getParentFile ();
            }
            String rootdir = root.getCanonicalPath();
            StringBuffer modulename = new StringBuffer ();
            while (dir != null) {
                if (dir.getCanonicalPath ().equals (rootdir)) {
                    break;
                }
                if (modulename.length () > 0) {
                    modulename.insert (0, '/');
                }
                modulename.insert (0, dir.getName ());
                dir = dir.getParentFile ();
            }
            
            //log("Basedir: " + basedir + " rootdir: " + rootdir);
            if (dir == null) throw new BuildException( "This module in on different path than the root dir",this.getLocation());
            this.getProject().setProperty(name, modulename.toString()); // XXX should be setNewProperty, when that is possible
        }
        catch (IOException ex) {
            throw new BuildException("Root dir or module's base dir wasn't recognized", ex, this.getLocation());
        }
    }
    
}

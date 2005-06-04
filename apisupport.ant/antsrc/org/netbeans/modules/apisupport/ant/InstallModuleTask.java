/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.ant;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;

import org.openide.util.Lookup;

import org.netbeans.core.startup.TestModuleDeployer;

// Note that Ant tasks in general are not internationalized.

public class InstallModuleTask extends Task {

    private File module = null;
    private String action = null;

    public static class Action extends EnumeratedAttribute {
        public String[] getValues () {
            return new String[] { /*XXX: "install", "uninstall",*/ "reinstall" }; // NOI18N
        }
    }

    public void setModule (File f) {
        module = f;
    }

    public void setAction (Action a) {
        action = a.getValue ();
    }

    public void execute () throws BuildException {
        if (module == null) throw new BuildException ("Required attribute: module", getLocation()); // NOI18N
        if (action == null) throw new BuildException ("Required attribute: action", getLocation()); // NOI18N
        try {
            if (action.equals ("reinstall")) { // NOI18N
                TestModuleDeployer.deployTestModule(module);
            } else {
                throw new BuildException ("Unsupported action: " + action, getLocation()); // NOI18N
            }
        } catch (IOException ioe) {
            throw new BuildException (ioe, getLocation());
        }
    }

}

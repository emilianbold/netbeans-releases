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

package org.netbeans.nbbuild;

import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** Set a default list of modules for a given configuration.
 * Ugly implementation because of the poor support for initialization
 * from nested elements in Ant.
 * @author Jesse Glick
 */
public class ConfigureModules extends Task {

    private String property, config;
    //private List configs = new LinkedList ();

    public class Config {
	public String name, modules;
	public void setName (String n) {
	    name = n;
	    checkMyself ();
	}
	public void setModules (String m) {
	    modules = m;
	    checkMyself ();
	}
	/*
	public String toString () {
	    return name;
	}
	*/
	private void checkMyself () {
	    if (name != null && modules != null && config.equals (name)) {
		if (project0 ().getProperty (property) == null) {
		    project0 ().setProperty (property, modules);
		} else {
		    log0 ("Note: not overriding property " + property, Project.MSG_VERBOSE);
		}
	    }
	}
    }
    // Javac 1.2 nonsense:
    private void log0 (String m, int l) {
	log (m, l);
    }
    private Project project0 () {
	return project;
    }

    public void setProperty (String p) {
	property = p;
    }
    public void setSelectedconfig (String c) {
	config = c;
    }

    public Config createConfig () {
	Config c = new Config ();
	//configs.add (c);
	return c;
    }

    public void init () throws BuildException {
	if (property == null || config == null)
	    throw new BuildException ("Must set both the property and config attributes", location);
	/*
	Iterator it = configs.iterator ();
	while (it.hasNext ()) {
	    Config c = (Config) it.next ();
	    if (config.equals (c.name)) {
		String value = c.modules;
		if (value == null)
		    throw new BuildException ("Missing modules attribute for config " + config, location);
		if (project.getProperty (property) == null) {
		    project.setProperty (property, value);
		} else {
		    log ("Note: not overriding property " + property, Project.MSG_VERBOSE);
		}
		return;
	    }
	}
	throw new BuildException ("Unrecognized module configuration: " + config + "; pick from: " + configs, location);
	*/
    }

}

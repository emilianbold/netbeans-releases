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

import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/** Set a default list of modules for a given configuration.
 * Ugly implementation because of the poor support for initialization
 * from nested elements in Ant.
 * @author Jesse Glick
 * @deprecated unused
 */
public class ConfigureModules extends Task {

    private String property, config;
    private List configs = new LinkedList ();

    /** You must add a <samp>&lt;config&gt;</samp> nested element for each configuration. */
    public class Config {
	public String name, modules;
        /** Name of the configuration. Used in <samp>-Dmoduleconfig=...</samp> option. */
	public void setName (String n) {
	    name = n;
	    //checkMyself ();
	}
        /** Comma-separated list of modules. */
	public void setModules (String m) {
	    modules = m;
	    //checkMyself ();
	}
	public String toString () {
	    return name;
	}
        /*
	private void checkMyself () {
	    if (name != null && modules != null && config.equals (name)) {
		if (project0 ().getProperty (property) == null) {
		    project0 ().setProperty (property, modules);
		} else {
		    log0 ("Note: not overriding property " + property, Project.MSG_VERBOSE);
		}
	    }
	}
        */
    }
    /*
    // Javac 1.2 nonsense:
    private void log0 (String m, int l) {
	log (m, l);
    }
    private Project project0 () {
	return project;
    }
    */

    /** Name of the property used to hold the resulting comma-separated list of modules. */
    public void setProperty (String p) {
	property = p;
    }
    /** The desired configuration. */
    public void setSelectedconfig (String c) {
	config = c;
    }

    public Config createConfig () {
	Config c = new Config ();
	configs.add (c);
	return c;
    }

    public void execute () throws BuildException {
	if (property == null || config == null)
	    throw new BuildException("Must set both the property and selectedconfig attributes", getLocation());
	Iterator it = configs.iterator ();
	while (it.hasNext ()) {
	    Config c = (Config) it.next ();
	    if (config.equals (c.name)) {
		String value = c.modules;
		if (value == null)
		    throw new BuildException("Missing modules attribute for config " + config, getLocation());
		if (getProject().getProperty(property) == null) {
		    getProject().setProperty(property, value);
		} else {
		    log ("Note: not overriding property " + property, Project.MSG_VERBOSE);
		}
		return;
	    }
	}
	throw new BuildException("Unrecognized module configuration: " + config + "; pick from: " + configs, getLocation());
    }

}

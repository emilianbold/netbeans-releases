/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
@Deprecated
public class ConfigureModules extends Task {

    private String property, config;
    private List<Config> configs = new LinkedList<Config>();

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

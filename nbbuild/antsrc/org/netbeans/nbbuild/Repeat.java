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
package org.netbeans.nbbuild;

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.CallTarget;
import org.apache.tools.ant.taskdefs.Property;

/**
 * For each of specified property values calls target task of a specified name
 * with property set to one of these values
 *
 * @author  Radim Kubacki
 */
public class Repeat extends Task {
    
    private List   values; // List<String>
    private String target;
    private String startdir;
    private String name;
    
    //
    // init
    //
    
    public Repeat() {
        values = new Vector();
        target    = null;
    }

    //
    // itself
    //

    /** Name of property that will be set for each call. */
    public void setName (String s) {
        log ("SET name = " + s, Project.MSG_DEBUG);

        name = s;
    }
    
    /** Comma separated list of values. */
    public void setValues (String s) {
        log ("SET values = " + s, Project.MSG_DEBUG);

        StringTokenizer tok = new StringTokenizer (s, ", ");
        values = new Vector ();
        while ( tok.hasMoreTokens() ) {
            values.add (tok.nextToken().trim());
        }
    }
    
    /** Name of target which will be used with ant task. If not specified,
     * owning target name is used.
     */
    public void setTarget (String s) {
        log ("SET target = " + s, Project.MSG_DEBUG);

        target = s;
    }

    /** Execute this task. */
    public void execute () throws BuildException {        
        if ( values.isEmpty() ) {
            throw new BuildException("You must set at least one value!", getLocation());
        }

        if ( target == null ) {
            throw new BuildException("Target must be set!", getLocation());
        }

        Iterator it = values.iterator();
        while ( it.hasNext() ) {
            String val = (String) it.next();

            log ("Process '" + val + "' location with '" + target + "' target ...", Project.MSG_VERBOSE);
            
            CallTarget antCall = (CallTarget) getProject().createTask("antcall");
            antCall.init();
            antCall.setLocation(getLocation());
            
            // ant.setDir (dir);
            antCall.setTarget (target);
            Property prop = antCall.createParam();
            prop.setName(name);
            prop.setValue(val);
            
            antCall.execute();
        }
    }
    
}

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

import java.io.File;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * For each specified location call ant task with specified target name.
 * If target name is not specified than name of current target is used.
 *
 * @author  Libor Kramolis
 * @version 0.2
 */
public class ForEach extends Task {
    private static final boolean DEBUG = false;
    private static final boolean ECHO  = true;
    
    private List   locations; // List<String>
    private String target;
    
    //
    // init
    //
    
    public ForEach () {
        locations = new Vector();
        target    = null;
    }

    //
    // itself
    //

    /** Comma separated list of locations. */
    public void setLocations (String s) {
        if ( DEBUG ) log ("SET locations = " + s);

        StringTokenizer tok = new StringTokenizer (s, ",");
        locations = new Vector ();
        while ( tok.hasMoreTokens() ) {
            locations.add (tok.nextToken().trim());
        }
    }
    
    /** Name of target which will be used with ant task. If not specified,
     * owning target name is used.
     */
    public void setTarget (String s) {
        if ( DEBUG ) log ("SET target = " + s);

        target = s;
    }
    
    /** Execute this task. */
    public void execute () throws BuildException {        
        if ( locations.isEmpty() ) {
            throw new BuildException ("You must set at least one location!", location);
        }

        if ( target == null ) {
            target = this.getOwningTarget().getName();

            if ( DEBUG ) log ("EXECUTE owningTarget = " + this.getOwningTarget());
        }

        File baseDir = project.getBaseDir();
        Iterator it = locations.iterator();
        while ( it.hasNext() ) {
            String dirName = (String) it.next();

            if ( ECHO ) log ("Process '" + dirName + "' location with '" + target + "' target ...");
            
            Ant ant = (Ant) project.createTask ("ant");
            ant.init();
            ant.setLocation (location);
            
            File dir = new File (baseDir, dirName);
            ant.setDir (dir);
            ant.setTarget (target);
            
            if ( DEBUG ) log ("--> next [ " + target + " ] " + dir.getAbsolutePath());
            
            ant.execute();
        }
    }
    
}

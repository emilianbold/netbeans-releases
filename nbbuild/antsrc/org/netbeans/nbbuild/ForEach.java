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
 */
public class ForEach extends Task {
    private static final boolean DEBUG = false;
    private static final boolean ECHO  = true;

    private List   locations; // List<String>
    private String target;
    private String startdir;
    
    //
    // init
    //
    
    public ForEach () {
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

    /** Where cd first
     */
    public void setStartdir (String s) {
        if ( DEBUG ) log ("SET startdir = " + s);

        startdir = s;
    }
    
    /** Execute this task. */
    public void execute () throws BuildException {        
        if (locations == null) {
            throw new BuildException("You must set at least one location!", getLocation());
        }

        if ( target == null ) {
            target = this.getOwningTarget().getName();

            if ( DEBUG ) log ("EXECUTE owningTarget = " + this.getOwningTarget());
        }
        File baseDir;
	if ( startdir == null ) {
            baseDir = getProject().getBaseDir();
        } else {
            baseDir = new File(getProject().getBaseDir(), startdir);
        }

        Iterator it = locations.iterator();
        while ( it.hasNext() ) {
            String dirName = (String) it.next();

            if ( ECHO ) log ("Process '" + dirName + "' location with '" + target + "' target ...");
            
            Ant ant = (Ant) getProject().createTask("ant");
            ant.init();
            ant.setLocation(getLocation());
            
            File dir = new File (baseDir, dirName);
            ant.setDir (dir);
            ant.setTarget (target);
            
            if ( DEBUG ) log ("--> next [ " + target + " ] " + dir.getAbsolutePath());
            
            ant.execute();
        }
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;
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

    private List<String> locations;
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
        locations = new ArrayList<String>();
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

        for (String dirName : locations) {
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

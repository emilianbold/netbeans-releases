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

package org.netbeans.modules.java.j2seproject;

import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;

/** The util methods for projectui module.
 *
 * @author  Jiri Rechtacek
 */
public class J2SEProjectUtil {
    private J2SEProjectUtil () {}
    
    /** Returns the J2SEProject source' directory.
     *
     * @param p project (assumed J2SEProject)
     * @return source directory or null if not set or a wrong type of project
     */    
    final public static FileObject getProjectSourceDirectory (Project p) {
        if (p instanceof J2SEProject) {
            J2SEProject j2se = (J2SEProject) p;
            return j2se.getSourceDirectory ();
        } else {
            // support only J2SEProject, better throw IAE
            return null;
        }
    }
}

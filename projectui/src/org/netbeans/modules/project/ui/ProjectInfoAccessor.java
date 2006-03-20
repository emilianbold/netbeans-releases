/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.net.URL;
import javax.swing.Icon;

import org.netbeans.modules.project.ui.api.UnloadedProjectInformation;

/**
 * Accessor for UnloadedProjectInformation class
 * @author Milan Kubec
 */
public abstract class ProjectInfoAccessor {
    
    public static ProjectInfoAccessor DEFAULT;
    
    static {
        Class c = UnloadedProjectInformation.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            assert false : cnfe;
        }
        assert DEFAULT != null;
    }
    
    public abstract UnloadedProjectInformation getProjectInfo(String dn, Icon ic, URL ur);
    
}

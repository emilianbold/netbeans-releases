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

package org.netbeans.modules.project.uiapi;

import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Permits {@link ProjectOpenedHook} methods to be called from a different package.
 * @author Jesse Glick
 */
public abstract class ProjectOpenedTrampoline {
    
    /** The trampoline singleton, defined by {@link ProjectOpenedHook}. */
    public static ProjectOpenedTrampoline DEFAULT;
    {
        Class c = ProjectOpenedHook.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** Used by {@link ProjectOpenedHook}. */
    protected ProjectOpenedTrampoline() {}
    
    /** Delegates to {@link ProjectOpenedHook#projectOpened}. */
    public abstract void projectOpened(ProjectOpenedHook hook);
    
    /** Delegates to {@link ProjectOpenedHook#projectClosed}. */
    public abstract void projectClosed(ProjectOpenedHook hook);
    
}

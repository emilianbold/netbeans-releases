/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.channel.filesharing.projecthandler;

import org.netbeans.api.project.Project;


/**
 * SharedProjectFactory
 *
 * @author  ayub.khan@sun.com
 * @version                1.0
 */
public class SharedProjectFactory extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private static SharedProjectFactory instance = null;

    /**
     *
     */
    public SharedProjectFactory() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     *
     * @return SharedProjectFactory
     */
    public static SharedProjectFactory getDefault() {
        if (instance == null) {
            instance = new SharedProjectFactory();
        }

        return instance;
    }

    /**
     *
     * @param projectName
     * @param user
     * @param manager
     */
    public static SharedProject createSharedProject(
        String projectName, String projectOwner, Project originalProject, SharedProjectManager manager
    ) {
        return new SharedProject(projectName, projectOwner, originalProject, manager);
    }
}

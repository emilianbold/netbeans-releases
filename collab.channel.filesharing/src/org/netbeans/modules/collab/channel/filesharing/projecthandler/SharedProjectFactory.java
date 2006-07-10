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

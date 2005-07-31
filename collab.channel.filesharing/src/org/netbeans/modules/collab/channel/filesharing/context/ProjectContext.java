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
package org.netbeans.modules.collab.channel.filesharing.context;

import org.openide.nodes.Node;

import javax.swing.Action;

import org.netbeans.api.project.Project;

import org.netbeans.modules.collab.channel.filesharing.mdc.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;


/**
 * Bean that holds channel context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class ProjectContext extends EventContext {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////	
    private String projectOwner;
    private Action[] nodeActions;

    /**
         *
         * @param eventID
         * @param projectNode
         * @param actions
         * @param isUserSame
         */
    public ProjectContext(String eventID, String projectOwner, String projectName, Action[] nodeActions) {
        super(eventID, projectName);
        this.projectOwner = projectOwner;
        this.nodeActions = nodeActions;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////
    public String getProjectName() {
        return (String) getSource();
    }

    public String getProjectOwner() {
        return this.projectOwner;
    }

    public Action[] getProjectActions() {
        return this.nodeActions;
    }
}

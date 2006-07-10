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

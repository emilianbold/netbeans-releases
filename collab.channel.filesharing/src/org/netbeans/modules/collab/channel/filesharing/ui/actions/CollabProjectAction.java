/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.collab.channel.filesharing.ui.actions;

import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.*;

import java.awt.event.*;

import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.channel.filesharing.FilesharingContext;
import org.netbeans.modules.collab.channel.filesharing.context.ProjectContext;
import org.netbeans.modules.collab.channel.filesharing.event.ProjectPerformActionEvent;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.FilesharingTimerTask;
import org.netbeans.modules.collab.channel.filesharing.eventlistener.ProjectPerformActionTimerTask;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.ui.ProjectsRootNode.ProjectNode;
import org.netbeans.modules.collab.core.Debug;

import org.netbeans.spi.project.ui.LogicalViewProvider;


/**
 * SyncAction
 *
 * @author  Ayub Khan
 * @version 1.0
 */
public class CollabProjectAction //extends SystemAction
implements Action, FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    public static String COMMAND_BUILD = "build";
    public static String COMMAND_INSTALL = "install";
    public static String COMMAND_REBUILD = "rebuild";
    public static String COMMAND_RUN = "run";
    private String name;
    private Action projectAction;
    private Project project;
    private FilesharingContext context;
    private Node projectNode;

    ////////////////////////////////////////////////////////////////////////////
    // Instant variables
    ////////////////////////////////////////////////////////////////////////////
    private HashMap prop = new HashMap();
    private List listeners = new ArrayList();
    private boolean enabled;

    public CollabProjectAction(
        String name, Project project, Action projectAction, FilesharingContext context, Node projectNode
    ) {
        this.name = name;
        this.project = project;
        this.projectAction = projectAction;
        this.context = context;
        this.projectNode = projectNode;
        putValue(Action.NAME, name);
    }

    public String getName() {
        return name;
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    private FilesharingContext getContext() {
        return this.context;
    }

    public String getProjectName() {
        if (this.projectNode != null) {
            return ((ProjectNode) this.projectNode).getName();
        } else {
            return null;
        }
    }

    public String getProjectOwner() {
        if (this.projectNode != null) {
            return ((ProjectNode) this.projectNode).getParentNode().getName();
        } else {
            return null;
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {
        Debug.out.println(name + ", actionPerformed"); //NoI18n

        if ((project != null) && (projectAction != null)) {
            LogicalViewProvider lvp = (LogicalViewProvider) project.getLookup().lookup(LogicalViewProvider.class);
            Node[] nodes = new Node[] { lvp.createLogicalView() };
            Debug.log("ProjectsRootNode", "ProjectsRootNode, PRN createNodes length: " + nodes.length);

            if (nodes[0].getLookup().lookup(Project.class) != project) {
                // Various actions, badging, etc. are not going to work.
                ErrorManager.getDefault().log(
                    ErrorManager.WARNING,
                    "Warning - project1 " + ProjectUtils.getInformation(project).getName() +
                    " failed to supply itself in the lookup of the " + "root node of its own logical view"
                ); // NOI18N
            }

            if (projectAction instanceof ContextAwareAction) {
                projectAction = ((ContextAwareAction) projectAction).createContextAwareInstance(nodes[0].getLookup());
            }

            if (projectAction != null) {
                projectAction.actionPerformed(new ActionEvent(nodes[0], ActionEvent.ACTION_PERFORMED, "")); // NOI18N
            }
        } else //remote action
         {
            sendMessageProjectPerformAction();
        }
    }

    private boolean sendMessageProjectPerformAction() {
        EventContext evContext = new ProjectContext(
                ProjectPerformActionEvent.getEventID(), getProjectOwner(), getProjectName(), new Action[] { this }
            );

        /* send projectActionList message after a delay */
        ProjectPerformActionTimerTask sendProjectPerformActionTimerTask = new ProjectPerformActionTimerTask(
                getContext().getChannelEventNotifier(), new ProjectPerformActionEvent(evContext), getContext()
            );
        getContext().addTimerTask(SEND_PROJECTPERFORMACTION_TIMER_TASK, sendProjectPerformActionTimerTask);
        sendProjectPerformActionTimerTask.schedule(FilesharingTimerTask.PERIOD);

        return true;
    }

    /**
     * Adds a listener
         *
     * @param listener a listener to add
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     * @param listener a listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void putValue(String key, Object value) {
        prop.put(key, value);
    }

    public Object getValue(String key) {
        return prop.get(key);
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }
}

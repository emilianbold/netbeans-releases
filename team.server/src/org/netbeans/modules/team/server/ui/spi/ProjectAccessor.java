/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */

package org.netbeans.modules.team.server.ui.spi;

import java.util.List;
import javax.swing.Action;

/**
 * Main access point to a Team Project.
 * All methods except those returning an Action or ActionListener are allowed
 * to block indefinetely as they will be called outside AWT thread.
 * However the Dashboard UI may declare appropriate service(s) as unreachable
 * after some configurable time out interval.
 *
 * @author S. Aubrecht
 */
public abstract class ProjectAccessor<P> {

    /**
     * Retrieve the list of projects the given user is member of.
     * @param server
     * @param login
     * @param forceRefresh force reload from server
     * @return list of member projects or null, if member projects
     * are not accessible
     */
    public abstract List<ProjectHandle<P>> getMemberProjects(TeamServer server, LoginHandle login, boolean forceRefresh );

    /**
     * Load details for given project.
     * @param server
     * @param projectId Project identification
     * @param forceRefresh force reload from server
     * @return projectHandle or null, if project handle not accessible
     */
    public abstract ProjectHandle<P> getNonMemberProject(TeamServer server, String projectId, boolean forceRefresh);

    /**
     * @param project
     * @return Show details of given project
     */
    public abstract Action getDetailsAction( ProjectHandle<P> project );
    /**
     *
     * @param project
     * @return Action to invokie when user pressed Enter key on the header line
     * for given project.
     */
    //maybe same as 'details'?
    public abstract Action getDefaultAction( ProjectHandle<P> project, boolean opened );
    /**
     *
     * @param project
     * @return Action for project's popup menu, null entries represent menu separator.
     */
    public abstract Action[] getPopupActions( ProjectHandle<P> project, boolean opened );

    public abstract Action getOpenWikiAction( ProjectHandle<P> project );

    public abstract Action getOpenDownloadsAction( ProjectHandle<P> project );

    public abstract boolean canBookmark();
    
    public abstract void bookmark(ProjectHandle<P> project);
}

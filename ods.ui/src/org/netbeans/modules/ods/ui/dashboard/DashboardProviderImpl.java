/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ods.ui.dashboard;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.ods.api.ODSProject;
import org.netbeans.modules.ods.client.api.ODSException;
import org.netbeans.modules.ods.ui.Utilities;
import org.netbeans.modules.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.spi.BuilderAccessor;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.MemberAccessor;
import org.netbeans.modules.team.ui.spi.MemberHandle;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.spi.TeamUIUtils;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class DashboardProviderImpl extends DashboardProvider<CloudUiServer, ODSProject> {

    private final CloudUiServer server;
    private ProjectAccessorImpl projectAccessor;

    public DashboardProviderImpl(CloudUiServer server) {
        this.server = server;
    }
    
    @Override
    public Action createLogoutAction() {
        return new AbstractAction() {  
            @Override
            public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        server.logout();
                    }
                });
            }
        };
    }

    @Override
    public Action createLoginAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TeamUIUtils.showLogin(server, true);
            }
        };
    }

    @Override
    public LeafNode createMemberNode(MemberHandle user, TreeListNode parent) {
        return null;
    }

    @Override
    public TreeListNode createProjectLinksNode(ProjectNode pn, ProjectHandle<ODSProject> project) {
        return new ProjectLinksNode(pn, project, this);
    }

    @Override
    public TreeListNode createMyProjectNode(ProjectHandle<ODSProject> p) {
        return new MyProjectNode(p, server.getDashboard());
    }

    @Override
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln) {
        return new SourceNode(s, sln, this);
    }

    @Override
    public ProjectAccessorImpl getProjectAccessor() {
        if(projectAccessor == null) {
            projectAccessor = new ProjectAccessorImpl(server);
        }
        return projectAccessor;
    }

    @Override
    public MessagingAccessor<ODSProject> getMessagingAccessor() {
        return null;
    }

    @Override
    public MemberAccessor<ODSProject> getMemberAccessor() {
        return null;
    }

    @Override
    public SourceAccessor getSourceAccessor() {
        return getSourceAccessor(ODSProject.class);
    }

    @Override
    public QueryAccessor<ODSProject> getQueryAccessor() {
        return getQueryAccessor(ODSProject.class);
    }
    
    @Override
    public BuilderAccessor<ODSProject> getBuilderAccessor() {
        return getBuildAccessor(ODSProject.class);
    }

    @Override
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<ODSProject> project) {
        return new SourceListNode(pn, this, (LeafNode[]) null);
    }

    @Override
    public Collection<ProjectHandle<ODSProject>> getMyProjects() {
        try {
            return Utilities.getMyProjects(server);
        } catch (ODSException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    public CloudUiServer forProject(ProjectHandle<ODSProject> project) {
//        return server; // should be save as long as there is one dashboardprovider for each dashboard
        return ((ProjectHandleImpl) project).getTeamServer();
    }
    
}

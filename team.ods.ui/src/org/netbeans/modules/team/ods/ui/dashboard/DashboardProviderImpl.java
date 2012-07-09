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
package org.netbeans.modules.team.ods.ui.dashboard;

import com.tasktop.c2c.server.profile.domain.project.Project;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.team.ods.ui.CloudUiServer;
import org.netbeans.modules.team.ods.ui.api.ODSProject;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.spi.BuildAccessor;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.MemberAccessor;
import org.netbeans.modules.team.ui.spi.MemberHandle;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.QueryHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle.ResultType;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.UIUtils;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class DashboardProviderImpl implements DashboardProvider<CloudUiServer, ODSProject> {

    private final CloudUiServer server;
    private ProjectAccessorImpl projectAccessor;
    private SourceAccessorImpl sourceAccessor;

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
                // XXX handle more instances
                TeamServer s = UIUtils.showLogin(server, false);
                if(s != null) {
                    
                }
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
        return new MyProjectNode(p, server.getDashboard(), this);
    }

    @Override
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln) {
        return new SourceNode(s, sln, this);
    }

    @Override
    public ProjectAccessor<CloudUiServer, ODSProject> getProjectAccessor() {
        if(projectAccessor == null) {
            projectAccessor = new ProjectAccessorImpl(server);
        }
        return projectAccessor;
    }

    @Override
    public MessagingAccessor getMessagingAccessor() {
        return null;
    }

    @Override
    public MemberAccessor getMemberAccessor() {
        return null;
    }

    @Override
    public SourceAccessor<ODSProject> getSourceAccessor() {
        if(sourceAccessor == null) {
            sourceAccessor = new SourceAccessorImpl(server);
        }
        return sourceAccessor;
    }

    @Override
    public QueryAccessor<ODSProject> getQueryAccessor() {
        return server.getDashboard().getQueryAccessor(ODSProject.class);
    }
    
    @Override
    public BuildAccessor<ODSProject> getBuildAccessor() {
        return server.getDashboard().getBuildAccessor(ODSProject.class);
    }

    @Override
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<ODSProject> project) {
        return new SourceListNode(pn, this, (LeafNode[]) null);
    }

    @Override
    public CloudUiServer getServer(ProjectHandle<ODSProject> project) {
        return server;
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ui.spi.QueryAccessor.class)
    public static class ODSQueryAccessor extends QueryAccessor<ODSProject> {
        private final QueryHandle allIssues;
        private final QueryHandle myIssues;
        private final QueryHandle someQuery;

        public ODSQueryAccessor() {
            allIssues = new QueryHandle() {
                @Override
                public String getDisplayName() {
                    return "All Issues";
                }
                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {}
                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {}
            };
            myIssues = new QueryHandle() {
                @Override
                public String getDisplayName() {
                    return "My Issues";
                }
                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {}
                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {}
            };
            someQuery = new QueryHandle() {
                @Override
                public String getDisplayName() {
                    return "Some another query";
                }
                @Override
                public void addPropertyChangeListener(PropertyChangeListener l) {}
                @Override
                public void removePropertyChangeListener(PropertyChangeListener l) {}
            };
        }
        
        @Override
        public Class<ODSProject> type() {
            return ODSProject.class;
        }
            
        @Override
        public QueryHandle getAllIssuesQuery(ProjectHandle<ODSProject> project) {
            return allIssues;
        }

        @Override
        public List<QueryHandle> getQueries(ProjectHandle<ODSProject> project) {
            try {
                // XXX emulate network latency
                Thread.currentThread().sleep(3000);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
            return Arrays.asList(allIssues, myIssues, someQuery);
        }

        @Override
        public List<QueryResultHandle> getQueryResults(QueryHandle query) {
            LinkedList<QueryResultHandle> ret = new LinkedList<QueryResultHandle>();
            Random r = new Random(System.currentTimeMillis());
            final int t = r.nextInt(100);
            final int c = r.nextInt(t);
            ret.add(new QueryResultHandle() {
                @Override public String getText() { return "" + c; }
                @Override public String getToolTipText() { return c + " changed tasks"; }
                @Override public ResultType getResultType() { return ResultType.ALL_CHANGES_RESULT; }
            });
            ret.add(new QueryResultHandle() {
                @Override public String getText() { return c + " new or changed"; }
                @Override public String getToolTipText() { return c + " new or changed tasks"; }
                @Override public ResultType getResultType() { return ResultType.NAMED_RESULT; }
            });
            ret.add(new QueryResultHandle() {
                @Override public String getText() { return t + ""; }
                @Override public String getToolTipText() { return t + " total"; }
                @Override public ResultType getResultType() { return ResultType.NAMED_RESULT; }
            });
            return ret;
        }
        
        @Override
        public Action getFindIssueAction(ProjectHandle<ODSProject> project) {
            return NotYetAction.instance;
        }

        @Override
        public Action getCreateIssueAction(ProjectHandle<ODSProject> project) {
            return NotYetAction.instance;
        }

        @Override
        public Action getOpenQueryResultAction(QueryResultHandle result) {
            return NotYetAction.instance;
        }

        @Override
        public Action getDefaultAction(QueryHandle query) {
            return NotYetAction.instance;
        }

    };
}

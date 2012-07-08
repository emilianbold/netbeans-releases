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
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.xml.ws.Holder;
import org.netbeans.modules.team.ods.ui.CloudUiServer;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.spi.DashboardProvider;
import org.netbeans.modules.team.ui.spi.MemberAccessor;
import org.netbeans.modules.team.ui.spi.MemberHandle;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.NbProjectHandle;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.QueryHandle;
import org.netbeans.modules.team.ui.spi.QueryResultHandle;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.UIUtils;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class DashboardProviderImpl implements DashboardProvider<CloudUiServer, Project> {

    private final CloudUiServer server;

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
    public TreeListNode createMessagingNode(ProjectNode pn, ProjectHandle<Project> project) {
        return new MessagingNode(pn, project, this);
    }

    @Override
    public TreeListNode createMyProjectNode(ProjectHandle<Project> p) {
        return new MyProjectNode(p, server.getDashboard(), this);
    }

    @Override
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln) {
        return new SourceNode(s, sln, this);
    }

    @Override
    public ProjectAccessor<CloudUiServer, Project> getProjectAccessor() {
        return new ProjectAccessorImpl(server.getDashboard());
    }

    @Override
    public MessagingAccessor getMessagingAccessor() {
//        if(messagingAccessor == null) {
//            messagingAccessor = new MessagingAccessor<Project>() {
//
//                @Override
//                public MessagingHandle getMessaging(ProjectHandle<Project> project) {
//                    return new MessagingHandle() {
//
//                        @Override
//                        public int getOnlineCount() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public int getMessageCount() {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void addPropertyChangeListener(PropertyChangeListener l) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//
//                        @Override
//                        public void removePropertyChangeListener(PropertyChangeListener l) {
//                            throw new UnsupportedOperationException("Not supported yet.");
//                        }
//                    };
//                }
//
//                @Override
//                public Action getOpenMessagesAction(ProjectHandle<Project> project) {
//                    return null;
//                }
//
//                @Override
//                public Action getCreateChatAction(ProjectHandle<Project> project) {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//
//                @Override
//                public Action getReconnectAction(ProjectHandle<Project> project) {
//                    throw new UnsupportedOperationException("Not supported yet.");
//                }
//                
//            };
//        }
        return null;
    }

    @Override
    public MemberAccessor getMemberAccessor() {
//        return new MemberAccessor() {
//
//            @Override
//            public List getMembers(ProjectHandle project) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public Action getStartChatAction(MemberHandle member) {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//        };
        return null;
    }

    @Override
    public SourceAccessor<Project> getSourceAccessor() {
        return new SourceAccessor<Project>() {

            @Override
            public Action getOpenFavoritesAction(SourceHandle src) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            @Override
            public List<SourceHandle> getSources(final ProjectHandle<Project> project) {
                LinkedList<SourceHandle> ret = new LinkedList<SourceHandle>();
                ret.add(new SourceHandle() {

                    @Override
                    public String getDisplayName() {
                        return project.getDisplayName() + "-s git repository";
                    }

                    @Override
                    public boolean isSupported() {
                        return true;
                    }

                    @Override
                    public String getScmFeatureName() {
                        return "MSG_GIT";
                    }

                    @Override
                    public List<NbProjectHandle> getRecentProjects() {
                        return Collections.emptyList();
                    }

                    @Override
                    public File getWorkingDirectory() {
                        return new File("/tmp");
                    }
                });
                return ret;
            }

            @Override
            public Action getOpenSourcesAction(SourceHandle project) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            @Override
            public Action getDefaultAction(SourceHandle source) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            @Override
            public Action getDefaultAction(NbProjectHandle prj) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }

            @Override
            public Action getOpenOtherAction(SourceHandle src) {
                return new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                };
            }
        };
    }

    @Override
    public QueryAccessor<Project> getQueryAccessor() {
        return server.getDashboard().getQueryAccessor(Project.class);
    }

    @Override
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<Project> project) {
        return new SourceListNode(pn, this, (LeafNode[]) null);
    }

    @Override
    public CloudUiServer getServer(ProjectHandle<Project> project) {
        return server;
    }
    
    @org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.team.ui.spi.QueryAccessor.class)
    public static class ODSQueryAccessor extends QueryAccessor<Project> {

        @Override
        public Class<Project> type() {
            return Project.class;
        }
            
        @Override
        public QueryHandle getAllIssuesQuery(ProjectHandle<Project> project) {
            return null;
        }

        @Override
        public List<QueryHandle> getQueries(ProjectHandle<Project> project) {
            return null;
        }

        @Override
        public List<QueryResultHandle> getQueryResults(QueryHandle query) {
            return null;
        }

        @Override
        public Action getFindIssueAction(ProjectHandle<Project> project) {
            return null;
        }

        @Override
        public Action getCreateIssueAction(ProjectHandle<Project> project) {
            return null;
        }

        @Override
        public Action getOpenQueryResultAction(QueryResultHandle result) {
            return null;
        }

        @Override
        public Action getDefaultAction(QueryHandle query) {
            return null;
        }

    };
}

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
package org.netbeans.modules.kenai.ui.dashboard;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.collab.chat.KenaiConnection;
import org.netbeans.modules.kenai.collab.chat.MessagingAccessorImpl;
import org.netbeans.modules.kenai.ui.MemberAccessorImpl;
import org.netbeans.modules.kenai.ui.OpenNetBeansIDEProjects;
import org.netbeans.modules.kenai.ui.ProjectAccessorImpl;
import org.netbeans.modules.kenai.ui.SourceAccessorImpl;
import org.netbeans.modules.kenai.ui.impl.KenaiServer;
import org.netbeans.modules.team.ui.common.AbstractDashboard;
import org.netbeans.modules.team.ui.common.ProjectNode;
import org.netbeans.modules.team.ui.common.SourceListNode;
import org.netbeans.modules.team.ui.spi.MemberAccessor;
import org.netbeans.modules.team.ui.spi.MemberHandle;
import org.netbeans.modules.team.ui.spi.MessagingAccessor;
import org.netbeans.modules.team.ui.spi.ProjectAccessor;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.QueryAccessor;
import org.netbeans.modules.team.ui.spi.SourceAccessor;
import org.netbeans.modules.team.ui.spi.SourceHandle;
import org.netbeans.modules.team.ui.treelist.LeafNode;
import org.netbeans.modules.team.ui.treelist.TreeListNode;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Stupka
 */
public class DashboardImpl extends AbstractDashboard<KenaiServer, KenaiProject> {

    private PropertyChangeListener kenaiListener;
    
    @Override
    public void setServer(KenaiServer server) {
        super.setServer(server);
        if (server==null) {
            return;
        }
        kenaiListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (Kenai.PROP_XMPP_LOGIN_STARTED.equals(pce.getPropertyName())) {
                    xmppStarted();
                } else if (Kenai.PROP_XMPP_LOGIN.equals(pce.getPropertyName())) {
                    xmppFinsihed();
                } else if (Kenai.PROP_XMPP_LOGIN_FAILED.equals(pce.getPropertyName())) {
                    xmppFinsihed();
                }
            }
        };

        server.addPropertyChangeListener(WeakListeners.propertyChange(kenaiListener, server));

        KenaiConnection.getDefault(server.getKenai()).addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (KenaiConnection.PROP_XMPP_STARTED.equals(evt.getPropertyName())) {
                    xmppStarted();
                } else if (KenaiConnection.PROP_XMPP_FINISHED.equals(evt.getPropertyName())) {
                    xmppFinsihed();
                }
            }
        });
    }

    @Override
    public Action createLogoutAction() {
        return new AbstractAction() {  
            @Override
            public void actionPerformed(ActionEvent e) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        getServer().logout();
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
                org.netbeans.modules.kenai.ui.spi.UIUtils.showLogin(getServer().getKenai());
            }
        };
    }

    @Override
    protected void setSelectedServer(ProjectHandle<KenaiProject> project) {
        org.netbeans.modules.team.ui.spi.UIUtils.setSelectedServer(KenaiServer.forKenai((project.getTeamProject()).getKenai()));
    }
    
    public static DashboardImpl getInstance() {
        return Holder.theInstance;
    }

    @Override
    public LeafNode createMemberNode(MemberHandle user, TreeListNode parent) {
        return new MemberNode(user, parent);
    }

    @Override
    public TreeListNode createMessagingNode(ProjectNode pn, ProjectHandle<KenaiProject> project) {
        return new MessagingNode(pn, project);
    }

    @Override
    public TreeListNode createMyProjectNode(ProjectHandle p) {
        return new MyProjectNode(p);
    }

    @Override
    public TreeListNode createSourceNode(SourceHandle s, SourceListNode sln) {
        return new SourceNode(s, sln);
    }

    @Override
    public ProjectAccessor<KenaiServer, KenaiProject> getProjectAccessor() {
        return ProjectAccessorImpl.getDefault();
    }

    @Override
    public MessagingAccessor<KenaiProject> getMessagingAccessor() {
        return MessagingAccessorImpl.getDefault();
    }

    @Override
    public MemberAccessor getMemberAccessor() {
        return MemberAccessorImpl.getDefault();
    }

    @Override
    public SourceAccessor getSourceAccessor() {
        return SourceAccessorImpl.getDefault();
    }

    @Override
    public QueryAccessor<KenaiProject> getQueryAccessor() {
        return getQueryAccessor(KenaiProject.class);
    }

    @Override
    public TreeListNode createSourceListNode(ProjectNode pn, ProjectHandle<KenaiProject> project) {
        if (getServer().getUrl().toString().equals("https://netbeans.org")) {//NOI18N
            return new SourceListNode(pn, this, new OpenNetBeansIDEProjects(getServer().getKenai(), pn));
        } else {
            return new SourceListNode(pn, this, (LeafNode[]) null);
        }
    }
    
    private static class Holder {
        private static final DashboardImpl theInstance = new DashboardImpl();
    }    
    
}

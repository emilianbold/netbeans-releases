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
package org.netbeans.modules.team.ods.ui.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.netbeans.modules.team.c2c.api.CloudServer;
import org.netbeans.modules.team.c2c.client.api.ClientFactory;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.c2c.client.api.CloudException;
import org.netbeans.modules.team.ods.ui.dashboard.DashboardProviderImpl;
import org.netbeans.modules.team.ui.common.DefaultDashboard;
import org.netbeans.modules.team.ui.spi.LoginPanelSupport;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.netbeans.modules.team.ui.spi.TeamServer;
import org.netbeans.modules.team.ui.spi.TeamServerProvider;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import com.tasktop.c2c.server.profile.domain.project.Project;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.team.ods.ui.CloudServerProviderImpl;
import org.netbeans.modules.team.ods.ui.LoginPanelSupportImpl;
import org.netbeans.modules.team.ods.ui.dashboard.ProjectHandleImpl;

/**
 *
 * @author Ondrej Vrabec
 */
public class CloudUiServer implements TeamServer {

    private static final Map<CloudServer, CloudUiServer> serverMap = new WeakHashMap<CloudServer, CloudUiServer>(3);
    private final WeakReference<CloudServer> impl;
    private PropertyChangeListener l;
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);
    private final DefaultDashboard<CloudUiServer, Project> dashboard;
    private CloudClient client;

    private CloudUiServer (CloudServer server) {
        this.impl = new WeakReference<CloudServer>(server);
        dashboard = new DefaultDashboard<CloudUiServer, Project>(this, new DashboardProviderImpl(this));
        server.addPropertyChangeListener(WeakListeners.propertyChange(l=new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                String propName = pce.getPropertyName();
                if (propName.equals(CloudServer.PROP_LOGIN)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(CloudServer.PROP_LOGIN_STARTED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_STARTED, pce.getOldValue(), pce.getNewValue());
                } else if (propName.equals(CloudServer.PROP_LOGIN_FAILED)) {
                    propertyChangeSupport.firePropertyChange(TeamServer.PROP_LOGIN_FAILED, pce.getOldValue(), pce.getNewValue());
                }
            }
        }, server));
    }

    public static CloudUiServer forServer (CloudServer server) {
        CloudUiServer serverUi;
        synchronized (serverMap) {
            serverUi = serverMap.get(server);
            if (serverUi == null) {
                serverUi = new CloudUiServer(server);
                serverMap.put(server, serverUi);
            }
        }
        return serverUi;
    }
    
    public DefaultDashboard getDashboard() {
        return dashboard;
    }    

    @Override
    public URL getUrl () {
        return getImpl(true).getUrl();
    }

    @Override
    public Status getStatus () {
        return getImpl(true).isLoggedIn() ? Status.ONLINE : Status.OFFLINE;
    }

    @Override
    public void logout () {
        getImpl(true).logout();
        client = null;
    }

    @Override
    public String getDisplayName () {
        return getImpl(true).getDisplayName();
    }

    @Override
    public Icon getIcon () {
        return getImpl(true).getIcon();
    }

    @Override
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        getImpl(true).addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        getImpl(true).removePropertyChangeListener(listener);
    }

    @Override
    public TeamServerProvider getProvider () {
        return CloudServerProviderImpl.getDefault();
    }

    @Override
    public JComponent getDashboardComponent () {
        return dashboard.getComponent();
    }

    @Override
    public LoginPanelSupport createLoginSupport () {
        return new LoginPanelSupportImpl(getImpl(true));
    }

    public CloudServer getServer () {
        return getImpl(false);
    }

    private CloudServer getImpl (boolean checkOriginal) {
        CloudServer server = impl.get();
        if (checkOriginal && server == null) {
            throw new IllegalStateException("Original cloud server no longer exists.");
        }
        return server;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return getServer().getPasswordAuthentication();
    }

    // XXX no need to have this implemented in the TeamServer
    public Collection<ProjectHandle<CloudUiServer, Project>> getMyProjects() {
        CloudClient client = getClient();
        List<Project> projects;
        try {
            projects = client.getMyProjects();
        } catch (CloudException ex) {
            Exceptions.printStackTrace(ex); // XXX
            return Collections.emptyList();
        }
        if(projects == null) {
            return Collections.emptyList();
        }
        Collection<ProjectHandle<CloudUiServer, Project>> ret = new ArrayList<ProjectHandle<CloudUiServer, Project>>(projects.size());
        for (Project project : projects) {
            ret.add(new ProjectHandleImpl(this, project));
        }
        return ret;
    }

    public CloudClient getClient() {
        assert getImpl(true).isLoggedIn();
        if(client == null) {
            client = ClientFactory.getInstance().createClient(getUrl().toString(), getPasswordAuthentication());
        }
        return client;
    }
    
}

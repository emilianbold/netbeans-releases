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
package org.netbeans.modules.ods.hudson;

import com.tasktop.c2c.server.cloud.domain.ServiceType;
import com.tasktop.c2c.server.profile.domain.project.Project;
import com.tasktop.c2c.server.profile.domain.project.ProjectService;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.UI;
import org.netbeans.modules.team.c2c.client.api.CloudClient;
import org.netbeans.modules.team.ods.ui.api.CloudUiServer;
import org.netbeans.modules.team.ui.spi.BuildAccessor;
import org.netbeans.modules.team.ui.spi.BuildHandle;
import org.netbeans.modules.team.ui.spi.BuildHandle.Status;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.awt.HtmlBrowser;
import org.openide.util.lookup.ServiceProvider;

/**
 * Build Accessor that uses Hudson Builders.
 *
 * @author jhavlin
 */
@ServiceProvider(service = BuildAccessor.class)
public class HudsonBuildAccessor extends BuildAccessor<Project> {

    @Override
    public boolean isEnabled(ProjectHandle<Project> projectHandle) {
        Project project = projectHandle.getTeamProject();
        List<ProjectService> services = project.getProjectServicesOfType(
                ServiceType.BUILD); // XXX BUILD_SLAVE
        if (services == null || services.isEmpty()) {
            return false;
        }
        return services.iterator().next().isAvailable(); // XXX what if more then 1 returned?
    }

    @Override
    public List<BuildHandle> getBuilds(ProjectHandle<Project> projectHandle) {
//        CloudClient client = projectHandle.getTeamServer().getClient();

        HudsonInstance hudsonServer = HudsonManager.addInstance(
                "Kenai", "http://bugtracking-test.cz.oracle.com:8180/", 1, false); //TODO

        if (hudsonServer != null) {
            Collection<HudsonJob> jobs = hudsonServer.getJobs();
            List<BuildHandle> buildHandles = new LinkedList<BuildHandle>();
            for (HudsonJob job : jobs) {
                buildHandles.add(new HudsonBuildHandle(hudsonServer,
                        job.getName(), job));
            }
            return buildHandles;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Action getNewBuildAction(ProjectHandle<Project> projectHandle) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    HtmlBrowser.URLDisplayer.getDefault().showURL(
                            new URL("http://bugtracking-test.cz.oracle.com:8180/")); //TODO
                } catch (MalformedURLException ex) {
                    Logger.getLogger(HudsonBuildAccessor.class.getName()).log(
                            Level.SEVERE, null, ex);
                }
            }
        };
    }

    @Override
    public Class<Project> type() {
        return Project.class;
    }

    private static class HudsonBuildHandle extends BuildHandle {

        private HudsonInstance hudsonInstance;
        private String jobName;
        private HudsonJob hudsonJob;
        private HudsonChangeAdapter hudsonChangeListener;
        private PropertyChangeSupport propertyChangeSupport;
        private final Object listenerLock = new Object();
        private Status currentStatus = null;

        public HudsonBuildHandle(HudsonInstance hudsonInstance,
                String jobName, HudsonJob initialJob) {

            this.hudsonInstance = hudsonInstance;
            this.jobName = jobName;
            this.hudsonJob = initialJob;
            this.propertyChangeSupport = new PropertyChangeSupport(this);
            this.hudsonChangeListener = new HudsonStatusChangeListener();
        }

        @Override
        public String getDisplayName() {
            return hudsonJob.getDisplayName();
        }

        private void updateJob() {
            Collection<HudsonJob> jobs = hudsonInstance.getJobs();
            for (HudsonJob job : jobs) {
                if (jobName.equals(job.getName())) {
                    hudsonJob = job;
                }
            }
        }

        private Status readStatus() {
            int lastBuildNumber = hudsonJob.getLastBuild();
            for (HudsonJobBuild build : hudsonJob.getBuilds()) {
                if (build.getNumber() == lastBuildNumber) {
                    if (build.isBuilding()) {
                        return Status.RUNNING;
                    } else {
                        switch (build.getResult()) {
                            case ABORTED:
                                return Status.UNKNOWN;
                            case FAILURE:
                                return Status.FAILED;
                            case NOT_BUILT:
                                return Status.UNKNOWN;
                            case SUCCESS:
                                return Status.STABLE;
                            case UNSTABLE:
                                return Status.UNSTABLE;
                            default:
                                return Status.UNKNOWN;
                        }
                    }
                }
            }
            return Status.UNKNOWN;
        }

        @Override
        public Status getStatus() {
            if (currentStatus == null) {
                currentStatus = readStatus();
            }
            return currentStatus;
        }

        @Override
        public Action getDefaultAction() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    UI.selectNode(hudsonInstance.getUrl(),
                            hudsonJob.getName());
                }
            };
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            synchronized (listenerLock) {
                if (propertyChangeSupport.getPropertyChangeListeners().length
                        == 0) {
                    hudsonInstance.addHudsonChangeListener(
                            hudsonChangeListener);
                }
                propertyChangeSupport.addPropertyChangeListener(l);
            }
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            synchronized (listenerLock) {
                propertyChangeSupport.removePropertyChangeListener(l);
                if (propertyChangeSupport.getPropertyChangeListeners().length
                        == 0) {
                    hudsonInstance.removeHudsonChangeListener(
                            hudsonChangeListener);
                }
            }
        }

        private class HudsonStatusChangeListener extends HudsonChangeAdapter {

            @Override
            public void stateChanged() {
                Status oldStatus = currentStatus;
                updateJob();
                Status newStatus = readStatus();
                if (oldStatus != newStatus) {
                    currentStatus = newStatus;
                    propertyChangeSupport.firePropertyChange(
                            PROP_STATUS, oldStatus, newStatus);
                }
            }

            @Override
            public void contentChanged() {
                stateChanged();
            }
        }
    }
}

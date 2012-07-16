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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonChangeAdapter;
import org.netbeans.modules.hudson.api.HudsonChangeListener;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.UI;
import org.netbeans.modules.team.c2c.api.ODSProject;
import org.netbeans.modules.team.ui.spi.BuildAccessor;
import org.netbeans.modules.team.ui.spi.BuildHandle;
import org.netbeans.modules.team.ui.spi.BuildHandle.Status;
import org.netbeans.modules.team.ui.spi.ProjectHandle;
import org.openide.awt.HtmlBrowser;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Build Accessor that uses Hudson Builders.
 *
 * @author jhavlin
 */
@ServiceProvider(service = BuildAccessor.class)
public class ODSBuildAccessor extends BuildAccessor<ODSProject> {

    private static final Logger LOG = Logger.getLogger(
            ODSBuildAccessor.class.getName());

    @Override
    public boolean isEnabled(ProjectHandle<ODSProject> projectHandle) {
        projectHandle.getTeamProject().getServer().addPropertyChangeListener(null);
        return projectHandle.getTeamProject().hasBuild(); 
    }

    @Override
    public List<BuildHandle> getBuilds(ProjectHandle<ODSProject> projectHandle) {
        ODSBuilderConnector odsBuilderConnector = new ODSBuilderConnector(projectHandle);
        HudsonInstance hi = HudsonManager.addInstance(
                projectHandle.getDisplayName(),
                projectHandle.getTeamProject().getServer().getUrl()
                + "/" + projectHandle.getId() + "/", //NOI18N
                1, odsBuilderConnector);
        if (hi == null) {
            return Collections.emptyList();
        }
        List<BuildHandle> buildHandles = new LinkedList<BuildHandle>();
        Collection<HudsonJob> jobs = waitForJobs(hi);
        for (HudsonJob job : jobs) {
            buildHandles.add(new HudsonBuildHandle(hi, job.getName(), job));
        }
        return buildHandles;
    }

    @Override
    public Action getNewBuildAction(ProjectHandle<ODSProject> projectHandle) {
        // TODO Use project URL rather than server URL>
        final URL url = projectHandle.getTeamProject().getServer().getUrl();
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HtmlBrowser.URLDisplayer.getDefault().showURL(url);
            }
        };
    }

    @Override
    public Class<ODSProject> type() {
        return ODSProject.class;
    }

    /**
     * If the HudsonInstance.getJobs is called right after initialization of the
     * hudson instance, jobs can be uninitialized. This method ensures that jobs
     * are loaded.
     */
    private Collection<HudsonJob> waitForJobs(HudsonInstance hi) {
        LOG.log(Level.INFO, "waiting for jobs in {0}", Thread.currentThread().getName());
        Collection<HudsonJob> jobs = hi.getJobs();
        if (jobs == null || jobs.isEmpty()) {
            final Semaphore semaphore = new Semaphore(0);
            HudsonChangeListener listener = new HudsonChangeAdapter() {
                @Override
                public void contentChanged() {
                    semaphore.release();
                }
            };
            hi.addHudsonChangeListener(listener);
            try {
                semaphore.tryAcquire(30, TimeUnit.SECONDS); // loop, prodluzovat interval, 3sec az 5min, koef 3
            } catch (InterruptedException ex) {
                LOG.log(Level.FINE, null, ex);
            }
            hi.removeHudsonChangeListener(listener);
            jobs = hi.getJobs();
        }
        return jobs;
    }

    private static class HudsonBuildHandle extends BuildHandle {

        private HudsonInstance hudsonInstance;
        private String jobName;
        private HudsonJob hudsonJob;
        private HudsonChangeAdapter hudsonChangeListener;
        private PropertyChangeSupport propertyChangeSupport;
        private final Object listenerLock = new Object();
        private volatile Status currentStatus = null;
        private volatile boolean updateStatusScheduled = false;

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

        private void scheduleUpdateStatus() {
            if (!updateStatusScheduled) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus();
                    }
                });
                updateStatusScheduled = true;
            }
        }

        private void updateStatus() {
            assert !EventQueue.isDispatchThread();
            Status oldStatus = currentStatus;
            Status newStatus = readStatus();
            updateStatusScheduled = false;
            if (oldStatus != newStatus) {
                currentStatus = newStatus;
                propertyChangeSupport.firePropertyChange(
                        PROP_STATUS, oldStatus, newStatus);
            }
        }

        private Status readStatus() {
            assert !EventQueue.isDispatchThread();
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
                scheduleUpdateStatus();
                return Status.UNKNOWN;
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
                updateJob();
                scheduleUpdateStatus();
            }

            @Override
            public void contentChanged() {
                stateChanged();
            }
        }
    }
}

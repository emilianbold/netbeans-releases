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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
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
import org.netbeans.modules.team.ods.api.CloudServer;
import org.netbeans.modules.team.ods.api.ODSProject;
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

    private static RequestProcessor rp = new RequestProcessor(
            "ODS Build Services", 10);                                 // NOI18N
    
    private final static Map<BuildsListener, Object> CACHE =
            Collections.synchronizedMap(
            new WeakHashMap<BuildsListener, Object>());

    @Override
    public boolean isEnabled(ProjectHandle<ODSProject> projectHandle) {
        return projectHandle.getTeamProject().hasBuild();
    }

    @Override
    public List<BuildHandle> getBuilds(ProjectHandle<ODSProject> projectHandle) {
        ODSPasswordAuthorizer.ProjectHandleRegistry.registerProjectHandle(
                projectHandle);
        HudsonInstance hi = HudsonManager.addInstance(
                projectHandle.getDisplayName(),
                projectHandle.getTeamProject().getBuildUrl(),
                1, false);
        if (hi == null) {
            return Collections.emptyList();
        }
        List<BuildHandle> cachedHandles = findBuildHandlesInCache(
                projectHandle);
        if (cachedHandles != null) {
            return cachedHandles;
        }
        List<HudsonBuildHandle> buildHandles =
                new LinkedList<HudsonBuildHandle>();
        BuildsListener bl = BuildsListener.create(hi, projectHandle);
        Collection<HudsonJob> jobs = waitForJobs(hi);
        for (HudsonJob job : jobs) {
            buildHandles.add(new HudsonBuildHandle(hi, job.getName(), job));
        }
        bl.setBuildHandles(buildHandles);
        CACHE.put(bl, new Object());
        return new LinkedList<BuildHandle>(buildHandles);
    }

    /**
     * Check cache of listeners at first. Listeners already contain all required
     * information.
     */
    private List<BuildHandle> findBuildHandlesInCache(
            ProjectHandle<ODSProject> projectHandle) {

        for (final BuildsListener listener : CACHE.keySet()) {
            if (listener.projectHandle.get() == projectHandle
                    && projectHandle.getTeamProject().getBuildUrl().equals(
                    listener.instance.getUrl())) {
                synchronized (listener.buildHandles) {
                    listener.checkJobList(); //update job list
                    return new LinkedList<BuildHandle>(listener.buildHandles);
                }
            }
        }
        return null;
    }

    @Override
    public Action getNewBuildAction(ProjectHandle<ODSProject> projectHandle) {
        final String urlString = projectHandle.getTeamProject().getBuildUrl();
        final URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException ex) {
            LOG.log(Level.INFO, null, ex);
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };
        }
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
                if (hi.getJobs() == null || hi.getJobs().isEmpty()) {
                    // Try again, jobs could have been initilized before adding
                    // the listener. If it is still uninitialized, wait for the
                    // listener.
                    semaphore.tryAcquire(5, TimeUnit.MINUTES);
                }
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
                rp.post(new Runnable() {
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

        public HudsonJob getJob() {
            return hudsonJob;
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

        public void cleanup() {
            hudsonInstance.removeHudsonChangeListener(hudsonChangeListener);
        }
    }

    /**
     * Listener that listens to various ProjectHandle-related events. It is
     * notified when the team project is closed or user logs out from the team
     * server. In this case the listener unregisters itself from objects that
     * hold references to it.
     *
     * It is also notified when a content of the Hudson build instance is
     * changed. Then it checks whether the list of jobs has changed, and if so,
     * fires appropriate event on the project handle.
     */
    private static class BuildsListener implements PropertyChangeListener,
            HudsonChangeListener {

        private HudsonInstance instance;
        private Reference<ProjectHandle<ODSProject>> projectHandle;
        private Reference<CloudServer> server;
        private final List<HudsonBuildHandle> buildHandles;

        public BuildsListener(HudsonInstance instance,
                ProjectHandle<ODSProject> projectHandle) {
            this.instance = instance;
            this.projectHandle = new WeakReference<ProjectHandle<ODSProject>>(
                    projectHandle);
            this.server = new WeakReference<CloudServer>(
                    projectHandle.getTeamProject().getServer());
            this.buildHandles = new LinkedList<HudsonBuildHandle>();
        }

        /**
         * Set list of build handles, after it was initialized in accessor
         * method {@link #getBuilds(ProjectHandle)}, and start handling Hudson
         * events.
         *
         * This listener is currently registered in project handle and team
         * server (in order to be able to remove Hudson instance on project
         * close or server logout), but is is not registered in Hudson - in
         * order not to fire changes after initial getting of jobs.
         */
        public void setBuildHandles(List<HudsonBuildHandle> buildHandles) {
            this.buildHandles.addAll(buildHandles);
            instance.addHudsonChangeListener(this);
        }

        /**
         * Property change in project handle or team server. Triggers actions if
         * the team project is closed or the user logged-out from the team
         * server.
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (isTeamClosedEvent(evt)) {
                removeHudsonAndClean();
            } else if (isUserLoggedOutEvent(evt)) {
                removeHudsonAndClean();
                CACHE.clear();
            } else if (projectHandle.get() == null) {
                cleanup();
            }
        }

        private void removeHudsonAndClean() {
            cleanup();
            if (!instance.isPersisted()) {
                HudsonManager.removeInstance(instance);
            }
        }

        private boolean isTeamClosedEvent(PropertyChangeEvent evt) {
            ProjectHandle<ODSProject> ph = projectHandle.get();
            if (ph == null) {
                return false;
            } else {
                return evt.getPropertyName().equals(ProjectHandle.PROP_CLOSE)
                        && evt.getSource() == ph;
            }
        }

        private boolean isUserLoggedOutEvent(PropertyChangeEvent evt) {
            CloudServer srv = server.get();
            if (srv == null) {
                return false;
            } else {
                return evt.getPropertyName().equals(CloudServer.PROP_LOGIN)
                        && evt.getNewValue() == null
                        && evt.getSource() == srv;
            }
        }

        private void cleanup() {
            ProjectHandle<ODSProject> ph = projectHandle.get();
            if (ph != null) {
                ph.removePropertyChangeListener(this);
            }
            CloudServer srv = server.get();
            if (srv != null) {
                srv.removePropertyChangeListener(this);
            }
            instance.removeHudsonChangeListener(this);
            synchronized (this) {
                for (HudsonBuildHandle handle: buildHandles) {
                    handle.cleanup();
                }
                buildHandles.clear();
            }
            projectHandle.clear();
            CACHE.remove(this);
        }

        /**
         * Called when status of Hudson instance has changed. Not important for
         * this listener.
         */
        @Override
        public void stateChanged() {
            if (projectHandle.get() == null) {
                cleanup();
            }
        }

        /**
         * Called when list of jobs in the hudson instance has changed.
         */
        @Override
        public void contentChanged() {
            ProjectHandle<ODSProject> ph = projectHandle.get();
            if (ph != null) {
                PairOfDifferentLists lists = checkJobList();
                if (lists != null) {
                    ph.firePropertyChange(
                            ProjectHandle.PROP_BUILD_LIST,
                            lists.getOriginal(),
                            lists.getUpdated());
                }
            } else {
                cleanup();
            }
        }

        /**
         * Check whether the job of list has changed. If so, update
         * {@link #buildHandles} list and return pair of original and update
         * list. If the list has not been changed, return null.
         */
        private synchronized PairOfDifferentLists checkJobList() {
            Collection<HudsonJob> jobs = instance.getJobs();
            List<HudsonJob> added = new LinkedList<HudsonJob>(); // new jobs
            boolean allFound = true; // jobs for all hanles have been found
            for (HudsonJob job : jobs) {
                boolean found = false;
                for (HudsonBuildHandle handle : buildHandles) {
                    if (handle.getJob().getUrl().equals(job.getUrl())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    added.add(job);
                    allFound = false;
                }
            }
            if (jobs.size() == buildHandles.size() && allFound) {
                return null; // no change
            } else {
                List<BuildHandle> origList =
                        new LinkedList<BuildHandle>(buildHandles);
                removeOrphanedHandles(jobs);
                addHandlesForAddedJobs(added);
                return new PairOfDifferentLists(origList,
                        new LinkedList<BuildHandle>(buildHandles));
            }
        }

        /**
         * Remove build handles that have no corresponding jobs on the hudson
         * server.
         */
        private void removeOrphanedHandles(Collection<HudsonJob> jobs) {
            for (HudsonBuildHandle handle : buildHandles) {
                boolean found = false;
                for (HudsonJob job : jobs) {
                    if (job.getUrl().equals(handle.getJob().getUrl())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    handle.cleanup();
                    buildHandles.remove(handle);
                }
            }
        }

        /**
         * Create handles for newly found Hudson jobs and add them to the
         * internal list of build handles.
         */
        private void addHandlesForAddedJobs(List<HudsonJob> added) {
            for (HudsonJob job : added) {
                buildHandles.add(new HudsonBuildHandle(instance,
                        job.getName(), job));
            }
        }

        /**
         * Create a new listener and register it to all relevant objects.
         *
         * Remember to call {@link #setBuildHandles(java.util.List)} to start
         * listening to Hudson events after the initial list of build handles is
         * initialized.
         */
        public static BuildsListener create(HudsonInstance hudsonInstance,
                ProjectHandle<ODSProject> projectHandle) {

            BuildsListener buildsListener = new BuildsListener(
                    hudsonInstance, projectHandle);
            projectHandle.addPropertyChangeListener(buildsListener);
            projectHandle.getTeamProject().getServer().
                    addPropertyChangeListener(buildsListener);
            return buildsListener;
        }

        /**
         * Pair of original and updated list of build handles.
         */
        private static class PairOfDifferentLists {

            List<BuildHandle> original;
            List<BuildHandle> updated;

            public PairOfDifferentLists(List<BuildHandle> original,
                    List<BuildHandle> updated) {
                this.original = original;
                this.updated = updated;
            }

            public List<BuildHandle> getOriginal() {
                return original;
            }

            public List<BuildHandle> getUpdated() {
                return updated;
            }
        }
    }
}

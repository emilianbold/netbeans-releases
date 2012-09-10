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
package org.netbeans.modules.hudson.spi;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJob.Color;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonJobBuild.Result;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.netbeans.modules.hudson.api.HudsonVersion;

/**
 * Interface of Hudson connectors. It specifies how data should be accessed and
 * retrieved from Hudson-like builders.
 *
 * @since 1.22
 *
 * @author jhavlin
 */
public abstract class BuilderConnector {

    /**
     * Get data for jobs and views of the builder instance.
     *
     * @param authentication To prompt for login if the anonymous user cannot
     * even see the job list; set to true for explicit user gesture, false
     * otherwise.
     */
    public abstract @NonNull InstanceData getInstanceData(
            boolean authentication);

    /**
     * Get builds for the specified job.
     */
    public abstract @NonNull Collection<BuildData> getJobBuildsData(
            @NonNull HudsonJob job);

    /**
     * Load actual result of a build. This method does not return a value, but
     * updates two last arguments.
     *
     * @param build Build to get result for.
     * @param building Atomic boolean that should be set to true if the build is
     * still being built, or to false otherwise.
     * @param result Atomic reference that should be set to value of the actual
     * built result.
     */
    public abstract void getJobBuildResult(@NonNull HudsonJobBuild build,
             @NonNull AtomicBoolean building,
             @NonNull AtomicReference<Result> result);

    /**
     * Get builds artifacts for a build.
     *
     * @return Filesystem containing the build artifacts, or null if it is not
     * available.
     */
    public abstract @CheckForNull RemoteFileSystem getArtifacts(
            @NonNull HudsonJobBuild build);

    /**
     * Get builds artifacts for a maven module build.
     *
     * @return Filesystem containing the build artifacts, or null if it is not
     * available.
     */
    public abstract @CheckForNull RemoteFileSystem getArtifacts(
            @NonNull HudsonMavenModuleBuild build);

    /**
     * Get child nodes for job workspace.
     *
     * @return Filesystem representing job workspace, or null if it is not
     * available.
     */
    public abstract @CheckForNull RemoteFileSystem getWorkspace(
            @NonNull HudsonJob job);

    /**
     * Check whether this connector is connected to the build server.
     */
    public abstract boolean isConnected();

    /**
     * Check if the connection to the build server is forbidden.
     */
    public abstract boolean isForbidden();

    /**
     * Get version of the Huson builder.
     */
    public abstract @NonNull HudsonVersion getHudsonVersion(
            boolean authentication);

    /**
     * Start the specified job.
     */
    public abstract void startJob(@NonNull HudsonJob job);

    /**
     * Get displayer for build console output.
     *
     * @return Console displayer, or null if it is not available for this
     * builder.
     */
    public abstract @CheckForNull ConsoleDisplayer getConsoleDisplayer();

    /**
     * Get displayer for failed tests of builds.
     *
     * @return Failure displayer, or null if it is not available for this
     * builder.
     */
    public abstract @CheckForNull FailureDisplayer getFailureDisplayer();

    /**
     * Get a collection of changes that have triggered the build.
     */
    public abstract Collection<? extends HudsonJobChangeItem> getJobBuildChanges(HudsonJobBuild build);

    /**
     * A displayer that can show console output of builds to the user. They can
     * be shown in output window, external browser, etc.
     */
    public static abstract class ConsoleDisplayer {

        /**
         * Show build console of a hudson job build.
         */
        public abstract void showConsole(@NonNull HudsonJobBuild build);

        /**
         * Show build console of a hudson maven module build.
         */
        public abstract void showConsole(
                @NonNull HudsonMavenModuleBuild modBuild);
    }

    /**
     * A displayer that can visualize failures of builds. They can be displayer
     * in test results window, output window, external browser, etc.
     */
    public static abstract class FailureDisplayer {

        /**
         * Show failures of a hudson job build.
         */
        public abstract void showFailures(@NonNull HudsonJobBuild build);

        /**
         * Show failures of a hudson maven module build.
         */
        public abstract void showFailures(
                @NonNull HudsonMavenModuleBuild moduleBuild);
    }

    /**
     * Type for storing builder job data.
     */
    public static final class JobData {

        private String jobName;
        private String jobUrl;
        private boolean secured;
        private HudsonJob.Color color;
        private String displayName;
        private boolean buildable;
        private boolean inQueue;
        private int lastBuild;
        private int lastFailedBuild;
        private int lastStableBuild;
        private int lastSuccessfulBuild;
        private int lastCompletedBuild;
        private List<ModuleData> modules = new LinkedList<ModuleData>();
        private List<String> views = new LinkedList<String>();

        public String getJobName() {
            return jobName;
        }

        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        public String getJobUrl() {
            return jobUrl;
        }

        public void setJobUrl(String jobUrl) {
            this.jobUrl = jobUrl;
        }

        public boolean isSecured() {
            return secured;
        }

        public void setSecured(boolean secured) {
            this.secured = secured;
        }

        public Color getColor() {
            return color;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public boolean isBuildable() {
            return buildable;
        }

        public void setBuildable(boolean buildable) {
            this.buildable = buildable;
        }

        public boolean isInQueue() {
            return inQueue;
        }

        public void setInQueue(boolean inQueue) {
            this.inQueue = inQueue;
        }

        public int getLastBuild() {
            return lastBuild;
        }

        public void setLastBuild(int lastBuild) {
            this.lastBuild = lastBuild;
        }

        public int getLastFailedBuild() {
            return lastFailedBuild;
        }

        public void setLastFailedBuild(int lastFailedBuild) {
            this.lastFailedBuild = lastFailedBuild;
        }

        public int getLastStableBuild() {
            return lastStableBuild;
        }

        public void setLastStableBuild(int lastStableBuild) {
            this.lastStableBuild = lastStableBuild;
        }

        public int getLastSuccessfulBuild() {
            return lastSuccessfulBuild;
        }

        public void setLastSuccessfulBuild(int lastSuccessfulBuild) {
            this.lastSuccessfulBuild = lastSuccessfulBuild;
        }

        public int getLastCompletedBuild() {
            return lastCompletedBuild;
        }

        public void setLastCompletedBuild(int lastCompletedBuild) {
            this.lastCompletedBuild = lastCompletedBuild;
        }

        public Collection<ModuleData> getModules() {
            return modules;
        }

        public void addModule(String name, String displayName, Color color,
                String url) {
            this.modules.add(new ModuleData(name, displayName, color, url));
        }

        public Collection<String> getViews() {
            return views;
        }

        public void addView(String viewName) {
            if (viewName != null && !viewName.trim().isEmpty()) {
                views.add(viewName);
            }
        }
    }

    /**
     * Type for storing build module data
     */
    public static final class ModuleData {

        private String name;
        private String displayName;
        private HudsonJob.Color color;
        private String url;

        public ModuleData(String name, String displayName, Color color,
                String url) {
            this.name = name;
            this.displayName = displayName;
            this.color = color;
            this.url = url;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public Color getColor() {
            return color;
        }

        public String getUrl() {
            return url;
        }
    }

    /**
     * Type for accessing job build data.
     */
    public static final class BuildData {

        private int number;
        private HudsonJobBuild.Result result;
        private boolean building;

        public BuildData(int number, Result result, boolean building) {
            this.number = number;
            this.result = result;
            this.building = building;
        }

        public int getNumber() {
            return number;
        }

        public Result getResult() {
            return result;
        }

        public boolean isBuilding() {
            return building;
        }
    }

    public static final class ViewData {

        private String name;
        private String url;
        private boolean primary;

        public ViewData(String name, String url, boolean primary) {
            this.name = name;
            this.url = url;
            this.primary = primary;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public boolean isPrimary() {
            return primary;
        }
    }

    public static final class InstanceData {

        private Collection<JobData> jobsData;
        private Collection<ViewData> viewsData;

        public InstanceData(Collection<JobData> jobsData, Collection<ViewData> viewsData) {
            this.jobsData = jobsData;
            this.viewsData = viewsData;
        }

        public Collection<JobData> getJobsData() {
            return jobsData;
        }

        public Collection<ViewData> getViewsData() {
            return viewsData;
        }
    }
}

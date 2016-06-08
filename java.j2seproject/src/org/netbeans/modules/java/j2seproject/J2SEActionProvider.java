/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.java.j2seproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.BuildArtifactMapper;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.BaseActionProvider;
import org.netbeans.modules.java.api.common.project.BaseActionProvider.Callback3;
import org.netbeans.modules.java.api.common.project.ProjectConfigurations;
import org.netbeans.modules.java.j2seproject.api.J2SEBuildPropertiesProvider;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.SingleMethod;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/** Action provider of the J2SE project. This is the place where to do
 * strange things to J2SE actions. E.g. compile-single.
 */
public class J2SEActionProvider extends BaseActionProvider {

    private static final Logger LOG = Logger.getLogger(J2SEActionProvider.class.getName());

    // Commands available from J2SE project
    private static final String[] supportedActions = {
        COMMAND_BUILD,
        COMMAND_CLEAN,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
    };


    private static final String[] platformSensitiveActions = {
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_COMPILE_SINGLE,
        COMMAND_RUN,
        COMMAND_RUN_SINGLE,
        COMMAND_DEBUG,
        COMMAND_DEBUG_SINGLE,
        COMMAND_PROFILE,
        COMMAND_PROFILE_SINGLE,
        JavaProjectConstants.COMMAND_JAVADOC,
        COMMAND_TEST,
        COMMAND_TEST_SINGLE,
        COMMAND_DEBUG_TEST_SINGLE,
        COMMAND_PROFILE_TEST_SINGLE,
        SingleMethod.COMMAND_RUN_SINGLE_METHOD,
        SingleMethod.COMMAND_DEBUG_SINGLE_METHOD,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
        COMMAND_DEBUG_STEP_INTO,
    };

    private static final String[] actionsDisabledForQuickRun = {
        COMMAND_COMPILE_SINGLE,
        JavaProjectConstants.COMMAND_DEBUG_FIX,
    };

    //Post compile on save actions
    private final CosAction cosAction;

    /** Map from commands to ant targets */
    private Map<String,String[]> commands;

    /**Set of commands which are affected by background scanning*/
    private Set<String> bkgScanSensitiveActions;

    /**Set of commands which need java model up to date*/
    private Set<String> needJavaModelActions;

    public J2SEActionProvider(J2SEProject project, UpdateHelper updateHelper) {
        super(
            project,
            updateHelper,
            project.evaluator(),
            project.getSourceRoots(),
            project.getTestSourceRoots(),
            project.getAntProjectHelper(),
            new CallbackImpl(project));
        commands = new HashMap<String,String[]>();
        // treated specially: COMMAND_{,RE}BUILD
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(COMMAND_COMPILE_SINGLE, new String[] {"compile-single"}); // NOI18N
        commands.put(COMMAND_RUN, new String[] {"run"}); // NOI18N
        commands.put(COMMAND_RUN_SINGLE, new String[] {"run-single"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        commands.put(COMMAND_DEBUG_SINGLE, new String[] {"debug-single"}); // NOI18N
        commands.put(COMMAND_PROFILE, new String[] {"profile"}); // NOI18N
        commands.put(COMMAND_PROFILE_SINGLE, new String[] {"profile-single"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_JAVADOC, new String[] {"javadoc"}); // NOI18N
        commands.put(COMMAND_TEST, new String[] {"test"}); // NOI18N
        commands.put(COMMAND_TEST_SINGLE, new String[] {"test-single"}); // NOI18N
        commands.put(COMMAND_DEBUG_TEST_SINGLE, new String[] {"debug-test"}); // NOI18N
        commands.put(COMMAND_PROFILE_TEST_SINGLE, new String[]{"profile-test"}); // NOI18N
        commands.put(JavaProjectConstants.COMMAND_DEBUG_FIX, new String[] {"debug-fix"}); // NOI18N
        commands.put(COMMAND_DEBUG_STEP_INTO, new String[] {"debug-stepinto"}); // NOI18N

        this.bkgScanSensitiveActions = new HashSet<String>(Arrays.asList(
            COMMAND_RUN,
            COMMAND_RUN_SINGLE,
            COMMAND_DEBUG,
            COMMAND_DEBUG_SINGLE,
            COMMAND_DEBUG_STEP_INTO
        ));

        this.needJavaModelActions = new HashSet<String>(Arrays.asList(
            JavaProjectConstants.COMMAND_DEBUG_FIX
        ));
        this.cosAction = new CosAction(
                this,
                project.evaluator(),
                project.getSourceRoots(),
                project.getTestSourceRoots());
    }

    @Override
    protected String[] getPlatformSensitiveActions() {
        return platformSensitiveActions;
    }

    @Override
    protected String[] getActionsDisabledForQuickRun() {
        return actionsDisabledForQuickRun;
    }

    @Override
    public Map<String, String[]> getCommands() {
        return commands;
    }

    @Override
    protected Set<String> getScanSensitiveActions() {
        return bkgScanSensitiveActions;
    }

    @Override
    protected Set<String> getJavaModelActions() {
        return needJavaModelActions;
    }

    @Override
    protected boolean isCompileOnSaveEnabled() {
        return J2SEProjectUtil.isCompileOnSaveEnabled((J2SEProject)getProject());
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public String[] getTargetNames(String command, Lookup context, Properties p, boolean doJavaChecks) throws IllegalArgumentException {
        String names[] = super.getTargetNames(command, context, p, doJavaChecks);
        ProjectConfigurations.Configuration c = context.lookup(ProjectConfigurations.Configuration.class);
        if (c != null) {
            String config;
            if (!c.isDefault()) {
                config = c.getName();
            } else {
                // Invalid but overrides any valid setting in config.properties.
                config = "";
            }
            p.setProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG, config);
        }
        return names;
    }

    @ProjectServiceProvider(
            service=ActionProvider.class,
            projectTypes={@LookupProvider.Registration.ProjectType(id="org-netbeans-modules-java-j2seproject",position=100)})
    public static J2SEActionProvider create(@NonNull final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        final J2SEProject project = lkp.lookup(J2SEProject.class);
        final J2SEActionProvider j2seActionProvider = new J2SEActionProvider(project, project.getUpdateHelper());
        j2seActionProvider.startFSListener();
        return j2seActionProvider;
    }

    private static final class CallbackImpl implements Callback3 {

        private final J2SEProject prj;

        CallbackImpl(@NonNull final J2SEProject project) {
            Parameters.notNull("project", project); //NOI18N
            this.prj = project;
        }

        @Override
        @NonNull
        public Map<String, String> createAdditionalProperties(@NonNull String command, @NonNull Lookup context) {
            final Map<String,String> result = new HashMap<>();
            for (J2SEBuildPropertiesProvider bpp : prj.getLookup().lookupAll(J2SEBuildPropertiesProvider.class)) {
                final Map<String,String> contrib = bpp.createAdditionalProperties(command, context);
                assert contrib != null;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "J2SEBuildPropertiesProvider: {0} added following build properties: {1}",   //NOI18N
                        new Object[]{
                            bpp.getClass(),
                            contrib
                        });
                }
                result.putAll(contrib);
            }
            return Collections.unmodifiableMap(result);
        }

        @Override
        public Set<String> createConcealedProperties(String command, Lookup context) {
            final Set<String> result = new HashSet<>();
            for (J2SEBuildPropertiesProvider bpp : prj.getLookup().lookupAll(J2SEBuildPropertiesProvider.class)) {
                final Set<String> contrib = bpp.createConcealedProperties(command, context);
                assert contrib != null;
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(
                        Level.FINE,
                        "J2SEBuildPropertiesProvider: {0} added following concealed properties: {1}",   //NOI18N
                        new Object[]{
                            bpp.getClass(),
                            contrib
                        });
                }
                result.addAll(contrib);
            }
            return Collections.unmodifiableSet(result);
        }

        @Override
        public void antTargetInvocationStarted(@NonNull String command, @NonNull Lookup context) {
        }

        @Override
        public void antTargetInvocationFinished(@NonNull String command, @NonNull Lookup context, int result) {
        }

        @Override
        public void antTargetInvocationFailed(@NonNull String command, @NonNull Lookup context) {
        }

        @CheckForNull
        @Override
        public ClassPath getProjectSourcesClassPath(@NonNull String type) {
            return prj.getClassPathProvider().getProjectSourcesClassPath(type);
        }

        @CheckForNull
        @Override
        public ClassPath findClassPath(@NonNull FileObject file, @NonNull String type) {
            return prj.getClassPathProvider().findClassPath(file, type);
        }

    }

    private static final class CosAction implements BuildArtifactMapper.ArtifactsUpdated,
            PropertyChangeListener {
        private static final String COS_UPDATED = "$cos.update";    //NOI18N
        private static final Object NONE = new Object();
        private final J2SEActionProvider owner;
        private final PropertyEvaluator eval;
        private final SourceRoots src;
        private final SourceRoots tests;
        private final BuildArtifactMapper mapper;
        private final Map</*@GuardedBy("this")*/URL,BuildArtifactMapper.ArtifactsUpdated> currentListeners;
        private volatile Object targetCache;

        private CosAction(
                @NonNull final J2SEActionProvider owner,
                @NonNull final PropertyEvaluator eval,
                @NonNull final SourceRoots src,
                @NonNull final SourceRoots tests) {
            this.owner = owner;
            this.eval = eval;
            this.src = src;
            this.tests = tests;
            this.mapper = new BuildArtifactMapper();
            this.currentListeners = new HashMap<>();
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            this.src.addPropertyChangeListener(WeakListeners.propertyChange(this, this.src));
            this.tests.addPropertyChangeListener(WeakListeners.propertyChange(this, this.tests));
            updateRootsListeners();
        }

        @Override
        public void artifactsUpdated(@NonNull final Iterable<File> artifacts) {
            final String target = getTarget();
            if (target != null) {
                final FileObject buildXml = owner.findBuildXml();
                if (buildXml != null) {
                    try {
                        ActionUtils.runTarget(
                                buildXml,
                                new String[] {target},
                                null,
                                null);
                    } catch (IOException ioe) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot execute pos compile on save target: {0} in: {1}",   //NOI18N
                                new Object[]{
                                    target,
                                    FileUtil.getFileDisplayName(buildXml)
                                });
                    }
                }
            }
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String name = evt.getPropertyName();
            if (name == null || COS_UPDATED.equals(name)) {
                targetCache = null;
            } else if (SourceRoots.PROP_ROOTS.equals(name)) {
                updateRootsListeners();
            }
        }

        private void updateRootsListeners() {
            final Set<URL> newRoots = new HashSet<>();
            Collections.addAll(newRoots, this.src.getRootURLs());
            Collections.addAll(newRoots, this.tests.getRootURLs());
            synchronized (this) {
                final Set<URL> toRemove = new HashSet<>(currentListeners.keySet());
                toRemove.removeAll(newRoots);
                newRoots.removeAll(currentListeners.keySet());
                for (URL u : toRemove) {
                    final BuildArtifactMapper.ArtifactsUpdated l = currentListeners.remove(u);
                    mapper.removeArtifactsUpdatedListener(u, l);
                }
                for (URL u : newRoots) {
                    final BuildArtifactMapper.ArtifactsUpdated l = new WeakArtifactUpdated(this, mapper, u);
                    currentListeners.put(u, l);
                    mapper.addArtifactsUpdatedListener(u, l);
                }
            }
        }

        @CheckForNull
        private String getTarget() {
            Object target = targetCache;
            if (target == null) {
                final String val = eval.getProperty(COS_UPDATED);
                target = targetCache = val != null && !val.isEmpty() ?
                        val :
                        NONE;
            }
            if (target == NONE) {
                return null;
            }
            return owner.isCompileOnSaveEnabled() ?
                    (String) target :
                    null;
        }

        private static final class WeakArtifactUpdated extends WeakReference<BuildArtifactMapper.ArtifactsUpdated>
                implements BuildArtifactMapper.ArtifactsUpdated, Runnable {

            private final BuildArtifactMapper source;
            private final URL url;

            WeakArtifactUpdated(
                    @NonNull final BuildArtifactMapper.ArtifactsUpdated delegate,
                    @NonNull final BuildArtifactMapper source,
                    @NonNull final URL url) {
                super(delegate);
                Parameters.notNull("source", source);   //NOI18N
                Parameters.notNull("url", url); //NOI18N
                this.source = source;
                this.url = url;
            }

            @Override
            public void artifactsUpdated(
                    @NonNull final Iterable<File> artifacts) {
                final BuildArtifactMapper.ArtifactsUpdated delegate = get();
                if (delegate != null) {
                    delegate.artifactsUpdated(artifacts);
                }
            }

            @Override
            public void run() {
                source.removeArtifactsUpdatedListener(url, this);
            }
        }
    }
}

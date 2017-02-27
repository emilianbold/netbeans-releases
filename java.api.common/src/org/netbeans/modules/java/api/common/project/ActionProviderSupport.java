/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.java.api.common.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import static org.netbeans.modules.java.api.common.project.Bundle.*;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.Context;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.CompileOnSaveOperation;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.ScriptAction.Result;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.lookup.Lookups;

/**
 * Support methods for {@link ActionProvider} implementation.
 * @author Tomas Zezula
 */
final class ActionProviderSupport {
    private static final Set<String> NO_SYNC_COMMANDS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList(new String[]{
                COMMAND_BUILD,
                COMMAND_CLEAN,
                COMMAND_REBUILD,
                COMMAND_COMPILE_SINGLE,
                JavaProjectConstants.COMMAND_JAVADOC
            })));

    private ActionProviderSupport() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    static enum ActionFlag {
        JAVA_MODEL_SENSITIVE,
        SCAN_SENITIVE
    }

    static enum UserPropertiesPolicy {
        RUN_ANYWAY(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_Anyway")),
        RUN_WITH(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_With")),
        RUN_UPDATE(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_Update"));

        private final String displayName;

        private UserPropertiesPolicy(@NonNull final String displayName) {
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    @NbBundle.Messages({
        "ACTION_run=Run Project",
        "ACTION_run.single=Run File",
        "ACTION_run.single.method=Run File",
        "ACTION_debug=Debug Project",
        "ACTION_debug.single=Debug File",
        "ACTION_debug.single.method=Debug File",
        "ACTION_debug.stepinto=Debug Project",
        "ACTION_debug.fix=Apply Code Changes",
        "ACTION_debug.test.single=Debug Test",
        "ACTION_profile=Profile Project",
        "ACTION_profile.single=Profile File",
        "ACTION_profile.test.single=Profile Test",
        "ACTION_rebuild=Rebuild Project",
        "ACTION_build=Build Project",
        "ACTION_clean=Clean Project",
        "ACTION_compile.single=Compile File",
        "ACTION_javadoc=Generate JavaDoc",
        "ACTION_test=Test Project",
        "ACTION_test.single=Test File"
    })
    static String getCommandDisplayName(String command) throws MissingResourceException {
        if (command.equals("run")) {
            return ACTION_run();
        } else if (command.equals("run.single")) {
            return ACTION_run_single();
        } else if (command.equals("run.single.method")) {
            return ACTION_run_single_method();
        } else if (command.equals("debug")) {
            return ACTION_debug();
        } else if (command.equals("debug.single")) {
            return ACTION_debug_single();
        } else if (command.equals("debug.single.method")) {
            return ACTION_debug_single_method();
        } else if (command.equals("debug.stepinto")) {
            return ACTION_debug_stepinto();
        } else if (command.equals("debug.fix")) {
            return ACTION_debug_fix();
        } else if (command.equals("debug.test.single")) {
            return ACTION_debug_test_single();
        } else if (command.equals("profile")) {
            return ACTION_profile();
        } else if (command.equals("profile.single")) {
            return ACTION_profile_single();
        } else if (command.equals("profile.test.single")) {
            return ACTION_profile_test_single();
        } else if (command.equals("rebuild")) {
            return ACTION_rebuild();
        } else if (command.equals("build")) {
            return ACTION_build();
        } else if (command.equals("clean")) {
            return ACTION_clean();
        } else if (command.equals("compile.single")) {
            return ACTION_compile_single();
        } else if (command.equals("javadoc")) {
            return ACTION_javadoc();
        } else if (command.equals("test")) {
            return ACTION_test();
        } else if (command.equals("test.single")) {
            return ACTION_test_single();
        } else {
            return command;
        }
    }

    static void invokeTarget(
            @NonNull final Function<Context,String[]> targetProvider,
            @NonNull final BiFunction<Context,String[],Result> cosPerformer,
            @NonNull final Context ctx,
            @NonNull final Set<? extends ActionFlag> flags,
            @NonNull final String actionDisplayName) {
        final String userPropertiesFile = verifyUserPropertiesFile(ctx);
        final JavaModelWork action = new JavaModelWork(targetProvider, cosPerformer, ctx, userPropertiesFile);
        if (flags.contains(ActionFlag.JAVA_MODEL_SENSITIVE) ||
                (ctx.getCompileOnSaveOperations().contains(CompileOnSaveOperation.UPDATE) && flags.contains(ActionFlag.SCAN_SENITIVE))) {
            //Always have to run with java model
            ScanDialog.runWhenScanFinished(action, actionDisplayName);
        } else if (flags.contains(ActionFlag.SCAN_SENITIVE)) {
            //Run without model if not yet ready
            try {
                action.needsJavaModel = false;
                invokeByJavaSource(action);
                if (!action.isCalled()) {
                    action.doJavaChecks = false;
                    action.run();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            //Does not need java model
            action.run();
        }
    }

    @CheckForNull
    static FileObject getBuildScript(@NonNull final Context context) {
        final String path =  CommonProjectUtils.getBuildXmlName(context.getPropertyEvaluator(), null);
        return context.getProject().getProjectDirectory().getFileObject(path);
    }

    static boolean allowAntBuild(
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final UpdateHelper updateHelper) {
        String buildClasses = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClasses == null) return false;
        File buildClassesFile = updateHelper.getAntProjectHelper().resolveFile(buildClasses);
        return !new File(buildClassesFile, BaseActionProvider.AUTOMATIC_BUILD_TAG).exists();
    }

    @CheckForNull
    static JavaPlatform getActivePlatform(
            @NonNull final Project prj,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String activePlatformProperty) {
        JavaPlatform plat = CommonProjectUtils.getActivePlatform(eval.getProperty(activePlatformProperty));
        if (plat == null) {
            plat = ProjectPlatform.forProject(prj, eval, CommonProjectUtils.J2SE_PLATFORM_TYPE);
        }
        return plat;
    }


    static @NonNull final Supplier<? extends String[]> createConditionalTarget(
            @NonNull final PropertyEvaluator eval,
            @NonNull final Predicate<PropertyEvaluator> predicate,
            @NonNull final String[] ifTargets,
            @NonNull final String[] elseTargets) {
        return () -> {
            return predicate.test(eval) ?
                    ifTargets:
                    elseTargets;
        };
    }

    @NonNull
    static Predicate<PropertyEvaluator> createJarEnabledPredicate() {
        return (evaluator) -> !"false".equalsIgnoreCase(evaluator.getProperty(ProjectProperties.DO_JAR));    //NOI18N
    }

    private static void invokeByJavaSource (
            @NonNull final Runnable runnable) throws IOException {
        Parameters.notNull("runnable", runnable);   //NOI18N
        final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]));
        final JavaSource js = JavaSource.create(info);
        js.runWhenScanFinished((final CompilationController controller) -> {
            runnable.run();
        }, true);
    }

    @CheckForNull
    private static String verifyUserPropertiesFile(
            @NonNull final Context ctx) {
        final String currentPath = ctx.getPropertyEvaluator().getProperty("user.properties.file");      //NOI18N
        final File current = currentPath == null ? null : FileUtil.normalizeFile(ctx.getUpdateHelper().getAntProjectHelper().resolveFile(currentPath));
        final File expected = FileUtil.normalizeFile(new File(System.getProperty("netbeans.user"), "build.properties")); // NOI18N
        if (!expected.equals(current)) {
            if (ctx.getUserPropertiesPolicy() == null) {
                final Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        NbBundle.getMessage(ActionProviderSupport.class, "MSG_InvalidBuildPropertiesPath", ProjectUtils.getInformation(ctx.getProject()).getDisplayName()),
                        NbBundle.getMessage(ActionProviderSupport.class, "TITLE_InvalidBuildPropertiesPath"),
                        0,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        UserPropertiesPolicy.values(),
                        UserPropertiesPolicy.RUN_ANYWAY));
                ctx.setUserPropertiesPolicy(option instanceof UserPropertiesPolicy ?
                        (UserPropertiesPolicy) option :
                        null);
            }
            if (null != ctx.getUserPropertiesPolicy()) {
                switch (ctx.getUserPropertiesPolicy()) {
                    case RUN_ANYWAY:
                        return null;
                    case RUN_WITH:
                        return expected.getAbsolutePath();
                    case RUN_UPDATE:
                        ProjectManager.mutex().writeAccess(() -> {
                            final EditableProperties ep = ctx.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            ep.setProperty("user.properties.file", expected.getAbsolutePath()); //NOI18N
                            ctx.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(ctx.getProject());
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        });
                        return null;
                    default:
                }
            }
        }
        return null;
    }

    private static void collectStartupExtenderArgs(@NonNull final Context ctx) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(ctx)) {
            b.append(' ').append(arg);
        }
        if (b.length() > 0) {
            ctx.setProperty(ProjectProperties.RUN_JVM_ARGS_IDE, b.toString());
        }
    }

    private static List<String> runJvmargsIde(@NonNull final Context ctx) {
        StartupExtender.StartMode mode;
        switch (ctx.getCommand()) {
            case COMMAND_RUN:
            case COMMAND_RUN_SINGLE:
                mode = StartupExtender.StartMode.NORMAL;
                break;
            case COMMAND_DEBUG:
            case COMMAND_DEBUG_SINGLE:
            case COMMAND_DEBUG_STEP_INTO:
                mode = StartupExtender.StartMode.DEBUG;
                break;
            case COMMAND_PROFILE:
            case COMMAND_PROFILE_SINGLE:
                mode = StartupExtender.StartMode.PROFILE;
                break;
            case COMMAND_TEST:
            case COMMAND_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_NORMAL;
                break;
            case COMMAND_DEBUG_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_DEBUG;
                break;
            case COMMAND_PROFILE_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_PROFILE;
                break;
            default:
                return Collections.emptyList();
        }
        final List<String> args = new ArrayList<>();
        final JavaPlatform p = ctx.getActiveJavaPlatform();
        for (StartupExtender group : StartupExtender.getExtenders(Lookups.fixed(
                ctx.getProject(),
                p != null ? p : JavaPlatformManager.getDefault().getDefaultPlatform()),
                mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }

    private static final class JavaModelWork implements Runnable {
        private final Function<Context,String[]> targetProvider;
        private final BiFunction<Context,String[],Result> cosPerformer;
        private final Context context;
        private final String userPropertiesFile;
        private final AtomicReference<Thread> caller;
        private final AtomicBoolean called;
        private final ActionProgress listener;
        /**
         * True when the action always requires access to java model
         */
        boolean needsJavaModel = true;
        /**
         * When true getTargetNames accesses java model, when false
         * the default values (possibly incorrect) are used.
         */
        boolean doJavaChecks = true;

        JavaModelWork(
                @NonNull final Function<Context,String[]> targetProvider,
                @NonNull final BiFunction<Context,String[],Result> cosPerformer,
                @NonNull final Context context,
                @NullAllowed final String userPropertiesFile) {
            this.targetProvider = targetProvider;
            this.cosPerformer = cosPerformer;
            this.context = context;
            this.userPropertiesFile = userPropertiesFile;
            this.caller = new AtomicReference<>(Thread.currentThread());
            this.called  = new AtomicBoolean(false);
            // XXX prefer to call just if and when actually starting target, but that is hard to calculate here
            this.listener = ActionProgress.start(this.context.getActiveLookup());
        }

        boolean isCalled() {
            return called.get();
        }

        @Override
        public void run() {
            if (!needsJavaModel && caller.get() != Thread.currentThread()) {
                return;
            }
            called.set(true);
            ExecutorTask task = null;
            try {
                task = execute();
            } finally {
                if (task != null) {
                    task.addTaskListener((t) -> {
                        listener.finished(((ExecutorTask)t).result() == 0);
                    });
                } else {
                    listener.finished(false);
                }
            }
        }

        private ExecutorTask execute() {
            context.setProperty("nb.internal.action.name", context.getCommand());                  //NOI18N
            if (userPropertiesFile != null) {
                context.setProperty("user.properties.file", userPropertiesFile);   //NOI18N
            }
            context.setJavaChecks(doJavaChecks);
            String[] targetNames = targetProvider.apply(context);
            if (targetNames == null) {
                return null;
            }
            if (context.getCompileOnSaveOperations().contains(CompileOnSaveOperation.EXECUTE)) {
                final Result r = cosPerformer.apply(context, targetNames);
                if (r == Result.abort()) {
                    return null;
                } else if (r != Result.follow()) {
                    final ExecutorTask t = r.getTask();
                    assert t != null;
                    return t;
                }
            }
            collectStartupExtenderArgs(context);
            if (targetNames.length == 0) {
                targetNames = null;
            }
            if (context.getCompileOnSaveOperations().contains(CompileOnSaveOperation.UPDATE) && !NO_SYNC_COMMANDS.contains(context.getCommand())) {
                context.setProperty("nb.wait.for.caches", "true");  //NOI18N
            }
            try {
                final FileObject buildFo = getBuildScript(context);
                if (buildFo == null || !buildFo.isValid()) {
                    //The build.xml was deleted after the isActionEnabled was called
                    NotifyDescriptor nd = new NotifyDescriptor.Message(LBL_No_Build_XML_Found(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    context.fireAntTargetInvocationListener(0, 0);
                    try {
                        final ExecutorTask task = ActionUtils.runTarget(buildFo, targetNames, context.getProperties(), context.getConcealedProperties());
                        task.addTaskListener((Task t) -> {
                            context.fireAntTargetInvocationListener(1, task.result());
                        });
                        return task;
                    } catch (IOException | RuntimeException ex) {
                        context.fireAntTargetInvocationListener(2, 0);
                        throw ex;
                    }
                }
            } catch (IOException e) {
                    Exceptions.printStackTrace(e);
            }
            return null;
        }
    }
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.KillableProcess;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.server.GrailsInstance;
import org.netbeans.modules.groovy.grails.server.GrailsInstanceProvider;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Class providing the access to basic Grails runtime routines.
 * The class may not be configured and the method {@link #isConfigured()} can
 * be used to find out the state.
 *
 * @author Petr Hejl
 */
// TODO instance should be always configured in future
// TODO more appropriate would be getDefault and forProject
public final class GrailsRuntime {

    private static final Logger LOGGER = Logger.getLogger(GrailsRuntime.class.getName());

    private static final Set<String> GUARDED_COMMANDS = new HashSet<String>();

    static {
        GrailsInstance.Accessor.DEFAULT = new GrailsInstance.Accessor() {

            @Override
            public String getVersion(GrailsRuntime runtime) {
                return runtime.getVersion();
            }
        };

        Collections.addAll(GUARDED_COMMANDS, "run-app", "shell"); //NOI18N
    }

    private static GrailsRuntime instance;

    private boolean initialized;

    private String version;

    private GrailsRuntime() {
        super();
    }

    /**
     * Return the instance representing the IDE configured Grails runtime.
     *
     * @return the instance representing the IDE configured Grails runtime
     */
    public static synchronized GrailsRuntime getInstance() {
        if (instance == null) {
            instance = new GrailsRuntime();
            GrailsSettings.getInstance().addPropertyChangeListener(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    if (GrailsSettings.GRAILS_BASE_PROPERTY.equals(evt.getPropertyName())) {
                        instance.reload();
                        GrailsInstanceProvider.getInstance().runtimeChanged();
                    }
                }
            });
            instance.reload();
        }
        return instance;
    }

    /**
     * Creates the callable spawning the command (process) described
     * by the command descriptor. Usually you don't need to use this method
     * directly as most of use cases can be solved with {@link ExecutionSupport}.
     *
     * @param descriptor descriptor of the command and its environment
     * @return the callable spawning the command (process)
     * @throws IllegalStateException if the runtime is not configured
     *
     * @see #isConfigured()
     * @see ExecutionSupport
     */
    public Callable<Process> createCommand(CommandDescriptor descriptor) {
        Parameters.notNull("descriptor", descriptor);

        if (!isConfigured()) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }
        return new GrailsCallable(descriptor);
    }

    /**
     * Returns <code>true</code> if the runtime is configured (usable).
     *
     * @return <code>true</code> if the runtime is configured (usable)
     */
    public boolean isConfigured() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null) {
            return false;
        }

        return RuntimeHelper.isValidRuntime(new File(grailsBase));
    }

    /**
     * Returns the grails home of the configured runtime.
     *
     * @return the grails home
     * @throws IllegalStateException if the runtime is not configured
     */
    // TODO this should be removed with CP abstraction
    public File getGrailsHome() {
        String grailsBase = GrailsSettings.getInstance().getGrailsBase();
        if (grailsBase == null || !RuntimeHelper.isValidRuntime(new File(grailsBase))) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }

        return new File(grailsBase);
    }

    // TODO not public API unless it is really needed
    private String getVersion() {
        synchronized (this) {
            if (initialized) {
                return version;
            }

            String grailsBase = GrailsSettings.getInstance().getGrailsBase();
            if (grailsBase != null) {
                version = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
            }
            initialized = true;

            return version;
        }
    }

    /**
     * Reloads the runtime instance variables.
     */
    private void reload() {
        synchronized (this) {
            initialized = false;
        }

        // figure out the version on background
        // default executor as general purpose should be enough for this
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                synchronized (GrailsRuntime.this) {
                    if (initialized) {
                        return;
                    }

                    String grailsBase = GrailsSettings.getInstance().getGrailsBase();
                    if (grailsBase != null) {
                        version = RuntimeHelper.getRuntimeVersion(new File(grailsBase));
                    }
                    initialized = true;
                }
            }
        });
    }

    private static String createJvmArguments(Properties properties) {
        StringBuilder builder = new StringBuilder();
        int i = 0;

        for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
            String key = e.nextElement().toString();
            String value = properties.getProperty(key);
            if (value != null) {
                if (i > 0) {
                    builder.append(" "); // NOI18N
                }
                builder.append("-D").append(key); // NOI18N
                builder.append("="); // NOI18N
                builder.append(value);
                i++;
            }
        }
        return builder.toString();
    }

    private static String createCommandArguments(String[] arguments) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) {
                builder.append(" "); // NOI18N
            }
            builder.append(arguments[i]);
        }
        return builder.toString();
    }

    private static void checkForServer(CommandDescriptor descriptor, Process process) {
        if ("run-app".equals(descriptor.getName()) // NOI18N
                || "run-app-https".equals(descriptor.getName()) // NOI18N
                || "run-war".equals(descriptor.getName())) { // NOI18N
            Project project = FileOwnerQuery.getOwner(
                    FileUtil.toFileObject(descriptor.getDirectory()));
            if (project != null) {
                GrailsInstanceProvider.getInstance().serverStarted(project, process);
            }
        }
    }

    /**
     * Class describing the command to invoke and its environment.
     *
     * This class is <i>Immutable</i>.
     */
    public static final class CommandDescriptor {

        private final String name;

        private final File directory;

        private final GrailsEnvironment environment;

        private final String[] arguments;

        private final Properties props;

        /**
         * Creates the minimal descriptor.
         *
         * @param name command name
         * @param directory working directory
         * @param env grails environment
         */
        public CommandDescriptor(String name, File directory, GrailsEnvironment env) {
            this(name, directory, env, new String[] {}, new Properties());
        }

        /**
         * Creates the descriptor including arguments.
         *
         * @param name command name
         * @param directory working directory
         * @param env grails environment
         * @param arguments command arguments
         */
        public CommandDescriptor(String name, File directory, GrailsEnvironment env, String[] arguments) {
            this(name, directory, env, arguments, new Properties());
        }

        /**
         * Creates the full customizable command descriptor.
         *
         * @param name command name
         * @param directory working directory
         * @param env grails environment
         * @param arguments command arguments
         * @param props environment properties
         */
        public CommandDescriptor(String name, File directory, GrailsEnvironment env, String[] arguments, Properties props) {
            this.name = name;
            this.directory = directory;
            this.environment = env;
            this.arguments = arguments.clone();
            this.props = new Properties(props);
        }

        /**
         * Returns the command name.
         *
         * @return the command name
         */
        public String getName() {
            return name;
        }

        /**
         * Returns the working directory.
         *
         * @return the working directory
         */
        public File getDirectory() {
            return directory;
        }

        /**
         * Returns the grails environment.
         *
         * @return the grails environment
         */
        public GrailsEnvironment getEnvironment() {
            return environment;
        }

        /**
         * Returns the command arguments.
         *
         * @return the command arguments
         */
        public String[] getArguments() {
            return arguments.clone();
        }

        /**
         * Returns the environment properties.
         *
         * @return the environment properties
         */
        public Properties getProps() {
            return new Properties(props);
        }

    }

    private static class GrailsCallable implements Callable<Process> {

        private final CommandDescriptor descriptor;

        public GrailsCallable(CommandDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        public Process call() throws Exception {
            String executable =  Utilities.isWindows() ? RuntimeHelper.WIN_EXECUTABLE : RuntimeHelper.NIX_EXECUTABLE;
            File grailsExecutable = new File(GrailsSettings.getInstance().getGrailsBase(), executable);

            if (!grailsExecutable.exists()) {
                LOGGER.log(Level.WARNING, "Executable doesn't exist: "
                        + grailsExecutable.getAbsolutePath());

                return null;
            }

            LOGGER.log(Level.FINEST, "About to run: {0}", descriptor.getName());

            Properties props = new Properties(descriptor.getProps());
            if (descriptor.getEnvironment() != null && descriptor.getEnvironment().isCustom()) {
                props.setProperty("grails.env", descriptor.getEnvironment().toString()); // NOI18N
            }

            StringBuilder command = new StringBuilder();
            command.append(createJvmArguments(props));
            if (descriptor.getEnvironment() != null && !descriptor.getEnvironment().isCustom()) {
                command.append(" ").append(descriptor.getEnvironment().toString());
            }
            command.append(" ").append(descriptor.getName());
            command.append(" ").append(createCommandArguments(descriptor.getArguments()));

            // FIXME fix this hack - needed for proper process tree kill
            // see KillableProcess
            if (Utilities.isWindows() && GUARDED_COMMANDS.contains(descriptor.getName())) {
                command.append(" ").append("REM NB:" // NOI18N
                        +  descriptor.getDirectory().getAbsolutePath());
            }

            LOGGER.log(Level.FINEST, "Command is: {0}", command.toString());

            NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(
                    grailsExecutable.getAbsolutePath(), command.toString());


            String javaHome = null;
            Collection<FileObject> dirs = JavaPlatformManager.getDefault().getDefaultPlatform().getInstallFolders();
            if (dirs.size() == 1) {
                File file = FileUtil.toFile(dirs.iterator().next());
                if (file != null) {
                    javaHome = file.getAbsolutePath();
                }
            }
            if (javaHome == null) {
                javaHome = System.getProperty("java.home");
            }

            String[] envp = new String[] {
                "GRAILS_HOME=" + GrailsSettings.getInstance().getGrailsBase(), // NOI18N
                "JAVA_HOME=" + javaHome // NOI18N
            };

            Process process = new KillableProcess(
                    grailsProcessDesc.exec(null, envp, true, descriptor.getDirectory()),
                    descriptor.getDirectory(), descriptor.getName());

            checkForServer(descriptor, process);
            return process;
        }

    }
}

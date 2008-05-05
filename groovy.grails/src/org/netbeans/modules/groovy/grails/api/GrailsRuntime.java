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

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.KillableProcess;
import org.netbeans.modules.groovy.grails.RuntimeHelper;
import org.netbeans.modules.groovy.grails.server.GrailsInstanceProvider;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class GrailsRuntime {

    private static final Logger LOGGER = Logger.getLogger(GrailsRuntime.class.getName());

    private static GrailsRuntime instance;

    private GrailsRuntime() {
        super();
    }

    public static synchronized GrailsRuntime getInstance() {
        if (instance == null) {
            instance = new GrailsRuntime();
        }
        return instance;
    }

    public Callable<Process> createCommand(CommandDescriptor descriptor) {
        if (!isConfigured()) {
            throw new IllegalStateException("Grails not configured"); // NOI18N
        }
        return new GrailsCallable(descriptor);
    }

    public boolean isConfigured() {
        GrailsSettings settings = GrailsSettings.getInstance();

        if (settings == null) {
            return false;
        }

        String grailsBase = settings.getGrailsBase();

        if (grailsBase == null) {
            return false;
        }

        return RuntimeHelper.isValidRuntime(new File(grailsBase));
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
        if ("run-app".equals(descriptor.getName()) || "run-app-https".equals(descriptor.getName())) { // NOI18N
            Project project = FileOwnerQuery.getOwner(
                    FileUtil.toFileObject(descriptor.getDirectory()));
            if (project != null) {
                GrailsInstanceProvider.getInstance().serverStarted(project, process);
            }
        }
    }

    /**
     * <i>Immutable</i>
     */
    public static final class CommandDescriptor {

        private final String name;

        private final File directory;

        private final GrailsEnvironment environment;

        private final String[] arguments;

        private final Properties props;

        public CommandDescriptor(String name, File directory, GrailsEnvironment env) {
            this(name, directory, env, new String[] {}, new Properties());
        }

        public CommandDescriptor(String name, File directory, GrailsEnvironment env, String[] arguments) {
            this(name, directory, env, arguments, new Properties());
        }

        public CommandDescriptor(String name, File directory, GrailsEnvironment env, String[] arguments, Properties props) {
            this.name = name;
            this.directory = directory;
            this.environment = env;
            this.arguments = arguments.clone();
            this.props = new Properties(props);
        }

        public String getName() {
            return name;
        }

        public File getDirectory() {
            return directory;
        }

        public GrailsEnvironment getEnvironment() {
            return environment;
        }

        public String[] getArguments() {
            return arguments.clone();
        }

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

            LOGGER.log(Level.FINEST, "About to run: " + descriptor.getName());

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

            LOGGER.log(Level.FINEST, "Command is: " + command.toString());

            NbProcessDescriptor grailsProcessDesc = new NbProcessDescriptor(
                    grailsExecutable.getAbsolutePath(), command.toString());

            String[] envp = new String[] {"GRAILS_HOME=" // NOI18N
                    + GrailsSettings.getInstance().getGrailsBase()};

            Process process = new KillableProcess(
                    grailsProcessDesc.exec(null, envp, true, descriptor.getDirectory()),
                    descriptor.getDirectory());

            checkForServer(descriptor, process);
            return process;
        }

    }
}

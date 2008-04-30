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
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Petr Hejl
 */
public final class ExecutionSupport {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(1);

    private static ExecutionSupport instance;

    private ExecutionSupport() {
        super();
    }

    public static synchronized ExecutionSupport getInstance() {
        if (instance == null) {
            instance = new ExecutionSupport();
        }
        return instance;
    }

    public Process executeCreateApp(File directory) throws Exception {
        if (directory.exists()) {
            throw new IOException("Project directory already exists"); // NOI18N
        }

        File work = directory.getAbsoluteFile().getParentFile();
        FileUtil.createFolder(work);
        String name = directory.getName();

        GrailsRuntime.CommandDescriptor descriptor = new GrailsRuntime.CommandDescriptor(
                "create-app", work, null, new String[] {name}); // NOI18N
        return execute(descriptor);
    }

    public Process executeRunApp(GrailsProjectConfig config) throws Exception {
        File directory = FileUtil.toFile(config.getProject().getProjectDirectory());
        Properties props = new Properties();
        props.setProperty("server.port", config.getPort());

        // FIXME fix this hack
        String argument = Utilities.isWindows() ? " REM NB:" +  // NOI18N
                    config.getProject().getProjectDirectory().getName() : "";

        GrailsRuntime.CommandDescriptor descriptor = new GrailsRuntime.CommandDescriptor(
                "run-app", directory, config.getEnvironment(), new String[] {argument}, props);
        return execute(descriptor);
    }

    public Process executeSimpleCommand(String command, GrailsProjectConfig config) throws Exception {
        File directory = FileUtil.toFile(config.getProject().getProjectDirectory());
        GrailsRuntime.CommandDescriptor descriptor = new GrailsRuntime.CommandDescriptor(
                command, directory, config.getEnvironment());
        return execute(descriptor);
    }

    private Process execute(GrailsRuntime.CommandDescriptor descriptor) throws Exception {
        Callable<Process> callable = GrailsRuntime.getInstance().createCommand(descriptor);
        Future<Process> future = EXECUTOR.submit(callable);
        try {
            return future.get();
        } catch (ExecutionException ex) {
            if (ex.getCause() instanceof Exception) {
                throw (Exception) ex.getCause();
            } else {
                throw ex;
            }
        }
    }
}

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

package org.netbeans.api.java.project.runner;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.netbeans.spi.java.project.runner.ProjectRunnerImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * <p>Class that allows to execute given file(s). API clients can check whether given
 * command is support, by calling
 * {@link #isSupported(String)} and execute the command by calling
 * {@link #execute(String, Properties, List)}. Please consult documentation of particular
 * commands for the list of supported properties.</p>
 *
 * @see ProjectRunnerImplementation
 * @since 1.19
 *
 * @author Jan Lahoda
 */
public final class ProjectRunner {

    /**
     * "Test" run the given file. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * Supported properties:
     * <ul>
     * <li><strong>run.jvmargs</strong> arguments that will be passed to the Java Virtual Machine</li>
     * <li><strong>application.args</strong> arguments that will be passed to the executed files</li>
     * </ul>
     *
     * @since 1.19
     */
    public static final String QUICK_RUN = "run";

    /**
     * "Test" run the given file in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * Supported properties:
     * <ul>
     * <li><strong>run.jvmargs</strong> arguments that will be passed to the Java Virtual Machine</li>
     * <li><strong>application.args</strong> arguments that will be passed to the executed files</li>
     * </ul>
     *
     * @since 1.19
     */
    public static final String QUICK_DEBUG = "debug";

    /**
     * "Test" run the given test. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * Supported properties:
     * <ul>
     * <li><strong>run.jvmargs</strong> arguments that will be passed to the Java Virtual Machine</li>
     * </ul>
     *
     * @since 1.19
     */
    public static final String QUICK_TEST = "junit";

    /**
     * "Test" run the given test in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * Supported properties:
     * <ul>
     * <li><strong>run.jvmargs</strong> arguments that will be passed to the Java Virtual Machine</li>
     * </ul>
     *
     * @since 1.19
     */
    public static final String QUICK_TEST_DEBUG = "junit-debug";

    private static final Logger LOG = Logger.getLogger(ProjectRunner.class.getName());

    /**
     * Check whether the given command is supported.
     *
     * @param command command name
     * @param toRun either the file that would be executed, or the project folder
     * @return true if and only if the given command is supported for given file/folder
     *
     * @since 1.19
     */
    public static boolean isSupported(String command, FileObject toRun) {
        Parameters.notNull("command", command);
        Parameters.notNull("toRun", toRun);

        for (ProjectRunnerImplementation i : Lookup.getDefault().lookupAll(ProjectRunnerImplementation.class)) {
            if (i.isSupported(command, toRun)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Execute the given command with given parameters. Please refer to the documentation
     * of the given command for supported properties.
     *
     * @param command command to execute
     * @param props properties
     * @param toRun file to run
     * @throws java.io.IOException if execution fails
     *
     * @since 1.19
     */
    public static void execute(String command, Properties props, FileObject toRun) throws IOException {
        Parameters.notNull("command", command);
        Parameters.notNull("props", props);
        Parameters.notNull("toRun", toRun);
        
        for (ProjectRunnerImplementation i : Lookup.getDefault().lookupAll(ProjectRunnerImplementation.class)) {
            if (i.isSupported(command, toRun)) {
                i.execute(command, props, toRun);
                break;
            }
        }
    }

}

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
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.project.runner.JavaRunnerImplementation;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * <p>Class that allows to execute given file(s). API clients can check whether given
 * command is support, by calling
 * {@link #isSupported(String)} and execute the command by calling
 * {@link #execute(String, Map)}. Please consult documentation of particular
 * commands for the list of supported properties.</p>
 *
 * The following "standard" properties are supported by most commands (unless stated otherwise):
 * <table>
 * <tr><td>{@link #PROP_EXECUTE_FILE}      </td> <td>file to be executed (optional)</td> <td>{@link String} (absolute path) or {@link FileObject}</td></tr>
 * <tr><td>{@link #PROP_WORK_DIR}          </td> <td> working directory, project directory of execute.file will be used if missing </td> <td> {@link String} or {@link FileObject} or {@link java.io.File}</td></tr>
 * <tr><td>{@link #PROP_CLASSNAME}         </td> <td> class to execute, will be autodetected from execute.file if missing </td> <td> {@link String}</td></tr>
 * <tr><td>{@link #PROP_EXECUTE_CLASSPATH} </td> <td> execute classpath, will be autodetected from execute.file if missing </td> <td> {@link ClassPath}</td></tr>
 * <tr><td>{@link #PROP_PLATFORM_JAVA}     </td> <td> java tool which should be used for execution, will be autodetected from platform property if missing </td> <td> {@link String} or {@link FileObject} or {@link java.io.File}</td></tr>
 * <tr><td>{@link #PROP_PLATFORM}          </td> <td> java platform on which the class should be executed, default if missing, not needed if platform.java is set </td> <td> {@link JavaPlatform}</td></tr>
 * <tr><td>{@link #PROP_PROJECT_NAME}      </td> <td> name of the current project, will be autodetected from execute.file if missing </td> <td> {@link String}</td></tr>
 * <tr><td>{@link #PROP_RUN_JVMARGS}  </td> <td> JVM arguments </td> <td> {@link Iterable} of {@link String}s</td></tr>
 * <tr><td>{@link #PROP_APPLICATION_ARGS}  </td> <td> application arguments </td> <td> {@link Iterable} of {@link String}s</td></tr>
 * </table>
 * 
 * @see JavaRunnerImplementation
 * @since 1.22
 *
 * @author Jan Lahoda
 */
public final class JavaRunner {

    /**
     * "Test" run the given file. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * @since 1.22
     */
    public static final String QUICK_RUN = "run";

    /**
     * "Test" run the given file in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * @since 1.22
     */
    public static final String QUICK_DEBUG = "debug";

    /**
     * "Test" run the given test. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * <strong>application.args</strong> property is not supported.
     *
     * @since 1.22
     */
    public static final String QUICK_TEST = "junit";

    /**
     * "Test" run the given test in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * <strong>application.args</strong> property is not supported.
     *
     * @since 1.22
     */
    public static final String QUICK_TEST_DEBUG = "junit-debug";

    /**
     * @since 1.22
     */
    public static final String QUICK_RUN_APPLET = "run-applet";
    
    /**
     * @since 1.22
     */
    public static final String QUICK_DEBUG_APPLET = "debug-applet";
    
    /**
     * @since 1.22
     */
    public static final String QUICK_CLEAN = "clean";

    /**
     * @since 1.22
     */
    public static final String PROP_EXECUTE_FILE = "execute.file";

    /**
     * @since 1.22
     */
    public static final String PROP_WORK_DIR = "work.dir";

    /**
     * @since 1.22
     */
    public static final String PROP_RUN_JVMARGS = "run.jvmargs";

    /**
     * @since 1.22
     */
    public static final String PROP_CLASSNAME = "classname";

    /**
     * @since 1.22
     */
    public static final String PROP_EXECUTE_CLASSPATH = "execute.classpath";

    /**
     * @since 1.22
     */
    public static final String PROP_PLATFORM_JAVA = "platform.java";

    /**
     * @since 1.22
     */
    public static final String PROP_PLATFORM = "platform";

    /**
     * @since 1.22
     */
    public static final String PROP_PROJECT_NAME = "project.name";

    /**
     * @since 1.22
     */
    public static final String PROP_APPLICATION_ARGS = "application.args";

    private static final Logger LOG = Logger.getLogger(JavaRunner.class.getName());

    /**
     * Check whether the given command is supported.
     *
     * @param command command name
     * @param toRun either the file that would be executed, or the project folder
     * @return true if and only if the given command is supported for given file/folder
     *
     * @since 1.22
     */
    public static boolean isSupported(String command, Map<String, ?> properties) {
        Parameters.notNull("command", command);
        Parameters.notNull("properties", properties);

        for (JavaRunnerImplementation i : Lookup.getDefault().lookupAll(JavaRunnerImplementation.class)) {
            if (i.isSupported(command, properties)) {
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
     * @throws java.lang.UnsupportedOperationException if the given command is not supported
     *
     * @since 1.22
     */
    public static ExecutorTask execute(String command, Map<String, ?> properties) throws IOException, UnsupportedOperationException {
        Parameters.notNull("command", command);
        Parameters.notNull("properties", properties);
        
        for (JavaRunnerImplementation i : Lookup.getDefault().lookupAll(JavaRunnerImplementation.class)) {
            if (i.isSupported(command, properties)) {
                return i.execute(command, properties);
            }
        }

        throw new UnsupportedOperationException();
    }

}

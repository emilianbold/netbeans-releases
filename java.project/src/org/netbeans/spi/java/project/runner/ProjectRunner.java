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

package org.netbeans.spi.java.project.runner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * <p>Class that allows to execute given file(s). API clients can check whether given
 * command is support, by calling
 * {@link #isSupported(String)} and execute the command by calling
 * {@link #execute(String, Properties, List)}. Please consult documentation of particular
 * commands for the list of supported properties.</p>
 *
 * <p>SPI clients (command providers) should write a short ant build script performing
 * the given command and register it on the default filesystem as <code>executor-snippets/&lt;command&gt;.xml</code>.
 * The project runner will automatically set the following properties:</p>
 * <ul>
 * <li><strong>classpath</strong> contains executable classpath of the executed file</li>
 * <li><strong>classname</strong> contains a classname corresponding to the file that should be executed</li>
 * </ul>
 *
 * <p>If the script defines an attribute <code>checkSupported</code> of a string value, the value is
 * interpreted as a name of a static method with one parameter of type {@link FileObject}.
 * When the API client invokes the {@link #isSupported(String,FileObject)} method, the
 * method specified by the <code>checkSupported</code> is invoked. Its parameter is
 * the same as the second parameter of {@link #isSupported(String,FileObject)}.
 * {@link #isSupported(String,FileObject)} returns true if and only if script exists for the given
 * command and either the <code>checkSupported</code> attribute is not specified or
 * the method specified by this attribute exists and returns boolean value <code>true</code>.
 *
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
     * Supported properties: none.
     *
     * @since 1.19
     */
    public static final String QUICK_TEST = "junit";

    /**
     * "Test" run the given test in the debugging mode. Classfiles produced by the Java infrastructure will be
     * executed.
     *
     * Supported properties: none.
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

        FileObject script = locateScript(command);

        if (script == null) {
            return false;
        }

        Object attribute = script.getAttribute("checkSupported");

        if (attribute == null) {
            return true;
        }

        if (!(attribute instanceof String)) {
            LOG.log(Level.WARNING, "checkSupported attribute of {0} is not a String value (class: {1})", new Object[] {FileUtil.getFileDisplayName(script), attribute.getClass()});
            return false;
        }

        try {
            String m = (String) attribute;
            int lastDot = m.lastIndexOf('.');

            if (lastDot == (-1)) {
                LOG.log(Level.WARNING, "checkSupported attribute of {0} is malformed: {1})", new Object[] {FileUtil.getFileDisplayName(script), m});
                return false;
            }

            String clazzName = m.substring(0, lastDot);
            String methodName = m.substring(lastDot + 1);

            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            Class clazz;

            if (loader != null) {
                clazz = Class.forName(clazzName, true, loader);
            } else {
                clazz = Class.forName(clazzName);
            }

            Method method = clazz.getDeclaredMethod(methodName, FileObject.class);

            method.setAccessible(true);

            Object r = method.invoke(null, toRun);

            return r instanceof Boolean && (Boolean) r;
        } catch (IllegalAccessException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        } catch (IllegalArgumentException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        } catch (InvocationTargetException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        } catch (NoSuchMethodException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        } catch (SecurityException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        } catch (ClassNotFoundException ex) {
            LOG.log(Level.WARNING, "Cannot execute checkSupported method for {0} because of an exception.", new Object[] {FileUtil.getFileDisplayName(script), ex});
            return false;
        }
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
        ClassPath exec = ClassPath.getClassPath(toRun, ClassPath.EXECUTE);
        ClassPath source = ClassPath.getClassPath(toRun, ClassPath.SOURCE);

        LOG.log(Level.FINE, "execute classpath={0}", exec);

        String cp = toString(exec);

        Properties antProps = (Properties) props.clone();

        antProps.setProperty("classpath", cp);
        antProps.setProperty("classname", source.getResourceName(toRun, '.', false));

        ActionUtils.runTarget(buildScript(command), new String[] {"execute"}, antProps);
    }

    private static String toString(ClassPath exec) {
        StringBuilder cp = new StringBuilder();
        boolean first = true;

        for (Entry e : exec.entries()) {
            if (!first) {
                cp.append(File.pathSeparatorChar);
            }

            File f = FileUtil.archiveOrDirForURL(e.getURL());

            if (f != null) {
                cp.append(f.getAbsolutePath());
                first = false;
            }
        }

        return cp.toString();
    }

    private static FileObject locateScript(String actionName) {
        return Repository.getDefault().getDefaultFileSystem().findResource("executor-snippets/" + actionName + ".xml");
    }

    private static FileObject buildScript(String actionName) {
        FileObject script = locateScript(actionName);

        if (script == null) {
            return null;
        }

        File scriptFile = new File(getCacheFolder(), actionName + ".xml");

        if (!scriptFile.canRead() || script.lastModified().getTime() > scriptFile.lastModified()) {
            try {
                scriptFile.delete();

                FileObject parent = FileUtil.createFolder(scriptFile.getParentFile());

                return FileUtil.copyFile(script, parent, actionName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }

        return FileUtil.toFileObject(scriptFile);
    }

    private static final String NB_USER_DIR = "netbeans.user";   //NOI18N
    private static final String SNIPPETS_CACHE_DIR = "var"+File.separatorChar+"cache"+File.separatorChar+"executor-snippets";    //NOI18N


    private static String getNbUserDir () {
        final String nbUserProp = System.getProperty(NB_USER_DIR);
        return nbUserProp;
    }

    private static File cacheFolder;

    private static synchronized File getCacheFolder () {
        if (cacheFolder == null) {
            final String nbUserDirProp = getNbUserDir();
            assert nbUserDirProp != null;
            final File nbUserDir = new File (nbUserDirProp);
            cacheFolder = FileUtil.normalizeFile(new File (nbUserDir, SNIPPETS_CACHE_DIR));
            if (!cacheFolder.exists()) {
                boolean created = cacheFolder.mkdirs();
                assert created : "Cannot create cache folder";  //NOI18N
            }
            else {
                assert cacheFolder.isDirectory() && cacheFolder.canRead() && cacheFolder.canWrite();
            }
        }
        return cacheFolder;
    }
}

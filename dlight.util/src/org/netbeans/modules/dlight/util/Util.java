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
package org.netbeans.modules.dlight.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

public class Util {

    private static final Logger log = DLightLogger.getLogger(Util.class);

    private Util() {
    }

    /**
     * Gets an absolute path of the module-installed file.
     * @param relpath path from install root, e.g. <samp>modules/ext/somelib.jar</samp>
     * (always using <samp>/</samp> as a separator, regardless of platform).
     * @return absolute path to the file
     */
    private static String getFullPath(String relpath) {
        File file = InstalledFileLocator.getDefault().locate(relpath, null, false);
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }
        return relpath;
    }

    /**
     * Copies a file from resources to a temporary directory
     *
     * @param clazz Determines the jar from which the file should be copied
     * @param resourceName The resource file name
     * @return the newly-created file
     * @throws IOException
     */
    public static File copyToTempDir(Class<?> clazz, String resourceName) throws IOException {
        return copyToTempDir(clazz.getClassLoader().getResource(resourceName));
    }

    /**
     * Downloads URL content to a file in temporary directory.
     *
     * @param url
     * @return the newly-created file
     * @throws IOException
     */
    public static File copyToTempDir(URL url) throws IOException {
        if (url == null) {
            return null;
        }

        InputStream is = url.openStream();
        try {
            HostInfo hostInfo = HostInfoUtils.getHostInfo(ExecutionEnvironmentFactory.getLocal());

            if (hostInfo == null) {
                throw new IOException("Failed to get local ExecutionEnvironment"); // NOI18N
            }

            String prefix = "_dlight_" + getBriefName(url); // NOI18N

            File result = File.createTempFile(prefix, "", hostInfo.getTempDirFile()); // NOI18N
            result.deleteOnExit();

            OutputStream os = new FileOutputStream(result);
            try {
                FileUtil.copy(is, os);
                os.flush();
                return result;
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    public static String getBriefName(URL resourceUrl) {
        String result = resourceUrl.getFile();
        int pos = result.lastIndexOf('/');
        result = (pos >= 0) ? result.substring(pos + 1) : result;
        return result;
    }

    /**
     * Sets execution permission for a list of files
     * @param files A list of files paths to set execution permissions.
     * Paths are relative to the install root, e.g. <samp>modules/ext/somelib.jar</samp>
     * (always using <samp>/</samp> as a separator, regardless of platform).
     */
    public static void setExecutionPermissions(final List<String> files) {
        if (files.isEmpty()) {
            return;
        }

        List<String> paths = new ArrayList<String>();
        for (String f : files) {
            String fullPath = f;
            if (!(new File(f)).exists()) {
                fullPath = getFullPath(f);
            }

            if (new File(fullPath).exists()) {
                paths.add(fullPath);
            }
        }

        List<String> commands = new ArrayList<String>();
        commands.add("/bin/chmod"); // NOI18N
        commands.add("755"); // NOI18N
        commands.addAll(paths);
        ProcessBuilder pb = new ProcessBuilder(commands);
        try {
            pb.start();
        } catch (IOException ex) {
            ex.printStackTrace();
            //Gizmo.err.log("Cannot set execution permissions of files! " + ex.getMessage()); // NOI18N
        }
    }

    /**
     * Gets a base path that corresponds a class.
     * For example, <samp>org.nebeabs.modules.dlight.MyClass -> org/nebeabs/modules/dlight</samp>.
     * @param cls a class to return base path for
     * @return the base path for the given class
     */
    public static String getBasePath(Class<?> cls) {
        String path = cls.getName().replace('.', '/');//NOI18N
        int pos = path.lastIndexOf('/');//NOI18N
        return (pos > 0) ? path.substring(0, pos) : path;
    }

    /**
     * The same as <code>Boolean.getBoolean(String)</code>,
     * but allows to set a default value.
     * @param name a name of a property
     * @param defaultValue
     * @return If the system property with the given name equals <code>"true"</code>,
     * returns <code>true</code>; if the system property with the given name equals <code>"false"</code>,
     * returns <code>false</code>; otherwise returns the <code>defaultValue</code>
     */
    public static boolean getBoolean(String name, boolean defaultValue) {
        String value = System.getProperty(name);
        return (value == null) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Removes a directory and all int content, recursively.
     * @param path a path to the directory to delete
     * @return true in the case of success, otherwise false
     */
    public static boolean deleteLocalDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteLocalDirectory(files[i]);
                    } else {
                        boolean result = files[i].delete();
                        if (!result && log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Unable to delete file {0}", files[i].getAbsolutePath()); // NOI18N
                        }
                    }
                }
            }
        }
        return (path.delete());
    }

    /**
     * Returns first instance of class from a collection.
     * @param <T>  class to search for
     * @param clazz  class to search for
     * @param objects  collection to search in
     * @return first instance of class from collection
     */
    public static <T> T firstInstanceOf(Class<T> clazz, Collection<? super T> objects) {
        for (Object obj : objects) {
            if (clazz.isAssignableFrom(obj.getClass())) {
                return clazz.cast(obj);
            }
        }
        return null;
    }

    public static <T> T maskNull(T value, T nullValue) {
        return value == null? nullValue : value;
    }
}

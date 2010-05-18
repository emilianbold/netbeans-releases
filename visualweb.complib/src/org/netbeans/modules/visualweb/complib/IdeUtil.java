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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.complib;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

/**
 * Misc methods to simplify interface to IDE
 * 
 * @author Edwin Goei
 */
public class IdeUtil {
    private static final Logger logger = Logger.getLogger(IdeUtil.class.getPackage().getName());

    private static File raveClusterDir;

    /**
     * Returns the Logger for this NB module. Use Java logging instead of the older NB ErrorManager APIs.
     * 
     * @return
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * @deprecated Use the newer Java logging API.
     * @param th
     */
    public static void logWarning(Throwable th) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, th);
    }

    public static void logWarning(String msg) {
        logWarning(new Throwable(msg));
    }

    public static void logWarning(String msg, Throwable cause) {
        logWarning(new Throwable(msg, cause));
    }

    /**
     * @deprecated Use the newer Java logging API.
     * @param th
     */
    public static void logError(Throwable th) {
        ErrorManager.getDefault().notify(ErrorManager.ERROR, th);
    }

    public static void logError(String msg) {
        logError(new Throwable(msg));
    }

    public static void logError(String msg, Throwable cause) {
        logError(new Throwable(msg, cause));
    }

    public static void printDebug(String msg) {
        System.err.println(msg);
    }

    /**
     * Return the Project that is currently active according to the Designer.
     * 
     * @return currently active project or null, if none.
     */
    public static Project getActiveProject() {
        FileObject fileObject = DesignerServiceHack.getDefault().getCurrentFile();
        if (fileObject == null) {
            return null;
        }
        return FileOwnerQuery.getOwner(fileObject);
    }

    /**
     * Returns a NB Project for a project directory or else null if not a project.
     * 
     * @param projectDir
     * @return NB Project object or null if not a project
     */
    public static Project fileToProject(File projectDir) {
        try {
            FileObject fo = FileUtil.toFileObject(projectDir);
            if (fo != null && /* #60518 */fo.isFolder()) {
                return ProjectManager.getDefault().findProject(fo);
            } else {
                return null;
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            return null;
        }
    }

    /**
     * Remove whitespace in a String and replace with underscores
     * 
     * @param str
     * @return new String with whitespace replaced with underscores
     */
    public static String removeWhiteSpace(String str) {
        String[] parts = str.split("\\s+"); // NOI18N
        if (parts.length <= 1) {
            return str;
        }

        StringBuffer buf = new StringBuffer();
        buf.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            buf.append('_');
            buf.append(parts[i]);
        }
        return buf.toString();
    }

    /**
     * Unzip a zip or jar file into a dest directory. Derived from jar command in JDK.
     * 
     * @param zipFile
     *            a zip or jar file
     * @param dest
     *            destination directory to contain expanded contents
     * @throws IOException
     */
    public static void unzip(File zipFile, File dest) throws IOException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(
                new FileInputStream(zipFile)));
        ZipEntry e;
        while ((e = zis.getNextEntry()) != null) {
            File f = new File(dest, e.getName().replace('/', File.separatorChar));
            if (e.isDirectory()) {
                if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
                    throw new IOException("Unable to create dir: " + f.getPath());
                }
            } else {
                if (f.getParent() != null) {
                    File d = new File(f.getParent());
                    if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
                        throw new IOException("Unable to create dir: " + d.getPath());
                    }
                }
                OutputStream os = new FileOutputStream(f);
                byte[] b = new byte[512];
                int len;
                while ((len = zis.read(b, 0, b.length)) != -1) {
                    os.write(b, 0, len);
                }
                zis.closeEntry();
                os.close();
            }
        }
        zis.close();
    }

    /**
     * @param antScript
     *            Ant build file
     * @param target
     *            target to build
     * @param props
     *            properties to set
     * @throws IOException
     */
    public static void runAntTask(File antScript, String target, Properties props)
            throws IOException {
        ExecutorTask executorTask = ActionUtils.runTarget(FileUtil.toFileObject(antScript),
                new String[] { target }, props);

        int result = executorTask.result();
        if (result != 0) {
            throw new IOException("Ant task execution failed");
        }
    }

    /**
     * Return the non-extension part of a filename. eg. foo.txt -> "foo"
     * 
     * @param name
     * @return
     */
    public static String removeExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) {
            return name;
        }
        return name.substring(0, i);
    }

    /**
     * Find unique file name within a directory which can either be a plain file or a directory.
     * 
     * @param dir
     *            eg. an absoluteLibDir
     * @param baseFileName
     *            eg. "sample-date-dt"
     * @param suffix
     *            Can be empty string to mean no suffix. eg. ".jar", ""
     * @return
     */
    public static File findUniqueFile(File dir, String baseFileName, String suffix) {
        assert dir.isDirectory();

        String testName = baseFileName + suffix;
        File testFile = new File(dir, testName);
        int i = 0;
        while (testFile.exists()) {
            i++;
            testName = baseFileName + "_" + i + suffix;
            testFile = new File(dir, testName);
        }
        return testFile;
    }

    /**
     * Get the top level netbeans install directory
     * 
     * @return
     */
    public static File getNetBeansInstallDirectory() {
        File dir = getRaveClusterDirectory();
        if (dir != null) {
            return dir.getParentFile();
        }
        return null;
        // return getRaveClusterDirectory().getParentFile();
    }

    /**
     * Get the rave samples directory
     * 
     * @return
     */
    public static File getRaveSamplesDirectory() {
        return new File(IdeUtil.getRaveClusterDirectory(), "samples"); // NOI18N
    }

    /**
     * Get the rave cluster directory
     * 
     * @return
     */
    private static File getRaveClusterDirectory() {
        // Isn't there a better way to find the top level rave directory??
        if (raveClusterDir == null) {
            File file = InstalledFileLocator.getDefault().locate(
                    "modules/org-netbeans-modules-visualweb-complib.jar", null, false); // NOI18N
            if (file != null) {
                raveClusterDir = file.getParentFile().getParentFile();
            }
        }
        return raveClusterDir;
    }

    public static void copyFileRecursive(File source, File dest) throws IOException {
        File newItem = null;
        if (dest.isDirectory()) {
            newItem = new File(dest, source.getName());
        } else {
            newItem = dest;
        }

        if (source.isDirectory()) {
            newItem.mkdir();
            File[] contents = source.listFiles();

            for (int i = 0; i < contents.length; i++) {
                copyFileRecursive(contents[i], newItem);
            }
        } else {
            copyFile(source, newItem);
        }
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all deletions were
     * successful. If a deletion fails, the method stops attempting to delete and returns false.
     * 
     * @param dir
     * @return
     */
    public static boolean deleteRecursive(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteRecursive(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Possibly make a copy of a file to workaround a problem on Windows where a jar file can be
     * locked and not allow the user to remove it. The tradeoff is that the file will be copied
     * which may take time for large files. A System property can be set to override the default
     * behavior.
     * 
     * @param origFile
     *            original file eg. jar or complib file
     * @return
     * @throws IOException
     */
    public static File freeJarFile(File origFile) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase() // NOI18N
                .indexOf("windows") != -1;// NOI18N

        // Escape hatch to allow user to override default copying behavior
        String doTempCopy = System.getProperty("toolbox.makeTempCopy", Boolean // NOI18N
                .toString(isWindows));

        if (!Boolean.parseBoolean(doTempCopy)) {
            return origFile;
        }

        // Create a temporary file that will be deleted on exit. Figure out a
        // user-friendly prefix and suffix if possible.
        String prefix;
        String suffix;
        String baseName = origFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1 && (dotIndex + 1) < baseName.length()) {
            prefix = baseName.substring(0, dotIndex) + "_";
            suffix = baseName.substring(dotIndex);
        } else {
            prefix = baseName;
            suffix = null;
        }
        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();
        copyFile(origFile, temp);
        return temp;
    }

    /**
     * Copy files using nio
     * 
     * @param src
     *            source file
     * @param dst
     *            destination file
     * @throws IOException
     */
    public static void copyFile(File src, File dst) throws IOException {
        FileChannel srcChannel = new FileInputStream(src).getChannel();
        FileChannel dstChannel = new FileOutputStream(dst).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
        srcChannel.close();
        dstChannel.close();
    }

    /**
     * Return base class name using "." as a separator
     * 
     * @param name
     * @return
     */
    public static String baseClassName(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0) {
            return name;
        }
        if (name.length() > 0) {
            return name.substring(i + 1);
        }
        return name;
    }

    /**
     * Return basename of a string similar to unix basename command. Uses platform file separator
     * char. Basename of root dir is itself.
     * 
     * @param name
     * @return
     */
    public static String baseName(String name) {
        int i = name.lastIndexOf(File.separatorChar);
        if (i < 0) {
            return name;
        }
        if (name.length() > 0) {
            return name.substring(i + 1);
        }
        return name;
    }

    /**
     * Returns a directory under the project root under which library resources such as jar files
     * can be managed by the IDE.
     * 
     * @param project
     *            Target project
     * @return File of the library directory or null if it does not exist
     */
    public static File getProjectLibraryDirectory(Project project) {
        FileObject projRoot = project.getProjectDirectory();
        File projRootFile = FileUtil.toFile(projRoot);
        return new File(projRootFile, JsfProjectConstants.PATH_LIBRARIES);
    }

    /** ********* Begin module-specifc methods ****************************** */

    /** Root directory in userDir where complib state is stored */
    private static File complibStateDir;

    /**
     * Returns the NetBeans module code name base.
     * 
     * @return
     */
    public static String getCodeNameBase() {
        // Code name base should be the same as the package name of this class
        return IdeUtil.class.getPackage().getName();
    }

    /**
     * Get the directory where state of this module is kept. This will be in the userdir and also
     * part of the NetBeans filesystem so that it will make it easier to migrate to future IDE
     * versions.
     * 
     * @return
     * @throws IOException
     */
    public static File getComplibStateDir() {
        if (complibStateDir == null) {
            File root = FileUtil.toFile(FileUtil.getConfigRoot());
            // Follow NetBeans convention of replacing dots with dashes
            String stateDirName = getCodeNameBase().replace('.', '-');
            complibStateDir = new File(root, stateDirName);
            if (!complibStateDir.exists() && !complibStateDir.mkdirs()) {
                IllegalStateException ex = new IllegalStateException("Unable to create dir: "
                        + complibStateDir);
                logError(ex);
                throw ex;
            }
        }
        return complibStateDir;
    }

    /** *********** End module-specifc methods ****************************** */
}

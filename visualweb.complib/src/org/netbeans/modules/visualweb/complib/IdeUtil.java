/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.ErrorManager;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;

import org.netbeans.modules.visualweb.api.designerapi.DesignerServiceHack;

/**
 * Misc methods to simplify interface to IDE
 * 
 * @author Edwin Goei
 */
public class IdeUtil {

    private static File raveClusterDir;

    public static void logWarning(Throwable th) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, th);
    }

    public static void logWarning(String msg) {
        logWarning(new Throwable(msg));
    }

    public static void logWarning(String msg, Throwable cause) {
        logWarning(new Throwable(msg, cause));
    }

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
        FileObject fileObject = DesignerServiceHack.getDefault()
                .getCurrentFile();
        if (fileObject == null) {
            return null;
        }
        return FileOwnerQuery.getOwner(fileObject);
    }

    /**
     * Returns a NB Project for a project directory or else null if not a
     * project.
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
     * Unzip a zip or jar file into a dest directory. Derived from jar command
     * in JDK.
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
            File f = new File(dest, e.getName()
                    .replace('/', File.separatorChar));
            if (e.isDirectory()) {
                if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
                    throw new IOException("Unable to create dir: "
                            + f.getPath());
                }
            } else {
                if (f.getParent() != null) {
                    File d = new File(f.getParent());
                    if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
                        throw new IOException("Unable to create dir: "
                                + d.getPath());
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
    public static void runAntTask(File antScript, String target,
        Properties props) throws IOException {
        ExecutorTask executorTask = ActionUtils.runTarget(FileUtil
                .toFileObject(antScript), new String[] { target }, props);

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
     * Find unique file name within a directory which can either be a plain file
     * or a directory.
     * 
     * @param dir
     *            eg. an absoluteLibDir
     * @param baseFileName
     *            eg. "sample-date-dt"
     * @param suffix
     *            Can be empty string to mean no suffix. eg. ".jar", ""
     * @return
     */
    public static File findUniqueFile(File dir, String baseFileName,
        String suffix) {
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
                    "modules/org-netbeans-modules-visualweb-complib.jar", null,
                    false); // NOI18N
            if (file != null) {
                raveClusterDir = file.getParentFile().getParentFile();
            }
        }
        return raveClusterDir;
    }

    public static void copyFileRecursive(File source, File dest)
            throws IOException {
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
     * Possibly make a copy of a file to workaround a problem on Windows where a
     * jar file can be locked and not allow the user to remove it. The tradeoff
     * is that the file will be copied which may take time for large files. A
     * System property can be set to override the default behavior.
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

        // In JDK 1.5 we can use Boolean.parseBoolean() instead
        if (!Boolean.valueOf(doTempCopy).booleanValue()) {
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
     * Return basename of a string similar to unix basename command. Uses
     * platform file separator char. Basename of root dir is itself.
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
}

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
package org.netbeans.modules.cnd.remote.support;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.utils.RemoteUtils;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Sergey Grinev
 */
public class SystemIncludesUtils {

    public static void load(final String hkey, final List<CompilerSet> csList) {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                String storagePrefix = null;
                try {
                    Set<String> paths = new HashSet<String>();
                    for (CompilerSet cs : csList) {
                        for (Tool tool : cs.getTools()) {
                            if (tool instanceof BasicCompiler) {
                                BasicCompiler bc = (BasicCompiler) tool;
                                storagePrefix = bc.getStoragePrefix();
                                for (Object obj : bc.getSystemIncludeDirectories()) {
                                    String localPath = (String) obj;
                                    if (localPath.length() < storagePrefix.length()) {
                                        System.err.println("CompilerSet " + bc.getDisplayName() + " has returned invalid include path: " + localPath);
                                    } else {
                                        paths.add(localPath.substring(storagePrefix.length()));
                                    }
                                }
                            }
                        }
                    }
                    synchronized (inProgress) {
                        if (inProgress.contains(storagePrefix)) { //TODO: very weak validation
                            return;
                        }
                        inProgress.add(storagePrefix);
                    }
                    if (storagePrefix != null) {
                        doLoad(hkey, storagePrefix, paths);
                    }
                } finally {
                    synchronized (inProgress) {
                        inProgress.remove(storagePrefix);
                    }
                }
            }
        });
    }    // TODO: to think about next way:
    // just put links in the path mapped from server and set up
    // toolchain accordingly. Although those files will confuse user...
    // Hiding links in nbproject can help but would lead for different
    // include set for each project and issues with connecting to new
    // hosts with the same project...
    private static final Set<String> inProgress = new HashSet<String>();

    static boolean doLoad(final String hkey, String storagePrefix, Collection<String> paths) {
        File includesStorageFolder = new File(storagePrefix);
        File tempIncludesStorageFolder = new File(includesStorageFolder.getParent(), includesStorageFolder.getName() + ".download"); //NOI18N

        if (includesStorageFolder.exists()) { //TODO: very weak validation
            return true;
        }

        if (!tempIncludesStorageFolder.exists()) {
            tempIncludesStorageFolder.mkdirs();
        }
        if (!tempIncludesStorageFolder.isDirectory()) {
            //log
            return false;
        }
        boolean success = false;
        ProgressHandle handle = ProgressHandleFactory.createHandle(getMessage("SIU_ProgressTitle") + " " + RemoteUtils.getHostName(hkey)); //NOI18N
        handle.start();
        try {
            RemoteCopySupport rcs = new RemoteCopySupport(hkey);
            success = load(tempIncludesStorageFolder.getAbsolutePath(), rcs, paths, handle);
            if (success) {
                tempIncludesStorageFolder.renameTo(includesStorageFolder);
            }
        } finally {
            handle.finish();
            if (!success && includesStorageFolder.exists()) {
                includesStorageFolder.delete();
            }
        }
        return true;
    }
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private static boolean load(String storageFolder, RemoteCopySupport copySupport, Collection<String> paths, ProgressHandle handle) {
        handle.switchToDeterminate(3 * paths.size());
        int workunit = 0;
        for (String path : paths) {
            //TODO: check file existence (or make shell script to rule them all ?)
            String zipRemote = "cnd" + path.replaceAll("(/|\\\\)", "-") + ".zip"; //NOI18N
            String zipRemotePath = "/tmp/" + zipRemote; // NOI18N
            String zipLocalPath = tempDir + File.separator + zipRemote;

            handle.progress(getMessage("SIU_Archiving") + " " + path, workunit++); // NOI18N
            copySupport.run("zip -r -q " + zipRemotePath + " " + path); //NOI18N
            handle.progress(getMessage("SIU_Downloading") + " " + path, workunit++); // NOI18N
            copySupport.copyFrom(zipRemotePath, zipLocalPath);
            handle.progress(getMessage("SIU_Preparing") + " " + path, workunit++); // NOI18N
            unzip(storageFolder, zipLocalPath);
        }
        copySupport.disconnect();
        return true;
    }

    static void unzip(String path, String fileName) {
        long start = System.currentTimeMillis();
        Enumeration entries;
        ZipFile zipFile;

        try {
            File parent = new File(path);
            if (!parent.exists()) {
                parent.mkdir();
            }
            assert parent.isDirectory();

            zipFile = new ZipFile(fileName);

            entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();

                File file = new File(parent, entry.getName());
                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    copyInputStream(zipFile.getInputStream(entry),
                            new BufferedOutputStream(new FileOutputStream(file.getAbsolutePath())));
                }
            }

            zipFile.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        } finally {
            System.err.println("unzipping " + fileName + " to " + path + " took " + (System.currentTimeMillis() - start) + " ms");
        }
    }

    private static final void copyInputStream(InputStream in, OutputStream out)
            throws IOException {
        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    private static String getMessage(String key) {
        return NbBundle.getMessage(SystemIncludesUtils.class, key);
    }
}

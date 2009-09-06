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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSupport;
import org.netbeans.modules.cnd.remote.fs.RemoteFileSystemsProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

/**
 *
 * @author Sergey Grinev
 */
public class SystemIncludesUtils {

    private static final Map<ExecutionEnvironment, Collection<WeakReference<Loader>>> loaders =
            new LinkedHashMap<ExecutionEnvironment, Collection<WeakReference<Loader>>>();

    private static final Object loadersLock = new Object();

    private static class Loader implements Runnable {

        private final ExecutionEnvironment execEnv;
        private final List<CompilerSet> csList;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private Thread thread;

        public Loader(ExecutionEnvironment execEnv, List<CompilerSet> csList) {
            this.execEnv = execEnv;
            this.csList = csList;
        }

        public void cancel() {
            cancelled.set(true);
            synchronized (this) {
                if (thread != null) {
                    thread.interrupt();
                }
            }
        }

        public void run() {
            if (cancelled.get()) {
                return;
            }
            synchronized (this) {
                thread = Thread.currentThread();
            }
            try {
                load();
            } finally {
                synchronized (this) {
                    thread = null;
                }
            }
        }

        private void load() {
            ProgressHandle handle = ProgressHandleFactory.createHandle(getMessage("SIU_ProgressTitle") + " " + execEnv.getHost()); //NOI18N
            handle.start();
            RemoteUtil.LOGGER.fine("SystemIncludesUtils.load for " + execEnv); // NOI18N
            String storagePrefix = null;
            try {
                Set<String> paths = new HashSet<String>();
                for (CompilerSet cs : csList) {
                    for (Tool tool : cs.getTools()) {
                        if (tool instanceof BasicCompiler) {
                            BasicCompiler bc = (BasicCompiler) tool;
                            storagePrefix = bc.getIncludeFilePathPrefix();
                            for (Object obj : bc.getSystemIncludeDirectories()) {
                                String localPath = (String) obj;
                                if (localPath.length() < storagePrefix.length()) {
                                    RemoteUtil.LOGGER.warning("CompilerSet " + bc.getDisplayName() + " has returned invalid include path: " + localPath);
                                } else {
                                    paths.add(localPath.substring(storagePrefix.length()));
                                }
                            }
                        }
                    }
                }
                if (storagePrefix != null) {
                    doLoad(execEnv, storagePrefix, paths, handle, cancelled);
                }
            } finally {
                handle.finish();
            }
        }
    }

    public static Runnable createLoader(final ExecutionEnvironment execEnv, final List<CompilerSet> csList) {
        Loader loader = new Loader(execEnv, csList);
        addLoader(execEnv, loader);
        return loader;
    }

    private static void addLoader(ExecutionEnvironment execEnv, Loader loader) {
        synchronized (loadersLock) {
            Collection<WeakReference<Loader>> envLoaders = loaders.get(execEnv);
            if (envLoaders == null) {
                envLoaders = new ArrayList<WeakReference<Loader>>();
                loaders.put(execEnv, envLoaders);
            }
            envLoaders.add(new WeakReference<Loader>(loader));
        }
    }

    public static void cancel(Collection<ExecutionEnvironment> environmants) {
        for (ExecutionEnvironment execEnv : environmants) {
            cancel(execEnv);
        }
    }

    public static void cancel(ExecutionEnvironment execEnv) {
        RemoteUtil.LOGGER.fine("Cancelling loaders for " + execEnv);
        Collection<WeakReference<Loader>> envLoaders;
        synchronized (loadersLock) {
            envLoaders = loaders.get(execEnv);
            loaders.remove(execEnv);
        }
        if (envLoaders != null) {
            for (WeakReference<Loader> ref : envLoaders) {
                Loader loader = ref.get();
                if (loader != null) {
                    RemoteUtil.LOGGER.fine("Cancelling loader " + loader + " for " + execEnv);
                    loader.cancel();
                }
            }
        }
    }

    private static boolean doLoad(final ExecutionEnvironment execEnv, String storagePrefix, 
            Collection<String> paths, ProgressHandle handle, AtomicBoolean cancelled) {
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
        if (cancelled.get()) {
            return false;
        }
        boolean success = false;
        try {
            success = load(tempIncludesStorageFolder.getAbsolutePath(), execEnv, paths, handle, cancelled);
            RemoteUtil.LOGGER.fine("SystemIncludesUtils.doLoad for " + tempIncludesStorageFolder + " finished " + success); // NOI18N
            if (success) {
                RemoteUtil.LOGGER.fine("SystemIncludesUtils.doLoad renaming " + tempIncludesStorageFolder + " to " + includesStorageFolder); // NOI18N
                tempIncludesStorageFolder.renameTo(includesStorageFolder);
            }
        } finally {
            if (!success && includesStorageFolder.exists()) {
                RemoteUtil.LOGGER.fine("SystemIncludesUtils.doLoad removing " + includesStorageFolder + " due to faile"); // NOI18N
                includesStorageFolder.delete();
            }
        }
        return true;
    }
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private static boolean load(String storageFolder, ExecutionEnvironment execEnv, 
            Collection<String> paths, ProgressHandle handle, AtomicBoolean cancelled) {
        handle.switchToDeterminate(3 * paths.size());
        int workunit = 0;
        List<String> cleanupList = new ArrayList<String>();
        boolean success = true;
        for (String path : paths) {
            if (cancelled.get()) {
                success = false;
                break;
            }
            RemoteUtil.LOGGER.fine("SystemIncludesUtils.load loading " + path); // NOI18N
            //TODO: check file existence (or make shell script to rule them all ?)
            String zipRemote = "cnd" + path.replaceAll("(/|\\\\)", "-") + ".zip"; //NOI18N
            String zipRemotePath = "/tmp/" + execEnv.getUser() + '-' + zipRemote; // NOI18N
            String zipLocalPath; 
            File zipLocalFile;
            try {
                zipLocalFile = File.createTempFile(zipRemote, ".zip", new File(tempDir)); // NOI18N
                zipLocalPath = zipLocalFile.getAbsolutePath();
            } catch (IOException ex) {
                zipLocalPath= tempDir + File.separator + zipRemote;
            }

            handle.progress(getMessage("SIU_Archiving") + " " + path, workunit++); // NOI18N
            List <String> appendix = new ArrayList<String>();
            String options = "-r -q"; //NOI18N
            if (RemoteFileSystemsProvider.USE_REMOTE_FS) {
                appendix.add(path);
                appendix.add(path + "/bits"); //NOI18N
                appendix.add(path + "/sys"); //NOI18N
                appendix.add(path + "/gnu"); //NOI18N
                options = "-q"; //NOI18N
            }
            StringBuilder cmdLine = new StringBuilder("zip "); //NOI18N
            cmdLine.append(options);
            cmdLine.append(' '); //NOI18N
            cmdLine.append(zipRemotePath);
            cmdLine.append(' '); //NOI18N
            cmdLine.append(path);
            for( String apx : appendix) {
                cmdLine.append(' '); //NOI18N
                cmdLine.append(apx);
                cmdLine.append("/*");
            }
            RemoteCommandSupport rcs = new RemoteCommandSupport(execEnv, cmdLine.toString());
            rcs.run();

            if (!cancelled.get()) {
                handle.progress(getMessage("SIU_Downloading") + " " + path, workunit++); // NOI18N
                RemoteCopySupport copySupport = new RemoteCopySupport(execEnv);
                copySupport.copyFrom(zipRemotePath, zipLocalPath);
            }

            rcs = new RemoteCommandSupport(execEnv, "rm " + zipRemotePath); //NOI18N
            rcs.run();

            if (cancelled.get()) {
                success = false;
                break;
            }

            handle.progress(getMessage("SIU_Preparing") + " " + path, workunit++); // NOI18N
            unzip(storageFolder, zipLocalPath, appendix);
            cleanupList.add(zipLocalPath);
            RemoteUtil.LOGGER.fine("SystemIncludesUtils.load loading done for " + path); // NOI18N
        }
//        copySupport.disconnect();
        for (String toDelete : cleanupList) {
            new File(toDelete).delete();
        }
        return success;
    }

    private static void unzip(String path, String fileName, List <String> appendix) {
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
            if (RemoteFileSystemsProvider.USE_REMOTE_FS) {
                new File(parent, RemoteFileSupport.FLAG_FILE_NAME).createNewFile();
                for (String apx : appendix) {
                    File dir = new File(parent, apx);
                    if (dir.exists()) {
                        new File(dir, RemoteFileSupport.FLAG_FILE_NAME).createNewFile();
                    }
                }
            }
        } catch (IOException ioe) {
            RemoteUtil.LOGGER.warning("unzipping " + fileName + " to " + path + " failed");
            RemoteUtil.LOGGER.warning(ioe.getMessage());
            return;
        }
        RemoteUtil.LOGGER.fine("unzipping " + fileName + " to " + path + " took " + (System.currentTimeMillis() - start) + " ms");
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

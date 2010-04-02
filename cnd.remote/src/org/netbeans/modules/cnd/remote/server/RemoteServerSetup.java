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

package org.netbeans.modules.cnd.remote.server;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.SetupProvider;
import org.netbeans.modules.cnd.remote.support.RemoteCommandSupport;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * NB: the class is not thread safe!
 * @author gordonp
 */
public class RemoteServerSetup {

    private final Map<String, String> binarySetupMap;
    private final Map<ExecutionEnvironment, List<String>> updateMap;
    private final ExecutionEnvironment executionEnvironment;
    private final Set<String> checkedDirs = new HashSet<String>();
    private boolean cancelled;
    private boolean failed;
    private String reason;
    private String libDir;

    /*package*/ RemoteServerSetup(ExecutionEnvironment executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
        Lookup.Result<SetupProvider> results = Lookup.getDefault().lookup(new Lookup.Template<SetupProvider>(SetupProvider.class));
        Collection<? extends SetupProvider> list = results.allInstances();
        SetupProvider[] providers = list.toArray(new SetupProvider[list.size()]);
        libDir = HostInfoProvider.getLibDir(executionEnvironment); //NB: should contain trailing '/'
        if (!libDir.endsWith("/")) { // NOI18N
            libDir += "/"; // NOI18N
        }
        // Binary setup map
        binarySetupMap = new HashMap<String, String>();
        for (SetupProvider provider : providers) {
            Map<String, String> map = provider.getBinaryFiles(executionEnvironment);
            if (map != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    binarySetupMap.put(libDir + entry.getKey(), entry.getValue());
                }
            }
        }

        updateMap = new HashMap<ExecutionEnvironment, List<String>>();
    }

    /*package*/ boolean needsSetupOrUpdate() {
        List<String> updateList = new ArrayList<String>();
        updateMap.clear();
        if (!isFailedOrCanceled()) {
            updateList = getBinaryUpdates();
        }
        if (isFailedOrCanceled()) {
            return false;
        }

        if (!updateList.isEmpty()) {
            updateMap.put(executionEnvironment, updateList);
            return true;
        } else {
            return false;
        }
    }

    protected  void setup() {
        List<String> list = updateMap.remove(executionEnvironment);
        for (String path : list) {
            RemoteUtil.LOGGER.fine("RSS.setup: Updating \"" + path + "\" on " + executionEnvironment); //NO18N
            if (binarySetupMap.containsKey(path)) {
                String localFileName = binarySetupMap.get(path);
                File file = InstalledFileLocator.getDefault().locate(localFileName, null, false);
                //String remotePath = REMOTE_LIB_DIR + file.getName();
                String remotePath = path;
                try {
                    if (file == null
                            || !file.exists()
                            || !copyTo(file, remotePath)) {
                        throw new Exception();
                    }
                } catch (Exception ex) {
                    setFailed(NbBundle.getMessage(RemoteServerSetup.class, "ERR_UpdateSetupFailure", executionEnvironment, path)); //NOI18N
                }
            }
        }
    }

    private boolean copyTo(File file, String remoteFilePath) throws InterruptedException, ExecutionException {
        int slashPos = remoteFilePath.lastIndexOf('/'); //NOI18N
        if (slashPos >= 0) {
            String remoteDir = remoteFilePath.substring(0, slashPos);
            if (!checkedDirs.contains(remoteDir)) {
                checkedDirs.add(remoteDir);
                String cmd = String.format("sh -c \"if [ ! -d %s ]; then mkdir -p %s; fi\"", remoteDir, remoteDir); // NOI18N
                RemoteCommandSupport.run(executionEnvironment, cmd);
            }
        }
        return CommonTasksSupport.uploadFile(file.getAbsolutePath(), executionEnvironment, remoteFilePath, 0775, null).get() == 0;
    }

    private List<String> getBinaryUpdates() {
        try {
            return getBinaryUpdatesByChecksum();
            // getBinaryUpdatesByExistence(list);
        } catch (CancellationException ex) {
            cancelled = true;
            return Collections.<String>emptyList();
        } catch (NoSuchAlgorithmException ex) {
            RemoteUtil.LOGGER.warning(ex.getMessage());
        } catch (IOException ex) {
            RemoteUtil.LOGGER.log(Level.WARNING, ex.getMessage(), ex);
        } catch (CheckSumException ex) {
            RemoteUtil.LOGGER.warning(ex.getMessage());
        }
        // can't check md5 sums => update them all!
        return new ArrayList<String>(binarySetupMap.keySet());
    }

    private static class CheckSumException extends Exception {
        public CheckSumException(String message) {
            super(message);
        }
    }

    private String getMd5command(List<String> paths2check) 
            throws NoSuchAlgorithmException, IOException, CheckSumException {

        StringBuilder sb;

        HostInfo hostIinfo = HostInfoUtils.getHostInfo(executionEnvironment);
        if (hostIinfo == null) {
            throw new CheckSumException("Can not get HostInfo for " + executionEnvironment); // NOI18N
        }
        final OSFamily oSFamily = hostIinfo.getOSFamily();
        switch (oSFamily) {
            case LINUX:
                sb = new StringBuilder("/usr/bin/md5sum -b"); // NOI18N
                for (String path : paths2check) {
                    sb.append(' ');
                    sb.append(path); // NOI18N
                }
                return sb.toString();
            case SUNOS:
                sb = new StringBuilder("sh -c \""); // NOI18N
                for (String path : paths2check) {
                    sb.append("/usr/bin/digest -a md5 "); // NOI18N
                    sb.append(path);
                    sb.append(";"); // NOI18N
                }
                sb.append("\""); // NOI18N
                return sb.toString();
            default:
                throw new NoSuchAlgorithmException("Unexpected OS: " + oSFamily); // NOI18N
        }
    }

    private List<String> getBinaryUpdatesByChecksum() throws NoSuchAlgorithmException, CancellationException, IOException, CheckSumException {
        // gather 
        // 1) file list separated by spaces
        // 2) an array of paths in the same order
        List<String> paths2check = new ArrayList<String>(binarySetupMap.size());
        for (String path : binarySetupMap.keySet()) {
            paths2check.add(path);
        }
        String cmd = getMd5command(paths2check);

        RemoteCommandSupport support = new RemoteCommandSupport(executionEnvironment, cmd);
        support.run();
        RemoteUtil.LOGGER.fine("RSS.getBinaryUpdatesByChecksum: RC " + support.getExitStatus());
        if (support.isFailed() || support.getExitStatus() != 0) {
            RemoteUtil.LOGGER.fine("Running " + cmd + " failed on remote host: " + support.getFailureReason()); //NOI18N
            return new ArrayList<String>(paths2check);
        }

        if (support.isCancelled()) {
            throw new CancellationException();
        } 
        String val = support.getOutput();
        List<String> result = new ArrayList<String>();
        int idx = 0;
        String[] lines = val.split("\n"); // NOI18N
        if (lines[lines.length-1].equals("")) { // the last one is usuall empthy, throw it away
            String[] corrected = new String[lines.length-1];
            System.arraycopy(lines, 0, corrected, 0, corrected.length);
            lines = corrected;
        }
        if (paths2check.size() != lines.length) {
            throw new CheckSumException(String.format("Incorrect line count: %d, should equal to the amount of files to check: %d", lines.length, paths2check.size())); //NOI18N
        }
        for (String line : lines) { // NOI18N
            // in Linux, it has form ﻿34f1cc1cefbded98edae74d9690a7c44 */usr/include/stdio.h
            if (line.length() > 0) {
                String[] parts = line.split(" "); // NOI18N
                if (parts.length == 0) {
                    throw new CheckSumException("Line shouldn't be empty"); // NOI18N
                }
                String path = paths2check.get(idx++);
                String remoteCheckSum = parts[0];
                String localCheckSum = getLocalChecksum(path);
                RemoteUtil.LOGGER.fine(String.format("Checking %s: remote: %s local: %s", path, localCheckSum, remoteCheckSum));
                if (!remoteCheckSum.equals(localCheckSum)) {
                    result.add(path);
                }
            }
        }
        return result;
    }

    private String getLocalChecksum(String remotePath) throws NoSuchAlgorithmException, FileNotFoundException, IOException {
        String localFileName = binarySetupMap.get(remotePath);
        File file = InstalledFileLocator.getDefault().locate(localFileName, null, false);
        if (file != null && file.exists()) {
            MessageDigest md5 = MessageDigest.getInstance("MD5"); // NOI18N
            InputStream is = new BufferedInputStream(new FileInputStream(file));
            try {
                byte[] buf = new byte[8192];
                int read;
                while ((read = is.read(buf)) != -1) {
                    md5.update(buf, 0, read);
                }
            } finally {
                is.close();
            }
            byte[] checkSum = md5.digest();
            return toHexString(checkSum);
        } else {
            return null;
        }
    }

    private static String toHexString(byte[] data) {
        char[] result = new char[data.length*2];
        for (int i = 0; i < data.length; i++) {
            //buf.append(String.format("%x", data[i]));
            for (int j = 0; j < 2; j++) {
                int half = (j == 0) ? (data[i] & 0x0F0) >>> 4 : data[i] & 0x0F;
                if (0 <= half && half <= 9) {
                    result[2*i+j] = (char) ('0' + half);
                } else {
                    result[2*i+j] = (char) ('a' + (half - 10));
                }
            }
        }
        return new String(result);
    }

    /**
     * Map the reason to a more human readable form. The original reason is currently
     * always in English. This method would need changing were that to change.
     *
     * @return The reason, possibly localized and more readable
     */
    public String getReason() {
        return reason;
    }

    protected boolean isCancelled() {
        return cancelled;
    }

    private void setFailed(String reason) {
        this.failed = true;
        this.reason = reason;
    }

    protected boolean isFailed() {
        return failed;
    }

    private boolean isFailedOrCanceled() {
        return failed || cancelled;
    }
}

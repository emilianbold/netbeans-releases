/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.glassfish.common.wizards;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 *
 * @author Peter Williams
 */
public class Retriever implements Runnable {

    public static final int LOCATION_DOWNLOAD_TIMEOUT = 20000;
    public static final int LOCATION_TRIES = 3;
    public static final int ZIP_DOWNLOAD_TIMEOUT = 120000;
    
    public static final int STATUS_START = 0;
    public static final int STATUS_CONNECTING = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_COMPLETE = 3;
    public static final int STATUS_FAILED = 4;
    public static final int STATUS_TERMINATED = 5;
    public static final int STATUS_BAD_DOWNLOAD = 6;
    
    private static final String [] STATUS_MESSAGE = {
        NbBundle.getMessage(Retriever.class, "STATUS_Ready"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Connecting"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Downloading"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Complete"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Failed"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_Terminated"),  //NOI18N
        NbBundle.getMessage(Retriever.class, "STATUS_InvalidWsdl")  //NOI18N
    };
    private String topLevelPrefix;
    
    public interface Updater {
        public void updateMessageText(String msg);
        public void updateStatusText(String status);
        public void clearCancelState();
    }
    
    private Updater updater;
    private final String locationUrl;
    private final String targetUrlPrefix;
    private final String defaultTargetUrl;
    private File targetInstallDir;
    
    
    public Retriever(File installDir, String locationUrl, String urlPrefix, 
            String defaultTargetUrl, Updater u, String topLevelPrefix) {
        this.targetInstallDir = installDir;
        this.locationUrl = locationUrl;
        this.targetUrlPrefix = urlPrefix;
        this.defaultTargetUrl = defaultTargetUrl;
        this.updater = u;
        this.topLevelPrefix = topLevelPrefix;
    }

    // Thread support for downloading...
    public void stopRetrieval() {
        shutdown = true;
    }
    
    public int getDownloadState() {
        return status;
    }
    
    private void setDownloadState(int newState) {
        setDownloadState(newState, true);
    }
    
    private void setDownloadState(int newState, boolean display) {
        status = newState;
        if(display) {
            updateMessage(STATUS_MESSAGE[newState]);
        }
    }

    private void setDownloadState(int newState, String msg, Exception ex) {
        status = newState;
        Object [] args = new Object [] { msg, ex.getMessage()};
        updateStatus(MessageFormat.format(STATUS_MESSAGE[newState], args));
    }
    
    private void updateMessage(final String msg) {
        updater.updateMessageText(msg);
    }
    
    private void updateStatus(final String status) {
        updater.updateStatusText(status);
    }
    
    private String countAsString(int c) {
        String size = "";  //NOI18N
        if(c < 1024) {
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeBytes", c);  //NOI18N
        } else if(c < 1048676) {
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeKb", c / 1024);  //NOI18N
        } else {
            int m = c / 1048676;
            int d = (c - m * 1048676)*10 / 1048676;
            size = NbBundle.getMessage(Retriever.class, "MSG_SizeMb", m, d);  //NOI18N
        }
        return size;
    }

    // Thread plumbing
    private volatile boolean shutdown;
    private volatile int status;
    
    public void run() {
        // Set name of thread for easier debugging in case of deadlocks, etc.
        Thread.currentThread().setName("Downloader"); // NOI18N
        
        shutdown = false;
        status = STATUS_START;
        URL targetUrl = null;
        URLConnection connection = null;
        InputStream in = null;
        File backupDir = null;
        long start = System.currentTimeMillis();
        String message = null;

        try {
            backupDir = backupInstallDir(targetInstallDir);
            
            setDownloadState(STATUS_CONNECTING);
            targetUrl = new URL(getDownloadLocation());

            Logger.getLogger("glassfish").fine("Downloading from " + targetUrl); // NOI18N
            connection = targetUrl.openConnection();
            connection.setConnectTimeout(ZIP_DOWNLOAD_TIMEOUT);
            connection.setReadTimeout(ZIP_DOWNLOAD_TIMEOUT);
            in = connection.getInputStream();
            setDownloadState(STATUS_DOWNLOADING);
            
            // Download and unzip the V3 archive.
            downloadAndInstall(in, targetInstallDir);
            
            if(!shutdown) {
                long end = System.currentTimeMillis();
                String duration = getDurationString((int) (end - start));
                setDownloadState(STATUS_COMPLETE, false);
                message = NbBundle.getMessage(Retriever.class, "MSG_DownloadComplete", duration); // NOI18N
            } else {
                setDownloadState(STATUS_TERMINATED, false);
                message = NbBundle.getMessage(Retriever.class, "MSG_DownloadCancelled"); // NOI18N
            }
        } catch(ConnectException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Connection Exception", ex); // NOI18N
        } catch(MalformedURLException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Badly formed URL", ex); // NOI18N
        } catch(IOException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "backupDir =="+backupDir);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "connection == "+connection);  //NOI18N
            Logger.getLogger("glassfish").log(Level.FINE, "in == "+in);  //NOI18N
            setDownloadState(STATUS_FAILED, "I/O Exception", ex); // NOI18N
            updateMessage(
                    in != null ?
                        NbBundle.getMessage(Retriever.class, "MSG_FileProblem", connection.getURL()) :
                        NbBundle.getMessage(Retriever.class, "MSG_InvalidUrl", connection.getURL()));
        } catch(RuntimeException ex) {
            Logger.getLogger("glassfish").log(Level.FINE, ex.getLocalizedMessage(), ex);  //NOI18N
            setDownloadState(STATUS_FAILED, "Runtime Exception", ex); // NOI18N
        } finally {
            if(shutdown || status != STATUS_COMPLETE) {
                restoreInstallDir(targetInstallDir, backupDir);
            }
            if(in != null) {
                try { in.close(); } catch(IOException ex) { }
            }
            if(message != null) {
                updateMessage(message);
            }
            updater.clearCancelState();
        }
    }
    
    private String getDownloadLocation() {
        URLConnection conn = null;
        BufferedReader reader = null;
        String result = defaultTargetUrl;

        if(locationUrl != null && locationUrl.length() > 0) {
            int tries = 0;
            while(tries++ < LOCATION_TRIES) {
                try {
                    URL url = new URL(locationUrl);
                    Logger.getLogger("glassfish").fine("Attempt " + tries + " to get download URL suffix from " + url); // NOI18N
                    conn = url.openConnection();
                    conn.setConnectTimeout(LOCATION_DOWNLOAD_TIMEOUT);
                    conn.setReadTimeout(LOCATION_DOWNLOAD_TIMEOUT);
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); // NOI18N
                    while((result = reader.readLine()) != null) {
                        return targetUrlPrefix + result; // Only need the the first line
                    }
                } catch(Exception ex) {
                    Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                } finally {
                    try {
                        if( reader != null) {
                             reader.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex); // NOI18N
                    }
                }
            }
        }
        
        return result;
    }    

    private static final int READ_BUF_SIZE = 131072; // 128k
    private static final int WRITE_BUF_SIZE = 131072; // 128k
    
    private boolean downloadAndInstall(final InputStream in, final File targetFolder) throws IOException {
        BufferedInputStream bufferedStream = null;
        JarInputStream jarStream = null;
        try {
            final byte [] buffer = new byte [WRITE_BUF_SIZE];
            bufferedStream = new BufferedInputStream(in, READ_BUF_SIZE);
            jarStream = new JarInputStream(bufferedStream);
            final InputStream entryStream = jarStream;
            JarEntry entry;
            while(!shutdown && (entry = (JarEntry) jarStream.getNextEntry()) != null) {
                String entryName = stripTopLevelDir(entry.getName());
                if(entryName == null || entryName.length() == 0) {
                    continue;
                }
                final File entryFile = new File(targetFolder, entryName);
                if(entryFile.exists()) {
                    // !PW FIXME entry already exists, offer overwrite option...
                    throw new RuntimeException(NbBundle.getMessage(
                            Retriever.class, "ERR_TargetExists", entryFile.getPath())); // NOI18N
                } else if(entry.isDirectory()) {
                    if(!entryFile.mkdirs()) {
                        throw new RuntimeException(NbBundle.getMessage(
                                Retriever.class, "ERR_FolderCreationFailed", entryFile.getName())); // NOI18N
                    }
                } else {
                    File parentFile = entryFile.getParentFile();
                    if(!parentFile.exists() && !parentFile.mkdirs()) {
                        throw new RuntimeException(NbBundle.getMessage(
                                Retriever.class, "ERR_FolderCreationFailed", parentFile.getName())); // NOI18N
                    }
                    
                    int bytesRead = 0;
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(entryFile);
                        int len;
                        long lastUpdate = 1;
                        while(!shutdown && (len = entryStream.read(buffer)) >= 0) {
                            bytesRead += len;
                            long update = System.currentTimeMillis() / 333;
                            if(update != lastUpdate) {
                                updateMessage(NbBundle.getMessage(Retriever.class,
                                        "MSG_Installing", entryName, countAsString(bytesRead))); // NOI18N
                                lastUpdate = update;
                            }
                            os.write(buffer, 0, len);
                        }
                    } finally {
                        if(os != null) {
                            try { os.close(); } catch(IOException ex) { }
                        }
                    }
                }
            }
        } finally {
            if(bufferedStream != null) {
                try { bufferedStream.close(); } catch(IOException ex) { }
            }
            if(jarStream != null) {
                try { jarStream.close(); } catch(IOException ex) { }
            }
        }

        // execute permissions on script files will be corrected in instantiate()
        return shutdown;
    }
    
    private String stripTopLevelDir(String name) {
        if(name.startsWith(topLevelPrefix)) {
            int slashIndex = slashIndexOf(name, topLevelPrefix.length());
            if(slashIndex >= 0) {
                name = name.substring(slashIndex + 1);
            }
        }
        return name;
    }
    
    private static int slashIndexOf(String s, int offset) {
        int len = s.length();
        for(int i = offset; i < len; i++) {
            char c = s.charAt(i);
            if(c == '/' || c == '\\') {
                return i;
            }
        }
        return -1;
    }
    
    private File backupInstallDir(File installDir) throws IOException {
        if(installDir.exists()) {
            File parent = installDir.getParentFile();
            String tempName = installDir.getName();
            for(int i = 1; i < 100; i++) {
                File target = new File(parent, tempName + i);
                if(!target.exists()) {
                    if(!installDir.renameTo(target)) {
                        throw new IOException(NbBundle.getMessage(Retriever.class,
                                installDir.isDirectory() ? "ERR_FolderCreationFailed" : "ERR_FileCreationFailed",  // NOI18N
                                installDir.getAbsolutePath()));
                    }
                    return target;
                }
            }
            throw new IOException(NbBundle.getMessage(
                    Retriever.class, "ERR_TooManyBackups", installDir.getAbsolutePath())); // NOI18N
        }
        return null;
    }
    
    private void restoreInstallDir(File installDir, File backupDir) {
        if(installDir != null && installDir.exists()) {
            Util.deleteFolder(installDir);
        }

        if(backupDir != null && backupDir.exists()) {
            backupDir.renameTo(installDir);
        }
    }
    
    static String getDurationString(int time) {
        // < 1000 -> XXX ms
        // > 1000 -> XX seconds
        // > 60000 -> XX minutes, XX seconds
        // > 3600000 -> XX hours, XX minutes, XX seconds
        StringBuilder builder = new StringBuilder(100);
        if(time < 0) {
            builder.append(NbBundle.getMessage(Retriever.class, "TIME_ETERNITY"));  //NOI18N
        } else if(time == 0) {
            builder.append(NbBundle.getMessage(Retriever.class, "TIME_NO_TIME"));  //NOI18N
        } else {
            String separator = NbBundle.getMessage(Retriever.class, "TIME_SEPARATOR"); //NOI18N
            if(time >= 3600000) {
                int hours = time / 3600000;
                time %= 3600000;
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_HOURS", hours));  //NOI18N
            }
            if(time >= 60000) {
                if(builder.length() > 0) {
                    builder.append(separator);
                }
                int minutes = time / 60000;
                time %= 60000;
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_MINUTES", minutes));  //NOI18N
            }
            if(time >= 1000) { //  || builder.length() > 0) {
                if(builder.length() > 0) {
                    builder.append(separator);
                }
                int seconds = (time + 500) / 1000;
                time %= 1000;
                if(seconds > 0) {
                    builder.append(NbBundle.getMessage(Retriever.class, "TIME_SECONDS", seconds));  //NOI18N
                }
            } else if (time > 0 && builder.length() < 1) {
                builder.append(NbBundle.getMessage(Retriever.class, "TIME_MILISECONDS", time));  //NOI18N
            }
        }
        
        return builder.toString();
    }
    
}

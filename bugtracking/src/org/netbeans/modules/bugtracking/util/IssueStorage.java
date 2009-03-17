/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.util.IssueCache.IssueEntry;

/**
 *
 * @author Tomas Stupka
 */
class IssueStorage {

    private static IssueStorage instance;
    private File storage;
    private static final String STORAGE_FILE  = "storage";          // NOI18N
    private static final String STORAGE_VERSION = "1.0";            // NOI18N

    private IssueStorage() { }

    public static IssueStorage getInstance() {
        if(instance == null) {
            instance = new IssueStorage();
            instance.initStorage();
        }
        return instance;
    }

    private void initStorage() {
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
        writeStorage();
    }

    public void storeIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        InputStream is = null;
        DataOutputStream dos = null;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            File file = new File(folder, entry.getId());

            dos = getIssueOutputStream(file);
            dos.writeBoolean(entry.wasSeen());
            if(entry.getSeenAttributes() != null) {
                for(Entry<String, String> e : entry.getSeenAttributes().entrySet()) {
                    writeString(dos, e.getKey());
                    writeString(dos, e.getValue());
                }
            }

        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
            try { if(dos != null) dos.close(); } catch (Exception e) {}
            try { if(is != null) is.close(); } catch (Exception e) {}
        }
    }

    public void readIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        DataInputStream is = null;
        try {
            File file = new File(getNameSpaceFolder(nameSpace), entry.getId());
            if(!file.exists()) return;
            is = getIssueInputStream(file);
            Map<String, String> m = new HashMap<String, String>();
            boolean seen = is.readBoolean();
            while(true) {
                try {
                    String key = readString(is);
                    String value = readString(is);
                    m.put(key, value);
                } catch (EOFException e) { // XXX
                    break;
                }
            }
            entry.setSeenAttributes(m);
            entry.setSeen(seen);
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
            if(is != null) try { is.close(); } catch(IOException e) {}
        }
    }

    public List<String> readQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        try {
            File folder = getNameSpaceFolder(nameSpace);
            if(!folder.exists()) return Collections.emptyList();

            DataInputStream dis = getQueryInputStream(folder, queryName);
            if(dis == null) return Collections.emptyList();
            List<String> ids = new ArrayList<String>();
            while(true) {
                String id = null;
                try {
                    id = readString(dis);
                } catch (EOFException e) {
                    break;
                }
                ids.add(id);
            }
            return ids;
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void storeQuery(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        try {
            File folder = getNameSpaceFolder(nameSpace);
            DataOutputStream dos = null;
            try {
                dos = getQueryOutputStream(folder, queryName);
                for (String id : ids) {
                    writeString(dos, id);
                }
                dos.flush();
            } finally {
                try { if(dos != null) dos.close(); } catch (IOException e) {}
            }

        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished storing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    private File getStorageRootFile() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        return new File(new File(userDir, "var"), "bugtracking"); // NOI18N
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = getDataOutputStream(new File(storage, STORAGE_FILE), false);
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (Exception e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
        } finally {
            if (dos != null) {
                try { dos.close(); } catch (IOException e) { }
            }
        }
    }

    private void writeString(DataOutputStream dos, String str) throws IOException {
        if(str != null) {
            dos.writeInt(str.length());
            dos.writeChars(str);
        } else {
            dos.writeInt(0);
        }
    }

    private static String readString(DataInputStream dis) throws IOException {
        int len = dis.readInt();
        if(len == 0) {
            return "";
        }
        StringBuffer sb = new StringBuffer();                
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);                       
        }        
        return sb.toString();
    }

    private DataOutputStream getIssueOutputStream(File file) throws IOException, InterruptedException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(getFileOutputStream(file, false)));
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getIssueInputStream(File file) throws IOException, InterruptedException {
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(getFileInputStream(file)));
        ZipEntry entry = new ZipEntry(file.getName());
        zis.getNextEntry();
        return new DataInputStream(zis);
    }

    private DataOutputStream getDataOutputStream(File file, boolean append) throws IOException, InterruptedException {
        return new DataOutputStream(getFileOutputStream(file, append));
    }

    private DataInputStream getDataInputStream(File file) throws IOException, InterruptedException {
        return new DataInputStream(getFileInputStream(file));
    }

    private FileOutputStream getFileOutputStream(File file, boolean append) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileOutputStream(file, append);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private FileInputStream getFileInputStream(File file) throws IOException, InterruptedException {
        int retry = 0;
        while (true) {
            try {
                return new FileInputStream(file);
            } catch (IOException ioex) {
                retry++;
                if (retry > 7) {
                    throw ioex;
                }
                Thread.sleep(retry * 30);
            }
        }
    }

    private static void copyStreams(OutputStream out, InputStream in) throws IOException {
        byte [] buffer = new byte[4096];
        for (;;) {
            int n = in.read(buffer);
            if (n < 0) break;
            out.write(buffer, 0, n);
        }
    }

    private File getNameSpaceFolder(String url) {
        File folder = new File(storage, encode(url));
        if(!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    private DataOutputStream getQueryOutputStream(File folder, String queryName) throws IOException, InterruptedException {
        File f = new File(folder, encode(queryName));
        return getDataOutputStream(f, false);
    }

    private DataInputStream getQueryInputStream(File folder, String queryName) throws IOException, InterruptedException {
        File f = new File(folder, encode(queryName));
        if(!f.exists()) return null;
        return getDataInputStream(f);
    }

    /**
     * Encodes URI by encoding to %XX escape sequences.
     *
     * @param url url to decode
     * @return decoded url
     */
    private String encode(String url) {
        if (url == null) return null;
        StringBuffer sb = new StringBuffer(url.length());

        for (int i = 0; i < url.length(); i++) {
            char c = url.charAt(i);
            if (!isAlowedChar(c)) {
                sb.append('%');
                sb.append(Integer.toHexString(c).toUpperCase());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isAlowedChar(char c) {
        return c >= '0' && c <= '9' ||
               c >= 'A' && c <= 'Z' ||
               c >= 'a' && c <= 'z' ||
               c == '.' ||
               c == '_';
    }

}

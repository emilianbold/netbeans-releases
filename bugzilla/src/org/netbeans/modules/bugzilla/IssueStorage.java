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

package org.netbeans.modules.bugzilla;

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

/**
 *
 * XXX need cleanup
 * @author Tomas Stupka
 */
public class IssueStorage {

    private static IssueStorage instance;
    private File storage;
    private static final String STORAGE_FILE  = "storage";          // NOI18N
    private static final String STORAGE_VERSION = "1.0";            // NOI18N
    private final static Map<String, Map<String, String>> EMPTY = new HashMap<String, Map<String, String>>();

    private IssueStorage() { }

    public static IssueStorage getInstance() {
        if(instance == null) {
            instance = new IssueStorage();
            instance.initStorage();
        }
        return instance;
    }

    public void storeIssue(String repoUrl, String issueID, boolean seen, Map<String, String> attrs) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt";
        File repoFolder = getRepoFolder(repoUrl);
        try {
            storeIssueAtributes(repoFolder, issueID, seen, attrs);
        } catch (InterruptedException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    public Map<String, String> readIssue(String repoUrl, String id) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt";
        File repoFolder = getRepoFolder(repoUrl);
        try {
            return readIssue(repoFolder, id);
        } catch (InterruptedException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    public List<String> readQuery(String repoUrl, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt";
        File repoFolder = getRepoFolder(repoUrl);
        if(!repoFolder.exists()) return Collections.EMPTY_LIST;

        try {
            DataInputStream dis = getQueryInputStream(repoFolder, queryName);
            if(dis == null) return Collections.EMPTY_LIST;
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
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    void storeQuery(String repoUrl, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt";
        File repoFolder = getRepoFolder(repoUrl);

        try {
            DataOutputStream dos = null;
            try {
                dos = getQueryOutputStream(repoFolder, queryName);
                for (String id : ids) {
                    writeString(dos, id);
                }
                dos.flush();
            } finally {
                try { if(dos != null) dos.close(); } catch (IOException e) {}
            }

        } catch (InterruptedException ex) {
            Bugzilla.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        }
    }

    private Map<String, String> readIssue(File repoFolder, String id) throws IOException, InterruptedException {
        File file = new File(repoFolder, id);
        if(!file.exists()) return null;
        DataInputStream is = getIssueInputStream(file);
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
        m.put("seen", seen ? "1" : "0"); // XXX ugly
        return m;
    }

    private void initStorage() {
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
        writeStorage();
    }

    private File getStorageRootFile() {
        String userDir = System.getProperty("netbeans.user"); // NOI18N
        return new File(new File(new File(userDir, "var"), "bugtracking"), "bugzilla"); // NOI18N
    }

    private void storeIssueAtributes(File repoFolder, String issueID, boolean seen, Map<String, String> attrs) throws IOException, InterruptedException {
        File file = new File(repoFolder, issueID);
        // XXX hold lock on issue files or synchronize whole storage?
        InputStream is = null;
        DataOutputStream dos = null;
        try {
            dos = getIssueOutputStream(file);
            dos.writeBoolean(seen);
            if(attrs != null) {
                for(Entry<String, String> entry : attrs.entrySet()) {
                    writeString(dos, entry.getKey());
                    writeString(dos, entry.getValue());
                }
            }
        } finally {
            try { if(dos != null) dos.close(); } catch (Exception e) {}
            try { if(is != null) is.close(); } catch (Exception e) {}
        }
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = getDataOutputStream(new File(storage, STORAGE_FILE), false);
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (Exception e) {
            Bugzilla.LOG.log(Level.INFO, null, e);
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

    private File getRepoFolder(String url) {
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
            if (!isCharOrDigit(c)) {
                sb.append('%');
                sb.append(Integer.toHexString(c).toUpperCase());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isCharOrDigit(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

}

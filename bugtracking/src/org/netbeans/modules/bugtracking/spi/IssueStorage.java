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

package org.netbeans.modules.bugtracking.spi;

import org.netbeans.modules.bugtracking.util.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.BugtrackingConfig;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.IssueCache.IssueEntry;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Tomas Stupka
 */
class IssueStorage {

    private static IssueStorage instance;
    private File storage;
    private static final String STORAGE_FILE  = "storage";          // NOI18N
    private static final String STORAGE_VERSION = "1.0";            // NOI18N
    private String QUERY_ARCHIVED_SUFIX = ".qa";                    // NOI18N
    private String QUERY_SUFIX = ".q";                              // NOI18N
    private String ISSUE_SUFIX = ".i";                              // NOI18N

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
        Task t = BugtrackingManager.getInstance().getRequestProcessor().create(new Runnable() {
            public void run() {
                cleanup();
            }
        });
        t.schedule(0);
    }

    public void storeIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        InputStream is = null;
        DataOutputStream dos = null;
        try {
            dos = getIssueOutputStream(getNameSpaceFolder(nameSpace), entry.getId());
            if(dos == null) {
                return;
            }
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
            try { if(dos != null) dos.close(); } catch (IOException e) {}
            try { if(is != null) is.close(); } catch (IOException e) {}
        }
    }

    public void readIssue(String nameSpace, IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        DataInputStream is = null;
        try {          
            is = getIssueInputStream(getNameSpaceFolder(nameSpace), entry.getId());
            if(is == null) {
                return;
            }
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

            DataInputStream dis = getQueryInputStream(folder, queryName, false);
            return readQuery(dis);
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    private List<String> readQuery(DataInputStream dis) throws IOException {
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
    }

    public Map<String, Long> readArchivedQueryIssues(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        long ttl = BugtrackingConfig.getInstance().getArchivedIssuesTTL() * 1000 * 60 * 60 * 24;
        try {
            File folder = getNameSpaceFolder(nameSpace);
            if(!folder.exists()) return Collections.emptyMap();

            DataInputStream dis = getQueryInputStream(folder, queryName, true);
            if(dis == null) return Collections.emptyMap();
            Map<String, Long> ids = readArchivedQueryIssues(dis);
            Iterator<String> it = ids.keySet().iterator();
            while(it.hasNext()) {
                String id = it.next();
                long ts = ids.get(id);
                if(ts < now - ttl) {
                    it.remove();
                }
            }
            return ids;
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            IOException ioe = new IOException(ex.getMessage());
            ioe.initCause(ex);
            throw ioe;
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    private Map<String, Long> readArchivedQueryIssues(DataInputStream dis) throws IOException {
        if(dis == null) return Collections.emptyMap();
        Map<String, Long> ids = new HashMap<String, Long>();
        while(true) {
            String id = null;
            long ts = -1;
            try {
                id = readString(dis);
                ts = dis.readLong();
                ids.put(id, ts);
            } catch (EOFException e) {
                break;
            }
        }
        return ids;
    }

    public void removeQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        try {
            File folder = getNameSpaceFolder(nameSpace);
            File query = new File(folder, encode(queryName) + QUERY_SUFIX);
            if(query.exists()) {
                BugtrackingUtil.deleteRecursively(query);
            }
            File queryArchived = new File(folder, encode(queryName) + QUERY_ARCHIVED_SUFIX);
            if(queryArchived.exists()) {
                BugtrackingUtil.deleteRecursively(queryArchived);
            }
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void storeQuery(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        try {
            File folder = getNameSpaceFolder(nameSpace);
            DataOutputStream dos = null;
            try {
                dos = getQueryOutputStream(folder, queryName, false);
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
            BugtrackingManager.LOG.log(Level.FINE, "finished storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void storeArchivedQueryIssues(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing archevid query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        Map<String, Long> archived = readArchivedQueryIssues(nameSpace, queryName);
        try {
            File folder = getNameSpaceFolder(nameSpace);
            DataOutputStream dos = null;
            try {
                dos = getQueryOutputStream(folder, queryName, true);
                for (String id : ids) {
                    writeString(dos, id);
                    Long ts = archived.get(id);
                    if(ts != null && ts.longValue() != -1) {
                        dos.writeLong(ts);
                    } else {
                        dos.writeLong(now);
                    }
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
            BugtrackingManager.LOG.log(Level.FINE, "finished storing archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void cleanup() {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup"); // NOI18N

            File root = getStorageRootFile();
            File[] repos = root.listFiles();
            if(repos == null) {
                return;
            }
            for (File repo : repos) {
                cleanup(repo);
            }
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finnished bugtrackig storage cleanup"); // NOI18N
        }
    }

    void cleanup(String namespace) {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup for {0}", new Object[] {namespace}); // NOI18N
            cleanup(getNameSpaceFolder(namespace));
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finnished bugtrackig storage cleanup for {0}", new Object[] {namespace}); // NOI18N
        }
    }


    private void cleanup(File repo) {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup for {0}", new Object[] {repo.getAbsoluteFile()}); // NOI18N
            Set<String> livingIssues = new HashSet<String>();
            File[] queries = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_SUFIX);
                }
            });
            if(queries != null && queries.length > 0) {
                for (File lq : queries) {
                    List<String> ids = readQuery(getDataInputStream(lq));
                    if(ids == null || ids.size() == 0) {
                        continue;
                    }
                    livingIssues.addAll(ids);
                }
            }
            queries = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_ARCHIVED_SUFIX);
                }
            });
            if(queries != null) {
                for (File lq : queries) {
                    Map<String, Long> ids = readArchivedQueryIssues(getDataInputStream(lq));
                    if(ids == null || ids.size() == 0) {
                        continue;
                    }
                    livingIssues.addAll(ids.keySet());
                }
            }
            BugtrackingManager.LOG.log(Level.FINER, "living query issues {0}", new Object[] {livingIssues}); // NOI18N
            File[] issues = repo.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(ISSUE_SUFIX);
                }
            });
            if(issues != null) {
                for (File issue : issues) {
                    String id = issue.getName();
                    id = id.substring(0, id.length() - ISSUE_SUFIX.length());
                    if(!livingIssues.contains(id)) {
                        BugtrackingManager.LOG.log(Level.FINE, "removing issue {0}", new Object[] {id}); // NOI18N
                        issue.delete();
                    }
                }
            }
            
        } catch (IOException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex); // NOI18N
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex); // NOI18N
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished bugtrackig storage cleanup for {0}", new Object[] {repo.getAbsoluteFile()}); // NOI18N
        }
    }

    private File getStorageRootFile() {
        String userDir = System.getProperty("netbeans.user");                   // NOI18N
        return new File(new File(userDir, "var"), "bugtracking");               // NOI18N
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = getDataOutputStream(new File(storage, STORAGE_FILE), false);
            writeString(dos, STORAGE_VERSION);
            dos.flush();
        } catch (IOException e) {
            BugtrackingManager.LOG.log(Level.INFO, null, e);
        } catch (InterruptedException ie) {
            BugtrackingManager.LOG.log(Level.INFO, null, ie);
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
            return "";                                                          // NOI18N
        }
        StringBuffer sb = new StringBuffer();                
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);                       
        }        
        return sb.toString();
    }

    private DataOutputStream getIssueOutputStream(File folder, String id) throws IOException, InterruptedException {
        File file = new File(folder, id + ISSUE_SUFIX);
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(getFileOutputStream(file, false)));
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getIssueInputStream(File folder, String id) throws IOException, InterruptedException {
        File file = new File(folder, id + ISSUE_SUFIX);
        if(!file.exists()) return null;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(getFileInputStream(file)));
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

    private DataOutputStream getQueryOutputStream(File folder, String queryName, boolean archived) throws IOException, InterruptedException {
        File f = new File(folder, encode(queryName) + (archived ? QUERY_ARCHIVED_SUFIX : QUERY_SUFIX));
        return getDataOutputStream(f, false);
    }

    private DataInputStream getQueryInputStream(File folder, String queryName, boolean archived) throws IOException, InterruptedException {
        File f = new File(folder, encode(queryName) + (archived ? QUERY_ARCHIVED_SUFIX : QUERY_SUFIX));
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
                sb.append('%');                                                 // NOI18N
                sb.append(Integer.toHexString(c).toUpperCase());
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static boolean isAlowedChar(char c) {
        return c >= '0' && c <= '9' ||                                          // NOI18N
               c >= 'A' && c <= 'Z' ||                                          // NOI18N
               c >= 'a' && c <= 'z' ||                                          // NOI18N
               c == '.' ||                                                      // NOI18N
               c == '_';                                                        // NOI18N
    }

}

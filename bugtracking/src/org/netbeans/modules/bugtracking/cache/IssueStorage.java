/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.bugtracking.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.util.TextUtils;
import org.openide.util.RequestProcessor.Task;
import org.openide.modules.Places;

/**
 *
 * @author Tomas Stupka
 */
class IssueStorage {

    private static IssueStorage instance;
    private final File storage;
    private static final String STORAGE_FILE  = "storage";              // NOI18N
    private static final String STORAGE_VERSION_1_0 = "1.0";            // NOI18N
    private static final String STORAGE_VERSION_1_1 = "1.1";            // NOI18N
    private static final String STORAGE_VERSION = STORAGE_VERSION_1_1;  // NOI18N
    private final static String QUERY_ARCHIVED_SUFIX = ".qa";           // NOI18N
    private final static String QUERY_SUFIX = ".q";                     // NOI18N
    private final static String ISSUE_SUFIX = ".i";                     // NOI18N

    private IssueStorage() { 
        storage = getStorageRootFile();
        if(!storage.exists()) {
            storage.mkdirs();
        }
        writeStorage();
        Task t = BugtrackingManager.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                cleanup();
            }
        });
        t.schedule(0);
    }

    public static IssueStorage getInstance() {
        if(instance == null) {
            instance = new IssueStorage();
        }
        return instance;
    }

    long getReferenceTime(String nameSpace) throws IOException {
        File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
        File data = new File(folder, "data");                                   // NOI18N
        
        StorageUtils.FileLocks.FileLock lock = StorageUtils.FileLocks.getLock(data);
        try {
            synchronized(lock) {
                final File parentFile = data.getParentFile();
                if(!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                int retry = 0;
                while(true) {
                    try {
                        return getReferenceTimeIntern(data, nameSpace, folder); 
                    } catch (EOFException ex) {
                        BugtrackingManager.LOG.log(Level.SEVERE, data.getAbsolutePath(), ex);
                        return -1;
                    } catch (InterruptedException ex) {
                        BugtrackingManager.LOG.log(Level.WARNING, null, ex);
                        throw new IOException(ex);
                    } catch (IOException ex) {
                        retry++;
                        if (retry > 7) {
                            BugtrackingManager.LOG.log(Level.WARNING, "could not access storage data file {0}", data.getAbsolutePath()); // NOI18N
                            throw ex;
                        }
                        try {
                            Thread.sleep(retry * 34);
                        } catch (InterruptedException iex) {
                            throw ex;
                        }
                    }
                }
            }
        } finally {
            if(lock != null) { lock.release(); }
        }
    }

    private long getReferenceTimeIntern(File data, String nameSpace, File folder) throws IOException, InterruptedException {
        long ret = -1;
        if(data.exists()) {
            DataInputStream is = null;
            try {
                is = StorageUtils.getDataInputStream(data);
                ret = is.readLong();
                return ret;
            } finally {
                if(BugtrackingManager.LOG.isLoggable(Level.FINE)) {
                    String dateString = ret > -1 ? new SimpleDateFormat().format(new Date(ret)) : "null";   // NOI18N
                    BugtrackingManager.LOG.log(Level.FINE, "finished reading greference time {0} - {1}", new Object[] {nameSpace, dateString}); // NOI18N
                }
                try { if(is != null) is.close(); } catch (IOException e) {}
            }
        } else {
            if(!folder.exists()) {
                folder.mkdirs();
            }
            data.createNewFile();
            ret = System.currentTimeMillis();
            DataOutputStream os = null;
            try {
                os = StorageUtils.getDataOutputStream(data, false);
                os.writeLong(ret);
                return ret;
            } finally {
                if(BugtrackingManager.LOG.isLoggable(Level.FINE)) {
                    String dateString = ret > -1 ? new SimpleDateFormat().format(new Date(ret)) : "null";   // NOI18N
                    BugtrackingManager.LOG.log(Level.FINE, "finished writing greference time {0} - {1}", new Object[] {nameSpace, dateString}); // NOI18N
                }
                try { if(os != null) os.close(); } catch (IOException e) {}
            }
        }
    }

    void storeIssue(String nameSpace, IssueCache.IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        InputStream is = null;
        DataOutputStream dos = null;
        StorageUtils.FileLocks.FileLock lock = null;
        try {
            File issueFile = getIssueFile(StorageUtils.getNameSpaceFolder(storage, nameSpace), entry.getId());
            lock = StorageUtils.FileLocks.getLock(issueFile);
            synchronized(lock) {
                dos = getIssueOutputStream(issueFile);
                if(dos == null) {
                    return;
                }
                dos.writeBoolean(entry.wasSeen());
                dos.writeLong(entry.getLastSeenModified());
                dos.writeInt(entry.getLastUnseenStatus().getVal());
                if(entry.getSeenAttributes() != null) {
                    Map<String, String> sa = entry.getSeenAttributes();
                    for(Entry<String, String> e : sa.entrySet()) {
                        writeString(dos, e.getKey());
                        writeString(dos, e.getValue());
                    }
                }
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            try { if(dos != null) dos.close(); } catch (IOException e) {}
            try { if(is != null) is.close(); } catch (IOException e) {}
            if(lock != null) {
                lock.release();
            }
            BugtrackingManager.LOG.log(Level.FINE, "finished storing issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        }
    }

    void readIssue(String nameSpace, IssueCache.IssueEntry entry) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        DataInputStream is = null;
        StorageUtils.FileLocks.FileLock lock = null;
        try {
            File issueFile = getIssueFile(StorageUtils.getNameSpaceFolder(storage, nameSpace), entry.getId());
            lock = StorageUtils.FileLocks.getLock(issueFile);
            synchronized(lock) {
                is = getIssueInputStream(issueFile);
                if(is == null) {
                    return;
                }
                Map<String, String> m = new HashMap<String, String>();
                boolean seen = is.readBoolean();
                long lastModified = -1;
                IssueCache.Status lastStatus = IssueCache.Status.ISSUE_STATUS_UNKNOWN;
                if(!STORAGE_VERSION.equals(STORAGE_VERSION_1_0)) {
                    lastModified = is.readLong();
                    int i = is.readInt();
                    for(IssueCache.Status s : IssueCache.Status.values()) {
                        if(s.getVal() == i) {
                            lastStatus = s;
                            break;
                        }
                    }
                    if(i != IssueCache.Status.ISSUE_STATUS_UNKNOWN.getVal() && 
                       lastStatus == IssueCache.Status.ISSUE_STATUS_UNKNOWN) 
                    {
                        assert false : "there is no Status value for " + i; // NOI18N
                    }
                }
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
                entry.setLastSeenModified(lastModified);
                entry.setLastUnseenStatus(lastStatus);
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            if(is != null) try { is.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
            BugtrackingManager.LOG.log(Level.FINE, "finished reading issue {0} - {1}", new Object[] {nameSpace, entry.getId()}); // NOI18N
        }
    }

    List<String> readQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N

        DataInputStream dis = null;
        StorageUtils.FileLocks.FileLock lock = null;
        try {
            File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
            if(!folder.exists()) return Collections.emptyList();

            File f = getQueryFile(folder, queryName, false);
            lock = StorageUtils.FileLocks.getLock(f);
            synchronized(lock) {
                dis = StorageUtils.getQueryInputStream(f);
                return readQuery(dis);
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dis != null) try { dis.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    private List<String> readQuery(DataInputStream dis) throws IOException {
        if(dis == null) return Collections.emptyList();
        List<String> ids = new ArrayList<String>();
        while(true) {
            String id;
            try {
                id = readString(dis);
            } catch (EOFException e) {
                    break;
            }
            ids.add(id);
        }
        return ids;
    }

    long getQueryTimestamp(String nameSpace, String name) {
        File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
        File file = new File(folder, TextUtils.encodeURL(name) + QUERY_SUFIX);
        return file.lastModified();
    }

    Map<String, Long> readArchivedQueryIssues(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        long ttl = BugtrackingConfig.getInstance().getArchivedIssuesTTL() * 1000 * 60 * 60 * 24;

        StorageUtils.FileLocks.FileLock lock = null;
        DataInputStream dis = null;
        try {
            File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
            if(!folder.exists()) return Collections.emptyMap();

            File f = getQueryFile(folder, queryName, true);
            lock = StorageUtils.FileLocks.getLock(f);
            synchronized(lock) {
                dis = StorageUtils.getQueryInputStream(f);
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
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished reading archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dis != null) try { dis.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    private Map<String, Long> readArchivedQueryIssues(DataInputStream dis) throws IOException {
        if(dis == null) return Collections.emptyMap();
        Map<String, Long> ids = new HashMap<String, Long>();
        while(true) {
            try {
                String id = readString(dis);
                long ts = dis.readLong();
                ids.put(id, ts);
            } catch (EOFException e) {
                break;
            }
        }
        return ids;
    }

    void removeQuery(String nameSpace, String queryName) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        try {
            StorageUtils.FileLocks.FileLock lock;
            File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
            File query = getQueryFile(folder, queryName, false);
            if(query.exists()) {
                lock = StorageUtils.FileLocks.getLock(query);
                try {
                    synchronized(lock) {
                        BugtrackingUtil.deleteRecursively(query);
                    }
                } finally {
                    if(lock != null) { lock.release(); }
                }
            }
            lock = null;
            File queryArchived = getQueryFile(folder, queryName, true);
            if(queryArchived.exists()) {
                lock = StorageUtils.FileLocks.getLock(queryArchived);
                try {
                    synchronized(lock) {
                        BugtrackingUtil.deleteRecursively(queryArchived);
                    }
                } finally {
                    if(lock != null) {lock.release();}
                }
            }
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished removing query {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        }
    }

    void storeQuery(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        StorageUtils.FileLocks.FileLock lock = null;
        DataOutputStream dos = null;
        try {
            File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
            File f = getQueryFile(folder, queryName, false);
            lock = StorageUtils.FileLocks.getLock(f);
            synchronized(lock) {
                dos = StorageUtils.getQueryOutputStream(f);
                for (String id : ids) {
                    writeString(dos, id);
                }
                dos.flush();
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finished storing query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
            if(dos != null) try { dos.close(); } catch(IOException e) {}
            if(lock != null) {
                lock.release();
            }
        }
    }

    void storeArchivedQueryIssues(String nameSpace, String queryName, String[] ids) throws IOException {
        assert !SwingUtilities.isEventDispatchThread() : "should not access the issue storage in awt"; // NOI18N
        BugtrackingManager.LOG.log(Level.FINE, "start storing archived query issues {0} - {1}", new Object[] {nameSpace, queryName}); // NOI18N
        long now = System.currentTimeMillis();
        Map<String, Long> archived = readArchivedQueryIssues(nameSpace, queryName);
        DataOutputStream dos = null;
        StorageUtils.FileLocks.FileLock lock = null;
        try {
            File folder = StorageUtils.getNameSpaceFolder(storage, nameSpace);
            File f = getQueryFile(folder, queryName, true);
            lock = StorageUtils.FileLocks.getLock(f);
            synchronized(lock) {
                dos = StorageUtils.getQueryOutputStream(f);
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
            }
        } catch (InterruptedException ex) {
            BugtrackingManager.LOG.log(Level.WARNING, null, ex);
            throw new IOException(ex);
        } finally {
            try { if(dos != null) dos.close(); } catch (IOException e) {}
            if(lock != null) {
                lock.release();
            }
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
            cleanup(StorageUtils.getNameSpaceFolder(storage, namespace));
        } finally {
            BugtrackingManager.LOG.log(Level.FINE, "finnished bugtrackig storage cleanup for {0}", new Object[] {namespace}); // NOI18N
        }
    }

    private void cleanup(File repo) {
        try {
            BugtrackingManager.LOG.log(Level.FINE, "starting bugtrackig storage cleanup for {0}", new Object[] {repo.getAbsoluteFile()}); // NOI18N
            Set<String> livingIssues = new HashSet<String>();
            File[] queries = repo.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_SUFIX);
                }
            });
            if(queries != null && queries.length > 0) {
                for (File lq : queries) {
                    StorageUtils.FileLocks.FileLock lock = StorageUtils.FileLocks.getLock(lq);
                    List<String> ids;
                    try {
                        synchronized(lock) {
                            ids = readQuery(StorageUtils.getDataInputStream(lq));
                        }
                    } finally {
                        if(lock != null) lock.release();
                    }
                    if(ids == null || ids.isEmpty()) {
                        continue;
                    }
                    livingIssues.addAll(ids);
                }
            }
            queries = repo.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(QUERY_ARCHIVED_SUFIX);
                }
            });
            if(queries != null) {
                for (File lq : queries) {
                    Map<String, Long> ids;
                    StorageUtils.FileLocks.FileLock lock = StorageUtils.FileLocks.getLock(lq);
                    try {
                        synchronized(lock) {
                            ids = readArchivedQueryIssues(StorageUtils.getDataInputStream(lq));
                        }
                    } finally {
                        if(lock != null) lock.release();
                    }
                    if(ids == null || ids.isEmpty()) {
                        continue;
                    }
                    livingIssues.addAll(ids.keySet());
                }
            }
            BugtrackingManager.LOG.log(Level.FINER, "living query issues {0}", new Object[] {livingIssues}); // NOI18N
            File[] issues = repo.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(ISSUE_SUFIX);
                }
            });
            if(issues != null) {
                for (File issue : issues) {
                    StorageUtils.FileLocks.FileLock lock = StorageUtils.FileLocks.getLock(issue);
                    try {
                        String id = issue.getName();
                        id = id.substring(0, id.length() - ISSUE_SUFIX.length());
                        synchronized(lock) {
                            if(!livingIssues.contains(id)) {
                                BugtrackingManager.LOG.log(Level.FINE, "removing issue {0}", new Object[] {id}); // NOI18N
                                issue.delete();
                            }
                        }
                    } finally {
                        if(lock != null) lock.release();
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
        return new File(new File(Places.getUserDirectory(), "var"), "bugtracking");               // NOI18N
    }

    private void writeStorage() {
        DataOutputStream dos = null;
        try {
            dos = StorageUtils.getDataOutputStream(new File(storage, STORAGE_FILE), false);
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
        StringBuilder sb = new StringBuilder();                
        while(len-- > 0) {
            char c = dis.readChar();
            sb.append(c);                       
        }        
        return sb.toString();
    }

    private DataOutputStream getIssueOutputStream(File issueFile) throws IOException, InterruptedException {
        ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(StorageUtils.getFileOutputStream(issueFile, false)));
        ZipEntry entry = new ZipEntry(issueFile.getName());
        zos.putNextEntry(entry);
        return new DataOutputStream(zos);
    }

    private DataInputStream getIssueInputStream(File file) throws IOException, InterruptedException {
        if(!file.exists()) return null;
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(StorageUtils.getFileInputStream(file)));
        zis.getNextEntry();
        return new DataInputStream(zis);
    }

    private File getIssueFile(File folder, String id) {
        return new File(folder, id + ISSUE_SUFIX);
    }
    
    private File getQueryFile(File folder, String queryName, boolean archived){
        return new File(folder, TextUtils.encodeURL(queryName) + (archived ? QUERY_ARCHIVED_SUFIX : QUERY_SUFIX));
    }
    
}

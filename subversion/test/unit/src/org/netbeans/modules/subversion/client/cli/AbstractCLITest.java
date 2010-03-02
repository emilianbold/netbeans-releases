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

package org.netbeans.modules.subversion.client.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.subversion.AbstractSvnTest;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.versioning.util.FileUtils;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
//import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 *
 * @author tomas
 */
public abstract class AbstractCLITest extends AbstractSvnTest {
    
    protected boolean importWC;
    protected String CI_FOLDER = "cifolder";    
    protected FileNotifyListener fileNotifyListener;
    
    public AbstractCLITest(String testName) throws Exception {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {        
        super.setUp();
        if(getName().startsWith("testCheckout") ) {
            cleanUpRepo(new String[] {CI_FOLDER});
        }        
        //CmdLineClientAdapterFactory.setup();
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(getName().startsWith("testInfoLocked")) { 
            try {
                unlock(createFile("lockfile"), "unlock", true);
            } catch (Exception e) {
                // ignore
            }
        }
        super.tearDown();
    }    
 

    protected void setAnnonWriteAccess() throws IOException {
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"), new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf.bk"));
        write(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"), "[general]\nanon-access = write\nauth-access = write\nauthz-db = authz");
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/authz"), new File(getRepoDir().getAbsolutePath() + "/conf/authz.bk"));        
        write(new File(getRepoDir().getAbsolutePath() + "/conf/authz"), "[/]\n* = rw");
    }

    protected void restoreAuthSettings() throws IOException {
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf.bk"), new File(getRepoDir().getAbsolutePath() + "/conf/svnserve.conf"));
        FileUtils.copyFile(new File(getRepoDir().getAbsolutePath() + "/conf/authz.bk"), new File(getRepoDir().getAbsolutePath() + "/conf/authz"));        
    }
    
    protected void assertInfos(ISVNInfo info, ISVNInfo refInfo) {
        assertNotNull(info);   
        assertNotNull(refInfo);   
        assertEquals(refInfo.getCopyRev(), info.getCopyRev());
        assertEquals(refInfo.getCopyUrl(), info.getCopyUrl());
        assertEquals(refInfo.getFile(), info.getFile());
        assertEquals(refInfo.getLastChangedDate(), info.getLastChangedDate());
        assertEquals(refInfo.getLastChangedRevision(), info.getLastChangedRevision());
        assertEquals(refInfo.getLastCommitAuthor(), info.getLastCommitAuthor());
        assertEquals(refInfo.getLastDatePropsUpdate(), info.getLastDatePropsUpdate());
        assertEquals(refInfo.getLastDateTextUpdate(), info.getLastDateTextUpdate());
        assertEquals(refInfo.getLockComment() != null ? refInfo.getLockComment().trim() : null, 
                     info.getLockComment() != null    ? info.getLockComment().trim()    : null);
        assertEquals(refInfo.getLockCreationDate(), info.getLockCreationDate());
        assertEquals(refInfo.getLockOwner(), info.getLockOwner());
        assertEquals(refInfo.getNodeKind(), info.getNodeKind());
        assertEquals(refInfo.getRepository(), info.getRepository());
        assertEquals(refInfo.getRevision(), info.getRevision());
        assertEquals(refInfo.getSchedule(), info.getSchedule());
        assertEquals(refInfo.getUrl(), info.getUrl());
        assertEquals(refInfo.getUrlString(), info.getUrlString());
        assertEquals(refInfo.getUuid(), info.getUuid());
    }
    
    protected void assertEntryArrays(ISVNDirEntry[] listedArray, ISVNDirEntry[] refArray) {
        assertEquals(listedArray.length, refArray.length);
        Map<String, ISVNDirEntry> entriesMap = new HashMap<String, ISVNDirEntry>();
        for (ISVNDirEntry e : listedArray) {
            entriesMap.put(e.getPath(), e);
        }
        ISVNDirEntry entry;
        for (int i = 0; i < refArray.length; i++) {
            entry = entriesMap.get(refArray[i].getPath());

            assertNotNull(entry);
            assertEquals(refArray[i].getPath(), entry.getPath());
            assertEquals(refArray[i].getHasProps(), entry.getHasProps());
            assertEquals(refArray[i].getLastChangedRevision(), entry.getLastChangedRevision());
            assertEquals(refArray[i].getLastCommitAuthor(), entry.getLastCommitAuthor());
            assertEquals(refArray[i].getNodeKind(), entry.getNodeKind());
            assertEquals(refArray[i].getSize(), entry.getSize());
            assertEquals(refArray[i].getLastChangedDate(), entry.getLastChangedDate());
        }
    }
        
    protected void assertNotifiedFiles(File... files) {
        Set<File> notifiedFiles = fileNotifyListener.getFiles();
        
        if(files.length != notifiedFiles.size()) {
            String prefix = getWC().getAbsolutePath();
            StringBuffer sb = new StringBuffer();
            sb.append("Expected files: \n");
            for (File file : files) {
                sb.append("\t");
                sb.append(file.getAbsolutePath().substring(prefix.length() + 1));
                sb.append("\n");
            }
            sb.append("Notified files: \n");
            for (File file : notifiedFiles) {
                sb.append("\t");
                sb.append(file.getAbsolutePath().substring(prefix.length() + 1));
                sb.append("\n");
            }    
            String l = sb.toString();
            Subversion.LOG.warning("assertNotifiedFiles: \n" + l);
            fail(l);
        }
        for (File f : files) {
            if(!notifiedFiles.contains(f)) fail("missing notification for file " + f);   
        }        
    }

    protected class FileNotifyListener implements ISVNNotifyListener {
        private Set<File> files = new HashSet<File>();
        public void setCommand(int arg0) { }
        public void logCommandLine(String arg0) { }
        public void logMessage(String arg0) { }
        public void logError(String arg0) { }
        public void logRevision(long arg0, String arg1) { }
        public void logCompleted(String arg0) { }
        public void onNotify(File file, SVNNodeKind arg1) {
            files.add(file);
        }
        public Set<File> getFiles() {            
            return files;
        }        
    }
        
    protected File createFolder(String name) throws IOException {
        File file = new File(getWC(), name);
        file.mkdirs();
        return file;
    }
    
    protected File createFolder(File folder, String name) throws IOException {
        File file = new File(folder, name);
        file.mkdirs();
        return file;
    }
    
    protected File createFile(File folder, String name) throws IOException {
        File file = new File(folder, name);
        file.createNewFile();
        return file;
    }
    
    protected File createFile(String name) throws IOException {
        File file = new File(getWC(), name);
        file.createNewFile();
        return file;
    }

    protected ISVNClientAdapter getNbClient() throws Exception {        
        ISVNClientAdapter c = new CommandlineClient();        
        fileNotifyListener = new FileNotifyListener();
        c.addNotifyListener(fileNotifyListener);
        return c;
    }
    
    protected ISVNClientAdapter getReferenceClient() throws Exception {
        ISVNClientAdapter c = null;//SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
        fileNotifyListener = new FileNotifyListener();
        c.addNotifyListener(fileNotifyListener);
        return c;
    }

    protected void clearNotifiedFiles() {
        fileNotifyListener.files.clear();
    }
                
    protected void write(File file, int data) throws IOException {
        OutputStream os = null;
        try {            
            os = new FileOutputStream(file);            
            os.write(data);
            os.flush();
        } finally {
            if (os != null) {
                os.close();
            }
        }        
    }

    protected void write(File file, String str) throws IOException {
        FileWriter w = null;
        try {            
            w = new FileWriter(file);            
            w.write(str);
            w.flush();
        } finally {
            if (w != null) {
                w.close();
            }
        }        
    }
    
    protected String read(File file) throws IOException {
        StringBuffer sb = new StringBuffer();
        BufferedReader r = null;
        try {            
            r = new BufferedReader(new FileReader(file));
            String s = r.readLine();
            while( true ) {
                sb.append(s);
                s = r.readLine();
                if (s == null) break;
                sb.append('\n');
            }
        } finally {
            if (r != null) {
                r.close();
            }
        }        
        return sb.toString();
    }
    
    
    
    protected void assertContents(File file, int contents) throws FileNotFoundException, IOException {        
        assertContents(new FileInputStream(file), contents);
    }
    
    protected void assertContents(InputStream is, int contents) throws FileNotFoundException, IOException {        
        try {
            int i = is.read();
            assertEquals(contents, i);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }
    
    protected void assertInputStreams(InputStream isref, InputStream is) throws FileNotFoundException, IOException {        
        if(isref == null || is == null) {
            assertNull(isref);
            assertNull(is);
        }
        int iref = -1;
        int i = -1;
        while( (iref = isref.read()) > -1 ) {
            i = is.read(); 
            assertEquals(iref, i);
        }
        i = is.read();
        assertEquals(iref, i);
    }
    
    protected void assertProperty(File file, String prop, String val) throws Exception {
        ISVNClientAdapter c = null;//getReferenceClient();
        ISVNProperty p = c.propertyGet(file, prop);
        assertEquals(val, new String(p.getData()));
    }

    protected void assertProperty(File file, String prop, byte[] data) throws Exception {
        ISVNClientAdapter c = null;//getReferenceClient();
        ISVNProperty p = c.propertyGet(file, prop);
        for (int i = 0; i < data.length; i++) {
            assertEquals(data[i], p.getData()[i]);
        }
    }

    protected void assertInfo(File file, SVNUrl url) throws SVNClientException {
        ISVNInfo info = getInfo(file);
        assertNotNull(info);
        assertEquals(url, info.getUrl());
    }
    
    protected void assertCopy(SVNUrl url) throws SVNClientException {
        ISVNInfo info = getInfo(url);
        assertNotNull(info);
        assertEquals(url, info.getUrl());
    }    
    
}

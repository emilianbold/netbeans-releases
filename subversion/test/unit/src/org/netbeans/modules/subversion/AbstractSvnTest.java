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

package org.netbeans.modules.subversion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

/**
 *
 * @author Tomas Stupka
 */
public abstract class AbstractSvnTest extends NbTestCase {
   
    private File workDir;
    private FileStatusCache cache;
    private SVNUrl repoUrl;
    private File wc;
    private File repoDir;
        
    public AbstractSvnTest(String testName) throws MalformedURLException, SVNClientException {
        super(testName);
        workDir = new File(System.getProperty("work.dir")); 
        FileUtil.refreshFor(workDir);          
        repoDir = new File(System.getProperty("work.dir") + "/repo");
        String repopath = repoDir.getAbsolutePath();
        if(repopath.startsWith("/")) {
            repopath = repopath.substring(1, repopath.length());
        }
        repoUrl = new SVNUrl("file:///" + repopath);
        wc = new File(workDir, getName() + "_wc");        
        CmdLineClientAdapterFactory.setup13(null);                                         
    }   

    @Override
    protected void setUp() throws Exception {          
        super.setUp();      
        
        System.setProperty("netbeans.user", System.getProperty("work.dir") + "/cache");
        cache = Subversion.getInstance().getStatusCache();
        cache.cleanUp();
        
        cleanUpWC();  
        initRepo(repoDir);  
        
        wc.mkdirs();        
        if(importOnSetup()) svnimportWC();                   
    }
    
    protected abstract boolean importOnSetup(); 
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cleanUpWC();        
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
        
    protected void commit(File folder) throws SVNClientException {
        add(folder);
        try {
            getClient().commit(new File[]{ folder }, "commit", true);
        } catch (SVNClientException e) {
            fail("commit was supposed to work");
        }    
        //assertStatus(SVNStatusKind.NORMAL, folder);
    }
    
    protected void remove(File file) throws SVNClientException {        
        getClient().remove(new File[] {file}, true);
    }
    
    protected void copy(SVNUrl urlFrom, SVNUrl urlTo) throws SVNClientException {        
        getClient().copy(urlFrom, urlTo, "copy", SVNRevision.HEAD);
    }

    protected void add(File file) throws SVNClientException {
        ISVNStatus status = getSVNStatus(file);
        if(status.getTextStatus().equals(SVNStatusKind.UNVERSIONED)) {
            getClient().addFile(file);
        }
        if(file.isFile()) {
            return; 
        }
        File[] files = file.listFiles();
        if(files != null) {
            for (File f : files) {
                if(!isMetadata(f)) {
                    add(f);
                }
            }            
        }
    }
    
    protected void cleanUpRepo() throws SVNClientException {
        ISVNClientAdapter client = getClient();
        ISVNDirEntry[] entries = client.getList(repoUrl, SVNRevision.HEAD, false);
        SVNUrl[] urls = new SVNUrl[entries.length];
        for (int i = 0; i < entries.length; i++) {
            urls[i] = repoUrl.appendPath(entries[i].getPath());            
        }        
        client.remove(urls, "cleanup");
    }
    
    protected void cleanUpRepo(String[] paths) throws SVNClientException {
        ISVNClientAdapter client = getClient();
        SVNUrl[] urls = new SVNUrl[paths.length];
        for (int i = 0; i < paths.length; i++) {
            urls[i] = repoUrl.appendPath(paths[i]);            
        }        
        try {
            client.remove(urls, "cleanup");
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("does not exist") < 0) {
                throw e; 
            }
        }
    }

    protected void cleanUpWC() throws IOException {
        if(wc.exists()) {
            File[] files = wc.listFiles();
            if(files != null) {
                for (File file : files) {
                    if(!file.getName().equals("cache")) { // do not delete the cache
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo != null) {
                            fo.delete();                    
                        }
                    }                    
                }
            }
        }
    }

    protected void assertStatus(SVNStatusKind status, File file) throws SVNClientException {
        ISVNStatus[] values = getClient().getStatus(new File[]{file});
        for (ISVNStatus iSVNStatus : values) {
            assertEquals(status, iSVNStatus.getTextStatus());
        }
    }
    
    protected void assertPropertyStatus(SVNStatusKind status, File file) throws SVNClientException {
        ISVNStatus[] values = getClient().getStatus(new File[]{file});
        for (ISVNStatus iSVNStatus : values) {
            assertEquals(status, iSVNStatus.getPropStatus());
        }
    }
 
    protected ISVNStatus getSVNStatus(File file) throws SVNClientException {
        return getClient().getSingleStatus(file);        
    }
    
    private ISVNClientAdapter getClient() {
        return SVNClientAdapterFactory.createSVNClient(CmdLineClientAdapterFactory.COMMANDLINE_CLIENT);
    }   
    
    protected void assertCachedStatus(File file, int expectedStatus) throws Exception {
        assert !file.isFile() || expectedStatus != FileInformation.STATUS_VERSIONED_UPTODATE : "doesn't work for dirs with FileInformation.STATUS_VERSIONED_UPTODATE. Use getStatus instead";
        int status = getCachedStatus(file, expectedStatus);
        assertEquals(expectedStatus, status);
    }        

    protected int getCachedStatus(File file, int exceptedStatus) throws Exception, InterruptedException {
        FileInformation info = null;
        for (int i = 0; i < 600; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                throw ex;
            }
            info = cache.getCachedStatus(file);
            if (info != null && info.getStatus() == exceptedStatus) {
                break;
            }            
        }
        if (info == null) {
            throw new Exception("Cache timeout!");
        }
        return info.getStatus();
    }
    
    protected int getStatus(File file) {
        return cache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN).getStatus();
    }
    
    protected void initRepo(File repoDir) throws MalformedURLException, IOException, InterruptedException, SVNClientException {        
        if(!repoDir.exists()) {
            repoDir.mkdirs();            
            String[] cmd = {"svnadmin", "create", repoDir.getAbsolutePath()};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();   
        } 
    }      
    
    protected void svnimportWC() throws SVNClientException {
        ISVNClientAdapter client = getClient();        
        SVNUrl url = getTestUrl().appendPath(wc.getName());
        client.mkdir(url, true, "msg");        
        client.checkout(url, wc, SVNRevision.HEAD, true);        
        File[] files = wc.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!isMetadata(file)) {
                    client.addFile(new File[] {file}, true);   
                }                
            }
            client.commit(new File[] {wc}, "commit", true);                    
        }        
    }        
    
    protected void importFile(File folder) throws SVNClientException {
        ISVNClientAdapter client = getClient();        
        SVNUrl url = getTestUrl().appendPath(folder.getName());        
        client.mkdir(url, true, "msg");        
        client.checkout(url, folder, SVNRevision.HEAD, true);        
        File[] files = folder.listFiles();
        if(files != null) {
            for (File file : files) {
                if(!isMetadata(file)) {
                    client.addFile(new File[] {file}, true);   
                }                
            }
            client.commit(new File[] {folder}, "commit", true);                    
        }        
    }        
        
    protected void addFile(File fromFile) {
        addFile(fromFile);
    }
        
    protected void delete(File file) throws IOException {
        DataObject dao = DataObject.find(FileUtil.toFileObject(file));    
        dao.delete();   
    }   
    
    protected SVNRevision getRevision(File file) throws SVNClientException {
        return getInfo(file).getRevision();
    }
    
    protected SVNRevision getRevision(SVNUrl url) throws SVNClientException {
        return getInfo(url).getRevision();
    }
    
    protected ISVNInfo getInfo(SVNUrl url) throws SVNClientException {
        return getClient().getInfo(url);
    }
    
    protected ISVNLogMessage[] getLog(SVNUrl url) throws SVNClientException {
        return getClient().getLogMessages(url, new SVNRevision.Number(0), new SVNRevision.Number(0), SVNRevision.HEAD, true, false, 0L);
    }
    
    protected InputStream getContent(SVNUrl url) throws SVNClientException {
        return getClient().getContent(url, SVNRevision.HEAD);
    }
    
    protected ISVNInfo getInfo(File file) throws SVNClientException {
        return getClient().getInfo(file);
    }

    protected ISVNDirEntry[] list(SVNUrl url) throws SVNClientException {
        return getClient().getList(url, SVNRevision.HEAD, false);
    }
    
    protected void unlock(File file, String msg, boolean force) throws SVNClientException {
        try {
            getClient().unlock(new File[]{file}, force);
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("No lock on path") < 0) {
                throw e;
            }
        }
    }
    
    protected void lock(File file, String msg, boolean force) throws SVNClientException {
        unlock(file, "unlock", force);
        getClient().lock(new File[] {file}, msg, force);
    }
    
    protected void waitALittleBit(long t) {
        try {
            Thread.sleep(t);  
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected File getWC() {
        return wc;
    }

    protected SVNUrl getRepoUrl() {
        return repoUrl;
    }
    
    protected File getRepoDir() {
        return repoDir;
    }

    protected boolean isMetadata(File file) {
        return SvnUtils.isAdministrative(file) || SvnUtils.isPartOfSubversionMetadata(file);
    }

    protected SVNUrl getTestUrl() {
        return repoUrl.appendPath(getName());
    }
    
}

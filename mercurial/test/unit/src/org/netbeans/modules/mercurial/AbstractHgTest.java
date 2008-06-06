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

package org.netbeans.modules.mercurial;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public abstract class AbstractHgTest extends NbTestCase {

    public FileStatusCache getCache() {
        return cache;
    }
    private FileStatusCache cache;
//    private File workDir;
//    private File wc;

    public AbstractHgTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        FileUtil.refreshFor(getWorkDir());
        Logger.getLogger("").addHandler(versionCheckBlocker);
        
        try {
            Mercurial.getInstance().checkVersion();
            for (int i = 0; i < 20; i++) {                
                Thread.sleep(200);
                if(versionCheckBlocker.versionChecked) break;
            }
            if(!versionCheckBlocker.versionChecked) throw new TimeoutException("hg version check timedout!");
        } finally {
            Logger.getLogger("").removeHandler(versionCheckBlocker);    
        }
        
//        workDir = new File(System.getProperty("work.dir")); 
//        FileUtil.refreshFor(workDir);          
        HgCommand.doCreate(getWorkDir(), null);
//        wc = new File(workDir, getName() + "_wc");        
        cache = Mercurial.getInstance().getFileStatusCache();
    }

//    protected File getWC() {
//        return wc;
//    }    
    
    
    protected void commit(File... files) throws HgException, IOException {       
        
        List<File> filesToAdd = new ArrayList<File>();
        FileInformation status;
        for (File file : files) {

            status = HgCommand.getSingleStatus(getWorkDir(), file.getParentFile().getAbsolutePath(), file.getName());
            if(status.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {                   
                filesToAdd.add(file);

                File parent = file.getParentFile();
                while (!getWorkDir().equals(parent)) {
                    status = HgCommand.getSingleStatus(getWorkDir(), parent.getParentFile().getAbsolutePath(), parent.getName());
                    if(status.getStatus() == FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) {
                        filesToAdd.add(0, parent);
                        parent = parent.getParentFile();
                    } else {
                        break;
                    }
                }                                    
            }    
        }            
            
        HgCommand.doAdd(getWorkDir(), filesToAdd, null);
        List<File> filesToCommit = new ArrayList<File>();
        for (File file : files) {
            if(file.isFile()) {
                filesToCommit.add(file);
            }
        }
        
        HgCommand.doCommit(getWorkDir(), filesToCommit, "commit", null);
        for (File file : filesToCommit) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_UPTODATE);
        }        
    }    
    
    protected  void assertStatus(File f, int status) throws HgException, IOException {
        FileInformation s = HgCommand.getSingleStatus(getWorkDir(), f.getParentFile().getAbsolutePath(), f.getName());
        assertEquals(status, s.getStatus());
    }        
    
    protected void assertCacheStatus(File f, int status) throws HgException, IOException {
        assertEquals(status, cache.getStatus(f).getStatus());
    }

    protected File createFolder(String name) throws IOException {
        File file = new File(getWorkDir(), name);
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
        File file = new File(getWorkDir(), name);
        file.createNewFile();
        return file;
    }    
    
    private static class VersionCheckBlocker extends Handler {
        boolean versionChecked = false;
        public void publish(LogRecord record) {
            if(record.getMessage().indexOf("version: ") > -1) {
                versionChecked = true;                    
            }
        }
        public void flush() { }
        public void close() throws SecurityException { }        
    };
    private static VersionCheckBlocker versionCheckBlocker = new VersionCheckBlocker();
}

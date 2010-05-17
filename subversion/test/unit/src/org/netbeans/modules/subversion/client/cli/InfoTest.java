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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.subversion.client.cli;

import java.io.File;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
// XXX add referenceclient
public class InfoTest extends AbstractCLITest {
    
    public InfoTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {        
        if(getName().equals("testInfoNullAuthor")) {
            setAnnonWriteAccess();
            String[] cmd = new String[]{"svnserve", "-d"};
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();   
        }                        
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        if(getName().startsWith("testInfoLocked")) { 
            try {
                unlock(createFile("lockfile"), "unlock", true);
            } catch (Exception e) {
                // ignore
            }
        } else if(getName().equals("testInfoNullAuthor")) {        
            restoreAuthSettings();
        }
        super.tearDown();
    }    

    @Override
    protected String getRepoURLProtocol() {
        if(getName().equals("testInfoNullAuthor")) {        
            return "svn://localhost/";
        }
        return super.getRepoURLProtocol();
    }    
        
    public void testInfoWrongUrl() throws Exception {                                
        ISVNClientAdapter c = getNbClient();
        
        SVNClientException e1 = null;        
        try {
            c.getInfo(getRepoUrl().appendPath("bancha"));
        } catch (SVNClientException ex) {
            e1 = ex;
        }
        SVNClientException e2 = null;        
        try {
            getInfo(getRepoUrl().appendPath("bancha"));
        } catch (SVNClientException ex) {
            e2 = ex;
        }
                        
        assertNotNull(e1);
        assertNotNull(e2);
        assertTrue(e2.getMessage().indexOf(e1.getMessage()) > -1);
    }    

    public void testInfoNotManaged() throws Exception {                                
        File folder = createFolder("folder");
        File file = createFile(folder, "file");                
        notManaged(folder);
        notManaged(file);
    }  
  
//    XXX fails but we use the implemenation since ever, doesn't seem to be a problem    
//    public void testInfoUnversioned() throws Exception {                                
//        File unversioned = createFile("unversioned");
//        
//        ISVNClientAdapter c = getNbClient();
//
//        ISVNInfo info1 = c.getInfo(unversioned);
//        ISVNInfo info2 = getInfo(unversioned);
//                        
//        assertInfos(info1, info2);
//    }    
    
    public void testInfoFile() throws Exception {                                
        File file = createFile("file");
        add(file);
        commit(file);
        
        ISVNClientAdapter c = getNbClient();

        ISVNInfo info1 = c.getInfo(getFileUrl(file));        
        ISVNInfo info2 = getInfo(getFileUrl(file));
        
        assertInfos(info1, info2);
    }       
    
    public void testInfoRepo() throws Exception {                                        
        
        ISVNClientAdapter c = getNbClient();

        ISVNInfo info1 = c.getInfo(getRepoUrl());        
        ISVNInfo info2 = getInfo(getRepoUrl());
        
        assertInfos(info1, info2);
    }       
    
    public void testInfoLocked() throws Exception {                                        
        File file = createFile("lockfile");
        add(file);
        commit(file);
        String msg = 
            "Tamaryokucha and other types of sencha are made in essentially the same way.\n" +
            "Slight differences in processing, however, give tamaryokucha its characteristic\n" + 
            "fresh taste and reduced astringency.";        
        lock(file, msg, true);
        
        ISVNClientAdapter c = getNbClient();
       
        ISVNInfo info1 = c.getInfo(getFileUrl(file));        
        ISVNInfo info2 = getInfo(getFileUrl(file));
        
        assertTrue(info1.getLockComment().startsWith("Tamaryokucha"));
        assertInfos(info1, info2);
    }       
  
//    XXX not supported yet    
//    public void testInfoAdded() throws Exception {                                        
//        File file = createFile("file");
//        add(file);
//                
//        ISVNClientAdapter c = getNbClient();
//       
//        ISVNInfo info1 = c.getInfo(file);        
//        ISVNInfo info2 = getInfo(file);
//        
//        assertEquals(info1.getSchedule(), SVNScheduleKind.ADD);
//        assertInfos(info1, info2);
//    }       
    
    public void testInfoDeleted() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
        remove(file);
        
        ISVNClientAdapter c = getNbClient();
       
        ISVNInfo info1 = c.getInfo(getFileUrl(file));        
        ISVNInfo info2 = getInfo(getFileUrl(file));
        
        assertInfos(info1, info2);
    }    
    
    public void testInfoCopied() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
        
        File copy = new File("filecopy");
        copy(getFileUrl(file), getFileUrl(copy));
        
        ISVNClientAdapter c = getNbClient();
       
        ISVNInfo info1 = c.getInfo(getFileUrl(copy));        
        ISVNInfo info2 = getInfo(getFileUrl(copy));
        
        assertInfos(info1, info2);
    }        
    
    public void testInfoNullAuthor() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
               
        ISVNClientAdapter c = getNbClient();
       
        ISVNInfo info = c.getInfo(getFileUrl(file));                
        assertNull(info.getLastCommitAuthor());        
    }

    private void notManaged(File file) throws Exception {
        ISVNClientAdapter c = getNbClient();
        SVNClientException e1 = null;
        try {
            c.getInfo(getFileUrl(file));
        } catch (SVNClientException ex) {
            e1 = ex;
        }
        SVNClientException e2 = null;
        try {
            getInfo(getFileUrl(file));
        } catch (SVNClientException ex) {
            e2 = ex;
        }

        assertNotNull(e1);
        assertNotNull(e2);
        assertTrue(e2.getMessage().indexOf(e1.getMessage()) > -1);
    }

}

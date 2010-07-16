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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTest;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.modules.subversion.Subversion;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 *
 * @author tomas
 */
public class StatusTest extends AbstractCommandTest {
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public StatusTest(String testName) throws Exception {
        super(testName);
    }

    public void testStatusFileWrong() throws Exception {                                
        statusWrong(StatusCall.file);
    }
    
    public void testStatusFileArrayWrong() throws Exception {                                
        statusWrong(StatusCall.filearray);
    }
        
    private void statusWrong(StatusCall call) throws Exception {
        // XXX add ref client
        // does not exist
        File file = new File(getWC(), "file") ;

        ISVNClientAdapter c = getNbClient();
        SVNClientException ex = null;        
        try {
            switch (call) {
                case file:                    
                    c.getStatus(new File[]{ file });                    
                    break;                
                case filearray:                    
                    c.getStatus(file, true, true, true, true);                    
                    break;
            }
        } catch (SVNClientException e) {
            ex = e;
        }
        assertNull(ex);
    }
//
    public void testStatusFileArray() throws Exception {
        File uptodate = createFile("uptodate");
        add(uptodate);        
        File deleted = createFile("deleted");
        add(deleted);        
        commit(getWC());                                        
        
        File ignoredFile = createFile("ignoredfile");
        ignore(ignoredFile);
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        commit(getWC());                                        
        
        File notmanagedfolder = createFolder("notmanagedfolder");
        File notmanagedfile = createFile(notmanagedfolder, "notmanagedfile");
        File unversioned = File.createTempFile("unversioned", null);        // XXX extra test in unversioned WC
        File added = createFile("added");
        add(added);
        remove(deleted);
        
        File external = createExternal("externals");
        
        File[] files = new File[] { notmanagedfile, notmanagedfolder, unversioned, added, uptodate, deleted, ignoredFile, ignoredFolder, fileInIgnoredFolder, external };
        ISVNStatus[] sNb = getNbClient().getStatus(files);
        ISVNStatus[] sRef = getReferenceClient().getStatus(files);
        
        assertEquals(10, sNb.length);
        assertStatus(sRef, sNb);        
    }

    /**
     * Simulates wrong implementation of CommandlineClient.getStatus(File, boolean, boolean)
     * This status call does not return status for deleted (svn remove) files. Unfortunately it's called from FileStatusCache, so there's a chance we're missing something
     */
    public void testStatusDeletedFile () throws Exception {
        File deleted = createFile("deleted");
        File folder = createFolder("f");
        folder = createFolder(folder, "f");
        File file = createFile(folder, "f");
        add(deleted);
        add(folder);
        add(file);
        commit(getWC());

        remove(deleted);
        ISVNStatus[] sNb = getNbClient().getStatus(deleted.getParentFile(), true, true);
        assertEquals(5, sNb.length);
        assertFiles(sNb, new File[] {deleted, deleted.getParentFile()});

        // now test for regression of the fix - no additional statuses may be returned and we should not miss locally new files either
        sNb = getNbClient().getStatus(folder.getParentFile(), true, true);
        assertEquals(3, sNb.length);
        assertFiles(sNb, new File[] {folder, folder.getParentFile(), file});

        sNb = getNbClient().getStatus(folder.getParentFile(), false, true);
        assertEquals(2, sNb.length);
        assertFiles(sNb, new File[] {folder, folder.getParentFile()});

        sNb = getNbClient().getStatus(folder, false, true);
        assertEquals(2, sNb.length);
        assertFiles(sNb, new File[] {file, folder});

        File file2 = createFile(folder, "f2");
        sNb = getNbClient().getStatus(folder, false, true);
        assertEquals(3, sNb.length);
        assertFiles(sNb, new File[] {file, file2, folder});
    }

    private void assertFiles (ISVNStatus[] sNb, File[] files) {
        int foundFiles = 0;
        HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
        for (ISVNStatus status : sNb) {
            if (fileSet.contains(status.getFile())) {
                ++foundFiles;
            }
        }
        assertEquals(files.length, foundFiles);
    }
    
    public void testStatusFile() throws Exception {
        File uptodate = createFile("uptodate");
        add(uptodate);        
        File deleted = createFile("deleted");
        add(deleted);        
        commit(getWC());                                        
        
        File ignoredFile = createFile("ignoredfile");
        ignore(ignoredFile);
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        commit(getWC());                                        
        
        File notmanagedfolder = createFolder("notmanagedfolder");
        File notmanagedfile = createFile(notmanagedfolder, "notmanagedfile");
        File unversioned = File.createTempFile("unversioned", null);        // XXX extra test in unversioned WC
        File added = createFile("added");
        add(added);
        remove(deleted);
                        
        //                          descend  getAll contactServer  ignoreExternals
        status(notmanagedfolder,    false,   true,  false,          true            , 1);               
        status(notmanagedfolder,    false,   false, false,          true            , 1);               
        status(notmanagedfile,      false,   true,  false,          true            , 1);               
        status(notmanagedfile,      false,   false, false,          true            , 1);               
        status(unversioned,         false,   true,  false,          true            , 1);               
        status(unversioned,         false,   false, false,          true            , 1);               
        status(added,               false,   true,  false,          true            , 1);               
        status(added,               false,   false, false,          true            , 1);               
        status(uptodate,            false,   true,  false,          true            , 1);               
        status(uptodate,            false,   false, false,          true            , 0);               
        status(deleted,             false,   true,  false,          true            , 1);               
        status(deleted,             false,   false, false,          true            , 1);               
        status(ignoredFile,         false,   false, false,          true            , 0);
        status(ignoredFolder,       false,   false, false,          true            , 0);
        status(fileInIgnoredFolder, false,   false, false,          true            , 0);
  
    }        
    
    public void testStatusFolder() throws Exception {
        File notmanagedFolder1 = createFolder("notmanagedfolder1");
        File notmanagedFolder2 = createFolder(notmanagedFolder1, "notmanagedfolder2");
        File notmanagedFile = createFile(notmanagedFolder2, "notmanagedfile");
        
        File unversionedFolder1 = createFolder("unversionedfolder1");        
        File unversionedFolder2 = createFolder(unversionedFolder1, "unversionedfolder2");        
        File unversionedFile = createFile(unversionedFolder2, "unversionedfile");        
        
        File addedFolder1 = createFolder("addedfolder1");
        File addedFolder2 = createFolder(addedFolder1, "addedfolder2");
        File addedFile = createFile(addedFolder2, "addedfile");        
        add(addedFolder1);
        add(addedFolder2);
        add(addedFile);
        
        File uptodateFolder1 = createFolder("uptodatefolder1");
        File uptodateFolder2 = createFolder(uptodateFolder1, "uptodatefolder2");
        File uptodateFile = createFile(uptodateFolder2, "uptodatefile");
        add(uptodateFolder1);
        add(uptodateFolder2);
        add(uptodateFile);
        commit(uptodateFolder1);        
        
        File ignoredFolder = createFolder("ignoredfolder");
        ignore(ignoredFolder);
        File fileInIgnoredFolder = createFile(ignoredFolder, "fileInIgnoredFolder");                
        
        File deletedFolder1 = createFolder("deletedfolder1");
        File deletedFolder2 = createFolder(deletedFolder1, "deletedfolder2");
        File deletedFile = createFile(deletedFolder2, "deletedfile");
        add(deletedFolder1);
        add(deletedFolder2);
        add(deletedFile);
        commit(deletedFolder1);        
        remove(deletedFolder1);
        
        File externals = createExternal("externals");
        
        //                        descend  getAll contactServer  ignoreExternals
        status(notmanagedFolder1,  false,  true,  false,          true           , 1);               
        status(notmanagedFolder1,  true,   true,  false,          true           , 1);               
        status(notmanagedFolder2,  false,  false, false,          true           , 1);               
        status(notmanagedFolder2,  true,   false, false,          true           , 1);               
                
        status(unversionedFolder1, false,  true,  false,          true          , 1);               
        status(unversionedFolder1, true,   true,  false,          true          , 1);                       
        status(unversionedFolder2, false,  true,  false,          true          , 1);               
        status(unversionedFolder2, true,   true,  false,          true          , 1);                       
        
        status(addedFolder1,       false,  true,  false,          true          , 2);               
        status(addedFolder1,       true,   true,  false,          true          , 3);                       
        
        status(uptodateFolder1,    false,  true,  false,          true          , 2);               
        status(uptodateFolder1,    true,   true,  false,          true          , 3);               
        status(uptodateFolder1,    false,  false, false,          true          , 0);               
        status(uptodateFolder1,    true,   false, false,          true          , 0);               
        
        status(deletedFolder1,     false,  true,  false,          true          , 2);               
        status(deletedFolder1,     true,   false, false,          true          , 3);               
        
        status(ignoredFolder,      false,  true,  false,          true          , 1);               
        status(ignoredFolder,      true,   false, false,          true          , 1);               
        
        status(externals,          false,  true,  false,          false         , 2);               
        status(externals,          true,   true,  false,          false         , 4);               
        status(externals,          true,   false, false,          false         , 1);               
        status(externals,          false,  false, false,          false         , 1);               
        
        status(externals,          false,  true,  false,          true          , 2);               
        status(externals,          true,   false, false,          true          , 1);               
    }        
        
    private void status(File file, boolean descend, boolean getAll, boolean contactServer, boolean ignoreExternals, int c) throws Exception {        
        ISVNStatus[] sNb  = getNbClient().getStatus(file, descend, getAll, contactServer, ignoreExternals);
        ISVNStatus[] sRef = getReferenceClient().getStatus(file, descend, getAll, contactServer, ignoreExternals);
        
        assertEquals(c, sNb.length);
        assertStatus(sRef, sNb);         
    }

    private void assertStatus(ISVNStatus[] refs, ISVNStatus[] nbs) {
        assertEquals(refs.length, nbs.length);
        for (int i = 0; i < nbs.length; i++) {
            
            Subversion.LOG.info("assertStatus " + refs[i].getPath());
            
            assertEquals(refs[i].getConflictNew(),          nbs[i].getConflictNew());
            assertEquals(refs[i].getConflictOld(),          nbs[i].getConflictOld());
            assertEquals(refs[i].getConflictWorking(),      nbs[i].getConflictWorking());
            assertEquals(refs[i].getLastChangedDate(),      nbs[i].getLastChangedDate());
            assertEquals(refs[i].getFile(),                 nbs[i].getFile());
            assertEquals(refs[i].getLastChangedRevision(),  nbs[i].getLastChangedRevision());
            assertEquals(refs[i].getLastCommitAuthor(),     nbs[i].getLastCommitAuthor());
            assertEquals(refs[i].getLockComment(),          nbs[i].getLockComment());
            assertEquals(refs[i].getLockCreationDate(),     nbs[i].getLockCreationDate());
            assertEquals(refs[i].getLockOwner(),            nbs[i].getLockOwner());
            assertEquals(refs[i].getNodeKind(),             nbs[i].getNodeKind());
            assertEquals(refs[i].getPath(),                 nbs[i].getPath());
            assertEquals(refs[i].getPropStatus(),           nbs[i].getPropStatus());
            assertEquals(refs[i].getRepositoryPropStatus(), nbs[i].getRepositoryPropStatus());
            assertEquals(refs[i].getRepositoryTextStatus(), nbs[i].getRepositoryTextStatus());
            assertEquals(refs[i].getRevision(),             nbs[i].getRevision());
            assertEquals(refs[i].getTextStatus(),           nbs[i].getTextStatus());
            assertEquals(refs[i].getUrl(),                  nbs[i].getUrl());
            assertEquals(refs[i].getUrlCopiedFrom(),        nbs[i].getUrlCopiedFrom());
            assertEquals(refs[i].getUrlString(),            nbs[i].getUrlString());                       
        }
    } 
    
    private File createExternal(String fileName) throws Exception {
        File externals = createFolder(fileName);                
        add(externals);
        
        try {
            mkdir(getRepo2Url().appendPath("e1"));
            mkdir(getRepo2Url().appendPath("e1").appendPath("e2"));
            mkdir(getRepo2Url().appendPath("e1").appendPath("e2").appendPath("e3"));
        } catch (SVNClientException e) {
            if(e.getMessage().indexOf("file already exists") > -1) {
                throw e;
            }
        }
        setProperty(externals, "svn:externals", "e1/e2\t" + getRepo2Url().appendPath("e1").appendPath("e2").toString().replaceAll(" ", "%20"));
        
        commit(externals);
        update(externals);        
        
        assertTrue(new File(new File(new File(externals, "e1"), "e2"), "e3").exists());
        
        return externals;
    }
}

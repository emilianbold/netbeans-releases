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

package org.netbeans.modules.subversion.client.commands;

import org.netbeans.modules.subversion.client.AbstractCommandTest;
import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class TreeConflictsTest extends AbstractCommandTest {
    private File wc1, wc2;
    private File f1, f2;
    private File fcopy1, fcopy2;
    private File folder1, folder2;
    
    // XXX terst remote change
    
    private enum StatusCall {
        filearray,
        file
    }
    
    public TreeConflictsTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        importWC = false;
        super.setUp();
        cleanUpRepo(new String[] {CI_FOLDER});
        File ciFolder = importFolder(CI_FOLDER);
        wc1 = checkout(ciFolder.getName(), "checkout1");
        folder1 = new File(wc1, "folder");
        f1 = new File(folder1, "file");
        fcopy1 = new File(folder1, "filecopy");
        wc2 = checkout(ciFolder.getName(), "checkout2");
        folder2 = new File(wc2, "folder");
        f2 = new File(folder2, "file");
        fcopy2 = new File(folder2, "filecopy");
    }
//
    public void testStatusLocalEditIncomingDeleteAcceptRemote () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalEditIncomingDelete(c);

        remove(f2);
        assertStatus(f2, true, SVNStatusKind.MISSING);
        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.UNVERSIONED);
        assertFalse(f2.exists());
    }

    public void testStatusLocalEditIncomingDeleteAcceptLocal () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalEditIncomingDelete(c);

        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.ADDED);
        assertTrue(f2.exists());
    }

    private void prepareTestStatusLocalEditIncomingDelete (ISVNClientAdapter c) throws Exception {
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        c.move(f1, fcopy1, true);
        assertTrue(fcopy1.exists());
        commit(wc1);
        write(f2, "change");
        assertFalse(fcopy2.exists());
        
        update(wc2);
        assertStatus(f2, true, SVNStatusKind.ADDED);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.MODIFIED);
    }

    public void testStatusLocalDeleteIncomingEditAcceptLocal () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalDeleteIncomingEdit(c);

        assertFalse(f2.exists());
        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.DELETED);
        assertFalse(f2.exists());
    }

    public void testStatusLocalDeleteIncomingEditAcceptRemote () throws Exception {
        ISVNClientAdapter c = getNbClient();
        prepareTestStatusLocalDeleteIncomingEdit(c);

        assertFalse(f2.exists());
        c.revert(f2, true);
        assertStatus(f2, false, SVNStatusKind.NORMAL);
        assertTrue(f2.exists());
    }

    private void prepareTestStatusLocalDeleteIncomingEdit (ISVNClientAdapter c) throws Exception {
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        write(f1, "change");
        commit(wc1);
        c.move(f2, fcopy2, true);
        assertTrue(fcopy2.exists());

        update(wc2);
        assertStatus(f2, true, SVNStatusKind.DELETED);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.ADDED);
    }

    public void testStatusLocalDeleteIncomingDelete () throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        c.move(f1, fcopy1, true);
        assertFalse(f1.exists());
        assertTrue(fcopy1.exists());
        commit(wc1);

        c.move(f2, fcopy2, true);
        assertFalse(f2.exists());
        assertTrue(fcopy2.exists());

        update(wc2);
        assertStatus(f2, true, SVNStatusKind.MISSING);
        assertStatus(fcopy2, true, SVNStatusKind.ADDED);

        c.resolved(f2);
        assertStatus(f2, false, SVNStatusKind.UNVERSIONED);
        assertFalse(f2.exists());
        c.resolved(fcopy2);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.ADDED);
        c.revert(fcopy2, true);
        assertStatus(fcopy2, false, SVNStatusKind.UNVERSIONED);
        update(fcopy2);
        assertTrue(fcopy2.exists());
        assertStatus(fcopy2, false, SVNStatusKind.NORMAL);
    }

    public void testStatusFolderLocalDeleteIncomingDelete () throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st1 = c.getSingleStatus(f1);
        ISVNStatus st2 = c.getSingleStatus(f2);
        assertFalse(st1.hasTreeConflict());
        assertFalse(st2.hasTreeConflict());
        File folderCopy1 = new File(folder1.getParent(), "folderCopy");
        c.move(folder1, folderCopy1, true);
        assertTrue(folder1.exists());
        assertTrue(folderCopy1.exists());
        commit(wc1);
        assertFalse(folder1.exists());
        assertTrue(folderCopy1.exists());

        File folderCopy2 = new File(folder2.getParent(), "folderCopy");
        c.move(folder2, folderCopy2, true);
        assertTrue(folder2.exists());
        assertTrue(folderCopy2.exists());

        update(wc2);
        assertFalse(folder2.exists());
        assertTrue(folderCopy2.exists());
        assertStatus(folder2, true, SVNStatusKind.MISSING);
        assertStatus(folderCopy2, true, SVNStatusKind.ADDED);

        c.resolved(folder2);
        assertStatus(folder2, false, SVNStatusKind.UNVERSIONED);
        assertFalse(folder2.exists());
        c.resolved(folderCopy2);
        assertTrue(folderCopy2.exists());
        assertStatus(folderCopy2, false, SVNStatusKind.ADDED);
        c.revert(folderCopy2, true);
        assertStatus(folderCopy2, false, SVNStatusKind.UNVERSIONED);
        update(folderCopy2);
        assertTrue(folderCopy2.exists());
        assertStatus(folderCopy2, false, SVNStatusKind.NORMAL);
    }

    private void assertStatus (File f, boolean hasConflicts, SVNStatusKind svnStatus) throws Exception {
        ISVNClientAdapter c = getNbClient();
        ISVNStatus st = c.getSingleStatus(f);
        assert hasConflicts == st.hasTreeConflict();
        assert hasConflicts == (st.getConflictDescriptor() != null);
        assertEquals(svnStatus, st.getTextStatus());
        ISVNStatus[] sts = c.getStatus(new File[] {f});
        assertStatus(f, hasConflicts, false, sts, svnStatus);
        sts = c.getStatus(f.getParentFile(), true, true);
        assertStatus(f, hasConflicts, hasConflicts, sts, svnStatus);
        sts = c.getStatus(f.getParentFile(), true, true, false);
        assertStatus(f, hasConflicts, false, sts, svnStatus);

        sts = c.getStatus(f, false, true);
        assertStatus(f, hasConflicts, true, sts, svnStatus);

        assert ((getStatus(f) & FileInformation.STATUS_VERSIONED_CONFLICT_TREE) != 0) == hasConflicts;
    }

    private void assertStatus (File f, boolean hasConflicts, boolean testConflictDescriptor, ISVNStatus[] sts, SVNStatusKind svnStatus) throws Exception {
        for (ISVNStatus st : sts) {
            if (f.equals(st.getFile())) {
                assertEquals(svnStatus, st.getTextStatus());
                if (st.hasTreeConflict() != hasConflicts) {
                    fail("hasConflicts !== status.hasTreeConflicts");
                }
                if (testConflictDescriptor && hasConflicts == (st.getConflictDescriptor() == null)) {
                    fail("hasConflicts === (status.getConflictDescriptor() == null)");
                }
                return;
            }
        }
        if (hasConflicts) {
            fail("No such status");
        }
    }
    
    private File importFolder (String repoFolderName) throws Exception {
        File cifolder = createFolder(repoFolderName);
        File folder1 = createFolder(cifolder, "folder");
        File file = createFile(folder1, "file");

        importFile(cifolder);
        return cifolder;
    }

    private File checkout (String ciFolderName,
                                    String targetFolderName) throws Exception {
        File checkout = createFolder(targetFolderName);
        SVNUrl url = getTestUrl().appendPath(ciFolderName);
        ISVNClientAdapter c = getNbClient();
        c.checkout(url, checkout, SVNRevision.HEAD, true);
        return checkout;
    }
}

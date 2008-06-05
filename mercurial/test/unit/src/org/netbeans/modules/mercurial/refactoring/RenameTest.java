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

package org.netbeans.modules.mercurial.refactoring;

import java.io.File;
import java.io.IOException;
import org.netbeans.modules.mercurial.AbstractHgTest;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.Mercurial;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author tomas
 */
public class RenameTest extends AbstractHgTest {

    public RenameTest(String arg0) {
        super(arg0);
    }

    public void testRenameFolder() throws HgException, IOException {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File file3 = createFile(folder, "file3");
        
        commit(folder);
        assertStatus(folder, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertStatus(file1, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertStatus(file2, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertStatus(file3, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCacheStatus(folder, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCacheStatus(file1, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCacheStatus(file2, FileInformation.STATUS_VERSIONED_UPTODATE);
        assertCacheStatus(file3, FileInformation.STATUS_VERSIONED_UPTODATE);
                      
        renameFO(folder, "folderenamed");
        
        File folderenamed = new File(getWorkDir(), "folderenamed");
        File file1renamed = new File(folderenamed, file1.getName());
        File file2renamed = new File(folderenamed, file2.getName());
        File file3renamed = new File(folderenamed, file3.getName());
        
        assertTrue(folderenamed.exists());
        assertFalse(folder.exists());

        FileInformation folderInfo = getCache().getStatus(folder);
        FileInformation file1Info = getCache().getStatus(file1);
        FileInformation file2Info = getCache().getStatus(file2);
        FileInformation file3Info = getCache().getStatus(file3);
        FileInformation folderenamedInfo = getCache().getStatus(folderenamed);
        FileInformation file1renamedInfo = getCache().getStatus(file1renamed);
        FileInformation file2renamedInfo = getCache().getStatus(file2renamed);
        FileInformation file3renamedInfo = getCache().getStatus(file3renamed);
        
        Mercurial.LOG.info("status " + folder + " : " + folderInfo);
        Mercurial.LOG.info("status " + file1 + " : " + file1Info);
        Mercurial.LOG.info("status " + file2 + " : " + file2Info);
        Mercurial.LOG.info("status " + file3 + " : " + file3Info);
        Mercurial.LOG.info("status " + folderenamed + " : " + folderenamedInfo);
        Mercurial.LOG.info("status " + file1renamed + " : " + file1renamedInfo);
        Mercurial.LOG.info("status " + file2renamed + " : " + file2renamedInfo);
        Mercurial.LOG.info("status " + file3renamed + " : " + file3renamedInfo);
        
        assertStatus(folder, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertStatus(file1, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertStatus(file2, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertStatus(file3, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertCacheStatus(folder, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertCacheStatus(file1, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertCacheStatus(file2, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);
        assertCacheStatus(file3, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY);        
        
    }

    private void renameFO(File file, String name) throws IOException {
        FileObject folderFO = FileUtil.toFileObject(file);
        assertNotNull(folderFO);
        FileLock lock = folderFO.lock();
        try {
            folderFO.rename(lock, name, null);
        } finally {
            lock.releaseLock();
        }
    }
    
    private void renameDO(File from, String name) throws IOException {
        DataObject daoFrom = DataObject.find(FileUtil.toFileObject(from));    
        daoFrom.rename(name);
    }
    
}

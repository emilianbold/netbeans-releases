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
import org.netbeans.modules.versioning.util.FileUtils;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public class RelocateTest extends AbstractCommandTest {
    
    public RelocateTest(String testName) throws Exception {
        super(testName);
    }
    
    public void testRelocateFile() throws Exception {                                        
        File file = createFile("file");
        add(file);
        commit(file);
        
        assertInfo(file, getFileUrl(file));
        
        ISVNClientAdapter c = getNbClient();
        c.relocate(getRepoUrl().toString(), getRepo2Url().toString(), file.getAbsolutePath(), false);

        assertInfo(file, getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(file.getName()));        
        
        //assertNotifiedFiles(file); // XXX no notif fromthe cli        
    }
    
    public void testRelocateFolderRec() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "folder");
        File file1 = createFile(folder1, "file1");
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        
        assertInfo(folder, getFileUrl(folder));
        assertInfo(file, getFileUrl(folder).appendPath(file.getName()));
        assertInfo(folder1, getFileUrl(folder).appendPath(folder1.getName()));
        assertInfo(file1, getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName()));
        
        ISVNClientAdapter c = getNbClient();
        c.relocate(getRepoUrl().toString(), getRepo2Url().toString(), folder.getAbsolutePath(), true);

        assertInfo(folder, getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName()));        
        assertInfo(file, getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName()).appendPath(file.getName()));        
        assertInfo(folder1, getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName()).appendPath(folder1.getName()));        
        assertInfo(file1, getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName()).appendPath(folder1.getName()).appendPath(file1.getName()));        
    }
    
    public void testRelocateFolderNonRec() throws Exception {                                        
        File folder = createFolder("folder");
        File folder1 = createFolder(folder, "folder1");
        File file = createFile(folder, "folder");
        File file1 = createFile(folder1, "file1");
        
        add(folder);
        add(file);
        add(folder1);
        add(file1);
        commit(folder);
        
        SVNUrl folderUrl = getFileUrl(folder);
        SVNUrl fileUrl = getFileUrl(folder).appendPath(file.getName());
        SVNUrl folder1Url = getFileUrl(folder).appendPath(folder1.getName());
        SVNUrl file1Url = getFileUrl(folder).appendPath(folder1.getName()).appendPath(file1.getName());
        
        assertInfo(folder, folderUrl);
        assertInfo(file, fileUrl);
        assertInfo(folder1, folder1Url);        
        assertInfo(file1,file1Url);
        
        ISVNClientAdapter c = getNbClient();
        c.relocate(getRepoUrl().toString(), getRepo2Url().toString(), folder.getAbsolutePath(), false);

        SVNUrl folderNewUrl = getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName());
        SVNUrl fileNewUrl = getRepo2Url().appendPath(getName()).appendPath(getWC().getName()).appendPath(folder.getName()).appendPath(file.getName());
        
        assertInfo(folder, folderNewUrl);        
        assertInfo(file, fileNewUrl);        
        assertInfo(folder1, folder1Url);        
        assertInfo(file1, file1Url);        
    }

}

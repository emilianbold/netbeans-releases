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

package org.netbeans.modules.mercurial;

import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 *
 * @author tomas
 */
public class StatusTest extends AbstractHgTest {

    public StatusTest(String arg0) {
        super(arg0);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", "/tmp/" + System.currentTimeMillis());
        super.setUp();
        
        // create
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        
    }

    public void testStatusForRenamedFolder_136448() throws HgException, IOException {
        File folder = createFolder("folder");
        File file1 = createFile(folder, "file1");
        File file2 = createFile(folder, "file2");
        File file3 = createFile(folder, "file3");
        
        commit(folder);
        getCache().refresh(folder); // force refresh
        
        // assert status given from cli
        assertStatus(folder, FileInformation.STATUS_VERSIONED_UPTODATE);
        Map<File, FileInformation> m = HgCommand.getStatus(getWorkDir(), Collections.singletonList(folder));
        assertEquals(0, m.keySet().size());
                
        // hg move the folder
        File folderenamed = new File(getWorkDir(), "folderenamed");
        HgCommand.doRename(getWorkDir(), folder, folderenamed, null);

        m = HgCommand.getStatus(getWorkDir(), Collections.singletonList(folder));
        assertEquals(3, m.keySet().size());
        for (File file : m.keySet()) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY);    
        }        
        m = HgCommand.getStatus(getWorkDir(), Collections.singletonList(folderenamed));
        assertEquals(3, m.keySet().size());        
        for (File file : m.keySet()) {
            assertStatus(file, FileInformation.STATUS_VERSIONED_ADDEDLOCALLY);    
        }                
    }
    
}

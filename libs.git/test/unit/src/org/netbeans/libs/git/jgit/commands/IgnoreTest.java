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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.jgit.lib.Constants;
import org.netbeans.libs.git.GitStatus;
import org.netbeans.libs.git.GitStatus.Status;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class IgnoreTest extends AbstractGitTestCase {
    private File workDir;

    public IgnoreTest (String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testIgnoreFileInRoot () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderInRoot () throws Exception {
        File f = new File(workDir, "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/subFolder/anotherSubfolder/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolder () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/subFolder/anotherSubfolder/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFileInRootAppend () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\n\n\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\n\n\nfff\nfff2\n/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderInRootAppend () throws Exception {
        File f = new File(workDir, "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\n\n\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\n\n\nfff\nfff2\n/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolderAppend () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\nfff\nfff2\n/subFolder/anotherSubfolder/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolderAppend () throws Exception {
        File f = new File(new File(new File(workDir, "subFolder"), "anotherSubfolder"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#this is ignore file\nfff\nfff2");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#this is ignore file\nfff\nfff2\n/subFolder/anotherSubfolder/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreIgnoredEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n#end ignoreFile");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/file\n#end ignoreFile", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreIgnoredPartialEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfile");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfile", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "sf1/sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf1/sf2/file", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "sf1/*/file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf1/*/file", read(gitIgnore));
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredPartialEqualPath () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        new File(f, "file").createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfolder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfolder", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "#ignoreFile\nfolder/");
        // test fails, there's an already fixed error in jgit, see http://egit.eclipse.org/w/?p=jgit.git;a=commit;h=c87ae94c70158ba2bcb310aa242102853d221f11
        // but we need to wait for the next JGit release
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(new File(f, "file"));
        assertNotNull(st);
//        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertTrue(gitIgnore.exists());
//        assertEquals("#ignoreFile\nfolder/", read(gitIgnore));
//        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreRemoveNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n!/sf1/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "");
        File ignore2 = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(ignore2, "!sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/sf1/sf2/file", read(gitIgnore));
        assertEquals("", read(ignore2));
        assertEquals(Arrays.asList(ignore2, gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderRemoveNegation () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder/\n!/sf1/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder/");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreNoNegationRemoval () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/file\n!file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n!file\n/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreFolderNoNegationRemoval () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!folder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder\n!folder\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf1/sf2/folder\n!/sf1/sf2/folder\n/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileInSubfolder_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#dummy ignore file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#dummy ignore file", read(gitIgnore));
        gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        assertEquals("/sf1/sf2/file", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderInSubfolder_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#dummy ignore file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#dummy ignore file", read(gitIgnore));
        gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        assertEquals("/sf1/sf2/folder/", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }

    public void testIgnoreIgnoredEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }

    public void testIgnoreFolderIgnoredEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreIgnoredPartialEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nf*");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nf*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreFolderIgnoredPartialEqualPath_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\nfold*");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nfold*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(0, ignores.length);
    }
    
    public void testIgnoreRemoveNegation_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf2/file\n!/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "sf2/file\n!sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\nsf2/f*\n!/sf2/file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nsf2/f*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderRemoveNegation_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "#ignoreFile\n/sf2/folder/\n!/sf2/folder/");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\n/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "#ignoreFile\nsf2/f*\n!/sf2/folder/");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("#ignoreFile\nsf2/f*", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreNoNegationRemoval_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "\n/sf2/file\n!file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("\n!file\n/sf2/file", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFolderNoNegationRemoval_NestedIgnoreFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "folder");
        f.mkdirs();
        File gitIgnore = new File(f.getParentFile().getParentFile(), Constants.DOT_GIT_IGNORE);
        write(gitIgnore, "\n/sf2/folder/\n!/sf2/folder");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(gitIgnore.exists());
        assertEquals("\n!/sf2/folder\n/sf2/folder/", read(gitIgnore));
        assertFalse(new File(workDir, Constants.DOT_GIT_IGNORE).exists());
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
    }
    
    public void testIgnoreFileWithStarChar () throws Exception {
        File f = new File(workDir, "fi*le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[*]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/fi[*]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[*]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\*le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertNotSame(Status.STATUS_IGNORED, st.getStatusIndexWC());
//        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertEquals("/fi\\*le", read(gitIgnore));
//        assertNull(getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f));
    }
    
    public void testIgnoreFileWithQuestionMark () throws Exception {
        File f = new File(workDir, "fi?le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[?]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/fi[?]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[?]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\?le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertNotSame(Status.STATUS_IGNORED, st.getStatusIndexWC());
//        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertEquals("/fi\\?le", read(gitIgnore));
    }
    
    public void testIgnoreFileWithBracket () throws Exception {
        File f = new File(workDir, "fi[le");
        f.createNewFile();
        File gitIgnore = new File(workDir, Constants.DOT_GIT_IGNORE);
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[[]le", read(gitIgnore));
        assertEquals(Arrays.asList(gitIgnore), Arrays.asList(ignores));
        
        write(gitIgnore, "/fi[[]le");
        GitStatus st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertEquals(Status.STATUS_IGNORED, st.getStatusIndexWC());
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertEquals("/fi[[]le", read(gitIgnore));
        assertEquals(0, ignores.length);
        
        write(gitIgnore, "/fi\\[le");
        // jgit seems to incorrectly handle escaped wildcards
        st = getClient(workDir).getStatus(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR).get(f);
        assertNotSame(Status.STATUS_IGNORED, st.getStatusIndexWC());
//        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
//        assertEquals("/fi[\\[]le", read(gitIgnore));
    }
    
    public void testDoNotIgnoreExcludedFile () throws Exception {
        File f = new File(new File(new File(workDir, "sf1"), "sf2"), "file");
        f.getParentFile().mkdirs();
        f.createNewFile();
        File excludeFile = new File(workDir, ".git/info/exclude");
        excludeFile.getParentFile().mkdirs();
        write(excludeFile, "/sf1/sf2/file");
        File[] ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("/sf1/sf2/file", read(excludeFile));
        assertEquals(0, ignores.length);
        
        write(excludeFile, "file");
        ignores = getClient(workDir).ignore(new File[] { f }, ProgressMonitor.NULL_PROGRESS_MONITOR);
        assertTrue(excludeFile.exists());
        assertEquals("file", read(excludeFile));
        assertEquals(0, ignores.length);
    }
}

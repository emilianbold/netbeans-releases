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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.project.ant;

import org.netbeans.modules.project.ant.*;
import org.netbeans.modules.project.ant.FileOwnerCollocationQueryImpl;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.TestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.test.MockLookup;

/**
 *
 * @author mkleint
 */
public class FileOwnerCollocationQueryImplTest extends NbTestCase {
    
    public FileOwnerCollocationQueryImplTest(String testName) {
        super(testName);
    }            
    private FileObject scratch;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.setInstances(TestUtil.testProjectFactory());
        scratch = TestUtil.makeScratchDir(this);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of findRoot method, of class FileOwnerCollocationQueryImpl.
     */
    public void testFindRoot() throws IOException {
        FileObject root =  scratch.createFolder("root");
        FileObject projdir = root.createFolder("prj1");
        projdir.createFolder("testproject");
        
        //root/prj1/foo
        FileOwnerCollocationQueryImpl instance = new FileOwnerCollocationQueryImpl();
        assertEquals(instance.findRoot(FileUtil.toFile(projdir.createData("foo"))), FileUtil.toFile(projdir));
        
        //root/prj2/foo/prj3/bar
        projdir = root.createFolder("prj2");
        FileObject expected = projdir;
        projdir.createFolder("testproject");
        projdir = projdir.createFolder("foo").createFolder("prj3");
        projdir.createFolder("testproject");
        assertEquals(instance.findRoot(FileUtil.toFile(projdir.createData("bar"))), FileUtil.toFile(expected));
        
        //root
        assertEquals(instance.findRoot(FileUtil.toFile(root)), null);
    }

    /**
     * Test of areCollocated method, of class FileOwnerCollocationQueryImpl.
     */
    public void testAreCollocated() throws IOException {
        FileObject root =  scratch.createFolder("root");
        FileObject projdir = scratch.createFolder("prj1");
        projdir.createFolder("testproject");
        FileObject lib = root.createFolder("libs");

        
        File file1 = FileUtil.toFile(lib.createData("pron"));
        File file2 = FileUtil.toFile(projdir.createData("xxx"));
        FileOwnerCollocationQueryImpl instance = new FileOwnerCollocationQueryImpl();
        assertFalse(instance.areCollocated(file1, file2));
        file1 = FileUtil.toFile(projdir.createData("pron"));
        assertTrue(instance.areCollocated(file1, file2));
        
        
        file1 = FileUtil.toFile(projdir);
        file2 = FileUtil.toFile(lib);
        assertFalse(instance.areCollocated(file1, file2));
        
        projdir = root.createFolder("noproj").createFolder("proj1");
        projdir.createFolder("testproject");
        FileObject projdir2 = root.createFolder("noproj2").createFolder("proj2");
        projdir2.createFolder("testproject");
        file1 = FileUtil.toFile(projdir.createData("foo"));
        file2 = FileUtil.toFile(projdir2.createData("bar"));
//        System.out.println("root1=" + instance.findRoot(file1));
//        System.out.println("root2=" + instance.findRoot(file2));
        assertFalse(instance.areCollocated(file1, file2));
        
    }

}

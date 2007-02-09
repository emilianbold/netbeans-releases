/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seproject.queries;

import java.io.IOException;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.j2seproject.J2SEProjectGenerator;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;

/**
 * Tests for BinaryForSourceQueryImpl
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceQueryImplTest extends NbTestCase {
    
    public BinaryForSourceQueryImplTest(String testName) {
        super(testName);
    }
    
    private FileObject scratch;
    private FileObject projdir;
    private FileObject sources;
    private FileObject buildClasses;
    private ProjectManager pm;
    private Project pp;
    AntProjectHelper helper;
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        scratch = null;
        projdir = null;
        pm = null;
        TestUtil.setLookup(Lookup.EMPTY);
        super.tearDown();
    }
    
    
    private void prepareProject () throws IOException {
        scratch = TestUtil.makeScratchDir(this);
        projdir = scratch.createFolder("proj");        
        J2SEProjectGenerator.setDefaultSourceLevel(new SpecificationVersion ("1.4"));   //NOI18N
        helper = J2SEProjectGenerator.createProject(FileUtil.toFile(projdir),"proj",null,null);
        J2SEProjectGenerator.setDefaultSourceLevel(null);   //NOI18N
        pm = ProjectManager.getDefault();
        pp = pm.findProject(projdir);
        sources = projdir.getFileObject("src");
        FileObject fo = projdir.createFolder("build");
        buildClasses = fo.createFolder("classes");        
    }
    
    public void testBinaryForSourceQuery() throws Exception {
        this.prepareProject();
        FileObject folder = scratch.createFolder("SomeFolder");
        BinaryForSourceQuery.Result result = BinaryForSourceQuery.findBinaryRoots(folder.getURL());
        assertEquals("Non-project folder does not have any source folder", 0, result.getRoots().length);
        folder = projdir.createFolder("SomeFolderInProject");
        result = BinaryForSourceQuery.findBinaryRoots(folder.getURL());
        assertEquals("Project non build folder does not have any source folder", 0, result.getRoots().length);
        result = BinaryForSourceQuery.findBinaryRoots(sources.getURL());        
        assertEquals("Project build folder must have source folder", 1, result.getRoots().length);
        assertEquals("Project build folder must have source folder",buildClasses.getURL(),result.getRoots()[0]);        
        assertEquals(BinaryForSourceQueryImpl.R.class, result.getClass());
        BinaryForSourceQuery.Result result2 = BinaryForSourceQuery.findBinaryRoots(sources.getURL());
        assertTrue (result == result2);
    }               
                    
}

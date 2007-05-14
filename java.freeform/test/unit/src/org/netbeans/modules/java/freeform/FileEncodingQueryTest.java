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
package org.netbeans.modules.java.freeform;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author Milan Kubec
 */
public class FileEncodingQueryTest extends NbTestCase {
    
    /** Creates a new instance of FileEncodingQeuryTest */
    public FileEncodingQueryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testFileEncodingQuery() throws Exception {
        
        String projectFolder = "proj1";
        String projectName = "proj-1";
      
        File prjBase = new File(getWorkDir(), projectFolder);
        prjBase.mkdir();
        File antScript = new File(prjBase, "build.xml");
        antScript.createNewFile();
        File srcDir = new File(prjBase, "src");
        srcDir.mkdir();
        File srcFile = createFile(srcDir);
        File testDir = new File(prjBase, "test");
        testDir.mkdir();
        File testFile = createFile(testDir);
        File libsrcDir = new File(prjBase, "libsrc");
        libsrcDir.mkdir();
        File libFile = createFile(libsrcDir);
        
        ArrayList sources = new ArrayList();
        AntProjectHelper helper = FreeformProjectGenerator.createProject(prjBase, prjBase, projectName, null);
        
        JavaProjectGenerator.SourceFolder sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "src";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "src";
        sf.encoding = "UTF-8";
        sources.add(sf);
        
        sf = new JavaProjectGenerator.SourceFolder();
        sf.label = "test";
        sf.type = "java";
        sf.style = "packages";
        sf.location = "test";
        sf.encoding = "UTF-16";
        sources.add(sf);
        
        JavaProjectGenerator.putSourceFolders(helper, sources, null);
        JavaProjectGenerator.putSourceViews(helper, sources, null);
        
        FileObject prjDir = helper.getProjectDirectory();
        Project p = ProjectManager.getDefault().findProject(prjDir);
        ProjectManager.getDefault().saveProject(p);
        
        assertNotNull("Project was not created", p);
        assertEquals("Project folder is incorrect", prjDir, p.getProjectDirectory());
        
        FileEncodingQuery feq = p.getLookup().lookup(FileEncodingQuery.class);
        Charset cset = feq.getEncoding(FileUtil.toFileObject(srcFile));
        assertEquals("UTF-8", cset.name());
        cset = feq.getEncoding(FileUtil.toFileObject(testFile));
        assertEquals("UTF-16", cset.name());
        // file not under any src root
        File lonelyFile = new File(prjBase, "testfile");
        lonelyFile.createNewFile();
        cset = feq.getEncoding(FileUtil.toFileObject(lonelyFile));
        assertEquals(cset, Charset.forName(System.getProperty("file.encoding")));
        
    }
    
    private File createFile(File root) throws IOException {
        File pkg = new File(root, "testpackage");
        pkg.mkdir();
        File file = new File(pkg, "Class.java");
        file.createNewFile();
        return file;
    }
    
}

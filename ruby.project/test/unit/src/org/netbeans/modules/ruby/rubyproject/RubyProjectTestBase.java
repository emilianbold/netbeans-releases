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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rubyproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.modules.ruby.RubyTestBase;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tor Norbye
 */
public abstract class RubyProjectTestBase extends RubyTestBase {

    public RubyProjectTestBase(String testName) {
        super(testName);
    }
    
    protected Project getTestProject(String path) {
        FileObject fo = getTestFile(path);
        Project p = FileOwnerQuery.getOwner(fo);
        assertNotNull(p);

        return p;
    }
    
    protected RubyProject getRubyProject(String path) {
        Project p = getTestProject(path);
        assertNotNull(p);
        assertTrue(p instanceof RubyProject);
        
        return (RubyProject)p;
    }
    
    protected void createFilesFromDesc(FileObject folder, String descFile) throws Exception {
        File taskFile = new File(getDataDir(), descFile);
        assertTrue(taskFile.exists());
        BufferedReader br = new BufferedReader(new FileReader(taskFile));
        while (true) {
            String line = br.readLine();
            if (line == null || line.trim().length() == 0) {
                break;
            }
            
            String path = line;
            FileObject f = FileUtil.createData(folder, path);
            assertNotNull(f);
        }
    }

    protected RubyProject createTestProject() throws Exception {
        String projectName = "RubyProject_" + getName();
        
        return createTestProject(projectName);
    }

    protected RubyProject createTestProject(String projectName) throws Exception {
        File dataDir = getDataDir();
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
                
        File projectFile = new File(dataDir, projectName);
        if (projectFile.exists()) {
            FileObject fo = FileUtil.toFileObject(projectFile);
            
            Project p = FileOwnerQuery.getOwner(fo);
            assertNotNull(p);
            assertTrue(p instanceof RubyProject);
            
            return (RubyProject)p;
        }
        
        // Build the Rails project
        FileObject parentDir = FileUtil.toFileObject(dataDir);
        assertNotNull(parentDir);
        FileObject dir = parentDir.createFolder(projectName);
        assertNotNull(dir);
        FileObject nbproject = dir.createFolder("nbproject");
        FileObject projectXml = nbproject.createData("project", "xml");
        String xml =
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<project xmlns=\"http://www.netbeans.org/ns/project/1\">\n" +
"    <type>org.netbeans.modules.ruby.rubyproject</type>\n" +
"    <configuration>\n" +
"        <data xmlns=\"http://www.netbeans.org/ns/ruby-project/1\">\n" +
"            <name>" + projectName + "</name>\n" +
"        </data>\n" +
"    </configuration>\n" +
"</project>\n";
        OutputStream os = projectXml.getOutputStream();
        Writer writer = new BufferedWriter(new OutputStreamWriter(os));
        writer.write(xml);
        writer.close();

        // Create the source folders
        FileUtil.createFolder(dir, "lib");
        FileUtil.createFolder(dir, "test");

        Project p = FileOwnerQuery.getOwner(dir);
        assertNotNull(p);
        assertTrue(p instanceof RubyProject);
        
        return (RubyProject)p;
    }
}

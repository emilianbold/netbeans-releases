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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.refactoring.java;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.diff.LineDiff;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.refactoring.api.RefactoringElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.ui.OpenProjects;



/** LogTestCase
 * @author Jan Becicka
 */
public class LogTestCase extends NbTestCase {
    
    /**
     * state - true - testing
     *       - false - generating goldenfiles
     */
    public static boolean CREATE_GOLDENFILES=false;
    
    private FileObject projectDirFo;
    
    
    private boolean backupFiles = true;
    private static boolean initProjects = true;
    
    
    static {
        if (System.getProperty("create.goldenfiles") != null && System.getProperty("create.goldenfiles").equals("true")) {
            CREATE_GOLDENFILES=true;
        }
    }
    
    /** directory, where the golden and .diff files resides
     */
    protected File classPathWorkDir;
    /** test will generate this file
     */
    protected File refFile;
    
    protected PrintWriter log = null;
    protected PrintWriter ref = null;
    protected PrintWriter golden = null;
    
    public LogTestCase(java.lang.String testName) {
        super(testName);
    }
    
    /** sets the PrintWriters
     */
    protected void setUp() throws IOException {
        MockServices.setServices();
        FileUtil.setMIMEType("java", "text/x-java");
        File cacheFolder = new File(getDataDir(), "var/cache/index");
        cacheFolder.mkdirs();
        System.setProperty("netbeans.user", cacheFolder.getPath());
        
        prepareProject();
        
        FileObject fo = FileUtil.toFileObject(classPathWorkDir);
        if (initProjects) {
            RepositoryUpdater.getDefault().scheduleCompilationAndWait(fo, fo);
            initProjects=false;
        }
        
        try {
            //logs and refs
            refFile = new File(getWorkDir(), getName() + ".ref");
            File logFile = new File(getWorkDir(), getName() + ".log");
            ref = new PrintWriter(new BufferedWriter(new FileWriter(refFile)));
            log = new PrintWriter(new BufferedWriter(new FileWriter(logFile)));
            if (CREATE_GOLDENFILES) { //generates golden files
                File f;
                //generate goldenfile name
                f=getDataDir().getParentFile();
                ArrayList names=new ArrayList();
                names.add("goldenfiles"); //!reverse order
                names.add("data"); //!reverse order
                names.add("unit"); //!reverse order
                while (!f.getName().equals("test")) {
                    if (!f.getName().equals("sys") && !f.getName().equals("work") &&!f.getName().equals("tests")) {
                        names.add(f.getName());
                    }
                    f=f.getParentFile(); //cr. goldenf. inside test/unit/data/
                }
                for (int i=names.size()-1;i > -1;i--) {
                    f=new File(f,(String)(names.get(i)));
                }
                f=new File(f, getClass().getName().replace('.', File.separatorChar));
                f=new File(f, getName()+".pass");
                if (!f.getParentFile().exists()) {
                    f.getParentFile().mkdirs();
                }
                golden=new PrintWriter(new BufferedWriter(new FileWriter(f)));
                log("Passive mode: generate golden file into "+f.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(e.toString(), false);
        }
    }
    
    public void prepareProject() {//default - override for another projects
        classPathWorkDir=new File(getDataDir(), "projects.default.src".replace('.', File.separatorChar));
    }
    
    public void log(String s) {
        log.println(s);
    }
    
    public void log(Object o) {
        log.println(o);
    }
    
    public void ref(String s) {
        ref.println(s);
        if (CREATE_GOLDENFILES) {
            golden.println(s);
        }
    }
    
    public void ref(Object o) {
        ref.println(o.toString());
        if (CREATE_GOLDENFILES) {
            golden.println(o.toString());
        }
    }
    
    public void ref(File file) {
        try {
            BufferedReader br=new BufferedReader(new FileReader(file));
            String line;
            while ((line=br.readLine()) != null) {
                ref(line);
            }
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    /** sets the PrintWriters
     */
    protected void tearDown() {
        ref.close();
        log.close();
        if (CREATE_GOLDENFILES && golden != null) {
            golden.close();
            assertTrue("Passive mode", false);
        } else {
            try {
                assertFile("Golden file differs ", refFile, getGoldenFile(), getWorkDir(), new LineDiff());
            } catch (Exception ex) {
                ex.printStackTrace();
                assertTrue(ex.toString(), false);
            }
        }
    }
    
    public FileObject openProject(String projectName) throws IOException {
        File projectsDir = FileUtil.normalizeFile(new File(getDataDir(), "projects"));
        FileObject projectsDirFO = FileUtil.toFileObject(projectsDir);
        FileObject projdir = projectsDirFO.getFileObject(projectName);
        Project p = ProjectManager.getDefault().findProject(projdir);
        OpenProjects.getDefault().open(new Project[]{p}, false);
        System.out.println(p.getClass().getName());
        //J2SEProject p2 = (J2SEProject)p;
        assertNotNull("Project is not opened",p);
        return projdir;                
    }
    
    private void copyFile(File src,File dst)  {
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            br = new BufferedReader(new FileReader(src));
            fw = new FileWriter(dst);
            String buff;
            while ((buff=br.readLine())!=null) fw.write(buff+"\n");
            fw.close();
            br.close();
        } catch (IOException ioexception) {
            ioexception.printStackTrace(log);
            fail("Error while creating backupfile");
        } finally {
            try {
                if(fw!=null) fw.close();
                if(br!=null) br.close();
            } catch (IOException ioexception) {
                ioexception.printStackTrace(log);
                fail("Error while closing backupfile");
            }
        }
    }
    
    private String getRelativeFileName(FileObject fo) {
        String relPath = FileUtil.getRelativePath(projectDirFo, fo);
        String res = relPath.replace('/', '.');
        if(res.startsWith("src.")) res = res.substring(4);
        return res;
    }
    
    Map<String,LinkedList<String>> refactoredFiles;
    
    protected void addRefactoringElement(RefactoringElement element) throws IOException {
        FileObject fo = element.getParentFile();
        String relPath = getRelativeFileName(fo);
        if(!refactoredFiles.keySet().contains(relPath)) { //new file
            if(backupFiles) {
                File fBackUp = new File(getWorkDir(),getRelativeFileName(fo));
                File oFile = FileUtil.toFile(fo);
                copyFile(oFile, fBackUp);
            }
            refactoredFiles.put(relPath, new LinkedList<String>());
        }
        List<String> list = refactoredFiles.get(relPath);
        list.add(element.getDisplayText());
    }
    
    protected void dumpRefactoredFiles() {
        
        for (String fileName: refactoredFiles.keySet()) {
            ref(fileName);
            ref("--------------------");
            for(String text : refactoredFiles.get(fileName)) {
                ref(text);
            }
            ref("\n");
        }
    }                
    
    protected FileObject getFileInProject(String project,String file) throws IOException {
        projectDirFo = openProject(project);
        FileObject test = projectDirFo.getFileObject(file);
        return test;
    }
        
}


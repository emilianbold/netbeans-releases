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
package org.netbeans.modules.refactoring.ast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.refactoring.java.ParameterSetter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jiri Prox
 */
public class ASTGenTest extends NbTestCase {

    protected PrintWriter ref = null;
    
    protected PrintWriter log = null;
    
    public static File sourceDir = null;
    
    public static File resultDir = null;    
    
    public static FileObject openedProject = null;

    public ASTGenTest(String name) {
        super(name);
    }
    
    @Override
    @SuppressWarnings("deprecation")
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        MockServices.setServices();
        FileUtil.setMIMEType("java", "text/x-java");
        File cacheFolder = new File(getWorkDir(), "var/cache/index");
        cacheFolder.mkdirs();
        System.setProperty("netbeans.user", cacheFolder.getPath());
        if (System.getProperty("source.dir") != null) {
            String sourceDirPath = System.getProperty("source.dir");
            File directory = new File(sourceDirPath);
            if(directory.isAbsolute()) sourceDir  = directory;
            else sourceDir = new File(getDataDir(),sourceDirPath);
        } else {
            sourceDir = getDataDir();
        }
        
        if(System.getProperty("result.dir")!= null) {
            String resultDirPath = System.getProperty("result.dir");
            File directory = new File(resultDirPath);
            if(directory.isAbsolute()) resultDir  = directory;
            else resultDir = new File(getDataDir(),resultDirPath);
        } else {
            resultDir = getWorkDir();
        }        
        System.out.println("#### " + getName() + " ####");
        ref = new PrintWriter(new File(resultDir, getName()+".ref"));
        log = new PrintWriter(new File(resultDir, getName()+".log"));                
        ref.println("#### " + getName() + " ####");
        log.println("#### " + getName() + " ####");
    }
        
    
    @Override
    protected void tearDown() throws Exception {
        log.close();
        ref.close();
        super.tearDown();
    }

    public void testRename() throws IOException, InterruptedException {
        File[] projects = sourceDir.listFiles();
        for (int i = 0; i < projects.length; i++) {
            File project = projects[i];
            if(project.isFile()) continue; //only directories
            FileObject srcRoot = FileUtil.toFileObject(new File(project,"src"));
            RepositoryUpdater.getDefault().scheduleCompilationAndWait(srcRoot, srcRoot).await();
            openedProject = openProject(project);
            String testClass = "A";
            if(System.getProperty("test.class")!= null) {
                testClass = System.getProperty("test.class");                
            }            
            final String newName;
            if(System.getProperty("new.name")!= null) {
                newName = System.getProperty("new.name");
            } else {
                newName = "NewName";
            }
            FileObject test = openedProject.getFileObject("src/"+testClass.replace('.', '/')+".java");
            final RenameRefactoring renameRefactoring = new RenameRefactoring(Lookups.singleton(test));
            performASTGenRerfactoring(renameRefactoring, new ParameterSetter() {
                public void setParameters() {
                    renameRefactoring.setNewName(newName);
                }
            }, project);
        }
    }
       
    public FileObject openProject(File project) throws IOException {        
        FileObject projectFO = FileUtil.toFileObject(project);        
        Project p = ProjectManager.getDefault().findProject(projectFO);
        OpenProjects.getDefault().open(new Project[]{p}, false);                
        assertNotNull("Project is not opened",p);
        log.println("Project "+project.getName()+" opened");
        return projectFO;                
    }

    public boolean performASTGenRerfactoring(AbstractRefactoring absRefactoring, ParameterSetter parameterSetter, File project) {
        Problem problem = absRefactoring.preCheck();
        boolean fatal = false;
        while (problem != null) {
            ref.print(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        parameterSetter.setParameters();
        problem = absRefactoring.fastCheckParameters();
        while (problem != null) {
            ref.print(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        problem = absRefactoring.checkParameters();
        while (problem != null) {
            ref.print(problem.getMessage());
            fatal = fatal || problem.isFatal();
            problem = problem.getNext();
        }
        if (fatal) {
            return false;
        }
        RefactoringSession rs = RefactoringSession.create("Session");
        absRefactoring.prepare(rs);        
        rs.doRefactoring(true);
        writeResult(project);
        return true;
    }

    private void writeResult(File project) {
        File srcRoot = new File(sourceDir,project.getName()+"/src");
        copyDir(srcRoot,new File(resultDir,project.getName()), true);                
    }
    
    public  void copyDir(File srcDir, File destDir,boolean recursive) {
        if(!destDir.exists()) destDir.mkdirs();
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().startsWith(".")) continue; //skip hidden
            if(file.isDirectory()) {
                if(recursive) copyDir(file,new File(destDir,file.getName()),true);
            } else {
                copyFile(file, new File(destDir,file.getName()));
            }
        }
    }
    
    public void copyFile(File src,File dst)  {
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
            fail("Error while storing results");
        } finally {
            try {
                if(fw!=null) fw.close();
                if(br!=null) br.close();
            } catch (IOException ioexception) {
                ioexception.printStackTrace(log);
                fail("Error while storing results");
            }
        }
    }
        
}
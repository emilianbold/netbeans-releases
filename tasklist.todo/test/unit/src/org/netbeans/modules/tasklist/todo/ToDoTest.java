package org.netbeans.modules.tasklist.todo;
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



import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.DialogDisplayerImpl;
import org.netbeans.modules.apisupport.project.InstalledFileLocatorImpl;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.tasklist.projectint.MainProjectScanningScope;
import org.netbeans.modules.tasklist.projectint.OpenedProjectsScanningScope;
import org.netbeans.spi.tasklist.Task;
import org.openide.DialogDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Tests ToDo scanner.
 *
 * @author Petr Zajac
 */
public class ToDoTest extends TestBase {
    
    static {
        // #65461: do not try to load ModuleInfo instances from ant module
        System.setProperty("org.netbeans.core.startup.ModuleSystem.CULPRIT", "true");
        LayerTestBase.Lkp.setLookup(new Object[0]);
        DialogDisplayerImpl.returnFromNotify(DialogDescriptor.NO_OPTION);
    }
    
    public ToDoTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        noDataDir = true;
        
        super.setUp();
        InstalledFileLocatorImpl.registerDestDir(destDirF);
    }
    
    String javaFile = "public class Main1 {\n" +
            "/** TODO \n" +
            " * TODO \n" +
            "*/\n" +
            "}";

    
    static FileObject createSrcFile(NbModuleProject prj,String path,String content ) throws IOException {
        FileObject fo = prj.getSourceDirectory().createData(path);
        Writer writer = new OutputStreamWriter(fo.getOutputStream());
        writer.write(content);
        writer.close();
        return fo;
    }
    
    private List<Task> scanOpenProjectsTasks() {
        TodoTaskScanner tts = TodoTaskScanner.create();
        OpenedProjectsScanningScope scope = OpenedProjectsScanningScope.create();
        List<Task> tasks = new ArrayList<Task>();
        
        
        for (FileObject jfo : scope) {
            List<? extends Task> filTasks = tts.scan(jfo);
            if (filTasks != null) {
                tasks.addAll(filTasks);
            }
        }
        return tasks;
    }
    private List<Task> scanMainProjectTasks() {
        TodoTaskScanner tts = TodoTaskScanner.create();
        MainProjectScanningScope scope = MainProjectScanningScope.create();
        List<Task> tasks = new ArrayList<Task>();
        
        for (FileObject jfo : scope) {
            List<? extends Task> filTasks = tts.scan(jfo);
            if (filTasks != null) {
                tasks.addAll(filTasks);
            }
        }
        return tasks;
    }
    public void testProject1() throws IOException {
        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
        NbModuleProject prj2 = generateStandaloneModule(getWorkDir(), "prj2");
        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
        FileObject fo2 = createSrcFile(prj1,"Main2.java",javaFile);
        FileObject fo3 = createSrcFile(prj2,"Main.java",javaFile);
        OpenProjects.getDefault().open(new Project[]{prj1,prj2}, false);

        List<Task> tasks = scanOpenProjectsTasks();        
        assertEquals("Number of tasks",6,tasks.size());
        
        OpenProjects.getDefault().close(new Project[]{prj1});
        tasks = scanOpenProjectsTasks();   
        logTasks(tasks); 
        assertEquals("Number of tasks",2,tasks.size());
    }
    public void testMainProject() throws Exception {
        NbModuleProject prj1 = generateStandaloneModule(getWorkDir(), "prj1");
        NbModuleProject prj2 = generateStandaloneModule(getWorkDir(), "prj2");
        FileObject fo = createSrcFile(prj1,"Main.java",javaFile);
        FileObject fo2 = createSrcFile(prj1,"Main2.java",javaFile);
        FileObject fo3 = createSrcFile(prj2,"Main.java",javaFile);
        OpenProjects.getDefault().open(new Project[]{prj1,prj2}, false);
        OpenProjects.getDefault().setMainProject(prj1);
        List<Task> tasks = scanMainProjectTasks();        
        assertEquals("Number of tasks",4,tasks.size());
        OpenProjects.getDefault().close(new Project[]{prj1});
        tasks = scanMainProjectTasks();        
        assertEquals("Number of tasks",0,tasks.size());        
        
        // test subproject
        
        SuiteProject suite = generateSuite("suiteproject");
        NbModuleProject prj3 = generateSuiteComponent(suite, getWorkDir(), "prjInSuite");
        FileObject fo4 = createSrcFile(prj3,"Main.java",javaFile);
        OpenProjects.getDefault().open(new Project[]{suite,prj3}, false);
        OpenProjects.getDefault().setMainProject(suite);
        tasks = scanMainProjectTasks();        
        assertEquals("Number of tasks",2,tasks.size());
    }
    
    private void logTasks(List<Task> tasks) {
        for (Task t : tasks) {
            System.out.println( t );
        }
    }       
}


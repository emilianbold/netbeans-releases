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
package org.netbeans.modules.j2ee.clientproject.classpath;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.test.TestUtil;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex.Action;

/**
 *
 * @author Jan Lahoda
 */
public class AppClientProjectClassPathExtenderTest extends NbTestCase {

    private String serverID;
    private FileObject workDir;

    public AppClientProjectClassPathExtenderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        workDir = TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }

    public void testPropertyChangeDeadlock74204() throws Exception {
        File prjDirF = new File(FileUtil.toFile(workDir), "test");
        AntProjectHelper helper = AppClientProjectGenerator.createProject(prjDirF, "test-project",
                "test.MyMain", J2eeModule.JAVA_EE_5, serverID);
        final Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        
        final Object privateLock = new Object();
        final CountDownLatch sync = new CountDownLatch(2);
        
        FileChangeListener l = new FileChangeAdapter() {
            public @Override void fileChanged(FileEvent fe) {
                try {
                    sync.countDown();
                    sync.await();
                    synchronized (privateLock) {}
                } catch (InterruptedException ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        };
        
        project.getProjectDirectory().getFileObject("nbproject").getChildren();
        project.getProjectDirectory().getFileObject("nbproject").addFileChangeListener(l);
        project.getProjectDirectory().getFileObject("nbproject/project.properties").addFileChangeListener(l);
        
        new Thread() {
            public void run() {
                synchronized (privateLock) {
                    try {
                        sync.countDown();
                        sync.await();
                        ProjectManager.mutex().readAccess(new Action() {
                            public Object run() {
                                return null;
                            }
                        });
                    } catch (InterruptedException ex) {
                        ErrorManager.getDefault().notify(ex);
                    }
                }
            }
        }.start();
        
        EditableProperties ep = new EditableProperties();
        
        ep.put(AppClientProjectProperties.JAVAC_CLASSPATH, "y");
        
        helper.putProperties(helper.PROJECT_PROPERTIES_PATH, ep);
    }
    
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.execution.NativeExecutor;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author Alexander Simon
 */
public abstract class MakeProjectBase  extends NbTestCase {
    public MakeProjectBase(String name) {
        super(name);
        System.setProperty("org.netbeans.modules.cnd.makeproject.api.runprofiles", "true"); // NOI18N
        System.setProperty("cnd.mode.unittest", "true");
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(MakeProjectType.class);
        startupModel();
    }

    private void startupModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        model.startup();
        RepositoryUtils.cleanCashes();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        shutdownModel();
    }

    private final void shutdownModel() {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        waitModelTasks(model);
        model.shutdown();
        waitModelTasks(model);
        RepositoryUtils.cleanCashes();
        RepositoryUtils.debugClear();
    }

    private void waitModelTasks(ModelImpl model) {
        Cancellable task = model.enqueueModelTask(new Runnable() {
            public void run() {
            }
        }, "wait finished other tasks"); //NOI18N
        if (task instanceof Task) {
            ((Task) task).waitFinished();
        }
    }

    public void performTestProject(String URL){
        try {
            final String path = download(URL);
            File configure = new File(path+File.separator+"configure");
            File makeFile = new File(path+File.separator+"Makefile");
            if (!configure.exists()) {
                if (!makeFile.exists()){
                    assertTrue("Cannot find configure or Makefile in folder "+path, false);
                }
            }
            WizardDescriptor wizard = new WizardDescriptor() {
                @Override
                public synchronized Object getProperty(String name) {
                    if ("simpleMode".equals(name)) {
                        return Boolean.TRUE;
                    } else if ("path".equals(name)) {
                        return path;
                    } else if ("configureName".equals(name)) {
                        return path+"/configure";
                    } else if ("realFlags".equals(name)) {
                        return "CFLAGS=\"-g3 -gdwarf-2\" CXXFLAGS=\"-g3 -gdwarf-2\"";
                    } else if ("buildProject".equals(name)) {
                        return Boolean.TRUE;
                    }
                    return null;
                }
            };
            ImportProject importer = new ImportProject(wizard);
            importer.setUILessMode();
            importer.create();
            OpenProjects.getDefault().open(new Project[]{importer.getProject()}, false);
            while(!importer.isFinished()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            CsmModel model = CsmModelAccessor.getModel();
            Project makeProject = importer.getProject();
            assertTrue("Not found model", model != null);
            assertTrue("Not found make project", makeProject != null);
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            assertTrue("Not found native project", np != null);
            CsmProject csmProject = model.getProject(np);
            assertTrue("Not found model project", csmProject != null);
            csmProject.waitParse();
            perform(csmProject);
            OpenProjects.getDefault().close(new Project[]{makeProject});
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            assertTrue(ex.getMessage(), false);
        }
    }

    abstract void perform(CsmProject csmProject);

    private String download(String urlName) throws IOException {
        String zipName = urlName.substring(urlName.lastIndexOf('/')+1);
        String tarName = zipName.substring(0, zipName.lastIndexOf('.'));
        String packageName = tarName.substring(0, tarName.lastIndexOf('.'));

        String dataPath;
        if (false) {
            // local downloads
            dataPath = getDataDir().getAbsolutePath();
            if (dataPath.endsWith("/data") || dataPath.endsWith("\\data")) {
                dataPath = dataPath.substring(0, dataPath.length()-4)+"downloads";
            }
        } else {
            // downloads in tmp dir
            dataPath = System.getProperty("java.io.tmpdir");
            if (dataPath.endsWith(File.separator)) {
                dataPath += System.getProperty("user.name") +  "-cnd-test-downloads";
            } else {
                dataPath += File.separator + System.getProperty("user.name") +  "-cnd-test-downloads";
            }
        }
        File fileDataPath = new File(dataPath);
        if (!fileDataPath.exists()) {
            FileUtil.createFolder(fileDataPath);
        }
        String createdFolder = dataPath+"/"+packageName;
        final AtomicBoolean finish = new AtomicBoolean(false);
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted() {
            }
            public void executionFinished(int rc) {
                finish.set(true);
            }
        };
        NativeExecutor ne = null;
        File fileCreatedFolder = new File(createdFolder);
        if (!fileCreatedFolder.exists() || fileCreatedFolder.list().length == 0){
            FileUtil.createFolder(fileCreatedFolder);
            ne = new NativeExecutor(dataPath,"wget", urlName, new String[0], "wget", "run", false, false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"gzip", "-d "+zipName, new String[0], "gzip", "run", false, false);
            waitExecution(ne, listener, finish);
            ne = new NativeExecutor(dataPath,"tar", "xf "+tarName, new String[0], "tar", "run", false, false);
            waitExecution(ne, listener, finish);
        }
        ne = new NativeExecutor(createdFolder, "rm", "-rf nbproject", new String[0], "rm", "run", false, false);
        waitExecution(ne, listener, finish);
        return createdFolder;
    }

    private void waitExecution(NativeExecutor ne, ExecutionListener listener, AtomicBoolean finish){
        finish.set(false);
        ne.addExecutionListener(listener);
        try {
            ne.execute();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        while(!finish.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}

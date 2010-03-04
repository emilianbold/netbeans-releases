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

package org.netbeans.modules.cnd.remote.pbuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.MakeProject;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.cnd.remote.support.RemoteUtil;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.CommonTasksSupport;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;
import org.netbeans.modules.nativeexecution.test.RcFile;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;

/**
 * A common base class for tests that build remote project
 * @author Vladimir Kvashin
 */
public class RemoteBuildTestBase extends RemoteTestBase {

    public RemoteBuildTestBase(String testName, ExecutionEnvironment execEnv) {
        super(testName, execEnv);
    }

    public RemoteBuildTestBase(String testName) {
        super(testName);
    }

    protected int getSampleBuildTimeout() throws Exception {
        int result = 120;
        RcFile rcFile = NativeExecutionTestSupport.getRcFile();
        String timeout = rcFile.get("remote", "sample.build.timeout");
        if (timeout != null) {
            result = Integer.parseInt(timeout);
        }
        return result;
    }

    protected static void instantiateSample(String name, final File destdir) throws IOException, InterruptedException, InvocationTargetException {
        if(destdir.exists()) {
            assertTrue("Can not remove directory " + destdir.getAbsolutePath(), removeDirectoryContent(destdir));
        }
        final FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        final DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        final AtomicReference<IOException> exRef = new AtomicReference<IOException>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
                TemplateWizard wiz = new TemplateWizard();
                wiz.setTemplate(templateDO);
                projectCreator.initialize(wiz);
                wiz.putProperty("name", destdir.getName());
                wiz.putProperty("projdir", destdir);
                try {
                    projectCreator.instantiate(wiz);
                } catch (IOException ex) {
                    exRef.set(ex);
                }
            }
        });
        if (exRef.get() != null) {
            throw exRef.get();
        }
        return;
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(MakeProjectType.class);
        list.addAll(super.getServices());
        return list;
    }

    protected void setupHost() throws Exception {
        setupHost((String) null);
    }

    protected void setSyncFactory(String remoteSyncFactoryID) {
        ServerRecord record = ServerList.get(getTestExecutionEnvironment());
        assertNotNull(record);
        RemoteSyncFactory syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        assertNotNull(syncFactory);
        ((RemoteServerRecord) record).setSyncFactory(syncFactory);
    }

    protected void setupHost(String remoteSyncFactoryID) throws Exception {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        setupHost(env);
        RemoteSyncFactory syncFactory = null;
        if (remoteSyncFactoryID != null) {
            syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        }
        if (syncFactory == null) {
            syncFactory = RemoteSyncFactory.getDefault();
        }
        RemoteServerRecord rec = (RemoteServerRecord) ServerList.addServer(env, env.getDisplayName(), syncFactory, true, true);
        rec.setSyncFactory(syncFactory);
        assertNotNull("Null ServerRecord for " + env, rec);
        String home = RemoteUtil.getHomeDirectory(env);
        String root = home + "/netbeans/.remote";
        Future<Integer> rmDirTask = CommonTasksSupport.rmDir(env, root, true, new PrintWriter(System.err));
        rmDirTask.get();
        assertFalse(HostInfoUtils.fileExists(env, root));
    }

    protected FileObject prepareSampleProject(String sampleName, String projectDirShortName) throws IOException, InterruptedException, InvocationTargetException {
        // reusing directories makes debugging much more difficult, so we add host name
        projectDirShortName += "_" + getTestHostName();
        File projectDir = new File(getWorkDir(), projectDirShortName);
        instantiateSample(sampleName, projectDir);
        FileObject projectDirFO = FileUtil.toFileObject(projectDir);
        ConfigurationDescriptorProvider descriptorProvider = new ConfigurationDescriptorProvider(projectDirFO);
        MakeConfigurationDescriptor descriptor = descriptorProvider.getConfigurationDescriptor(true);
        descriptor.save(); // make sure all necessary configuration files in nbproject/ are written
        File makefile = new File(projectDir, "Makefile");
        FileObject makefileFileObject = FileUtil.toFileObject(makefile);
        assertTrue("makefileFileObject == null", makefileFileObject != null);
        DataObject dObj = null;
        try {
            dObj = DataObject.find(makefileFileObject);
        } catch (DataObjectNotFoundException ex) {
        }
        assertTrue("DataObjectNotFoundException", dObj != null);
        Node node = dObj.getNodeDelegate();
        assertTrue("node == null", node != null);
        MakeExecSupport ses = node.getCookie(MakeExecSupport.class);
        assertTrue("ses == null", ses != null);
        return projectDirFO;
    }

    protected void setDefaultCompilerSet(String name) {
        ExecutionEnvironment execEnv = getTestExecutionEnvironment();
        ServerRecord record = ServerList.get(execEnv);
        assertNotNull(record);
        final CompilerSetManager csm = CompilerSetManager.get(execEnv);
        for (CompilerSet cset : csm.getCompilerSets()) {
            if (cset.getName().equals(name)) {
                csm.setDefault(cset);
                break;
            }
        }
    }

    protected void changeProjectHost(FileObject projectDir) throws Exception {
        changeProjectHost(FileUtil.toFile(projectDir));
    }

    protected void changeProjectHost(File projectDir) throws Exception {
        File nbproject = new File(projectDir, "nbproject");
        assertTrue("file does not exist: " + nbproject.getAbsolutePath(), nbproject.exists());
        File confFile = new File(nbproject, "configurations.xml");
        assertTrue(confFile.exists());
        String text = readFile(confFile);
        String openTag = "<developmentServer>";
        String closeTag = "</developmentServer>";
        int start = text.indexOf(openTag);
        start += openTag.length();
        assertTrue(start >= 0);
        int end = text.indexOf(closeTag);
        assertTrue(end >= 0);
        StringBuilder newText = new StringBuilder();
        newText.append(text.substring(0, start));
        newText.append(ExecutionEnvironmentFactory.toUniqueID(getTestExecutionEnvironment()));
        newText.append(text.substring(end));
        writeFile(confFile, newText);
    }

    protected void buildSample(Sync sync, Toolchain toolchain, String projectName, String projectDir, int count) throws Exception {
        int timeout = getSampleBuildTimeout();
        buildSample(sync, toolchain, projectName, projectDir, count, timeout, timeout);
    }

    protected void buildSample(Sync sync, Toolchain toolchain, String projectName, String projectDir,
            int count, int firstTimeout, int subsequentTimeout) throws Exception {

        setupHost();
        setSyncFactory(sync.ID);

        assertEquals("Wrong sync factory:", sync.ID,
                ServerList.get(getTestExecutionEnvironment()).getSyncFactory().getID());

        setDefaultCompilerSet(toolchain.ID);
        assertEquals("Wrong tools collection", toolchain.ID,
                CompilerSetManager.get(getTestExecutionEnvironment()).getDefaultCompilerSet().getName());

        clearRemoteSyncRoot();
        FileObject projectDirFO = prepareSampleProject(projectName, "Args_01");
        MakeProject makeProject = (MakeProject) ProjectManager.getDefault().findProject(projectDirFO);
        for (int i = 0; i < count; i++) {
            if (count > 0) {
                System.err.printf("BUILDING %s, PASS %d\n", projectName, i);
            }
            buildProject(makeProject, firstTimeout, TimeUnit.SECONDS);
        }
    }
}

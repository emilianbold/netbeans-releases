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

package org.netbeans.modules.cnd.remote.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.netbeans.modules.cnd.makeproject.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.wizards.MakeSampleProjectIterator;
import org.netbeans.modules.cnd.remote.server.RemoteServerRecord;
import org.netbeans.modules.cnd.remote.support.RemoteTestBase;
import org.netbeans.modules.cnd.spi.remote.RemoteSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
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

    protected static void instantiateSample(String name, File destdir) throws IOException {
        FileObject templateFO = FileUtil.getConfigFile("Templates/Project/Samples/Native/" + name);
        assertNotNull("FileObject for " + name + " sample not found", templateFO);
        DataObject templateDO = DataObject.find(templateFO);
        assertNotNull("DataObject for " + name + " sample not found", templateDO);
        MakeSampleProjectIterator projectCreator = new MakeSampleProjectIterator();
        TemplateWizard wiz = new TemplateWizard();
        wiz.setTemplate(templateDO);
        projectCreator.initialize(wiz);
        wiz.putProperty("name", destdir.getName());
        wiz.putProperty("projdir", destdir);
        projectCreator.instantiate(wiz);
        return;
    }

    @Override
    protected List<Class> getServises() {
        List<Class> list = new ArrayList<Class>();
        list.add(MakeProjectType.class);
        list.addAll(super.getServises());
        return list;
    }

    protected void setupHost() {
        setupHost((String) null);
    }

    protected void setSyncFactory(String remoteSyncFactoryID) {
        ServerRecord record = ServerList.get(getTestExecutionEnvironment());
        assertNotNull(record);
        RemoteSyncFactory syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        assertNotNull(syncFactory);
        ((RemoteServerRecord) record).setSyncFactory(syncFactory);
    }

    protected void setupHost(String remoteSyncFactoryID) {
        ExecutionEnvironment env = getTestExecutionEnvironment();
        setupHost(env);
        RemoteSyncFactory syncFactory = null;
        if (remoteSyncFactoryID != null) {
            syncFactory = RemoteSyncFactory.fromID(remoteSyncFactoryID);
        }
        if (syncFactory == null) {
            syncFactory = RemoteSyncFactory.getDefault();
        }
        ServerRecord rec = ServerList.addServer(env, env.getDisplayName(), syncFactory, true, true);
        assertNotNull("Null ServerRecord for " + env, rec);
    }

    protected FileObject prepareSampleProject(String sampleName, String projectDirShortName) throws IOException {
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
        final CompilerSetManager csm = CompilerSetManager.getDefault(execEnv);
        for (CompilerSet cset : csm.getCompilerSets()) {
            if (cset.getName().equals(name)) {
                csm.setDefault(cset);
                break;
            }
        }
    }


}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.discovery.project.cases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.WizardConstants;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author as204739
 */
public class SimpleScriptTestCase extends MakeProjectTestBase {

    public SimpleScriptTestCase() {
        super("SimpleTestCase");
    }

    @Override
    protected boolean optimizeSimpleProjects() {
        return false;
    }

    @Test
    public void testSimple() throws Exception {
        File dataDir = getDataDir();
        String zip = dataDir.getAbsolutePath()+"/org/netbeans/modules/cnd/discovery/project/DiscoveryTestApplication.tar.gz";
        assert new File(zip).exists() : "Not  found file "+zip;
        performTestProject(zip, null, false, "/src");
    }
    
    @Override
    public void performTestProject(String URL, List<String> additionalScripts, boolean useSunCompilers, final String subFolder) throws Exception {
        Map<String, String> tools = findTools();
        CompilerSet def = CompilerSetManager.get(getEE()).getDefaultCompilerSet();
        if (useSunCompilers) {
            if (def != null && def.getCompilerFlavor().isGnuCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(getEE()).getCompilerSets()){
                    if (set.getCompilerFlavor().isSunStudioCompiler()) {
                        CompilerSetManager.get(getEE()).setDefault(set);
                        break;
                    }
                }
            }
        } else {
            if (def != null && def.getCompilerFlavor().isSunStudioCompiler()) {
                for(CompilerSet set : CompilerSetManager.get(getEE()).getCompilerSets()){
                    if (set.getCompilerFlavor().isGnuCompiler()) {
                        CompilerSetManager.get(getEE()).setDefault(set);
                        break;
                    }
                }
            }
        }
        def = CompilerSetManager.get(getEE()).getDefaultCompilerSet();
        final boolean isSUN = def != null ? def.getCompilerFlavor().isSunStudioCompiler() : false;
        if (tools == null) {
            assertTrue("Please install required tools.", false);
            System.err.println("Test did not run because required tools do not found");
            return;
        }
        try {
            String aPath = download(URL, additionalScripts, tools);

            final File configure = detectConfigure(aPath+subFolder);
            final String path = aPath;
            if (Utilities.isWindows()){
                // cygwin does not allow test discovery in real time, so disable tests on windows
                //return;
            }

            WizardDescriptor wizard = new WizardDescriptor() {
                @Override
                public synchronized Object getProperty(String name) {
                    if (WizardConstants.PROPERTY_SIMPLE_MODE.equals(name)) {
                        return Boolean.FALSE;
                    } else if (WizardConstants.PROPERTY_NATIVE_PROJ_DIR.equals(name)) {
                        return path;
                    } else if (WizardConstants.PROPERTY_NATIVE_PROJ_FO.equals(name)) {
                        return CndFileUtils.toFileObject(path);
                    } else if (WizardConstants.PROPERTY_PROJECT_FOLDER.equals(name)) {
                        ExecutionEnvironment ee = ExecutionEnvironmentFactory.getLocal();
                        return new FSPath(FileSystemProvider.getFileSystem(ee), RemoteFileUtil.normalizeAbsolutePath(path, ee));
                    } else if (WizardConstants.PROPERTY_TOOLCHAIN.equals(name)) {
                        return CompilerSetManager.get(getEE()).getDefaultCompilerSet();
                    } else if (WizardConstants.PROPERTY_HOST_UID.equals(name)) {
                        return ExecutionEnvironmentFactory.toUniqueID(getEE());
                    } else if (WizardConstants.PROPERTY_CONFIGURE_SCRIPT_PATH.equals(name)) {
                        return configure.getAbsolutePath();
                    } else if (WizardConstants.PROPERTY_NAME.equals(name)) {
                        return new File(path).getName();
                    } else if (WizardConstants.PROPERTY_WORKING_DIR.equals(name)) {
                        return path;
                    } else if (WizardConstants.PROPERTY_BUILD_COMMAND.equals(name)) {
                        return "./build.bash";
                    } else if (WizardConstants.PROPERTY_CLEAN_COMMAND.equals(name)) {
                        return "./clean.bash";
                    } else if (WizardConstants.PROPERTY_RUN_CONFIGURE.equals(name)) {
                        return "true";
                    } else if (WizardConstants.PROPERTY_CONSOLIDATION_LEVEL.equals(name)) {
                        return ConsolidationStrategy.FILE_LEVEL;
                    } else if (WizardConstants.PROPERTY_SOURCE_FOLDERS_FILTER.equals(name)) {
                        return MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN_EXISTING_PROJECT;
                    } else if (WizardConstants.PROPERTY_SOURCE_FOLDERS.equals(name)) {
                        List<SourceFolderInfo> list = new ArrayList<SourceFolderInfo>();
                        list.add(new SourceFolderInfo() {

                            @Override
                            public FileObject getFileObject() {
                                return CndFileUtils.toFileObject(path);
                            }

                            @Override
                            public String getFolderName() {
                                return CndFileUtils.toFileObject(path).getNameExt();
                            }

                            @Override
                            public boolean isAddSubfoldersSelected() {
                                return true;
                            }
                        });
                        return list.iterator();
                    } else if ("realFlags".equals(name)) {
                        if (isSUN) {
                            return "CC=cc CXX=CC CFLAGS=-g CXXFLAGS=-g";
                        } else {
                            return "CFLAGS=\"-g3 -gdwarf-2\" CXXFLAGS=\"-g3 -gdwarf-2\"";
                        }
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
            int i = 0;
            while(!importer.isFinished()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (i > 10 && !OpenProjects.getDefault().isProjectOpen(importer.getProject())){
                    break;
                }
                i++;
            }

            assertEquals("Failed configure", ImportProject.State.Successful, importer.getState().get(ImportProject.Step.Configure));
            assertEquals("Failed build", ImportProject.State.Successful, importer.getState().get(ImportProject.Step.Make));
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
}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.discovery.projectimport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.actions.MakeAction;
import org.netbeans.modules.cnd.actions.ShellRunAction;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.api.utils.AllSourceFileFilter;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProjectWizardPanel1.WizardStorage;
import org.netbeans.modules.cnd.discovery.wizard.ConsolidationStrategyPanel;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryWizardDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.bridge.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.execution.ShellExecSupport;
import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.wizards.IteratorExtension;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Alexander Simon
 */
public class ImportProject {

    private File dirF;
    private String name;
    private String makefileName = "Makefile";  // NOI18N
    private String makefilePath;
    private String configurePath;
    private String configureArguments;
    private boolean runConfigure;
    private boolean setAsMain;
    private String workingDir;
    private String buildCommand = "${MAKE} all";  // NOI18N
    private String cleanCommand = "${MAKE} clean";  // NOI18N
    private String buildResult = "";  // NOI18N
    private Project makeProject;
    private boolean runMake;
    private boolean postponeModel;


    public ImportProject(WizardStorage wizardStorage) {
        String path = wizardStorage.getPath().trim();
        dirF = new File(path);
        name = dirF.getName();
        makefileName = "Makefile-"+name+".mk"; // NOI18N
        workingDir = path;
        File file = new File(path + "/Makefile"); // NOI18N
        if (file.exists() && file.isFile() && file.canRead()) {
            makefilePath = file.getAbsolutePath();
        } else {
            file = new File(path + "/makefile"); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                makefilePath = file.getAbsolutePath();
            } else {
                file = new File(path + "/configure"); // NOI18N
                configurePath = file.getAbsolutePath();
                configureArguments = wizardStorage.getRealFlags();
                runConfigure = true;
                file = new File(path + "/Makefile"); // NOI18N
                makefilePath = file.getAbsolutePath();
            }
        }
        runMake = wizardStorage.isBuildProject();
        setAsMain = wizardStorage.isSetMain();
    }

    public Set<FileObject> create() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        dirF = FileUtil.normalizeFile(dirF);
        MakeConfiguration extConf = new MakeConfiguration(dirF.getPath(), "Default", MakeConfiguration.TYPE_MAKEFILE); // NOI18N
        String workingDirRel = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(workingDir));
        workingDirRel = FilePathAdaptor.normalize(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(workingDirRel);
        extConf.getMakefileConfiguration().getBuildCommand().setValue(buildCommand);
        extConf.getMakefileConfiguration().getCleanCommand().setValue(cleanCommand);
        // Build result
        if (buildResult != null && buildResult.length() > 0) {
            buildResult = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(buildResult));
            buildResult = FilePathAdaptor.normalize(buildResult);
            extConf.getMakefileConfiguration().getOutput().setValue(buildResult);
        }
        // Add makefile and configure script to important files
        ArrayList<String> importantItems = new ArrayList<String>();
        if (makefilePath != null && makefilePath.length() > 0) {
            makefilePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(makefilePath));
            makefilePath = FilePathAdaptor.normalize(makefilePath);
            importantItems.add(makefilePath);
        }
        if (configurePath != null && configurePath.length() > 0) {
            File configureFile = new File(configurePath);
            configurePath = IpeUtils.toRelativePath(dirF.getPath(), FilePathAdaptor.naturalize(configurePath));
            configurePath = FilePathAdaptor.normalize(configurePath);
            importantItems.add(configurePath);

            try {
                FileObject configureFileObject = FileUtil.toFileObject(configureFile);
                DataObject dObj = DataObject.find(configureFileObject);
                Node node = dObj.getNodeDelegate();

                // Add arguments to configure script?
                if (configureArguments != null) {
                    ShellExecSupport ses = node.getCookie(ShellExecSupport.class);
                    // Keep user arguments as is in args[0]
                    ses.setArguments(new String[]{configureArguments});
                }
                // Possibly run the configure script
                if (runConfigure) {
                    // If no makefile, create empty one so it shows up in Interesting Files
                    //if (!makefileFile.exists()) {
                    //    makefileFile.createNewFile();
                    //}
                    final boolean userRunMake = runMake;
                    //final File configureLog = createTempFile("configure");
                    ExecutionListener listener = new ExecutionListener() {
                        public void executionStarted() {
                        }
                        public void executionFinished(int rc) {
                            if (userRunMake && rc == 0) {
                                //parseConfigureLog(configureLog);
                                makeProject(false);
                            }
                        }
                    };
                    if (runMake) {
                        runMake = false;
                        postponeModel = true;
                    }
                    ShellRunAction.performAction(node, listener, null);//, new BufferedWriter(new FileWriter(configureLog)));
                }
            } catch (DataObjectNotFoundException e) {
            }
        }
        Iterator<String> importantItemsIterator = importantItems.iterator();
        if (!importantItemsIterator.hasNext()) {
            importantItemsIterator = null;
        }

        SourceFolderInfo info = new SourceFolderInfo() {
            public File getFile() {
                return dirF;
            }
            public String getFolderName() {
                return dirF.getName();
            }
            public boolean isAddSubfoldersSelected() {
                return true;
            }

            public FileFilter getFileFilter() {
                return AllSourceFileFilter.getInstance();
            }
        };
        List<SourceFolderInfo> sources = new ArrayList<SourceFolderInfo>();
        sources.add(info);
        makeProject = ProjectGenerator.createProject(dirF, name, makefileName, 
                new MakeConfiguration[]{extConf}, sources.iterator(), importantItemsIterator);
        FileObject dir = FileUtil.toFileObject(dirF);
        resultSet.add(dir);
        OpenProjects.getDefault().open(new Project[]{makeProject}, false);
        if (setAsMain) {
            OpenProjects.getDefault().setMainProject(makeProject);
        }
        if (runMake) {
            makeProject(true);
            postponeModel = true;
        }
        if (postponeModel) {
            switchModel(false);
        } else {
            postModelDiscovery();
        }

        return resultSet;
    }

//    private void parseConfigureLog(File configureLog){
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(configureLog));
//            while (true) {
//                String line;
//                line = reader.readLine();
//                if (line == null) {
//                    break;
//                }
//            }
//            reader.close();
//        } catch (FileNotFoundException ex) {
//            Exceptions.printStackTrace(ex);
//        } catch (IOException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }

    private File createTempFile(String prefix) {
        try {
            File file = File.createTempFile(prefix, ".log"); // NOI18N
            file.deleteOnExit();
            return file;
        } catch (IOException ex) {
            return null;
        }
    }

    private void makeProject(boolean doClean){
        String path = dirF.getAbsolutePath();
        File file = new File(path + "/Makefile"); // NOI18N
        if (file.exists() && file.isFile() && file.canRead()) {
            makefilePath = file.getAbsolutePath();
        } else {
            file = new File(path + "/makefile"); // NOI18N
            if (file.exists() && file.isFile() && file.canRead()) {
                makefilePath = file.getAbsolutePath();
            }
        }
        if (file.exists()) {
            FileObject makeFileObject = FileUtil.toFileObject(file);
            DataObject dObj;
            try {
                dObj = DataObject.find(makeFileObject);
                Node node = dObj.getNodeDelegate();
                if (doClean) {
                    postClean(node);
                } else {
                    postMake(node);
                }
            } catch (DataObjectNotFoundException ex) {
            }
        } else {
            runMake = false;
            postponeModel = false;
            switchModel(true);
            postModelDiscovery();
        }
    }

    private void postClean(final Node node){
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted() {
            }
            public void executionFinished(int rc) {
                postMake(node);
            }
        };
        MakeAction.execute(node, "clean", listener, null); // NOI18N
    }

    private void postMake(Node node){
        final File makeLog = createTempFile("make"); // NOI18N
        ExecutionListener listener = new ExecutionListener() {
            public void executionStarted() {
            }
            public void executionFinished(int rc) {
                discovery(rc, makeLog);
            }
        };
        Writer outputListener = null;
        if (makeLog != null){
            try {
                outputListener = new BufferedWriter(new FileWriter(makeLog));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        MakeAction.execute(node, "", listener, outputListener); // NOI18N
    }

    private DiscoveryProvider getProvider(String id){
       Lookup.Result<DiscoveryProvider> providers = Lookup.getDefault().lookup(new Lookup.Template<DiscoveryProvider>(DiscoveryProvider.class));
        for(DiscoveryProvider provider : providers.allInstances()){
            provider.clean();
            if (id.equals(provider.getID())) {
                return provider;
            }
        }
        return null;
    }

    private void modelDiscovery() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, dirF.getAbsolutePath());
        map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
        map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, ConsolidationStrategyPanel.FILE_LEVEL);
        boolean does = false;
        IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
        if (extension != null) {
            if (extension.canApply(map, makeProject)) {
                try {
                    extension.apply(map, makeProject);
                    does = true;
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (!does) {
            map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, dirF.getAbsolutePath());
            DiscoveryProvider provider = getProvider("model-folder"); // NOI18N
            provider.getProperty("folder").setValue(dirF.getAbsolutePath()); // NOI18N
            map.put(DiscoveryWizardDescriptor.PROVIDER, provider);
            map.put(DiscoveryWizardDescriptor.INVOKE_PROVIDER, Boolean.TRUE);
            DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
            descriptor.setProject(makeProject);
            SelectConfigurationPanel.buildModel(descriptor);
            try {
                DiscoveryProjectGenerator generator = new DiscoveryProjectGenerator(descriptor);
                generator.makeProject();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private void discovery(int rc, File makeLog) {
        boolean done = false;
        final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
        if (rc == 0) {
            if (extension != null) {
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, dirF.getAbsolutePath());
                map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, ConsolidationStrategyPanel.FILE_LEVEL);
                if (extension.canApply(map, makeProject)) {
                    try {
                        done = true;
                        extension.apply(map, makeProject);
                        switchModel(true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (!done && makeLog != null){
            if (extension != null) {
                final Map<String, Object> map = new HashMap<String, Object>();
                map.put(DiscoveryWizardDescriptor.ROOT_FOLDER, dirF.getAbsolutePath());
                map.put(DiscoveryWizardDescriptor.LOG_FILE, makeLog.getAbsolutePath());
                map.put(DiscoveryWizardDescriptor.CONSOLIDATION_STRATEGY, ConsolidationStrategyPanel.FILE_LEVEL);
                if (extension.canApply(map, makeProject)) {
                    try {
                        done = true;
                        extension.apply(map, makeProject);
                        switchModel(true);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        if (!done){
            postponeModel = false;
            switchModel(true);
            postModelDiscovery();
        }
    }

    private void postModelDiscovery() {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            final CsmProject p = model.getProject(np);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    if (project.equals(p)) {
                        ImportProject.listeners.remove(p);
                        CsmListeners.getDefault().removeProgressListener(this);
                        modelDiscovery();
                    }
                }
            };
            CsmListeners.getDefault().addProgressListener(listener);
            ImportProject.listeners.put(p, listener);
        }
    }

    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model instanceof ModelImpl && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                ((ModelImpl) model).enableProject(np);
            } else {
                ((ModelImpl) model).disableProject(np);
            }
        }
    }

    private static final Map<CsmProject, CsmProgressListener> listeners = new WeakHashMap<CsmProject, CsmProgressListener>();
}

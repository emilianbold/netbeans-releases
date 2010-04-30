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
package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.cnd.editor.filecreation.CCFSrcFileIterator;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 *
 * @author sg155630
 */
public class TestCppUnitIterator extends CCFSrcFileIterator {

    @Override
    public void initialize(TemplateWizard wiz) {
        Project project = Templates.getProject(wiz);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        targetChooserDescriptorPanel = new NewTestCppUnitPanel(project, groups, null);
    }

    @Override
    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        Set<DataObject> dataObjects = new HashSet<DataObject>();

        if(getTestName() == null) {
            return dataObjects;
        }

        DataFolder targetFolder = wiz.getTargetFolder();

        Project project = Templates.getProject(wiz);

        Folder folder = null;
        Folder testsRoot = getTestsRootFolder(project);
        if(testsRoot != null) {
            Folder newFolder = testsRoot.addNewFolder(true, Folder.Kind.TEST);
            newFolder.setDisplayName(getTestName());
            folder = newFolder;
        }

        if(folder == null) {
            return dataObjects;
        }

        setCUnitLinkerOptions(project, folder);

        DataObject formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestclassfile.cpp"); // NOI18N
        DataObject dataObject = formDataObject.createFromTemplate(targetFolder, getTestClassSourceFileName());
        addItemToTestFolder(project, folder, dataObject);

        formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestclassfile.h"); // NOI18N
        dataObject = formDataObject.createFromTemplate(targetFolder, getTestClassHeaderFileName());
        addItemToTestFolder(project, folder, dataObject);

        formDataObject = NewTestCppUnitPanel.getTemplateDataObject("cppunittestrunnerfile.cpp"); // NOI18N
        dataObject = formDataObject.createFromTemplate(targetFolder, getTestRunnerFileName());
        addItemToTestFolder(project, folder, dataObject);

        dataObjects.add(dataObject);
        return dataObjects;
    }

    private static boolean addItemToTestFolder(Project project, Folder folder, DataObject dataObject) {
        FileObject file = dataObject.getPrimaryFile();
        Project owner = FileOwnerQuery.getOwner(file);

        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(project);

        if (owner != null && owner.getProjectDirectory() == project.getProjectDirectory()) {
            File ioFile = FileUtil.toFile(file);
            if (ioFile.isDirectory()) {
                return false;
            } // don't add directories.
            if (!makeConfigurationDescriptor.okToChange()) {
                return false;
            }
            String itemPath;
            if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                itemPath = CndPathUtilitities.toAbsoluteOrRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
            } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                itemPath = CndPathUtilitities.toRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
            } else {
                itemPath = ioFile.getPath();
            }
            itemPath = CndPathUtilitities.normalize(itemPath);
            Item item = new Item(itemPath);

            folder.addItemAction(item);
        }

        return true;
    }

    private String getTestClassSourceFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getSourceFileName();
    }

    private String getTestClassHeaderFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getHeaderFileName();
    }

    private String getTestRunnerFileName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getRunnerFileName();
    }

    private String getTestName() {
        return ((NewTestCppUnitPanelGUI)targetChooserDescriptorPanel.getComponent()).getTestName();
    }

    private static MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
        ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (pdp == null) {
            return null;
        }
        return pdp.getConfigurationDescriptor();
    }

    private static Folder getTestsRootFolder(Project project) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();

        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if(folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        return testRootFolder;
    }

    private static void setCUnitLinkerOptions(Project project, Folder testFolder) {
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
        FolderConfiguration folderConfiguration = testFolder.getFolderConfiguration(projectDescriptor.getActiveConfiguration());
        LinkerConfiguration linkerConfiguration = folderConfiguration.getLinkerConfiguration();
        LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();
        librariesConfiguration.add(new LibraryItem.StdLibItem("CppUnit", "CppUnit", new String[]{"cppunit"})); // NOI18N
        linkerConfiguration.setLibrariesConfiguration(librariesConfiguration);
    }

}
